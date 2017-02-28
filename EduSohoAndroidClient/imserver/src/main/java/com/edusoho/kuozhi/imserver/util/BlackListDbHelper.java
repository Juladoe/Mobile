package com.edusoho.kuozhi.imserver.util;

import android.content.ContentValues;
import android.content.Context;

import com.edusoho.kuozhi.imserver.factory.DbManagerFactory;
import com.edusoho.kuozhi.imserver.managar.IMBlackListManager;

import java.util.HashMap;

/**
 * Created by suju on 16/8/20.
 */
public class BlackListDbHelper {

    private static final String TABLE = "im_blacklist";
    private DbHelper mDbHelper;

    public BlackListDbHelper(Context context) {
        mDbHelper = new DbHelper(context, DbManagerFactory.getDefaultFactory().createIMDbManager(context));
    }

    public int getBlackList(String convNo) {
        HashMap<String, String> arrayMap = mDbHelper.querySingle(TABLE, "convNo=?", new String[]{convNo});
        if (arrayMap == null) {
            return IMBlackListManager.NONE;
        }
        return MessageUtil.parseInt(arrayMap.get("status"));
    }

    public long create(String convNo, int status) {
        ContentValues cv = new ContentValues();
        cv.put("convNo", convNo);
        cv.put("status", status);
        return mDbHelper.insert(TABLE, cv);
    }

    public long updateByName(String name, String convNo, int status) {
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        return mDbHelper.update(TABLE, cv, String.format("%s=?", name), new String[]{convNo});
    }

    public long deleteByName(String name, String value) {
        return mDbHelper.delete(TABLE, String.format("%s=?", name), new String[]{value});
    }
}
