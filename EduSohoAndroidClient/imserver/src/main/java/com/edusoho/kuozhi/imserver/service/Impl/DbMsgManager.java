package com.edusoho.kuozhi.imserver.service.Impl;

import android.content.Context;
import com.edusoho.kuozhi.imserver.helper.impl.ConvDbHelper;
import com.edusoho.kuozhi.imserver.helper.impl.MsgDbHelper;

/**
 * Created by 菊 on 2016/4/22.
 */
public class DbMsgManager extends AbstractMsgManager {

    private Context mContext;

    public DbMsgManager(Context context) {
        super();
        this.mContext = context;
        reset();
    }

    @Override
    public void clear() {
        mMsgDbHelper = null;
        mConvDbHelper = null;
    }

    @Override
    public void reset() {
        mMsgDbHelper = new MsgDbHelper(mContext);
        mConvDbHelper = new ConvDbHelper(mContext);
    }
}
