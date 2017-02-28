package com.edusoho.kuozhi.imserver.managar;

import android.content.Context;

import com.edusoho.kuozhi.imserver.IImServerAidlInterface;
import com.edusoho.kuozhi.imserver.SendEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.helper.impl.MsgDbHelper;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by 菊 on 2016/4/23.
 */
public class IMChatRoom {

    protected String mConvNo;
    protected Context mContext;
    protected WeakReference<IImServerAidlInterface> mImBinderRef;

    public IMChatRoom(Context context, String convNo, IImServerAidlInterface imBinder) {
        this.mConvNo = convNo;
        this.mContext = context;
        this.mImBinderRef = new WeakReference<IImServerAidlInterface>(imBinder);
    }

    public void send(SendEntity sendEntity) {
        try {
            sendEntity.setConvNo(mConvNo);
            mImBinderRef.get().send(sendEntity);
        } catch (Exception e) {
        }
    }

    public List<MessageEntity> getMessageList(int start) {
        List<MessageEntity> messageEntities =  new MsgDbHelper(mContext).getMessageList(mConvNo, start, 10);
        Collections.sort(messageEntities, new Comparator<MessageEntity>() {
            @Override
            public int compare(MessageEntity t1, MessageEntity t2) {
                return t2.getTime() - t1.getTime();
            }
        });
        return messageEntities;
    }
}
