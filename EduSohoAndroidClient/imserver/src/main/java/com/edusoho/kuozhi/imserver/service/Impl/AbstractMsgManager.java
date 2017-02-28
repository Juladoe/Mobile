package com.edusoho.kuozhi.imserver.service.Impl;

import android.text.TextUtils;

import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.helper.IConvDbHelper;
import com.edusoho.kuozhi.imserver.helper.IMsgDbHelper;
import com.edusoho.kuozhi.imserver.service.IMsgManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by suju on 16/11/3.
 */
public abstract class AbstractMsgManager implements IMsgManager {

    protected IMsgDbHelper mMsgDbHelper;
    protected IConvDbHelper mConvDbHelper;
    private Map<String, String> mMsgNoArray;

    public AbstractMsgManager() {
        this.mMsgNoArray = new ConcurrentHashMap<>();
    }
    @Override
    public boolean hasMessageByNo(String msgNo) {
        if (TextUtils.isEmpty(msgNo)) {
            return false;
        }
        if (mMsgNoArray.containsKey(msgNo)) {
            return true;
        }
        boolean hasMessageByNo = mMsgDbHelper.hasMessageByNo(msgNo);
        if (hasMessageByNo) {
            mMsgNoArray.put(msgNo, "");
        }

        return hasMessageByNo;
    }

    public String getLaterNo() {
        return mMsgDbHelper.getLaterNo();
    }

    public void createConvNoEntity(ConvEntity convEntity) {
        mConvDbHelper.save(convEntity);
    }

    public ConvEntity getConvByTypeAndId(String type, int id) {
        return mConvDbHelper.getConvByTypeAndId(type, id);
    }

    @Override
    public ConvEntity getConvByConvNo(String convNo) {
        return mConvDbHelper.getConvByConvNo(convNo);
    }

    @Override
    public MessageEntity getMessageByMsgNo(String msgNo) {
        return mMsgDbHelper.getMessageByMsgNo(msgNo);
    }

    @Override
    public void updateConvEntityByConvNo(ConvEntity convEntity) {
        mConvDbHelper.updateByConvNo(convEntity);
    }

    @Override
    public void updateConvEntityById(ConvEntity convEntity) {
        mConvDbHelper.update(convEntity);
    }

    @Override
    public long createMessageEntity(MessageEntity messageEntity) {
        return mMsgDbHelper.save(messageEntity);
    }

    @Override
    public void updateMessageEntityByUID(MessageEntity messageEntity) {
        mMsgDbHelper.update(messageEntity);
    }

    @Override
    public MessageEntity getMessageByUID(String uid) {
        return mMsgDbHelper.getMessageByUID(uid);
    }
}
