package com.edusoho.kuozhi.imserver.managar;

import android.content.ContentValues;
import android.content.Context;

import com.edusoho.kuozhi.imserver.entity.IMUploadEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.helper.impl.MsgDbHelper;

import java.util.List;

/**
 * Created by 菊 on 2016/5/15.
 */
public class IMMessageManager {

    private Context mContext;

    public IMMessageManager(Context context)
    {
        this.mContext = context;
    }

    public int updateMessage(MessageEntity messageEntity) {
        return new MsgDbHelper(mContext).update(messageEntity);
    }

    public int updateMessageFieldByMsgNo(String msgNo, ContentValues cv) {
        return new MsgDbHelper(mContext).updateFiledByMsgNo(msgNo, cv);
    }

    public int updateMessageField(int id, ContentValues cv) {
        return new MsgDbHelper(mContext).updateFiled(id, cv);
    }

    public int updateMessageFieldByUid(String uid, ContentValues cv) {
        return new MsgDbHelper(mContext).updateFiledByUid(uid, cv);
    }

    public MessageEntity getMessage(int id) {
        return new MsgDbHelper(mContext).getMessage(id);
    }

    public List<MessageEntity> getMessageListByConvNo(String convNo, int start, int limit) {
        return new MsgDbHelper(mContext).getMessageList(convNo, start, limit);
    }

    public MessageEntity getMessageByUID(String uid) {
        return new MsgDbHelper(mContext).getMessageByUID(uid);
    }

    public IMUploadEntity getUploadEntity(String muid) {
        return new MsgDbHelper(mContext).getUploadEntity(muid);
    }

    public long saveUploadEntity(String muid, String type, String source) {
        return new MsgDbHelper(mContext).saveUploadEntity(muid, type, source);
    }

    public MessageEntity createMessage(MessageEntity messageEntity) {
        MsgDbHelper msgDbHelper = new MsgDbHelper(mContext);
        long resultId = msgDbHelper.save(messageEntity);
        return msgDbHelper.getMessageByUID(messageEntity.getUid());
    }

    public long deleteByConvNo(String convNo) {
        return new MsgDbHelper(mContext).deleteByConvNo(convNo);
    }

    public int deleteById(int id) {
        return new MsgDbHelper(mContext).deleteById(id);
    }
}
