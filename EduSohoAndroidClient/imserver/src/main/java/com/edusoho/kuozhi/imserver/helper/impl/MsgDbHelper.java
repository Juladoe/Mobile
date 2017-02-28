package com.edusoho.kuozhi.imserver.helper.impl;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import com.edusoho.kuozhi.imserver.entity.IMUploadEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.factory.DbManagerFactory;
import com.edusoho.kuozhi.imserver.helper.IMsgDbHelper;
import com.edusoho.kuozhi.imserver.util.DbHelper;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;
import com.edusoho.kuozhi.imserver.util.MessageUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * Created by 菊 on 2016/4/29.
 */
public class MsgDbHelper implements IMsgDbHelper {

    private static final String TABLE = "im_message";

    private DbHelper mDbHelper;

    public MsgDbHelper(Context context) {
        mDbHelper = new DbHelper(context, DbManagerFactory.getDefaultFactory().createIMDbManager(context));
    }

    public List<MessageEntity> getMessageList(String convNo, int start, int limit) {
        List<MessageEntity> entityList = new ArrayList<>();
        if (TextUtils.isEmpty(convNo)) {
            return entityList;
        }
        ArrayList<HashMap<String, String>> arrayList = mDbHelper.queryBySortAndLimit(TABLE, "convNo=?", new String[]{convNo}, "time desc", String.format("%d, %d", start, limit));
        if (arrayList == null) {
            return entityList;
        }
        for (HashMap<String, String> arrayMap : arrayList) {
            MessageEntity entity = createMessageEntity(arrayMap);
            entityList.add(entity);
        }

        return entityList;
    }

    public MessageEntity getMessage(int id) {
        HashMap arrayMap = mDbHelper.querySingle(TABLE, "id=?", new String[]{String.valueOf(id)});
        if (arrayMap == null || arrayMap.isEmpty()) {
            return null;
        }
        return createMessageEntity(arrayMap);
    }

    public MessageEntity getMessageByMsgNo(String msgNo) {
        HashMap arrayMap = mDbHelper.querySingle(TABLE, "msgNo=?", new String[]{msgNo});
        if (arrayMap == null || arrayMap.isEmpty()) {
            return null;
        }
        return createMessageEntity(arrayMap);
    }

    public MessageEntity getMessageByUID(String uid) {
        HashMap arrayMap = mDbHelper.querySingle(TABLE, "uid=?", new String[]{String.valueOf(uid)});
        if (arrayMap == null || arrayMap.isEmpty()) {
            return null;
        }
        return createMessageEntity(arrayMap);
    }

    public IMUploadEntity getUploadEntity(String muid) {
        if (TextUtils.isEmpty(muid)) {
            return null;
        }
        HashMap arrayMap = mDbHelper.querySingle("im_upload_extr", "message_uid=?", new String[]{muid});
        return createUploadEntity(arrayMap);
    }

    public long saveUploadEntity(String muid, String type, String source) {
        ContentValues cv = new ContentValues();
        cv.put("type", type);
        cv.put("message_uid", muid);
        cv.put("source", source);
        return mDbHelper.insert("im_upload_extr", cv);
    }

    public String getLaterNo() {
        ArrayList<HashMap<String, String>> list = mDbHelper.queryBySortAndLimit("im_message", null, null, "time desc", "1");
        if (list.isEmpty()) {
            return null;
        }
        HashMap<String, String> arrayMap = list.get(0);
        return arrayMap.get("msgNo");
    }

    public boolean hasMessageByNo(String msgNo) {
        if (msgNo == null || "".equals(msgNo)) {
            return false;
        }
        return mDbHelper.querySingle(TABLE, "msgNo=?", new String[]{msgNo}) != null;
    }

    public int deleteByConvNo(String convNo) {
        return mDbHelper.delete(TABLE, "convNo=?", new String[]{convNo});
    }

    public long save(MessageEntity messageEntity) {
        ContentValues cv = new ContentValues();
        cv.put("convNo", messageEntity.getConvNo());
        cv.put("fromId", messageEntity.getFromId());
        cv.put("fromName", messageEntity.getFromName());
        cv.put("toId", messageEntity.getToId());
        cv.put("toName", messageEntity.getToName());
        cv.put("msg", messageEntity.getMsg());
        cv.put("msgNo", messageEntity.getMsgNo());
        cv.put("time", messageEntity.getTime());
        cv.put("uid", messageEntity.getUid());
        cv.put("status", messageEntity.getStatus());
        return mDbHelper.insert(TABLE, cv);
    }

    public int updateFiledByMsgNo(String msgNo, ContentValues cv) {
        return mDbHelper.update(TABLE, cv, "msgNo=?", new String[]{msgNo});
    }

    public int updateFiled(int id, ContentValues cv) {
        return mDbHelper.update(TABLE, cv, "id=?", new String[]{String.valueOf(id)});
    }

    public int updateFiledByUid(String uid, ContentValues cv) {
        return mDbHelper.update(TABLE, cv, "uid=?", new String[]{uid});
    }

    public int update(MessageEntity messageEntity) {
        ContentValues cv = new ContentValues();
        cv.put("convNo", messageEntity.getConvNo());
        cv.put("fromId", messageEntity.getFromId());
        cv.put("fromName", messageEntity.getFromName());
        cv.put("toId", messageEntity.getToId());
        cv.put("toName", messageEntity.getToName());
        cv.put("msg", messageEntity.getMsg());
        cv.put("msgNo", messageEntity.getMsgNo());
        cv.put("status", messageEntity.getStatus());
        return mDbHelper.update(TABLE, cv, "uid=?", new String[]{String.valueOf(messageEntity.getUid())});
    }

    private IMUploadEntity createUploadEntity(HashMap<String, String> arrayMap) {
        IMUploadEntity uploadEntity = new IMUploadEntity();
        uploadEntity.setMessageId(MessageUtil.parseInt(arrayMap.get("message_uid")));
        uploadEntity.setType(arrayMap.get("type"));
        uploadEntity.setSource(arrayMap.get("source"));

        return uploadEntity;
    }

    private MessageEntity createMessageEntity(HashMap<String, String> arrayMap) {
        return new MessageEntityBuildr()
                .addMsgNo(arrayMap.get("msgNo"))
                .addMsg(arrayMap.get("msg"))
                .addToName(arrayMap.get("toName"))
                .addFromName(arrayMap.get("fromName"))
                .addConvNo(arrayMap.get("convNo"))
                .addToId(arrayMap.get("toId"))
                .addFromId(arrayMap.get("fromId"))
                .addTime(MessageUtil.parseInt(arrayMap.get("time")))
                .addUID(arrayMap.get("uid"))
                .addId(MessageUtil.parseInt(arrayMap.get("id")))
                .addStatus(MessageUtil.parseInt(arrayMap.get("status")))
                .builder();
    }

    public int deleteById(int id) {
        return mDbHelper.delete(TABLE, "id=?", new String[]{String.valueOf(id)});
    }
}
