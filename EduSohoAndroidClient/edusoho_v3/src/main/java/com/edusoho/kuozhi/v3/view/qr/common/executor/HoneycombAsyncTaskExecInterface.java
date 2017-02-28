package com.edusoho.kuozhi.v3.view.qr.common.executor;

import android.annotation.TargetApi;
import android.os.AsyncTask;

/**
 * Created by howzhi on 15/3/16.
 */
@TargetApi(11)
public class HoneycombAsyncTaskExecInterface implements AsyncTaskExecInterface {

    @Override
    public <T> void execute(AsyncTask<T, ?, ?> task, T... args) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args);
    }

}
