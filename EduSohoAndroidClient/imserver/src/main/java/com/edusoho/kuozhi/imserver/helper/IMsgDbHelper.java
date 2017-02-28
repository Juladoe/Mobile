package com.edusoho.kuozhi.imserver.helper;

import android.content.ContentValues;
import com.edusoho.kuozhi.imserver.entity.IMUploadEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import java.util.List;

/**
 * Created by suju on 16/11/3.
 */
public interface IMsgDbHelper {

    List<MessageEntity> getMessageList(String convNo, int start, int limit);

    MessageEntity getMessage(int id);

    MessageEntity getMessageByMsgNo(String msgNo);

    MessageEntity getMessageByUID(String uid);

    IMUploadEntity getUploadEntity(String muid);

    long saveUploadEntity(String muid, String type, String source);

    String getLaterNo();

    boolean hasMessageByNo(String msgNo);

    int deleteByConvNo(String convNo);

    long save(MessageEntity messageEntity);

    int updateFiledByMsgNo(String msgNo, ContentValues cv);

    int updateFiled(int id, ContentValues cv);

    int updateFiledByUid(String uid, ContentValues cv);

    int update(MessageEntity messageEntity);

    int deleteById(int id);
}
