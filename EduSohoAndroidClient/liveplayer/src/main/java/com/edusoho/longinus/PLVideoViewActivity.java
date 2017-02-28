package com.edusoho.longinus;

/**
 * Created by suju on 16/10/12
 */

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edusoho.longinus.widget.MediaController;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoView;
import com.umeng.analytics.MobclickAgent;

/**
 * This is a demo activity of PLVideoView
 */
public class PLVideoViewActivity extends AppCompatActivity {

    private static final String TAG = "PLVideoViewActivity";

    private static final int MESSAGE_ID_RECONNECTING = 0x01;

    protected static final String NOT_START = "init";
    protected static final String LIVE = "start";
    protected static final String PAUSE = "pause";
    protected static final String CLOSE = "close";

    private MediaController mMediaController;
    private PLVideoView mVideoView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_video_view);
        mIsLiveStreaming = getIntent().getIntExtra("liveStreaming", 1);
        initView();
        AVOptions avOptions = getOptions(getIntent());
        mVideoView.setAVOptions(avOptions);
        mVideoView.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_16_9);
        setMediaController();
        bindListener();
    }

    protected void setMediaController() {
        mMediaController = new MediaController(this, false, mIsLiveStreaming == 1);
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
        mVideoView.setMediaController(mMediaController);
        mMediaController.hide();
    }

    private void changeScreenToPortrait() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
        lp.height = mVideoHeight;
        mBottomLayout.setVisibility(View.VISIBLE);
        mNoticeView.setVisibility(View.VISIBLE);
        mVideoView.setLayoutParams(lp);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mMediaController.updateStatus(newConfig.orientation);
    }

    private void changeScreenToLandspace() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
        lp.height = getWindowManager().getDefaultDisplay().getHeight();
        mBottomLayout.setVisibility(View.GONE);
        mNoticeView.setVisibility(View.GONE);
        mVideoView.setLayoutParams(lp);
    }

    protected void startPlay(String videoUri) {
        if (TextUtils.isEmpty(videoUri)) {
            Toast.makeText(getBaseContext(), R.string.live_uri_error, Toast.LENGTH_LONG).show();
            return;
        }
        setPlayStatus(LIVE);
        mVideoPath = videoUri;
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();
    }

    private void bindListener() {
        // Set some listeners
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
    }

    private AVOptions getOptions(Intent intent) {
        AVOptions options = new AVOptions();

        int isLiveStreaming = intent.getIntExtra("liveStreaming", 1);
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000);
        // Some optimization with buffering mechanism when be set to 1
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, isLiveStreaming);
        if (isLiveStreaming == 1) {
            options.setInteger(AVOptions.KEY_DELAY_OPTIMIZATION, 1);
        }

        // 1 -> hw codec enable, 0 -> disable [recommended]
        int codec = getIntent().getIntExtra("mediaCodec", 0);
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);

        // whether start play automatically after prepared, default value is 1
        options.setInteger(AVOptions.KEY_START_ON_PREPARED, 0);

        return options;
    }

    protected void initView() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        mVideoView = (PLVideoView) findViewById(R.id.VideoView);
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
        mVideoView.setBufferingIndicator(mLoadingView);

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

    protected void setLiveChatLoadContentStatus(int visibility, String title) {
        mChatLoadProgressBar.setVisibility(visibility);
        mChatLoadTitleView.setText(title);
    }

    protected void setLiveChatLoadShowStatus(int visibility) {
        mChatLoadLayout.setVisibility(visibility);
    }

    protected void setPlayStatus(String status) {
        mLiveStatus = status;
        switch (status) {
            case NOT_START:
                setPlayNotStart();
                break;
            case LIVE:
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
    }

    private void setPlayError() {
        mLoadStatusView.setImageResource(R.drawable.live_load_error);
        mLoadStatusView.setVisibility(View.VISIBLE);
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadTitleView.setText(R.string.live_load_error);
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void setPlayEnd() {
        mHandler.removeCallbacksAndMessages(null);
        mVideoView.pause();
        mVideoView.stopPlayback();
        getSupportActionBar().show();
        mLoadStatusView.setImageResource(R.drawable.icon_live_close);
        mLoadStatusView.setVisibility(View.VISIBLE);
        mLoadProgressBar.setVisibility(View.GONE);
        mLoadTitleView.setText(R.string.live_end);
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.setOnClickListener(null);
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

    @Override
    protected void onResume() {
        super.onResume();
        mIsActivityPaused = false;
        if (LIVE.equals(mLiveStatus)) {
            mVideoView.setVideoPath(mVideoPath);
            mVideoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mToast = null;
        mIsActivityPaused = true;
        mVideoView.pause();
    }

    protected void pauseLive() {
        mVideoView.pause();
        setPlayStatus(PAUSE);
    }

    protected void resumeLive() {
        setPlayStatus(LIVE);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();
    }

    protected String getViewPath() {
        return mVideoPath;
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
    }

    public void onClickSwitchScreen(View v) {
        mDisplayAspectRatio = (mDisplayAspectRatio + 1) % 5;
        mVideoView.setDisplayAspectRatio(mDisplayAspectRatio);
        switch (mVideoView.getDisplayAspectRatio()) {
            case PLVideoView.ASPECT_RATIO_ORIGIN:
                showToastTips("Origin mode");
                break;
            case PLVideoView.ASPECT_RATIO_FIT_PARENT:
                showToastTips("Fit parent !");
                break;
            case PLVideoView.ASPECT_RATIO_PAVED_PARENT:
                showToastTips("Paved parent !");
                break;
            case PLVideoView.ASPECT_RATIO_16_9:
                showToastTips("16 : 9 !");
                break;
            case PLVideoView.ASPECT_RATIO_4_3:
                showToastTips("4 : 3 !");
                break;
            default:
                break;
        }
    }

    private PLMediaPlayer.OnInfoListener mOnInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
            Log.d(TAG, "onInfo: " + what + ", " + extra);
            return false;
        }
    };

    private View.OnClickListener mVideoErrorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mTimeoutLength = 0;
            sendReconnectMessage();
        }
    };

    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer plMediaPlayer, int errorCode) {
            Log.e(TAG, String.format("Error happened, errorCode = %d, %s", errorCode, mLiveStatus));
            if (!LIVE.equals(mLiveStatus)) {
                setPlayStatus(mLiveStatus);
                return true;
            }
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
                    showToastTips("直播课列表为空!");
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
                    checkLivePlayStatus();
                    sendReconnectMessage();
                    break;
                case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    showToastTips("加载直播错误");
                    break;
                default:
                    sendReconnectMessage();
                    break;
            }
            return true;
        }
    };

    public void checkLivePlayStatus() {
    }

    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            Log.d(TAG, "Play Completed !");
            showToastTips("直播完成");
        }
    };

    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int precent) {
            Log.d(TAG, "onBufferingUpdate: " + precent);
        }
    };

    private PLMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new PLMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(PLMediaPlayer plMediaPlayer) {
            Log.d(TAG, "onSeekComplete !");
        }
    };

    private PLMediaPlayer.OnPreparedListener mOnPreparedListener = new PLMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(PLMediaPlayer plMediaPlayer) {
            mTimeoutLength = 0;
        }
    };

    private PLMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int width, int height) {
            Log.d(TAG, "onVideoSizeChanged: " + width + "," + height);
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                return;
            }
            int videoWidth = mVideoView.getWidth();
            mVideoHeight = (int) (videoWidth / (width / (float) height));
            if (mVideoHeight == 0) {
                mVideoHeight = getResources().getDimensionPixelOffset(R.dimen.live_video_height);
            }
            ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
            lp.height = mVideoHeight;
            mVideoView.setLayoutParams(lp);

            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams();
            rlp.topMargin = mVideoHeight - 40;
            mBottomLayout.setLayoutParams(rlp);
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
                mToast = Toast.makeText(getBaseContext(), tips, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MESSAGE_ID_RECONNECTING) {
                return;
            }

            if (!Utils.isNetworkAvailable(getBaseContext())) {
                sendReconnectMessage();
                return;
            }
            if (!LIVE.equals(mLiveStatus) || mVideoView.isPlaying()) {
                return;
            }
            mVideoView.stopPlayback();
            mVideoView.setVideoPath(mVideoPath);
            mVideoView.start();
        }
    };

    private void sendReconnectMessage() {
        if (mTimeoutLength != 0 && (System.currentTimeMillis() - mTimeoutLength) > 60000) {
            setPlayError();
            mVideoView.stopPlayback();
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
}
