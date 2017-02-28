package com.edusoho.kuozhi.v3.ui.base;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.broadcast.AudioDownloadReceiver;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.push.UpYunUploadResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.ChatAudioRecord;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.view.EduSohoIconView;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.trinea.android.common.util.DigestUtils;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by JesseHuang on 15/10/16.
 */
public class BaseChatActivity extends ActionBarBaseActivity implements View.OnClickListener, View.OnFocusChangeListener, View.OnTouchListener {

    public static final int SEND_IMAGE = 1;
    public static final int SEND_CAMERA = 2;

    protected EduSohoIconView btnVoice;
    protected EduSohoIconView btnKeyBoard;
    protected EditText etSend;
    protected ListView lvMessage;
    protected Button btnSend;
    protected EduSohoIconView ivAddMedia;
    protected PtrClassicFrameLayout mPtrFrame;
    protected View viewMediaLayout;
    protected View viewPressToSpeak;
    protected View viewMsgInput;
    protected TextView tvSpeak;
    protected TextView tvSpeakHint;
    protected View mViewSpeakContainer;
    protected ImageView ivRecordImage;

    protected float mPressDownY;
    protected MediaRecorderTask mMediaRecorderTask;
    protected VolumeHandler mHandler;
    protected AudioDownloadReceiver mAudioDownloadReceiver;

    protected int mSendTime;
    protected int mStart = 0;
    protected File mCameraFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(mAudioDownloadReceiver, intentFilter);
    }

    public void initView() {
        mHandler = new VolumeHandler(this);
        mAudioDownloadReceiver = new AudioDownloadReceiver();
        etSend = (EditText) findViewById(R.id.et_send_content);
        etSend.addTextChangedListener(mContentTextWatcher);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);
        etSend.setOnFocusChangeListener(this);
        etSend.setOnClickListener(this);
        lvMessage = (ListView) findViewById(R.id.lv_messages);
        lvMessage.setOnTouchListener(this);
        ivAddMedia = (EduSohoIconView) findViewById(R.id.iv_show_media_layout);
        ivAddMedia.setOnClickListener(this);
        viewMediaLayout = findViewById(R.id.ll_media_layout);
        btnVoice = (EduSohoIconView) findViewById(R.id.btn_voice);
        btnVoice.setOnClickListener(this);
        btnKeyBoard = (EduSohoIconView) findViewById(R.id.btn_set_mode_keyboard);
        btnKeyBoard.setOnClickListener(this);
        viewPressToSpeak = findViewById(R.id.rl_btn_press_to_speak);
        viewPressToSpeak.setOnTouchListener(this);
        viewPressToSpeak.setOnClickListener(this);
        viewMsgInput = findViewById(R.id.rl_msg_input);
        EduSohoIconView ivPhoto = (EduSohoIconView) findViewById(R.id.iv_image);
        ivPhoto.setOnClickListener(this);
        EduSohoIconView ivCamera = (EduSohoIconView) findViewById(R.id.iv_camera);
        ivCamera.setOnClickListener(this);
        tvSpeak = (TextView) findViewById(R.id.tv_speak);
        tvSpeakHint = (TextView) findViewById(R.id.tv_speak_hint);
        ivRecordImage = (ImageView) findViewById(R.id.iv_voice_volume);
        mViewSpeakContainer = findViewById(R.id.recording_container);
        mViewSpeakContainer.bringToFront();
        mPtrFrame = (PtrClassicFrameLayout) findViewById(R.id.rotate_header_list_view_frame);
    }

    /**
     * 获取本地聊天记录
     */
    public void initData() {

    }

    /**
     * 初始化Cache文件夹
     */
    protected void initCacheFolder() {
        File imageFolder = new File(AppUtil.getAppStorage() + Const.UPLOAD_IMAGE_CACHE_FILE);
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }
        File imageThumbFolder = new File(AppUtil.getAppStorage() + Const.UPLOAD_IMAGE_CACHE_THUMB_FILE);
        if (!imageThumbFolder.exists()) {
            imageThumbFolder.mkdirs();
        }
        File audioFolder = new File(AppUtil.getAppStorage() + Const.UPLOAD_AUDIO_CACHE_FILE);
        if (!audioFolder.exists()) {
            audioFolder.mkdirs();
        }
    }

    /**
     * 发送普通文本
     * 子类重写
     *
     * @param content
     */
    public void sendMsg(String content) {

    }

    /**
     * 上传资源:音频、图片
     * 子类重写
     *
     * @param file upload file
     */
    public void uploadMedia(final File file, final String type, String strType) {

    }

    public void getUpYunUploadInfo(final File file, int fromId, final NormalCallback<UpYunUploadResult> callback) {
        String path = String.format(Const.GET_UPLOAD_INFO, fromId, file.length(), file.getName());
        RequestUrl url = app.bindPushUrl(path);
        ajaxGet(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UpYunUploadResult result = parseJsonValue(response, new TypeToken<UpYunUploadResult>() {
                });
                callback.success(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.success(null);
                CommonUtil.longToast(mActivity, getString(R.string.network_does_not_work));
                Log.d(TAG, "get upload info from upyun failed");
            }
        });
    }

    private void rnameUploadFile(File file, String uploadUrl) {
        String fileName = file.getName();
        String newFileName = DigestUtils.md5(uploadUrl);
        file.renameTo(new File(AppUtil.getAppStorage() + Const.UPLOAD_IMAGE_CACHE_FILE + "/" + newFileName));

        File thubFile = new File(AppUtil.getAppStorage() + Const.UPLOAD_IMAGE_CACHE_THUMB_FILE + "/" + fileName);
        thubFile.renameTo(new File(AppUtil.getAppStorage() + Const.UPLOAD_IMAGE_CACHE_THUMB_FILE + "/" + newFileName));
    }

    public void saveUploadResult(String putUrl, String getUrl, int fromId) {
        String path = String.format(Const.SAVE_UPLOAD_INFO, fromId);
        RequestUrl url = app.bindPushUrl(path);
        Map<String, String> hashMap = url.getParams();
        hashMap.put("putUrl", putUrl);
        hashMap.put("getUrl", getUrl);
        ajaxPost(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject result = new JSONObject(response);
                    if ("success".equals(result.getString("result"))) {
                        Log.d(TAG, "save upload result success");
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "convert json to obj error");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "save upload info error");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == SEND_IMAGE) {
            List<String> pathList = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            if (pathList == null || pathList.isEmpty()) {
                return;
            }

            for (String path : pathList) {
                uploadMedia(compressImage(path), PushUtil.ChatMsgType.IMAGE, Const.MEDIA_IMAGE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAudioDownloadReceiver);
    }

    /**
     * 从图库获取图片
     */
    protected void openPictureFromLocal() {
        Intent intent = new Intent(getBaseContext(), MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 5);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        startActivityForResult(intent, SEND_IMAGE);
    }

    protected void openPictureFromCamera() {
        Intent intent = new Intent(getBaseContext(), MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_TAKE_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        startActivityForResult(intent, SEND_IMAGE);
    }

    /**
     * 选择图片并压缩
     *
     * @param selectedImage 原图
     * @return file
     */
    protected File selectPicture(Uri selectedImage) {
        Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
        String picturePath = null;
        if (cursor != null) {
            cursor.moveToFirst();
            picturePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            cursor.close();

            if (TextUtils.isEmpty(picturePath)) {
                CommonUtil.shortToast(mContext, "找不到图片");
                return null;
            }
        }
        if (TextUtils.isEmpty(picturePath)) {
            CommonUtil.shortToast(mContext, "图片不存在");
            return null;
        }
        return compressImage(picturePath);
    }

    protected File compressImage(String filePath) {
        File compressedFile;
        try {
            Bitmap tmpBitmap = AppUtil.CompressImage(filePath);
            Bitmap resultBitmap = AppUtil.scaleImage(tmpBitmap, tmpBitmap.getWidth(), AppUtil.getImageDegree(filePath));
            Bitmap thumbBitmap = AppUtil.scaleImage(tmpBitmap, EdusohoApp.screenW * 0.4f, AppUtil.getImageDegree(filePath));
            compressedFile = AppUtil.convertBitmap2File(resultBitmap,
                    AppUtil.getImageStorage() + "/" + System.currentTimeMillis());
            AppUtil.convertBitmap2File(thumbBitmap, AppUtil.getThumbImageStorage() + "/" + compressedFile.getName());
            if (!tmpBitmap.isRecycled()) {
                tmpBitmap.recycle();
            }
            if (!thumbBitmap.isRecycled()) {
                thumbBitmap.recycle();
            }
            if (!resultBitmap.isRecycled()) {
                resultBitmap.recycle();
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            return null;
        }
        return compressedFile;
    }

    public class MediaRecorderTask extends AsyncTask<Void, Integer, Boolean> {

        private int COUNT_DOWN_NUM = 50;
        private int TOTAL_NUM = 59;

        private ChatAudioRecord mAudioRecord;
        private boolean mCancelSave = false;
        private boolean mStopRecord = false;
        private boolean mIsCountDown = false;
        private File mUploadAudio;

        @Override
        protected void onPreExecute() {
            if (mAudioRecord == null) {
                mAudioRecord = new ChatAudioRecord(mContext);
            }
            mViewSpeakContainer.setVisibility(View.VISIBLE);
            tvSpeak.setText(getString(R.string.hand_up_and_end));
            tvSpeakHint.setText(getResources().getString(R.string.hand_move_up_and_send_cancel));
            tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_transparent_bg);
            ivRecordImage.setImageResource(R.drawable.record_animate_1);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mAudioRecord.ready();
            mAudioRecord.start();
            while (true) {
                if (mStopRecord) {
                    //结束录音
                    mUploadAudio = mAudioRecord.stop(mCancelSave);
                    int audioLength = mAudioRecord.getAudioLength();
                    if (audioLength >= 1) {
                        Log.d(TAG, "上传成功");
                    } else {
                        return false;
                    }
                    mAudioRecord.clear();
                    break;
                } else {
                    long recordTime = (System.currentTimeMillis() - mAudioRecord.getAudioStartTime()) / 1000;
                    if (recordTime > TOTAL_NUM) {
                        mStopRecord = true;
                        mCancelSave = false;
                        continue;
                    }
                    if (!mCancelSave) {
                        //录音中动画
                        double ratio = 0;
                        if (mAudioRecord.getMediaRecorder() != null) {
                            ratio = (double) mAudioRecord.getMediaRecorder().getRealVolume();
                        }

                        double db = 0;
                        if (ratio > 1) {
                            db = 20 * Math.log10(ratio);
                        }
                        if (recordTime >= COUNT_DOWN_NUM) {
                            mIsCountDown = true;
                            mHandler.obtainMessage(VolumeHandler.COUNT_DOWN, (int)(TOTAL_NUM - recordTime), 0).sendToTarget();
                        } else if (db < 60) {
                            mHandler.sendEmptyMessage(0);
                        } else if (db < 70) {
                            mHandler.sendEmptyMessage(1);
                        } else if (db < 80) {
                            mHandler.sendEmptyMessage(2);
                        } else if (db < 90) {
                            mHandler.sendEmptyMessage(3);
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSave) {
            if (mCancelSave) {
                mViewSpeakContainer.setVisibility(View.GONE);
                Log.d(TAG, "手指松开取消保存");
            } else {
                if (isSave) {
                    Log.d(TAG, "正常保存上传");
                    uploadMedia(mUploadAudio, PushUtil.ChatMsgType.AUDIO, Const.MEDIA_AUDIO);
                    mViewSpeakContainer.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "录制时间太短");
                    tvSpeakHint.setText(getString(R.string.audio_length_too_short));
                    tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_transparent_bg);
                    ivRecordImage.setImageResource(R.drawable.record_duration_short);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mViewSpeakContainer.setVisibility(View.GONE);
                        }
                    }, 200);
                    mAudioRecord.delete();
                }
            }
            tvSpeak.setText(getString(R.string.hand_press_and_speak));
            viewPressToSpeak.setPressed(false);
            super.onPostExecute(isSave);
        }

        public void setCancel(boolean cancel) {
            mCancelSave = cancel;
        }

        public void setAudioStop(boolean stop) {
            mStopRecord = stop;
        }

        public boolean getStopRecord() {
            return mStopRecord;
        }

        public boolean isCountDown() {
            return mIsCountDown;
        }

        public ChatAudioRecord getAudioRecord() {
            return mAudioRecord;
        }
    }

    protected static class VolumeHandler extends Handler {

        public static final int COUNT_DOWN = 4;

        protected int[] mSpeakerAnimResId = new int[]{
                R.drawable.record_animate_1,
                R.drawable.record_animate_2,
                R.drawable.record_animate_3,
                R.drawable.record_animate_4};

        private WeakReference<BaseChatActivity> mWeakReference;

        private VolumeHandler(BaseChatActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseChatActivity activity = mWeakReference.get();
            if (activity != null) {
                if (msg.what == COUNT_DOWN) {
                    int w = activity.ivRecordImage.getWidth();
                    int h = activity.ivRecordImage.getWidth();
                    activity.ivRecordImage.setImageBitmap(getCountDownBitmap(w, h, msg.arg1));
                    return;
                }
                activity.ivRecordImage.setImageResource(mSpeakerAnimResId[msg.what]);
            }
        }

        private Bitmap getCountDownBitmap(int w, int h, int number) {
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setTextSize(w * 0.9f);
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);

            Rect rect = new Rect();
            paint.getTextBounds(String.valueOf(number), 0, 1, rect);
            canvas.drawText(String.valueOf(number), (w - (rect.right - rect.left)) / 2, (h - rect.bottom - rect.top) / 2, paint);
            return bitmap;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.lv_messages) {
            if (viewMediaLayout.getVisibility() == View.VISIBLE) {
                viewMediaLayout.setVisibility(View.GONE);
            } else {
                AppUtil.setSoftKeyBoard(etSend, mActivity, Const.HIDE_KEYBOARD);
            }
        } else if (v.getId() == R.id.rl_btn_press_to_speak) {
            lvMessage.post(mListViewSelectRunnable);
            boolean mHandUpAndCancel;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    try {
                        if (!CommonUtil.isExitsSdcard()) {
                            CommonUtil.longToast(mContext, "发送语音需要sdcard");
                            return false;
                        }
                        mPressDownY = event.getY();
                        mMediaRecorderTask = new MediaRecorderTask();
                        mMediaRecorderTask.execute();
                    } catch (Exception e) {
                        mMediaRecorderTask.getAudioRecord().clear();
                        Log.d(TAG, e.getMessage());
                        return false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mMediaRecorderTask.getStopRecord()) {
                        return true;
                    }
                    float mPressMoveY = event.getY();
                    if (Math.abs(mPressDownY - mPressMoveY) > EdusohoApp.screenH * 0.1) {
                        tvSpeak.setText(getString(R.string.hand_up_and_exit));
                        tvSpeakHint.setText(getString(R.string.hand_up_and_exit));
                        tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_bg);
                        ivRecordImage.setImageResource(R.drawable.record_cancel);
                        mHandUpAndCancel = true;
                    } else {
                        if (!mMediaRecorderTask.isCountDown()) {
                            ivRecordImage.setImageResource(R.drawable.record_animate_1);
                        }
                        tvSpeakHint.setText(getString(R.string.hand_move_up_and_send_cancel));
                        tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_transparent_bg);
                        tvSpeak.setText(getString(R.string.hand_up_and_end));

                        mHandUpAndCancel = false;
                    }
                    mMediaRecorderTask.setCancel(mHandUpAndCancel);
                    return true;
                case MotionEvent.ACTION_UP:
                    mMediaRecorderTask.setAudioStop(true);
                    return false;
            }
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            viewMediaLayout.setVisibility(View.GONE);
            AppUtil.setSoftKeyBoard(etSend, mActivity, Const.SHOW_KEYBOARD);
            lvMessage.post(mListViewSelectRunnable);
        }
    }

    public static final String TAG = "BaseChatActivity";

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.et_send_content) {
            lvMessage.post(mListViewSelectRunnable);
        } else if (v.getId() == R.id.iv_show_media_layout) {
            //加号，显示多媒体框
            if (viewMediaLayout.getVisibility() == View.GONE) {
                viewMediaLayout.setVisibility(View.VISIBLE);
                etSend.clearFocus();
                AppUtil.setSoftKeyBoard(etSend, mActivity, Const.HIDE_KEYBOARD);
            } else {
                viewMediaLayout.setVisibility(View.GONE);
            }
            lvMessage.post(mListViewSelectRunnable);
        } else if (v.getId() == R.id.btn_send) {
            //发送消息
            if (etSend.getText().length() == 0) {
                return;
            }
            sendMsg(etSend.getText().toString());
        } else if (v.getId() == R.id.btn_voice) {
            //语音
            viewMediaLayout.setVisibility(View.GONE);
            btnVoice.setVisibility(View.GONE);
            viewMsgInput.setVisibility(View.GONE);
            btnKeyBoard.setVisibility(View.VISIBLE);
            viewPressToSpeak.setVisibility(View.VISIBLE);
            AppUtil.setSoftKeyBoard(etSend, mActivity, Const.HIDE_KEYBOARD);
        } else if (v.getId() == R.id.btn_set_mode_keyboard) {
            //键盘
            viewMediaLayout.setVisibility(View.GONE);
            btnVoice.setVisibility(View.VISIBLE);
            viewPressToSpeak.setVisibility(View.GONE);
            viewMsgInput.setVisibility(View.VISIBLE);
            btnKeyBoard.setVisibility(View.GONE);
            etSend.requestFocus();
            lvMessage.post(mListViewSelectRunnable);
        } else if (v.getId() == R.id.rl_btn_press_to_speak) {
            viewMediaLayout.setVisibility(View.GONE);
        } else if (v.getId() == R.id.iv_image) {
            openPictureFromLocal();
        } else if (v.getId() == R.id.iv_camera) {
            openPictureFromCamera();
        }
    }

    protected TextWatcher mContentTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(s)) {
                btnSend.setVisibility(View.VISIBLE);
                ivAddMedia.setVisibility(View.GONE);
            } else {
                ivAddMedia.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    // endregion widget events

    protected Runnable mListViewSelectRunnable = new Runnable() {
        @Override
        public void run() {
            if (lvMessage != null && lvMessage.getAdapter() != null) {
                lvMessage.setSelection(lvMessage.getCount());
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (viewMediaLayout.getVisibility() == View.VISIBLE) {
            viewMediaLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
