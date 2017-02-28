package com.edusoho.kuozhi.imserver.service.Impl;

import android.content.Context;

import com.edusoho.kuozhi.imserver.helper.impl.ConvMemHelper;
import com.edusoho.kuozhi.imserver.helper.impl.MsgMemHelper;

/**
 * Created by suju on 16/11/3.
 */
public class MemMsgManager extends AbstractMsgManager {

    private Context mContext;

    public MemMsgManager(Context context) {
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
        mMsgDbHelper = new MsgMemHelper(mContext);
        mConvDbHelper = new ConvMemHelper(mContext);
    }
}
