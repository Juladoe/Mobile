package com.edusoho.longinus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.edusoho.longinus.util.CPUUtil;
import com.edusoho.longinus.util.LibUpdateHelper;
import com.edusoho.longinus.widget.MediaController;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.SharedLibraryNameHelper;
import com.pili.pldroid.player.widget.PLVideoView;
import com.umeng.analytics.MobclickAgent;


import java.io.IOException;

/**
 * Created by suju on 17/1/20.
 */

public class PLMediaPlayerActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private PLMediaPlayer mMediaPlayer;
    private AVOptions mAVOptions;
    private boolean mIsStopped;

    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;

    private static final String TAG = "PLVideoViewActivity";

    private static final int MESSAGE_ID_RECONNECTING = 0x01;

    protected static final String NOT_START = "init";
    protected static final String LIVE = "start";
    protected static final String PAUSE = "pause";
    protected static final String CLOSE = "close";

    private MediaController mMediaController;
    private Toast mToast = null;
    private String mVideoPath = null;
    private int mDisplayAspectRatio = PLVideoView.ASPECT_RATIO_FIT_PARENT;
    private boolean mIsActivityPaused = true;
    private View mLoadingView;
    private TextView mLiveTitleView;
    private TextView mLiveDescView;
    private TextView mLoadTitleView;
    private ImageView mLoadStatusView;
    private ProgressBar mLoadProgressBar;
    private Toolbar mToolBar;
    private ViewGroup mBottomLayout;
    private ViewGroup mVideoContainer;
    private int mIsLiveStreaming;
    private String mLiveStatus;
    private int mVideoHeight;
    private long mTimeoutLength;

    private View mMaskView;
    protected View mChatLoadLayout;
    protected ProgressBar mChatLoadProgressBar;
    protected TextView mChatLoadTitleView;
    protected TextView mNoticeView;
    private LibUpdateHelper mLibUpdateHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (CPUUtil.hasX86CPU()) {
            initX86SharedLib();
        }
        if (CPUUtil.hasX86CPU() && !CPUUtil.hasX86Library(getApplicationContext())) {
            mLibUpdateHelper = new LibUpdateHelper(this);
            mLibUpdateHelper.update("x86", new LibUpdateHelper.LibUpdateListener() {
                @Override
                public void onInstalled() {
                    showAlert("解码库更新完成，请重新打开直播播放", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exit();
                        }
                    });
                }

                @Override
                public void onFail() {
                    Toast.makeText(getApplicationContext(), R.string.video_not_support, Toast.LENGTH_SHORT).show();
                    exit();
                }
            });
            return;
        }

        setContentView(R.layout.activity_pl_mediaplayer_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.SurfaceView);
        mSurfaceView.getHolder().addCallback(mCallback);

        initView();
        mAVOptions = getOptions(getIntent());
        setMediaController();
        bindListener();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void initX86SharedLib() {
        SharedLibraryNameHelper helper = SharedLibraryNameHelper.getInstance();
        helper.renameSharedLibrary(getBaseContext().getDir("lib", Context.MODE_PRIVATE) + "/libpldroidplayer.so");
    }

    private void exit() {
        if (isFinishing())
            return;
        finish();
    }

    private void showAlert(String message, DialogInterface.OnClickListener cancelClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提醒")
                .setMessage(message)
                .setPositiveButton("确认", cancelClickListener)
                .create()
                .show();
    }

    private void bindListener() {
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaController.isShowing()) {
                    mMediaController.hide();
                    return;
                }
                mMediaController.show(3000);
            }
        });
    }

    protected void setMediaController() {
        mMediaController = new MediaController(getBaseContext(), false, mIsLiveStreaming == 1);
        mMediaController.setOnShownListener(new MediaController.OnShownListener() {
            @Override
            public void onShown() {
                getSupportActionBar().show();
                int currentOrientation = getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    return;
                }
            }
        });
        mMediaController.setOnHiddenListener(new MediaController.OnHiddenListener() {
            @Override
            public void onHidden() {
                getSupportActionBar().hide();
                int currentOrientation = getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    return;
                }
            }
        });
        mMediaController.setOnScreenChangeListener(new MediaController.OnScreenChangeListener() {
            @Override
            public void onChange(int orientation) {
                int currentOrientation = getResources().getConfiguration().orientation;
                if (orientation == currentOrientation) {
                    return;
                }
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    changeScreenToPortrait();
                } else {
                    MobclickAgent.onEvent(getBaseContext(), "liveRoom_fullScreenButton");
                    changeScreenToLandspace();
                }
            }
        });

        mMediaController.setAnchorView(findViewById(R.id.vg_live_controller));
        mMediaController.hide();
    }

    protected void setLiveTitle(String title) {
        mLiveTitleView.setText(title);
    }

    protected void setLiveDesc(String desc) {
        if (TextUtils.isEmpty(desc)) {
            mLiveDescView.setVisibility(View.GONE);
        }
        mLiveDescView.setText(desc);
    }

    private void changeScreenToPortrait() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        lp.height = getResources().getDimensionPixelOffset(R.dimen.live_video_height);
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mBottomLayout.setVisibility(View.VISIBLE);
        mNoticeView.setVisibility(View.VISIBLE);
        mSurfaceView.setLayoutParams(lp);
    }

    protected void setPlayStatus(String status) {
        mLiveStatus = status;
        switch (status) {
            case NOT_START:
                setPlayNotStart();
                break;
            case LIVE:
                mSurfaceView.setBackgroundColor(Color.TRANSPARENT);
                setPlayOnBuffering();
                break;
            case PAUSE:
                setPlayPause();
                break;
            case CLOSE:
                setPlayEnd();
        }
    }

    private void setPlayNotStart() {
        mLoadStatusView.setImageResource(R.drawable.icon_live_nostart);
        mLoadStatusView.setVisibility(View.VISIBLE);
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadTitleView.setText(R.string.live_no_start);
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setOnClickListener(null);
        mSurfaceView.setBackgroundColor(Color.BLACK);
    }

    private void setPlayOnBuffering() {
        mLoadStatusView.setVisibility(View.GONE);
        mLoadProgressBar.setVisibility(View.VISIBLE);
        mLoadTitleView.setText(R.string.live_buffering);
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setOnClickListener(null);
    }

    private void setPlayPause() {
        getSupportActionBar().show();
        mLoadStatusView.setImageResource(R.drawable.icon_live_status);
        mLoadStatusView.setVisibility(View.VISIBLE);
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadTitleView.setText(R.string.live_no_pause);
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setOnClickListener(null);
        mSurfaceView.setBackgroundColor(Color.BLACK);
    }

    protected String getViewPath() {
        return mVideoPath;
    }

    protected void startPlay(String videoUri) {
        if (TextUtils.isEmpty(videoUri)) {
            Toast.makeText(getBaseContext(), R.string.live_uri_error, Toast.LENGTH_LONG).show();
            return;
        }
        setPlayStatus(LIVE);
        mVideoPath = videoUri;

        prepare();
    }

    protected void pauseLive() {
        release();
        setPlayStatus(PAUSE);
    }

    protected void resumeLive() {
        setPlayStatus(LIVE);
        prepare();
    }

    private void setPlayError() {
        mLoadStatusView.setImageResource(R.drawable.live_load_error);
        mLoadStatusView.setVisibility(View.VISIBLE);
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadTitleView.setText(R.string.live_load_error);
        mLoadingView.setVisibility(View.VISIBLE);
        mSurfaceView.setBackgroundColor(Color.BLACK);
    }

    private void setPlayEnd() {
        mHandler.removeCallbacksAndMessages(null);
        getSupportActionBar().show();
        mLoadStatusView.setImageResource(R.drawable.icon_live_close);
        mLoadStatusView.setVisibility(View.VISIBLE);
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadTitleView.setText(R.string.live_end);
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setOnClickListener(null);
        mSurfaceView.setBackgroundColor(Color.BLACK);
        release();
    }

    protected void setLiveChatLoadContentStatus(int visibility, String title) {
        mChatLoadProgressBar.setVisibility(visibility);
        mChatLoadTitleView.setText(title);
    }

    protected void setLiveChatLoadShowStatus(int visibility) {
        mChatLoadLayout.setVisibility(visibility);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mMediaController.updateStatus(newConfig.orientation);
    }

    private void changeScreenToLandspace() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        lp.height = getWindowManager().getDefaultDisplay().getHeight();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mBottomLayout.setVisibility(View.GONE);
        mNoticeView.setVisibility(View.GONE);
        mSurfaceView.setLayoutParams(lp);
    }

    private AVOptions getOptions(Intent intent) {
        AVOptions options = new AVOptions();

        int isLiveStreaming = intent.getIntExtra("liveStreaming", 1);
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000);
        // Some optimization with buffering mechanism when be set to 1
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, isLiveStreaming);
        if (isLiveStreaming == 1) {
            options.setInteger(AVOptions.KEY_DELAY_OPTIMIZATION, 1);
        }

        // 1 -> hw codec enable, 0 -> disable [recommended]
        int iCodec = getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_SW_DECODE);
        options.setInteger(AVOptions.KEY_MEDIACODEC, iCodec);

        // whether start play automatically after prepared, default value is 1
        options.setInteger(AVOptions.KEY_START_ON_PREPARED, 0);
        options.setInteger(AVOptions.KEY_CACHE_BUFFER_DURATION, 3000);
        options.setInteger(AVOptions.KEY_MAX_CACHE_BUFFER_DURATION, 6000);

        return options;
    }

    protected void initView() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        mVideoContainer = (ViewGroup) findViewById(R.id.fl_live_container);
        mBottomLayout = (ViewGroup) findViewById(R.id.chat_content);
        mLiveTitleView = (TextView) findViewById(R.id.tv_live_title);
        mLiveDescView = (TextView) findViewById(R.id.tv_live_desc);
        mNoticeView = (TextView) findViewById(R.id.tv_live_notice);
        mNoticeView.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobclickAgent.onEvent(getBaseContext(), "liveRoom_1minuteDisplayArea");
            }
        });
        mChatLoadLayout = findViewById(R.id.ll_chat_load);
        mChatLoadTitleView = (TextView) findViewById(R.id.tv_chat_load_title);
        mChatLoadProgressBar = (ProgressBar) findViewById(R.id.pb_chat_load);

        mMaskView = findViewById(R.id.view_live_mask);
        mLoadingView = findViewById(R.id.vg_live_loadingView);
        mLoadTitleView = (TextView) findViewById(R.id.tv_live_loadtitle);
        mLoadStatusView = (ImageView) findViewById(R.id.iv_live_statusicon);
        mLoadProgressBar = (ProgressBar) findViewById(R.id.iv_live_progressbar);

        initTouchListener();
    }

    private void initTouchListener() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(getOnGlobalLayoutListener());
    }

    private ViewTreeObserver.OnGlobalLayoutListener getOnGlobalLayoutListener() {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                final int softKeyboardHeight = 100;
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
                int heightDiff = rootView.getBottom() - r.bottom;
                if (heightDiff > softKeyboardHeight * dm.density) {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    showMaskView();
                }
            }
        };
    }

    private void showMaskView() {
        mMaskView.setVisibility(View.VISIBLE);
        Rect r = new Rect();
        mMaskView.getWindowVisibleDisplayFrame(r);

        ViewGroup.LayoutParams lp = mMaskView.getLayoutParams();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        lp.height = r.bottom - r.top - (int)((48 + 25) * dm.density);
        mMaskView.setLayoutParams(lp);
        mMaskView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mMaskView.setVisibility(View.INVISIBLE);
                Utils.setSoftKeyBoard(v, getBaseContext(), Utils.HIDE_KEYBOARD);
                initTouchListener();
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLibUpdateHelper != null) {
            mLibUpdateHelper.stop();
        }
    }

    @Override
    public void finish() {
        super.finish();
        Log.d(TAG, "finish");
        release();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActivityPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsActivityPaused = true;
    }

    public void releaseWithoutStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(null);
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void prepare() {
        if (TextUtils.isEmpty(mVideoPath)) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
                mIsStopped = false;
            }
            return;
        }

        try {
            mMediaPlayer = new PLMediaPlayer(this, mAVOptions);

            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);

            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mMediaPlayer.setDataSource(mVideoPath);
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            mMediaPlayer.prepareAsync();
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            prepare();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSurfaceWidth = width;
            mSurfaceHeight = height;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseWithoutStop();
        }
    };

    private PLMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLMediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(PLMediaPlayer mp, int width, int height) {
            Log.i(TAG, "onVideoSizeChanged, width = " + width + ",height = " + height);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                changeScreenToPortrait();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                changeScreenToPortrait();
                return true;
            }
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private PLMediaPlayer.OnPreparedListener mOnPreparedListener = new PLMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(PLMediaPlayer mp) {
            Log.i(TAG, "On Prepared !");
            mMediaPlayer.start();
            mIsStopped = false;
        }
    };

    private PLMediaPlayer.OnInfoListener mOnInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer mp, int what, int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mLoadingView.setVisibility(View.VISIBLE);
                    break;
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                case PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    mLoadingView.setVisibility(View.GONE);
                    break;
                case PLMediaPlayer.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer mp, int percent) {
            Log.d(TAG, "onBufferingUpdate: " + percent + "%");
        }
    };

    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer mp) {
            Log.d(TAG, "Play Completed !");
            showToastTips("直播完成");
        }
    };

    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer mp, int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            if (!LIVE.equals(mLiveStatus)) {
                setPlayStatus(mLiveStatus);
                return true;
            }
            release();
            switch (errorCode) {
                case PLMediaPlayer.ERROR_CODE_INVALID_URI:
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.ERROR_CODE_404_NOT_FOUND:
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.ERROR_CODE_CONNECTION_REFUSED:
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.ERROR_CODE_CONNECTION_TIMEOUT:
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.ERROR_CODE_EMPTY_PLAYLIST:
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.ERROR_CODE_STREAM_DISCONNECTED:
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.ERROR_CODE_UNAUTHORIZED:
                    showToastTips("Unauthorized Error !");
                    break;
                case PLMediaPlayer.ERROR_CODE_PREPARE_TIMEOUT:
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.ERROR_CODE_READ_FRAME_TIMEOUT:
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.ERROR_CODE_HW_DECODE_FAILURE:
                    mAVOptions.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_SW_DECODE);
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    sendReconnectMessage();
                    break;
                default:
                    sendReconnectMessage();
                    break;
            }
            return true;
        }
    };

    private void showToastTips(final String tips) {
        if (mIsActivityPaused) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(PLMediaPlayerActivity.this, tips, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }

    private View.OnClickListener mVideoErrorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mTimeoutLength = 0;
            sendReconnectMessage();
        }
    };

    private void sendReconnectMessage() {
        if (mTimeoutLength != 0 && (System.currentTimeMillis() - mTimeoutLength) > 60000) {
            setPlayError();
            mLoadingView.setOnClickListener(mVideoErrorClickListener);
            return;
        }
        if (mTimeoutLength == 0) {
            mTimeoutLength = System.currentTimeMillis();
        }
        Log.d(TAG, "TimeoutCount :" + mTimeoutLength);
        setPlayOnBuffering();
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_ID_RECONNECTING), 500);
    }

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MESSAGE_ID_RECONNECTING) {
                return;
            }
            if (mIsActivityPaused || !Utils.isLiveStreamingAvailable()) {
                finish();
                return;
            }
            if (!Utils.isNetworkAvailable(PLMediaPlayerActivity.this)) {
                sendReconnectMessage();
                return;
            }
            prepare();
        }
    };
}