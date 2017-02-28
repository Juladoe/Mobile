package com.edusoho.kuozhi.imserver.helper.impl;

import android.content.ContentValues;
import android.content.Context;
import com.edusoho.kuozhi.imserver.entity.IMUploadEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.helper.IMsgDbHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 菊 on 2016/4/29.
 */
public class MsgMemHelper implements IMsgDbHelper {

    private static final String TABLE = "im_message";

    private ConcurrentHashMap<String, String> mMsgNoArray;
    private Map<String, MessageEntity> mMessageEntityMap;

    public MsgMemHelper(Context context) {
        mMsgNoArray = new ConcurrentHashMap<>();
        mMessageEntityMap = new ConcurrentHashMap<>();
    }

    public List<MessageEntity> getMessageList(String convNo, int start, int limit) {
        List<MessageEntity> entityList = new ArrayList<>();
        return entityList;
    }

    public MessageEntity getMessage(int id) {
        return null;
    }

    public MessageEntity getMessageByMsgNo(String msgNo) {
        return mMessageEntityMap.get(msgNo);
    }

    public MessageEntity getMessageByUID(String uid) {
        return null;
    }

    public IMUploadEntity getUploadEntity(String muid) {
        return null;
    }

    public long saveUploadEntity(String muid, String type, String source) {
        return 1;
    }

    public String getLaterNo() {
        return null;
    }

    public boolean hasMessageByNo(String msgNo) {
        return false;
    }

    public int deleteByConvNo(String convNo) {
        return 0;
    }

    public long save(MessageEntity messageEntity) {
        mMessageEntityMap.put(messageEntity.getMsgNo(), messageEntity);
        return 1;
    }

    public int updateFiledByMsgNo(String msgNo, ContentValues cv) {
        return 0;
    }

    public int updateFiled(int id, ContentValues cv) {
        return 0;
    }

    public int updateFiledByUid(String uid, ContentValues cv) {
        return 0;
    }

    public int update(MessageEntity messageEntity) {
        return 0;
    }

    public int deleteById(int id) {
        return 0;
    }
}
