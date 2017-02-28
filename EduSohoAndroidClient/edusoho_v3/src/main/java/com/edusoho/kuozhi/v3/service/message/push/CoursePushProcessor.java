package com.edusoho.kuozhi.v3.service.message.push;

import android.content.Context;
import android.content.Intent;

import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.ui.BulletinActivity;
import com.edusoho.kuozhi.v3.ui.ThreadDiscussChatActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.google.gson.Gson;

import java.util.LinkedHashMap;

/**
 * Created by suju on 16/11/10.
 */
public class CoursePushProcessor implements IPushProcessor {

    private Context mContext;
    private MessageBody mMessageBody;

    public CoursePushProcessor(Context context, MessageBody messageBody) {
        this.mContext = context;
        this.mMessageBody = messageBody;
    }

    @Override
    public void processor() {
        String type = mMessageBody.getSource().getType();
        int targetId = mMessageBody.getSource().getId();
        new IMProvider(mContext).updateConvEntityByPush(mMessageBody.getConvNo(), type, targetId);
        Role role = IMClient.getClient().getRoleManager().getRole(type, targetId);
        ConvEntity convEntity = IMClient.getClient().getConvManager().getConvByConvNo(type);
        if (convEntity != null && role.getRid() != 0) {
            convEntity.setAvatar(role.getAvatar());
            convEntity.setTargetName(role.getNickname());
            IMClient.getClient().getConvManager().updateConvByConvNo(convEntity);
        }
    }

    @Override
    public Intent getNotifyIntent() {
        Intent notifyIntent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        notifyIntent.removeCategory(Intent.CATEGORY_LAUNCHER);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        LinkedHashMap<String, String> linkedHashMap = new Gson().fromJson(mMessageBody.getBody(), LinkedHashMap.class);
        if (linkedHashMap != null) {
            if ("question.created".equals(linkedHashMap.get("type"))
                    || "question.answered".equals(linkedHashMap.get("type"))) {
                notifyIntent.putExtra(Const.INTENT_TARGET, ThreadDiscussChatActivity.class);
                notifyIntent.putExtra(ThreadDiscussChatActivity.THREAD_TARGET_ID, AppUtil.parseInt(linkedHashMap.get("courseId")));
                notifyIntent.putExtra(ThreadDiscussChatActivity.THREAD_TARGET_TYPE, mMessageBody.getSource().getType());
                notifyIntent.putExtra(ThreadDiscussChatActivity.FROM_ID, AppUtil.parseInt(linkedHashMap.get("threadId")));
                notifyIntent.putExtra(ThreadDiscussChatActivity.THREAD_TYPE, "question");
            }
        }

        return notifyIntent;
    }

    @Override
    public String[] getNotificationContent(String body) {
        LinkedHashMap<String, String> linkedHashMap = new Gson().fromJson(body, LinkedHashMap.class);
        String type = linkedHashMap.get("type");
        switch (type) {
            case "question.created":
                return new String[]{
                        AppUtil.coverCourseAbout(linkedHashMap.get("title")),
                        String.format("[课程问答]:%s", linkedHashMap.get("questionTitle"))
                };
        }
        return new String[]{"课程信息更新", AppUtil.coverCourseAbout(linkedHashMap.get("title"))};
    }
}
