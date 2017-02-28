package com.edusoho.kuozhi.v3.service.message.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.helper.IDbManager;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.model.bal.push.Notify;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.ui.NotifyActivity;
import com.edusoho.kuozhi.v3.ui.fragment.NewsFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by suju on 16/11/10.
 */
public class LessonPushProcessor implements IPushProcessor {

    static final String TAG = "LessonPushProcessor";
    private Context mContext;
    private MessageBody mMessageBody;

    public LessonPushProcessor(Context context, MessageBody messageBody) {
        this.mContext = context;
        this.mMessageBody = messageBody;
        this.mMessageBody.setConvNo(Destination.NOTIFY);
    }

    @Override
    public void processor() {
        School school = getAppSettingProvider().getCurrentSchool();
        if (school == null) {
            Log.d(TAG, "createNotify: no school host");
            return;
        }
        IDbManager dbManager = new ESDbManager(mContext, school.getDomain());
        NotifyDbHelper notifyDbHelper = new NotifyDbHelper(mContext, dbManager);
        if (notifyDbHelper.hasNotifyByMsgNo(mMessageBody.getMsgNo())) {
            Log.d(TAG, "has notify");
            return;
        }
        Map<String, String> dataMap = new Gson().fromJson(mMessageBody.getBody(), LinkedHashMap.class);
        Notify notify = new Notify();
        notify.setCreatedTime(mMessageBody.getCreatedTime());
        notify.setType(dataMap.get("type"));
        notify.setContent(mMessageBody.getBody());
        notify.setTitle(getNotifyTitle(notify.getType()));
        notifyDbHelper.createNotify(notify);
        IMClient.getClient().getConvManager().deleteConv(Destination.LESSON);
        updateConvEntity(notify);
        Log.d(TAG, "createNotify:" + dataMap.get("type"));
    }

    private ConvEntity createConvEntityFromNofity(Notify notify) {
        ConvEntity convEntity = new ConvEntity();
        convEntity.setType(PushUtil.ChatUserType.NOTIFY);
        convEntity.setConvNo(PushUtil.ChatUserType.NOTIFY);
        convEntity.setUpdatedTime(notify.getCreatedTime());
        convEntity.setAvatar("drawable://" + mContext.getResources().getIdentifier("icon", "drawable", mContext.getPackageName()));
        convEntity.setTargetId(0);
        convEntity.setTargetName("通知中心");
        convEntity.setUnRead(0);
        convEntity.setLaterMsg(mMessageBody.toJson());
        IMClient.getClient().getConvManager().createConv(convEntity);

        return convEntity;
    }

    private void updateConvEntity(Notify notify) {
        ConvEntity convEntity = IMClient.getClient().getConvManager().getConvByConvNo(PushUtil.ChatUserType.NOTIFY);
        if (convEntity == null) {
            convEntity = createConvEntityFromNofity(notify);
        }
        convEntity.setLaterMsg(mMessageBody.toJson());
        convEntity.setUpdatedTime(notify.getCreatedTime());
        convEntity.setUnRead(convEntity.getUnRead() + 1);
        IMClient.getClient().getConvManager().updateConvByConvNo(convEntity);
        MessageEngine.getInstance().sendMsgToTaget(Const.REFRESH_LIST, null, NewsFragment.class);
    }

    private String getNotifyTitle(String type) {
        switch (type) {
            case "live_start":
                return "直播提醒";
        }

        return "课时通知";
    }

    @Override
    public Intent getNotifyIntent() {
        Intent notifyIntent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        notifyIntent.removeCategory(Intent.CATEGORY_LAUNCHER);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        LinkedHashMap<String, String> linkedHashMap = new Gson().fromJson(mMessageBody.getBody(), LinkedHashMap.class);
        if (linkedHashMap != null) {
            if ("live_start".equals(linkedHashMap.get("type"))) {
                notifyIntent.putExtra(Const.INTENT_TARGET, NotifyActivity.class);
            }
        }

        return notifyIntent;
    }

    @Override
    public String[] getNotificationContent(String body) {
        LinkedHashMap<String, String> linkedHashMap = new Gson().fromJson(body, LinkedHashMap.class);
        String type = linkedHashMap.get("type");
        String title = getNotifyTitle(type);
        switch (type) {
            case "live_start":
                return new String[] {
                        title,
                        linkedHashMap.get("message")
                };
        }
        return new String[]{ title, AppUtil.coverCourseAbout(linkedHashMap.get("message"))};
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }
}
