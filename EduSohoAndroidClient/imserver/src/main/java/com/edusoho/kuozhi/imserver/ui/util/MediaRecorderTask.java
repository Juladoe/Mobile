package com.edusoho.kuozhi.imserver.ui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.edusoho.kuozhi.imserver.ui.view.MessageInputView;

import java.io.File;

/**
 * Created by suju on 16/8/28.
 */
public class MediaRecorderTask extends AsyncTask<Void, Integer, Boolean> {

    private static final String TAG = "MediaRecorderTask";

    private int COUNT_DOWN_NUM = 50;
    private int TOTAL_NUM = 58;

    private ChatAudioRecord mAudioRecord;
    private boolean mCancelSave = false;
    private boolean mStopRecord = false;
    private boolean mIsCountDown = false;
    private File mUploadAudio;
    private Context mContext;
    private Handler mHandler;
    private MediaRecorderTackListener mMediaRecorderTackListener;

    public MediaRecorderTask(Context context, Handler handler, MediaRecorderTackListener listener) {
        this.mContext = context;
        this.mHandler = handler;
        this.mMediaRecorderTackListener = listener;
    }

    @Override
    protected void onPreExecute() {
        if (mAudioRecord == null) {
            mAudioRecord = new ChatAudioRecord(mContext);
        }
        mMediaRecorderTackListener.onPreRecord();
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
                long audioLength = mAudioRecord.getAudioLength();
                if (audioLength >= 1000) {
                    Log.d(TAG, "录制成功");
                } else {
                    return false;
                }
                mAudioRecord.clear();
                break;
            } else {
                long recordTime = (System.currentTimeMillis() - mAudioRecord.getAudioStartTime()) / 1000;
                if (mAudioRecord.getAudioStartTime() > 0 && recordTime > TOTAL_NUM) {
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
                    if (recordTime > COUNT_DOWN_NUM) {
                        mIsCountDown = true;
                        mHandler.obtainMessage(MessageInputView.VolumeHandler.COUNT_DOWN, (int) (TOTAL_NUM - recordTime), 0).sendToTarget();
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
            mMediaRecorderTackListener.onCancel();
            Log.d(TAG, "手指松开取消保存");
        } else {
            if (isSave) {
                Log.d(TAG, "正常保存上传");
                mMediaRecorderTackListener.onStopRecord(mUploadAudio);
            } else {
                mMediaRecorderTackListener.onStopRecord(null);
                Log.d(TAG, "录制时间太短");
                mAudioRecord.delete();
            }
        }

        mMediaRecorderTackListener.onReset();
        super.onPostExecute(isSave);
    }

    public void setCancel(boolean cancel) {
        mCancelSave = cancel;
    }

    public void setAudioStop(boolean stop) {
        mStopRecord = stop;
    }

    public ChatAudioRecord getAudioRecord() {
        return mAudioRecord;
    }

    public boolean getStopRecord() {
        return mStopRecord;
    }

    public boolean isCountDown() {
        return mIsCountDown;
    }


    public interface MediaRecorderTackListener {

        void onPreRecord();

        void onCancel();

        void onStopRecord(File audioFile);

        void onReset();
    }


}

