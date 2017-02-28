package com.edusoho.kuozhi.imserver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.edusoho.kuozhi.imserver.broadcast.IMServiceStartedBroadcastReceiver;
import com.edusoho.kuozhi.imserver.broadcast.NetWorkStatusBroadcastReceiver;
import com.edusoho.kuozhi.imserver.service.Impl.DbMsgManager;
import com.edusoho.kuozhi.imserver.util.IMConnectStatus;
import com.edusoho.kuozhi.imserver.util.NetTypeConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by su on 2016/3/18.
 */
public class ImService extends Service {

    private static final String TAG = "ImService";

    public static final String HOST = "host";
    public static final String IGNORE_NOS = "ignoreNos";
    public static final String CLIENT_NAME = "clientName";
    public static final String ACTION = "action";
    public static final int ACTION_INIT = 0011;

    private NetWorkStatusBroadcastReceiver mReceiver;
    private ImServer mImServer;
    private boolean isCloseByUser;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mImServer = getIMServer();
        registNetWorkStatusBroadcastReceiver();
    }

    protected ImServer getIMServer() {
        ImServer imServer = new ImServer(getBaseContext());
        imServer.setMsgManager(new DbMsgManager(getBaseContext()));
        return imServer;
    }

    private void registNetWorkStatusBroadcastReceiver() {
        mReceiver = new NetWorkStatusBroadcastReceiver(getNetWorkStatusCallback());
        registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected NetWorkStatusBroadcastReceiver.NetWorkStatusCallback getNetWorkStatusCallback() {

        return new NetWorkStatusBroadcastReceiver.NetWorkStatusCallback() {
            @Override
            public void onStatusChange(int netType, boolean isConnected) {
                Log.d(TAG, String.format("onStatusChange netType:%d isConnected:%b", netType, isConnected));
                if (!mImServer.isReady()) {
                    return;
                }
                switch (netType) {
                    case NetTypeConst.WIFI:
                        mImServer.getHeartManager().switchPingType(NetTypeConst.WIFI);
                        break;
                    case NetTypeConst.NONE:
                        if (!isConnected && mImServer.isConnected()) {
                            mImServer.pause();
                        }
                        break;
                    case NetTypeConst.GSM:
                    case NetTypeConst.LTE:
                    case NetTypeConst.WCDMA:
                        mImServer.getHeartManager().switchPingType(netType);
                        break;
                }

                if (isConnected && mImServer.isCancel()) {
                    Log.d(TAG, "network Connected and start ImServer");
                    mImServer.start();
                    return;
                }

                if (!isConnected && mImServer.isConnected()) {
                    Log.d(TAG, "network not Connected and stop ImServer");
                    mImServer.pause();
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand" + intent);
        sendServiceStatusBroadCast();
        if (intent == null) {
            initServerHostFromLater();
            return super.onStartCommand(intent, flags, startId);
        }
        int action = intent.getIntExtra(ACTION, 0);

        if (action == ACTION_INIT) {
            Log.d(TAG, "init");
            return Service.START_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendServiceStatusBroadCast() {
        Intent intent = new Intent(IMServiceStartedBroadcastReceiver.ACTION_NAME);
        sendBroadcast(intent);
    }

    private void setServerHostConfig(int clientId, String clientName, String[] ignoreNosArray, String[] hostArray) {
        List<String> hostList = Arrays.asList(hostArray);
        List<String> ignoreNosList = Arrays.asList(ignoreNosArray);

        initServerHost(clientId, clientName, hostList, ignoreNosList);
        saveLaterHost(clientId, clientName, ignoreNosList, hostList);
    }

    private void initServerHostFromLater() {
        SharedPreferences sp = getSharedPreferences("laterHost", Context.MODE_PRIVATE);
        String clientName = sp.getString("clientName", null);
        int clientId = sp.getInt("clientId", 0);
        Set ignoreNosSet = sp.getStringSet("ignoreNosList", null);
        Set hostSet = sp.getStringSet("hostList", null);

        if (TextUtils.isEmpty(clientName) || ignoreNosSet == null || hostSet == null) {
            return;
        }

        Log.d(TAG, "initServerHostFromLater");
        initServerHost(clientId, clientName, new ArrayList<>(ignoreNosSet), new ArrayList<>(hostSet));
    }

    private void saveLaterHost(int clientId, String clientName, List<String> ignoreNosList, List<String> hostList) {
        SharedPreferences.Editor editor = getSharedPreferences("laterHost", Context.MODE_PRIVATE).edit();
        editor.putString("clientName", clientName);
        editor.putInt("clientId", clientId);
        editor.putStringSet("ignoreNosList", new HashSet<>(ignoreNosList));
        editor.putStringSet("hostList", new HashSet<>(hostList));
        editor.commit();
    }

    private void initServerHost(int clientId, String clientName, List<String> hostList, List<String> ignoreNosList) {
        if (hostList == null || hostList.isEmpty()) {
            Log.d(TAG, "no server host");
            return;
        }

        Log.d(getClass().getSimpleName(), Arrays.toString(hostList.toArray()));
        mImServer.initWithHost(clientId, clientName, hostList, ignoreNosList);
    }

    protected void sendWakeUpAlert() {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, ImService.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        mImServer.stop();
        mImServer = null;
        if (!isCloseByUser) {
            sendWakeUpAlert();
        }
        Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return new ImBinder();
    }

    public class ImBinder extends IImServerAidlInterface.Stub {
        public void start(int clientId, String clientName, String[] ignoreNosList, String[] hostList) {
            if (mImServer == null) {
                return;
            }
            setServerHostConfig(clientId, clientName, ignoreNosList, hostList);
            mImServer.start();
        }

        public void requestConnect() {
            if (mImServer == null) {
                return;
            }
            mImServer.requestConnect();
        }

        public void requestOfflineMsg() {
            if (mImServer == null) {
                return;
            }
            mImServer.requestOfflineMsg();
        }

        public void send(SendEntity sendEntity) {
            if (mImServer == null) {
                return;
            }
            mImServer.sendMessage(sendEntity);
        }

        @Override
        public void closeIMServer() throws RemoteException {
            if (mImServer != null) {
                mImServer.stop();
                isCloseByUser = true;
            }
        }

        @Override
        public int getIMStatus() throws RemoteException {
            if (!mImServer.isReady()) {
                return IMConnectStatus.NO_READY;
            }
            return mImServer.getStatus();
        }

        public void joinConversation(String token, String convNo) {
            if (mImServer != null) {
                mImServer.send(new String[] {
                        "cmd", "add",
                        "convNo", convNo,
                        "token", token
                });
            }
        }
    }
}
