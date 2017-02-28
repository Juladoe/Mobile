package com.edusoho.longinus.util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.edusoho.kuozhi.imserver.IImServerAidlInterface;
import com.edusoho.kuozhi.imserver.ImService;
import com.edusoho.kuozhi.imserver.broadcast.IMServiceStartedBroadcastReceiver;
import com.edusoho.kuozhi.imserver.listener.IMConnectStatusListener;
import com.edusoho.kuozhi.imserver.listener.IMMessageReceiver;
import com.edusoho.kuozhi.imserver.util.IMConnectStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by suju on 16/10/18.
 */
public class LiveImClient {

    String TAG = "LiveImClient";

    private int mIMConnectStatus;
    private Context mContext;
    private List<IMMessageReceiver> mMessageReceiverList;
    private List<IMConnectStatusListener> mIMConnectStatusListenerList;
    private BroadcastReceiver mIMServiceStatusBroadcastReceiver;
    private IImServerAidlInterface mImBinder;
    private ServiceConnection mServiceConnection;
    private OnConnectedCallback mOnConnectedCallback;
    private ConnectIMServiceRunnable mConnectIMServiceRunnable;
    private static LiveImClient liveImClient;

    private LiveImClient(Context context) {
        this.mContext = context;
        this.mIMConnectStatus = IMConnectStatus.NO_READY;
        mMessageReceiverList = new LinkedList<>();
        mIMConnectStatusListenerList = new LinkedList<>();
        registIMServiceStatusBroadcastReceiver();
    }

    public static LiveImClient getIMClient(Context context) {
        if (context != null && liveImClient == null) {
            liveImClient = new LiveImClient(context);
        }
        return liveImClient;
    }

    public IImServerAidlInterface getImBinder() {
        return mImBinder;
    }

    public void setOnConnectedCallback(OnConnectedCallback onConnectedCallback) {
        this.mOnConnectedCallback = onConnectedCallback;
    }

    public void destory() {
        if (mImBinder != null) {
            try {
                mImBinder.closeIMServer();
            } catch (RemoteException e) {
                Log.e(TAG, "closeIMServer error");
            }
        }
        if (mServiceConnection != null) {
            mContext.unbindService(mServiceConnection);
            mServiceConnection = null;
        }

        unRegistIMServiceStatusBroadcastReceiver();
        mContext.stopService(getIMServiceIntent());
        mImBinder = null;

        mIMConnectStatusListenerList.clear();
        mMessageReceiverList.clear();
        liveImClient = null;
    }

    private void unRegistIMServiceStatusBroadcastReceiver() {
        if (mIMServiceStatusBroadcastReceiver != null) {
            mContext.unregisterReceiver(mIMServiceStatusBroadcastReceiver);
        }
    }

    private void registIMServiceStatusBroadcastReceiver() {
        mIMServiceStatusBroadcastReceiver = new IMServiceStartedBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mConnectIMServiceRunnable == null) {
                    return;
                }
                new Handler(Looper.getMainLooper()).post(mConnectIMServiceRunnable);
            }
        };

        mContext.registerReceiver(mIMServiceStatusBroadcastReceiver, new IntentFilter(IMServiceStartedBroadcastReceiver.ACTION_NAME));
    }

    public void start(
            int clientId, String clientName, ArrayList<String> ignoreNosList, ArrayList<String> hostList) {
        this.mConnectIMServiceRunnable = new ConnectIMServiceRunnable(clientId, clientName, ignoreNosList, hostList);
        startImService();
    }

    private void startImService() {
        Intent intent = getIMServiceIntent();
        intent.putExtra(ImService.ACTION, ImService.ACTION_INIT);
        mContext.startService(intent);
    }

    private Intent getIMServiceIntent() {
        Intent intent = new Intent("com.edusoho.kuozhi.imserver.IImMemServerAidlInterface");
        intent.setPackage(mContext.getPackageName());
        return intent;
    }

    private void connectService(
            final int clientId, final String clientName, final String[] ignoreNosList, final String[] hostList) {
        if (mServiceConnection != null) {
            mContext.unbindService(mServiceConnection);
        }
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mImBinder = IImServerAidlInterface.Stub.asInterface(service);
                try {
                    if (mImBinder != null && mOnConnectedCallback != null) {
                        mOnConnectedCallback.onConnected();
                    }
                    mImBinder.start(clientId, clientName, ignoreNosList, hostList);
                } catch (RemoteException e) {
                    Log.d(TAG, "bind live im server error");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, name.toString());
            }
        };
        boolean result = mContext.bindService(
                new Intent("com.edusoho.kuozhi.imserver.IImMemServerAidlInterface")
                        .setPackage(mContext.getPackageName()),
                mServiceConnection,
                Context.BIND_AUTO_CREATE
        );
        Log.d(TAG, "bind:" + result);
    }

    public interface OnConnectedCallback {

        void onConnected();
    }

    private class ConnectIMServiceRunnable implements Runnable {

        private int mClientId;
        private String mClientName;
        private String[] mHostList;
        private String[] mIgnoreNosList;

        public ConnectIMServiceRunnable(
                int clientId, String clientName, ArrayList<String> ignoreNosList, ArrayList<String> hostList) {
            this.mClientId = clientId;
            this.mClientName = clientName;
            this.mHostList = new String[hostList.size()];
            this.mIgnoreNosList = new String[ignoreNosList.size()];
            ignoreNosList.toArray(mIgnoreNosList);
            hostList.toArray(mHostList);
        }

        @Override
        public void run() {
            connectService(mClientId, mClientName, mIgnoreNosList, mHostList);
        }
    }
}
