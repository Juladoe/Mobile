package com.edusoho.kuozhi.imserver.managar;

import android.content.Context;
import com.edusoho.kuozhi.imserver.util.BlackListDbHelper;

/**
 * Created by suju on 16/8/21.
 */
public class IMBlackListManager {

    /*
        normal 0
        no_disturb 1
     */
    public static final int NONE = -1;
    public static final int NORMAL = 0;
    public static final int NO_DISTURB = 1;

    private Context mContext;

    public IMBlackListManager(Context context) {
        this.mContext = context;
    }

    public int getBlackListByConvNo(String convNo) {
        return new BlackListDbHelper(mContext).getBlackList(convNo);
    }

    public long createBlackList(String convNo, int status) {
        return new BlackListDbHelper(mContext).create(convNo, status);
    }

    public long updateByConvNo(String convNo, int status) {
        return new BlackListDbHelper(mContext).updateByName("convNo", convNo, status);
    }

    public long deleteByConvNo(String convNo) {
        return new BlackListDbHelper(mContext).deleteByName("convNo", convNo);
    }
}
