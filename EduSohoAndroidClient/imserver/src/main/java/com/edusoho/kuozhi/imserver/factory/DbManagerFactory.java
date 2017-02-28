package com.edusoho.kuozhi.imserver.factory;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.edusoho.kuozhi.imserver.helper.IDbManager;
import com.edusoho.kuozhi.imserver.helper.IMDbManager;

/**
 * Created by suju on 16/8/17.
 */
public class DbManagerFactory {

    private static DbManagerFactory defaultDbManagerFactory = new DbManagerFactory();

    public static DbManagerFactory getDefaultFactory() {
        return defaultDbManagerFactory;
    }

    /**
     * 跨进程读取
     * @param context
     * @param dbName
     */
    public void setDbName(Context context, String dbName) {
        SharedPreferences sp = context.getSharedPreferences("current_db", Context.MODE_MULTI_PROCESS);
        sp.edit().putString("name", String.format("im_db_%s", dbName)).commit();
    }

    private String getDbName(Context context) {
        SharedPreferences sp = context.getSharedPreferences("current_db", Context.MODE_MULTI_PROCESS);
        return sp.getString("name", "");
    }

    public IDbManager createIMDbManager(Context context) {
        String dbName = getDbName(context);
        if (TextUtils.isEmpty(dbName)) {
            throw new RuntimeException("dbName no empty");
        }
        return new IMDbManager(context, dbName);
    }
}
