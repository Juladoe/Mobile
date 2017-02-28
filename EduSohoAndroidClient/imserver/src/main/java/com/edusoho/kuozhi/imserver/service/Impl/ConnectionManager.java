package com.edusoho.kuozhi.imserver.service.Impl;

import android.util.Log;

import com.edusoho.kuozhi.imserver.listener.IChannelReceiveListener;
import com.edusoho.kuozhi.imserver.listener.IConnectManagerListener;
import com.edusoho.kuozhi.imserver.service.IConnectionManager;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.WritableCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created by 菊 on 2016/4/22.
 */
public class ConnectionManager implements IConnectionManager {

    public static final String TAG = "ConnectionManager";

    protected int mCurrentHostIndex;
    protected int mStatus;
    protected List<String> mHostList;

    private Future<WebSocket> mWebSocketFuture;
    private IConnectManagerListener mIConnectStatusListener;
    private IChannelReceiveListener mIChannelReceiveListener;
    private String mClientName;

    public ConnectionManager(String clientName) {
        this.mClientName = clientName;
        this.mStatus = IConnectManagerListener.NONE;
    }

    @Override
    public void stop() {
        this.mIChannelReceiveListener = null;
        this.mIConnectStatusListener = null;
        close();
    }

    private void close() {
        if (mWebSocketFuture == null) {
            return;
        }
        WebSocket webSocket = mWebSocketFuture.tryGet();
        if (webSocket != null) {
            webSocket.close();
            Log.d(TAG, "webSocket close");
        }
        mWebSocketFuture.cancel();
        mWebSocketFuture = null;
        AsyncHttpClient.getDefaultInstance().getServer().stop();
    }

    @Override
    public void setServerHostList(List<String> hostList) {
        this.mHostList = hostList;
    }

    @Override
    public void addIConnectStatusListener(IConnectManagerListener iConnectStatusListener) {
        this.mIConnectStatusListener = iConnectStatusListener;
    }

    @Override
    public void send(String content) {
        if (mWebSocketFuture == null) {
            return;
        }
        WebSocket webSocket = mWebSocketFuture.tryGet();
        if (webSocket == null) {
            return;
        }
        webSocket.send(content);
    }

    @Override
    public int getStatus() {
        return mStatus;
    }

    @Override
    public void accept() {
        mCurrentHostIndex = 0;
        connectWebSocket();
    }

    @Override
    public void addIChannelReceiveListener(IChannelReceiveListener listener) {
        this.mIChannelReceiveListener = listener;
    }

    @Override
    public boolean isConnected() {
        if (mWebSocketFuture == null) {
            return false;
        }

        WebSocket webSocket = mWebSocketFuture.tryGet();
        if (webSocket == null) {
            return false;
        }
        return webSocket != null && webSocket.isOpen();
    }

    private void connectWebSocket() {
        String host = mHostList.get(mCurrentHostIndex);
        Log.d(getClass().getSimpleName(), host);
        mWebSocketFuture = AsyncHttpClient.getDefaultInstance().websocket(
                host + "&clientName=" + URLEncoder.encode(mClientName),
                null,
                getWebSocketConnectCallback()
        );
        this.mStatus = IConnectManagerListener.CONNECTING;
        mIConnectStatusListener.onStatusChange(IConnectManagerListener.CONNECTING, "connect...");
    }

    @Override
    public void switchConnect() {
        close();
        mCurrentHostIndex++;
        if (mCurrentHostIndex >= mHostList.size()) {
            Log.d(TAG, "switchConnect is full try error");
            this.mStatus = IConnectManagerListener.ERROR;
            if (mIConnectStatusListener != null) {
                mIConnectStatusListener.onStatusChange(IConnectManagerListener.ERROR, "error");
            }
            return;
        }
        Log.d(TAG, "switchConnect:" + mHostList.get(mCurrentHostIndex));
        connectWebSocket();
    }

    protected AsyncHttpClient.WebSocketConnectCallback getWebSocketConnectCallback() {
        return new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    Log.d(TAG, String.format("onCompleted error:%s, %d", ex.getMessage(), mCurrentHostIndex));
                    mStatus = IConnectManagerListener.CLOSE;
                    if (mIConnectStatusListener != null) {
                        mIConnectStatusListener.onStatusChange(IConnectManagerListener.CLOSE, ex.getMessage());
                    }
                    return;
                }
                Log.d(TAG, "onCompleted:" + webSocket);
                initOnReceiveCallback(webSocket);
                webSocket.setEndCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception e) {
                        e.printStackTrace();
                        mStatus = IConnectManagerListener.END;
                        if (mIConnectStatusListener != null) {
                            mIConnectStatusListener.onStatusChange(IConnectManagerListener.END, e.getMessage());
                        }
                    }
                });

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception e) {
                        Log.d(TAG, "close");

                        mStatus = IConnectManagerListener.CLOSE;
                        if (mIConnectStatusListener != null) {
                            mIConnectStatusListener.onStatusChange(IConnectManagerListener.CLOSE, "close");
                        }
                    }
                });

                webSocket.setWriteableCallback(new WritableCallback() {
                    @Override
                    public void onWriteable() {
                        Log.d(TAG, "onWriteable");
                    }
                });
                if (webSocket.isOpen() && mIConnectStatusListener != null) {
                    mStatus = IConnectManagerListener.OPEN;
                    mIConnectStatusListener.onStatusChange(IConnectManagerListener.OPEN, "open");
                }
            }
        };
    }

    protected void initOnReceiveCallback(WebSocket webSocket) {
        webSocket.setStringCallback(new WebSocket.StringCallback() {
            @Override
            public void onStringAvailable(String s) {
                Log.d(TAG, "onStringAvailable:" + s);
                if (mIChannelReceiveListener == null) {
                    return;
                }
                mIChannelReceiveListener.onReceiver(s);
            }
        });

        webSocket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                if (mIChannelReceiveListener == null) {
                    return;
                }
                mIChannelReceiveListener.onReceiver(new String(bb.getAllByteArray()));
            }
        });
    }
}
