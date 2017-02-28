package com.edusoho.kuozhi.imserver.util;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.future.Cancellable;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.WebSocketHandshakeException;
import com.koushikdutta.async.http.WebSocketImpl;
import com.koushikdutta.async.http.callback.HttpConnectCallback;

/**
 * Created by suju on 16/8/12.
 */
public class CustomAsyncHttpClient extends AsyncHttpClient {

    private static CustomAsyncHttpClient mDefaultInstance;
    public static CustomAsyncHttpClient getDefaultInstance() {
        if (mDefaultInstance == null)
            mDefaultInstance = new CustomAsyncHttpClient(AsyncServer.getDefault());

        return mDefaultInstance;
    }

    public CustomAsyncHttpClient(AsyncServer server) {
        super(server);
    }

    @Override
    public Future<WebSocket> websocket(final AsyncHttpRequest req, String protocol, final WebSocketConnectCallback callback) {
        WebSocketImpl.addWebSocketUpgradeHeaders(req, protocol);
        Headers headers = req.getHeaders();
        headers.remove("cache-Control");
        headers.remove("Sec-WebSocket-Extensions");
        headers.remove("Pragma");
        headers.remove("User-Agent");
        headers.remove("accept-encoding");
        headers.remove("Accept");
        final SimpleFuture<WebSocket> ret = new SimpleFuture<WebSocket>();
        Cancellable connect = execute(req, new HttpConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, AsyncHttpResponse response) {
                if (ex != null) {
                    if (ret.setComplete(ex)) {
                        if (callback != null)
                            callback.onCompleted(ex, null);
                    }
                    return;
                }
                WebSocket ws = WebSocketImpl.finishHandshake(req.getHeaders(), response);
                if (ws == null) {
                    if (!ret.setComplete(new WebSocketHandshakeException("Unable to complete websocket handshake")))
                        return;
                }
                else {
                    if (!ret.setComplete(ws))
                        return;
                }
                if (callback != null)
                    callback.onCompleted(ex, ws);
            }
        });

        ret.setParent(connect);
        return ret;
    }
}
