package com.edusoho.kuozhi.imserver.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.edusoho.kuozhi.imserver.util.NetTypeConst;

/**
 * Created by 菊 on 2016/4/24.
 */
public class NetWorkStatusBroadcastReceiver extends BroadcastReceiver {

    private NetWorkStatusCallback mNetWorkStatusCallback;

    public NetWorkStatusBroadcastReceiver(NetWorkStatusCallback callback) {
        this.mNetWorkStatusCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();

        boolean isConnected = false;
        int netType = NetTypeConst.WCDMA;
        if (mobileInfo != null) {
            isConnected = mobileInfo.isConnected();
        }
        if (wifiInfo != null) {
            netType = NetTypeConst.WIFI;
            isConnected = wifiInfo.isConnected();
        }

        if (activeInfo == null) {
            netType = NetTypeConst.NONE;
            isConnected = false;
        }

        if (mNetWorkStatusCallback != null) {
            mNetWorkStatusCallback.onStatusChange(netType, isConnected);
        }
    }

    public interface NetWorkStatusCallback {
        void onStatusChange(int netType, boolean isConnected);
    }
}
