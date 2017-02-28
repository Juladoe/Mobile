package com.edusoho.kuozhi.v3.service.message.push;

import android.content.Context;
import android.content.Intent;

import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.v3.ui.ServiceProviderActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.google.gson.Gson;

import java.util.LinkedHashMap;

/**
 * Created by suju on 16/11/10.
 */
public class ArticlePushProcessor implements IPushProcessor {

    private Context mContext;
    private MessageBody mMessageBody;

    public ArticlePushProcessor(Context context, MessageBody messageBody) {
        this.mContext = context;
        this.mMessageBody = messageBody;
    }

    @Override
    public void processor() {

    }

    @Override
    public Intent getNotifyIntent() {
        Intent notifyIntent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        notifyIntent.removeCategory(Intent.CATEGORY_LAUNCHER);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        notifyIntent.putExtra(ServiceProviderActivity.SERVICE_TYPE, PushUtil.ArticleType.TYPE);
        notifyIntent.putExtra(ServiceProviderActivity.SERVICE_ID, mMessageBody.getSource().getId());
        notifyIntent.putExtra(Const.ACTIONBAR_TITLE, "资讯");
        notifyIntent.putExtra(Const.INTENT_TARGET, ServiceProviderActivity.class);
        return notifyIntent;
    }

    @Override
    public String[] getNotificationContent(String body) {
        LinkedHashMap<String, String> linkedHashMap = new Gson().fromJson(body, LinkedHashMap.class);
        return new String[]{linkedHashMap.get("title"), AppUtil.coverCourseAbout(linkedHashMap.get("content"))};
    }
}
