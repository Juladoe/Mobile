package com.edusoho.kuozhi.imserver.service.Impl;

import android.os.SystemClock;
import android.util.Log;

import com.edusoho.kuozhi.imserver.listener.IHeartStatusListener;
import com.edusoho.kuozhi.imserver.service.IHeartManager;
import com.edusoho.kuozhi.imserver.util.NetTypeConst;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 菊 on 2016/4/22.
 */
public class HeartManagerImpl implements IHeartManager {

    private static final String TAG = "HeartManager";
    private static final int MAX_PING_COUNT = 6;

    private int pingType;
    private int pingSpeedType;
    private int pingSuccessCount;
    private int pingFailCount;
    private long lasterPingTime;
    private int pingTotalCount;
    private int currentPongStatus;

    private IHeartStatusListener mIHeartStatusListener;
    private Thread mPingThread;
    private Timer mPingTimer;

    public HeartManagerImpl() {
        init();
    }

    private void init() {
        this.pingFailCount = 0;
        this.pingTotalCount = 0;
        this.pingSuccessCount = 0;
        switchPingType(NetTypeConst.WCDMA);
    }

    private void upgradePingSpeed() {
        if (pingSpeedType == PING_FAST) {
            return;
        }
        pingSpeedType++;
    }

    public void switchPingType(int type) {
        this.pingType = type;
        switch (type) {
            case NetTypeConst.WCDMA:
                pingSpeedType = PING_NORMAL;
                break;
            case NetTypeConst.GSM:
                pingSpeedType = PING_FAST;
                break;
            case NetTypeConst.LTE:
            case NetTypeConst.WIFI:
                pingSpeedType = PING_SLOW;
                break;
        }
    }

    private int getPingTime() {
        float pingSpeed = 0;
        if (pingFailCount == 0) {
            pingSpeed = pingSuccessCount > MAX_PING_COUNT ? MAX_PING_COUNT : pingSuccessCount;
        } else {
            pingSpeed = (MAX_PING_COUNT - pingFailCount) / (float) MAX_PING_COUNT;
        }

        switch (pingSpeedType) {
            case PING_FAST:
                return (int) (6000 * pingSpeed);
            case PING_NORMAL:
                return (int) (6000 * 3 * pingSpeed);
            case PING_SLOW:
                return (int) (6000 * 6 * pingSpeed);
        }
        return 6000;
    }

    private int getPingMaxCount() {
        return MAX_PING_COUNT;
    }

    @Override
    public void start() {
        mPingTimer = new Timer();
        mPingTimer.schedule(getPingTimerTask(), 1);
    }

    private TimerTask getPingTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (currentPongStatus == PONG_TIMEOUT) {
                    setPongResult(PONG_FAIL);
                }
                lasterPingTime = SystemClock.currentThreadTimeMillis();
                pingTotalCount ++;
                Log.d(TAG, "lasterPingTime:" + pingTotalCount);
                Log.d(TAG, "current PingSpeed:" + pingSpeedType);
                if (pingFailCount >= MAX_PING_COUNT) {
                    Log.d(TAG, "ping timeout");
                    stop();
                    mIHeartStatusListener.onPong(IHeartStatusListener.TIMEOUT);
                    return;
                }
                if (pingSuccessCount > getPingMaxCount()) {
                    pingSuccessCount = 0;
                    upgradePingSpeed();
                }
                mIHeartStatusListener.onPing();
                currentPongStatus = PONG_TIMEOUT;

                mPingTimer.schedule(getPingTimerTask(), getPingTime());
            }
        };
    }

    public void setPongResult(int pongType) {
        switch (pongType) {
            case PONG_SUCCESS:
                pingSuccessCount ++;
                pingFailCount = 0;
                currentPongStatus = PONG_SUCCESS;
                break;
            case PONG_FAIL:
                currentPongStatus = PONG_FAIL;
                pingFailCount ++;
                pingSuccessCount = 0;
                Log.d(TAG, "pingFail:" + pingFailCount);
                break;
        }
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop");
        if (mPingTimer != null) {
            mPingTimer.cancel();
        }
        if (mPingThread != null) {
            mPingThread.interrupt();
            mPingThread = null;
        }

        init();
    }

    @Override
    public void addHeartStatusListener(IHeartStatusListener listener) {
        this.mIHeartStatusListener = listener;
    }
}
