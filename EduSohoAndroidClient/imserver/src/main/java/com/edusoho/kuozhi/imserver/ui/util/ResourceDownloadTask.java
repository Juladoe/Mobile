package com.edusoho.kuozhi.imserver.ui.util;

import android.content.Context;
import android.util.Log;

import com.edusoho.kuozhi.imserver.ui.helper.MessageHelper;
import com.edusoho.kuozhi.imserver.util.SystemUtil;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpResponse;

import java.io.File;

/**
 * Created by suju on 16/8/28.
 */
public class ResourceDownloadTask implements IResourceTask {

    private  static final String TAG = "ResourceDownloadTask";

    private String mUrl;
    private int mTaskId;
    private File mRealFile;
    private Context mContext;
    private TaskFeature mTaskFeature;
    private Future<File> mFuture;

    public ResourceDownloadTask(Context context, int taskId, String url, File readFile) {
        this.mUrl = url;
        this.mTaskId = taskId;
        this.mContext = context;
        this.mRealFile = readFile;
    }

    @Override
    public TaskFeature execute() {
        mTaskFeature = new TaskFeature(mTaskId, ITaskStatusListener.DOWNLOAD);
        AsyncHttpGet httpGet = new AsyncHttpGet(mUrl);
        mFuture = AsyncHttpClient.getDefaultInstance().executeFile(httpGet, mRealFile.getAbsolutePath(), new AsyncHttpClient.FileCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, File file) {
                if ( e != null) {
                    e.printStackTrace();
                    mTaskFeature.fail();
                    return;
                }
                if (!file.exists() || source.code() != 200) {
                    Log.d(TAG, "resource down fail " + mTaskId);
                    mTaskFeature.fail();
                    return;
                }
                if (file.getName().endsWith(ChatAudioRecord.AUDIO_EXTENSION)) {
                    mTaskFeature.success(file.getAbsolutePath());
                    return;
                }
                MessageHelper messageHelper = new MessageHelper(mContext);
                File thumbFile = messageHelper.compressTumbImageByFile(file.getAbsolutePath(), SystemUtil.getScreenWidth(mContext));
                if (thumbFile.exists()) {
                    mTaskFeature.success(thumbFile.getAbsolutePath());
                    return;
                }

                mTaskFeature.fail();
            }
        });
        return mTaskFeature;
    }

    @Override
    public void cancel() {
        if (mFuture != null) {
            mFuture.cancel();
        }
    }

    @Override
    public int getTaskId() {
        return mTaskId;
    }
}
