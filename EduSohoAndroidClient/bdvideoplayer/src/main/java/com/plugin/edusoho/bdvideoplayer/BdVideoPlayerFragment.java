package com.plugin.edusoho.bdvideoplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.cyberplayer.core.BVideoView;
import com.baidu.cyberplayer.core.BVideoView.OnCompletionListener;
import com.baidu.cyberplayer.core.BVideoView.OnErrorListener;
import com.baidu.cyberplayer.core.BVideoView.OnInfoListener;
import com.baidu.cyberplayer.core.BVideoView.OnPlayingBufferCacheListener;
import com.baidu.cyberplayer.core.BVideoView.OnPreparedListener;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by howzhi on 14-8-5.
 */
public class BdVideoPlayerFragment extends Fragment implements OnPreparedListener, OnCompletionListener, OnErrorListener,
        OnInfoListener, OnPlayingBufferCacheListener {
    protected final String TAG = "BdVideoPlayerFragment";

    private String AK = "6ZB2kShzunG7baVCPLWe7Ebc";
    private String SK = "wt18pcUSSryXdl09jFvGvsuNHhGCZTvF";

    protected String mCurMediaSource = null;
    protected String mCurMediaHeadSource = null;
    protected String mVideoSource = null;
    protected String mVideoHead = null;
    protected String mediaStorage = null;
    protected String cloudVideoConvertStatus = null;

    protected boolean mLearnStatus;

    private Activity mContext = null;
    private BVideoView mVV = null;

    private LinearLayout llayoutPlayerControlPanel;
    private ImageView ivVideoPlay = null;
    private ImageView ivVideoReplay = null;
    private CheckBox chkFullScreen = null;
    private PopupWindow popupWindow;

    protected RelativeLayout rlayoutTitleStatus;
    protected ImageView ivBack;
    protected ImageView ivLearnStatus;
    protected TextView tvLearn;
    protected TextView tvVideoTitle;
    protected ImageView ivShare;
    protected TextView tvStreamType;
    protected ImageView ivQuestion;
    protected ImageView ivNote;

    private SeekBar mProgress = null;
    private TextView mDuration = null;
    private TextView mCurrPosition = null;
    protected ViewGroup mViewContainerView = null;

    protected int mCourseId;
    protected int mLessonId;
    protected String mLessonName;
    protected boolean mIsHwDecode = false;
    protected boolean mIsPlayEnd;
    protected boolean isCacheVideo;
    protected int mDecodeMode;

    protected LessonLearnStatus mLearnStatusChanged;
    protected EventHandler mEventHandler;
    protected HandlerThread mHandlerThread;
    private String mSoLibDir;

    protected final Object SYNC_Playing = new Object();

    public final int EVENT_START = 0;
    public final int EVENT_PLAY = 3;
    public final int EVENT_PAUSE = 4;
    public final int EVENT_FINISH = 5;
    public final int EVENT_REPLAY = 6;

    private final int UI_EVENT_UPDATE_CURRPOSITION = 1;
    private final int UI_EVENT_ERROR = 5;
    private final int UI_EVENT_PAUSE = 3;
    private final int UI_EVENT_PLAY = 4;
    private final int UI_EVENT_FINISH = 6;
    private final int UI_HEAD_PLAY = 7;
    private final int UI_HEAD_FINISHED = 8;
    private static final int HIDE = 2;

    protected PowerManager.WakeLock mWakeLock = null;
    private static final String POWER_LOCK = "BdVideoPlayerActivity";

    /**
     *
     */
    protected enum PLAYER_STATUS {
        PLAYER_IDLE, PLAYER_PREPARING, PLAYER_PREPARED, PLAYER_PAUSE
    }

    protected enum PLAYER_HEAD_STATUS {
        PLAYER_START, PLAYER_END, PLAYER_IDLE
    }

    protected PLAYER_STATUS mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;

    /**
     * 位置
     */
    protected int mLastPos = 0;
    protected int mSavePos = 0;
    protected int mCurrentPos = 0;
    protected int mDurationCount = 0;

    protected boolean isSwitched;
    protected boolean isHeadPlaying;
    private PLAYER_HEAD_STATUS mPlayHeadStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "bd fragment create");
        mContext = getActivity();

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, POWER_LOCK);
        initSoLib();
        setMediaSource();
        mPlayHeadStatus = PLAYER_HEAD_STATUS.PLAYER_IDLE;
    }

    public void setMediaSource() {
        Bundle bundle = getArguments();
        mIsHwDecode = bundle.getBoolean("isHW", false);
        isCacheVideo = bundle.getBoolean("from_cache", false);
        mVideoSource = getUrlPath(bundle.getString("streamUrls"));
        mediaStorage = bundle.getString("video_type");
        cloudVideoConvertStatus = bundle.getString("cloud_video_convert_status");
        if (isCacheVideo || "local".equals(mediaStorage) || !"success".equals(cloudVideoConvertStatus)) {
            mCurMediaSource = mVideoSource;
        }
        int decodeMode = TextUtils.isEmpty(mVideoSource) || "local".equals(mediaStorage) ? BVideoView.DECODE_HW : BVideoView.DECODE_SW;
        mDecodeMode = bundle.getInt("decode", decodeMode);
        mVideoHead = getUrlPath(bundle.getString("headUrl"));
        mCourseId = bundle.getInt("courseId");
        mLessonId = bundle.getInt("lessonId");
        mLessonName = bundle.getString("lesson_name");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.controllerplaying, container, false);
        initView(view);

        rlayoutTitleStatus.setVisibility(View.GONE);
        setVideoViewHeight();
        /**
         *  启后台事件
         */
        mHandlerThread = new HandlerThread("event handler thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mEventHandler = new EventHandler(mHandlerThread.getLooper());
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        if (mPlayerStatus == PLAYER_STATUS.PLAYER_PREPARED) {
            mLastPos = mVV.getCurrentPosition();
            mVV.pause();
            mVV.setTag("pause");
            Log.v(TAG, "mVV onPause");
            mUIHandler.sendEmptyMessage(UI_EVENT_PAUSE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mWakeLock && (!mWakeLock.isHeld())) {
            mWakeLock.acquire();
        }
        ivVideoReplay.setVisibility(View.GONE);
        resumePlay();
    }

    private void initSoLib() {
        File libDir = mContext.getDir(getCpuType() + "lib", Context.MODE_PRIVATE);
        if (new File(libDir, "libcyberplayer.so").exists()) {
            mSoLibDir = libDir.getAbsolutePath();
        }
    }

    /**
     * 初 化
     */
    protected void initView(View view) {
        llayoutPlayerControlPanel = (LinearLayout) view.findViewById(R.id.llayout_player_control_panel);
        ivVideoPlay = (ImageView) view.findViewById(R.id.play_btn);
        chkFullScreen = (CheckBox) view.findViewById(R.id.full_btn);
        ivVideoReplay = (ImageView) view.findViewById(R.id.video_replay);
        mProgress = (SeekBar) view.findViewById(R.id.media_progress);
        mDuration = (TextView) view.findViewById(R.id.time_total);
        mCurrPosition = (TextView) view.findViewById(R.id.time_current);
        rlayoutTitleStatus = (RelativeLayout) view.findViewById(R.id.rlayout_title_status);
        ivBack = (ImageView) view.findViewById(R.id.iv_back);
        ivLearnStatus = (ImageView) view.findViewById(R.id.iv_learn_status);
        tvLearn = (TextView) view.findViewById(R.id.tv_learned);
        tvVideoTitle = (TextView) view.findViewById(R.id.tv_video_title);
        ivShare = (ImageView) view.findViewById(R.id.iv_share);
        ivQuestion = (ImageView) view.findViewById(R.id.iv_question);
        ivNote = (ImageView) view.findViewById(R.id.iv_note);
        tvStreamType = (TextView) view.findViewById(R.id.tv_stream);
        registerCallbackForControl();
        if ("local".equals(mediaStorage)) {
            tvStreamType.setVisibility(View.GONE);
        }
        /**
         *   ak及sk 前16位
         */
        BVideoView.setAKSK(AK, SK);
        if (!TextUtils.isEmpty(mSoLibDir)) {
            BVideoView.setNativeLibsDirectory(mSoLibDir);
        }
        mVV = (BVideoView) view.findViewById(R.id.video_view);
        /**
         * regist listener
         */
        mVV.setOnPreparedListener(this);
        mVV.setOnCompletionListener(this);
        mVV.setOnErrorListener(this);
        mVV.setOnInfoListener(this);
        mVV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        mIsShowControllerCount = 0;
                        if (mIsShowController) {
                            hideController();
                        } else {
                            showController();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        mVV.setDecodeMode(mDecodeMode);
        if (mDecodeMode == BVideoView.DECODE_HW) {
            mVV.setVideoScalingMode(BVideoView.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        }
        llayoutPlayerControlPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mIsShowControllerCount = 0;
                    return true;
                }
                return false;
            }
        });
    }

    // region handler

    Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UI_EVENT_UPDATE_CURRPOSITION:
                    mCurrentPos = mVV.getCurrentPosition();
                    if (mCurrentPos != 0) {
                        mSavePos = mCurrentPos;
                    }
                    int duration = mVV.getDuration();
                    updateTextViewWithTimeFormat(mCurrPosition, mCurrentPos);
                    updateTextViewWithTimeFormat(mDuration, duration);
                    mProgress.setMax(duration);
                    mProgress.setProgress(mCurrentPos);
                    mUIHandler.sendEmptyMessageDelayed(UI_EVENT_UPDATE_CURRPOSITION, 200);
                    break;
                case UI_EVENT_ERROR:
                    showErrorDialog(msg.arg1, msg.arg2);
                    break;
                case UI_EVENT_FINISH:
                    //ivVideoReplay.setVisibility(View.VISIBLE);
                    ivVideoPlay.setImageResource(R.drawable.icon_video_play);
                    break;
                case HIDE:
                    hideController();
                    break;
                case UI_EVENT_PAUSE:
                    Log.d(TAG, "UI_EVENT_PAUSE");
                    ivVideoPlay.setImageResource(R.drawable.icon_video_play);
                    break;
                case UI_EVENT_PLAY:
                    ivVideoPlay.setImageResource(R.drawable.icon_video_pause);
                    break;
                case UI_HEAD_PLAY:
                    if (isCacheVideo) {
                        return;
                    }
                    setPlayerFunctionButton(View.INVISIBLE);
                    isHeadPlaying = true;
                    break;
                case UI_HEAD_FINISHED:
                    if (isCacheVideo) {
                        return;
                    }
                    isHeadPlaying = false;
                    break;
                default:
                    break;
            }
        }
    };

    public class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_START:
                    Log.d(TAG, "EVENT_START");
                    if (isCacheVideo || mCurMediaHeadSource == null || mPlayHeadStatus == PLAYER_HEAD_STATUS.PLAYER_END) {
                        mUIHandler.sendEmptyMessage(UI_HEAD_FINISHED);
                        playVideo();
                    } else {
                        mUIHandler.sendEmptyMessage(UI_HEAD_PLAY);
                        playHeadUrl();
                    }
                    break;
                case EVENT_PAUSE:
                    mVV.pause();
                    break;
                case EVENT_REPLAY:
                    Log.d(TAG, "EVENT_REPLAY");
                    startVideo();
                    mVV.seekTo(mLastPos);
                    mLastPos = 0;
                    break;
                case EVENT_FINISH:
                    mUIHandler.sendEmptyMessage(UI_EVENT_PAUSE);
                    Log.d(TAG, "EVENT_FINISH");
                    break;
                default:
                    break;
            }
        }
    }

    // endregion

    // region player action

    private void playVideo() {
        if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
            synchronized (SYNC_Playing) {
                try {
                    SYNC_Playing.wait();
                    Log.v(TAG, "wait player status to idle");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        mVV.setVideoPath(mCurMediaSource);
        if (mLastPos > 0) {
            mVV.seekTo(mLastPos);
            mLastPos = 0;
        }
        mVV.showCacheInfo(true);
        startVideo();
        mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARING;
    }

    private void playHeadUrl() {
        Log.v(TAG, "playHeadUrl " + mCurMediaHeadSource);
        mVV.setVideoPath(mCurMediaHeadSource);
        mVV.showCacheInfo(true);
        startVideo();

        mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARING;
        mPlayHeadStatus = PLAYER_HEAD_STATUS.PLAYER_START;
    }

    protected void startVideo() {
        try {
            checkVideoCanPlayer();
            mVV.start();
            mVV.setTag("start");
        } catch (Exception e) {
            Log.d(TAG, "error:" + e.getMessage());
        }
    }

    // endregion

    private String getCpuType() {
        String CPU_ABI = android.os.Build.CPU_ABI;
        if (TextUtils.isEmpty(CPU_ABI)) {
            CPU_ABI = Build.CPU_ABI2;
        }

        return TextUtils.isEmpty(CPU_ABI) ? "" : CPU_ABI;
    }

    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog alertDialog = builder.setTitle("播放提示")
                .setMessage(message)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        getActivity().finish();
                    }
                })
                .create();
        alertDialog.show();
    }

    private int getCpuBit() {
        int cupBit;
        try {
            Field SUPPORTED_64_BIT_ABIS_FIELD = android.os.Build.class.getField("SUPPORTED_64_BIT_ABIS");
            SUPPORTED_64_BIT_ABIS_FIELD.setAccessible(true);
            String[] SUPPORTED_64_BIT_ABIS = (String[]) SUPPORTED_64_BIT_ABIS_FIELD.get(null);
            cupBit = SUPPORTED_64_BIT_ABIS == null || SUPPORTED_64_BIT_ABIS.length == 0 ? 32 : 64;
        } catch (Exception e) {
            cupBit = 32;
        }

        return cupBit;
    }

    private void checkVideoCanPlayer() {
        String CPU_ABI = getCpuType();
        if (CPU_ABI.contains("x86")) {
            File libDir = mContext.getDir("x86lib", Context.MODE_PRIVATE);
            if (new File(libDir, "libcyberplayer.so").exists()) {
                return;
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    updateSoLib();
                }
            });
            throw new RuntimeException("not_support");
        }
    }

    private void updateSoLib() {
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        Log.d(TAG, "" + Thread.currentThread());
        new AsyncTask<Integer, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Integer... params) {
                SoLibManager.DownProcessListener listener = new SoLibManager.DownProcessListener() {
                    @Override
                    public void update(int count, int process) {
                        publishProgress(count, process);
                    }
                };
                return new SoLibManager(listener).downPlayerSoLib("x86", mContext);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                Log.d(TAG, "" + Thread.currentThread());
                progressDialog.setMessage(String.format(
                        "正在下载解码包:%s/%s"
                        , Formatter.formatFileSize(mContext, values[0]),
                        Formatter.formatFileSize(mContext, values[1])
                ));
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    progressDialog.cancel();
                    showAlertDialog("解码库更新完成，请重新打开视频播放");
                    return;
                }
                showAlertDialog("暂不支持在该设备上播放视频");
            }

        }.execute(0);
    }

    private boolean mIsShowController = true;
    private int mIsShowControllerCount;

    protected void hideController() {
        mIsShowController = false;
        llayoutPlayerControlPanel.setVisibility(View.GONE);
        rlayoutTitleStatus.setVisibility(View.GONE);
    }

    protected void showController() {
        mIsShowController = true;
        llayoutPlayerControlPanel.setVisibility(View.VISIBLE);
        rlayoutTitleStatus.setVisibility(View.VISIBLE);
    }

    private String getUrlPath(String uriPath) {
        String playUrl = null;
        if (uriPath == null) {
            return null;
        }
        Uri uri = Uri.parse(uriPath);
        if (null != uri) {
            String scheme = uri.getScheme();
            if (null != scheme) {
                playUrl = uri.toString();
            } else {
                playUrl = uri.getPath();
            }
        }
        return playUrl;
    }

    protected void resumePlay() {
        if (mPlayerStatus == PLAYER_STATUS.PLAYER_PREPARED
                || mPlayerStatus == PLAYER_STATUS.PLAYER_PAUSE) {
            mEventHandler.sendEmptyMessage(EVENT_REPLAY);
        } else {
            mEventHandler.sendEmptyMessage(EVENT_START);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG, "onStop + position:" + mVV.getCurrentPosition());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandlerThread.quit();
        Log.v(TAG, "onDestroy + position:" + mVV.getCurrentPosition());
        if (mWakeLock != null) {
            try {
                mWakeLock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onInfo(int what, int extra) {
        switch (what) {
            case BVideoView.MEDIA_INFO_BUFFERING_START:
                break;
            case BVideoView.MEDIA_INFO_BUFFERING_END:
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onPlayingBufferCache(int percent) {

    }

    @Override
    public boolean onError(int what, int extra) {
        mLastPos = mCurrentPos > 0 ? mCurrentPos + 16 : 0;
        Log.v(TAG, "onError what:" + what + " mCurrentPos:" + extra);
        synchronized (SYNC_Playing) {
            SYNC_Playing.notify();
        }

        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
        mUIHandler.obtainMessage(UI_EVENT_ERROR, what, extra).sendToTarget();
        return true;
    }

    private void changeDecodeMode() {
        mDecodeMode = BVideoView.DECODE_HW;
        mVV.setDecodeMode(mDecodeMode);
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        mEventHandler.sendEmptyMessage(EVENT_START);
    }

    protected void showErrorDialog(int what, int extra) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog alertDialog = builder.setTitle("播放提示")
                .setMessage("该课时视频不能播放")
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        getActivity().finish();
                    }
                })
                .create();
        alertDialog.show();
    }

    public void pause() {
        if (mVV.isPlaying()) {
            mIsPlayEnd = true;
            ivVideoPlay.setImageResource(R.drawable.icon_video_play);
            mVV.pause();
        }
        Log.d(TAG, "pauseVideo: ");
    }

    public void resume() {
        if (!mVV.isPlaying()) {
            mIsPlayEnd = false;
            ivVideoPlay.setImageResource(R.drawable.icon_video_pause);
            if (mPlayerStatus == PLAYER_STATUS.PLAYER_IDLE) {
                ivVideoReplay.setVisibility(View.GONE);
                resumePlay();
                return;
            }
            mVV.resume();
        }
        Log.d(TAG, "resumeVideo: ");
    }

    private void registerCallbackForControl() {
        chkFullScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    //切换到竖屏
                    setFullScreen(false);
                    rlayoutTitleStatus.setVisibility(View.VISIBLE);
                } else {
                    mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    //切换到横屏
                    setFullScreen(true);
                    rlayoutTitleStatus.setVisibility(View.GONE);
                }
            }
        });

        ivVideoPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mVV.isPlaying()) {
                    mIsPlayEnd = true;
                    ivVideoPlay.setImageResource(R.drawable.icon_video_play);
                    /**
                     *  停
                     */
                    mVV.pause();
                } else {
                    mIsPlayEnd = false;
                    ivVideoPlay.setImageResource(R.drawable.icon_video_pause);
                    if (mPlayerStatus == PLAYER_STATUS.PLAYER_IDLE) {
                        ivVideoReplay.setVisibility(View.GONE);
                        resumePlay();
                        return;
                    }
                    /**
                     *
                     */
                    mVV.resume();
                }

            }
        });

        SeekBar.OnSeekBarChangeListener osbc1 = new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                updateTextViewWithTimeFormat(mCurrPosition, progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                /**
                 * SeekBar  seek 停
                 */
                mUIHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                int iseekPos = seekBar.getProgress();
                /**
                 * SeekBark  seek   seekTo 作
                 *
                 */
                mVV.seekTo(iseekPos);
                Log.v(TAG, "seek to " + iseekPos);
                mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
            }
        };
        mProgress.setOnSeekBarChangeListener(osbc1);

        ivVideoReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivVideoReplay.setVisibility(View.GONE);
                resumePlay();
            }
        });
    }

    private void updateTextViewWithTimeFormat(TextView view, int second) {
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String strTemp;
        if (0 != hh) {
            strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            strTemp = String.format("%02d:%02d", mm, ss);
        }
        view.setText(strTemp);
    }

    @Override
    public void onCompletion() {
        Log.v(TAG, "onCompletion");

        synchronized (SYNC_Playing) {
            SYNC_Playing.notify();
            Log.v(TAG, "SYNC_Playing notify");
        }

        boolean isPlayEnd = mCurrentPos > 0 && mCurrentPos >= mDurationCount;
        if (isPlayEnd && mPlayHeadStatus == PLAYER_HEAD_STATUS.PLAYER_START) {
            Log.v(TAG, "start media");
            mPlayHeadStatus = PLAYER_HEAD_STATUS.PLAYER_END;
            mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
            mEventHandler.sendEmptyMessage(EVENT_START);
            return;
        }

        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        if (isSwitched) {
            mEventHandler.sendEmptyMessage(EVENT_START);
            isSwitched = false;
        } else {
            mUIHandler.sendEmptyMessage(UI_EVENT_FINISH);
        }
        if (mLearnStatusChanged != null) {
            mLearnStatusChanged.setLearnStatus();
        }
    }

    @Override
    public void onPrepared() {
        Log.v(TAG, "onPrepared" + mPlayerStatus);
        mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
        mUIHandler.sendEmptyMessage(UI_EVENT_PLAY);
        mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
        mDurationCount = mVV.getDuration();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setVideoViewHeight();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setVideoViewHeight();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && !isHeadPlaying) {
            setPlayerFunctionButton(View.VISIBLE);
        } else {
            setPlayerFunctionButton(View.GONE);
        }
    }

    private void setVideoViewHeight() {
        int screenWidth = mContext.getWindowManager().getDefaultDisplay().getWidth();
        int videoViewHeight = (int) (screenWidth / (16 / 9.0f));
        ViewGroup.LayoutParams lp = mVV.getLayoutParams();
        lp.height = videoViewHeight;
        mVV.setLayoutParams(lp);
    }

    protected void showPopupWindows() {
        if (popupWindow != null) {
            popupWindow.showAsDropDown(tvStreamType, dip2px(mContext, 60) / -4, mProgress.getHeight() + 10);
        } else {
            Toast.makeText(mContext, "视频信息获取失败", Toast.LENGTH_LONG).show();
        }
    }

    protected void initPopupWindows(List<StreamInfo> streamInfoLists) {
        if (popupWindow == null) {
            LinearLayout popupView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.popup_stream_windows, null);
            createMediaTypeTextViews(popupView, streamInfoLists);
            popupWindow = new PopupWindow(mContext);
            popupWindow.setHeight(dip2px(mContext, 15 * 2 + 30 * (streamInfoLists.size() - 1)) + sp2px(mContext, 14 * streamInfoLists.size()) + dip2px(mContext, 15));
            popupWindow.setWidth(dip2px(mContext, 60));
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.argb(127, 52, 53, 55)));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setContentView(popupView);
        }
    }

    private void createMediaTypeTextViews(final LinearLayout parentView, List<StreamInfo> streamInfoLists) {
        int TopBottomMargin = dip2px(mContext, 15);
        int middleMargin = dip2px(mContext, 30);
        for (int i = 0; i < streamInfoLists.size(); i++) {
            TextView tv = new TextView(mContext);
            final StreamInfo streamInfo = streamInfoLists.get(i);
            if (i == 0) {
                tvStreamType.setText(convertSourceName(streamInfo.name));
                tv.setTextColor(getResources().getColor(R.color.video_checked));
            } else {
                tv.setTextColor(getResources().getColor(android.R.color.white));
            }
            tv.setText(convertSourceName(streamInfo.name));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_size_x_s));
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            tv.setTag(streamInfo);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StreamInfo si = (StreamInfo) v.getTag();
                    mCurMediaSource = si.src;
                    mLastPos = mVV.getCurrentPosition();
                    tvStreamType.setText(((TextView) v).getText());
                    initMediaSourceTextViewColor(parentView, si.name);
                    mVV.setVideoPath(mCurMediaSource);
                    mVV.seekTo(mVV.getDuration());
                    isSwitched = true;
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i == streamInfoLists.size() - 1 && streamInfoLists.size() == 1) {
                params.setMargins(0, TopBottomMargin, 0, TopBottomMargin);
            } else {
                if (i == 0) {
                    params.setMargins(0, TopBottomMargin, 0, 0);
                } else if (i == streamInfoLists.size() - 1) {
                    params.setMargins(0, middleMargin, 0, TopBottomMargin);
                } else {
                    params.setMargins(0, middleMargin, 0, 0);
                }
            }
            parentView.addView(tv, params);
        }
    }

    private void initMediaSourceTextViewColor(LinearLayout parentView, String name) {
        int size = parentView.getChildCount();
        for (int i = 0; i < size; i++) {
            TextView tv = (TextView) parentView.getChildAt(i);
            StreamInfo streamInfo = (StreamInfo) tv.getTag();
            if (name.equals(streamInfo.name)) {
                tv.setTextColor(getResources().getColor(R.color.video_checked));
            } else {
                tv.setTextColor(getResources().getColor(android.R.color.white));
            }
        }
    }

    public String convertSourceName(String name) {
        if ("SD".equals(name)) {
            return "标清";
        } else if ("HD".equals(name)) {
            return "高清";
        } else {
            return "超清";
        }
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int sp2px(Context context, float spValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (spValue * scale + 0.5f);
    }

    protected void setFullScreen(boolean enable) {
        if (getView() != null) {
            if (enable) {
                getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }
    }

    protected int getCurrentPos() {
        return mSavePos;
    }

    protected void setCurrentPos(int seek) {
        mLastPos = seek;
    }

    protected void setPlayerFunctionButton(int visibility) {
        if (isCacheVideo) {
            tvStreamType.setText("缓存");
        } else {
            if ("local".equals(mediaStorage) || !"success".equals(cloudVideoConvertStatus)) {
                tvStreamType.setVisibility(View.INVISIBLE);
            } else {
                tvStreamType.setVisibility(visibility);
            }
        }
        ivNote.setVisibility(visibility);
        ivQuestion.setVisibility(visibility);
    }

    public interface LessonLearnStatus {
        void setLearnStatus();
    }
}

