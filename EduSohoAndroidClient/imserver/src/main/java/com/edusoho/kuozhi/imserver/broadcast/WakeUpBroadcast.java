package com.edusoho.kuozhi.imserver.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by 菊 on 2016/4/28.
 */
public class WakeUpBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(getClass().getSimpleName(), "onReceive");
        Intent imServiceIntent = new Intent("com.edusoho.kuozhi.imserver.IImServerAidlInterface");
        imServiceIntent.setPackage(context.getPackageName());
        context.startService(imServiceIntent);
    }
}
