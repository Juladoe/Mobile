package com.edusoho.kuozhi.v3.util.server;

import android.content.Context;

import com.edusoho.kuozhi.v3.service.handler.FileHandler;

/**
 * Created by suju on 17/2/15.
 */
public class CacheServerFactory {

    private static CacheServerFactory ourInstance = new CacheServerFactory();

    public static CacheServerFactory getInstance() {
        return ourInstance;
    }

    private volatile int mRefCount = 0;
    private CacheServer mCacheServer;

    private CacheServerFactory() {
    }

    public void start(Context context, String host, int loginUserId) {
        mRefCount ++;
        mCacheServer = createServer(context, host, loginUserId);
        mCacheServer.start();
    }

    private CacheServer createServer(Context context, String host, int loginUserId) {
        return new CacheServer.Builder(context)
                .addHandler("*", new FileHandler(context, host, loginUserId))
                .builder();
    }

    public void stop() {
        mRefCount --;
        if (mRefCount <= 0) {
            if (mCacheServer != null) {
                mCacheServer.close();
            }
            mRefCount = 0;
        }
    }

    public void resume() {
        if (mCacheServer != null) {
            mCacheServer.keepOn();
        }
    }

    public void pause() {
        if (mCacheServer != null) {
            mCacheServer.pause();
        }
    }
}
