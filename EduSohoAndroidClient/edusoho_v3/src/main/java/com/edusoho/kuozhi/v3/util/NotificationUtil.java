package com.edusoho.kuozhi.v3.util;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.push.New;
import com.edusoho.kuozhi.v3.model.bal.push.RedirectBody;
import com.edusoho.kuozhi.v3.model.bal.push.V2CustomContent;
import com.edusoho.kuozhi.v3.ui.DefaultPageActivity;
import com.edusoho.kuozhi.v3.ui.NewsCourseActivity;
import com.google.gson.Gson;
import java.util.List;

/**
 * Created by JesseHuang on 15/7/4.
 */
public class NotificationUtil {
    public static final int DISCOUNT_ID = -1;

    public static void showMsgNotification(Context context, MessageBody messageBody) {

    }

    public static void showCourseDiscuss(Context context, V2CustomContent v2CustomContent) {
        try {
            String content = null;
            String title = v2CustomContent.getBody().getTitle();
            switch (v2CustomContent.getBody().getType()) {
                case PushUtil.ChatMsgType.IMAGE:
                    content = String.format("[%s]", Const.MEDIA_IMAGE);
                    break;
                case PushUtil.ChatMsgType.AUDIO:
                    content = String.format("[%s]", Const.MEDIA_AUDIO);
                    break;
                case PushUtil.ChatMsgType.MULTI:
                    RedirectBody redirectBody = new Gson().fromJson(v2CustomContent.getBody().getContent(), RedirectBody.class);
                    content = redirectBody.content;
                    break;
                default:
                    content = v2CustomContent.getBody().getContent();
            }


            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context).setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(v2CustomContent.getFrom().getNickname() + ": " + content).setAutoCancel(true);

            int fromId = v2CustomContent.getTo().getId();

            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notifyIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            notifyIntent.removeCategory(Intent.CATEGORY_LAUNCHER);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            New newModel = new New();
            newModel.title = title;
            newModel.fromId = v2CustomContent.getTo().getId();
            newModel.type = v2CustomContent.getTo().getType();
            newModel.imgUrl = v2CustomContent.getTo().getImage();
            newModel.createdTime = v2CustomContent.getCreatedTime();
            notifyIntent.putExtra(Const.NEW_ITEM_INFO, newModel);
            notifyIntent.putExtra(Const.INTENT_TARGET, NewsCourseActivity.class);
            PendingIntent pendIntent = PendingIntent.getActivity(context, fromId,
                    notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendIntent);
            mBuilder.setDefaults(EdusohoApp.app.config.msgSound | EdusohoApp.app.config.msgVibrate);
            mNotificationManager.notify(fromId, mBuilder.build());
        } catch (Exception ex) {
            Log.d("Classroom-->", ex.getMessage());
        }
    }


    public static void cancelById(int id) {
        NotificationManager mNotificationManager =
                (NotificationManager) EdusohoApp.app.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    public static void cancelAll() {
        NotificationManager mNotificationManager =
                (NotificationManager) EdusohoApp.app.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    /**
     * 判断应用是否在退出状态下接收到消息
     *
     * @param context Context
     * @return 是 true ，否 false
     */
    public static boolean isAppExit(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(1);
        int size = task.size();
        for (int i = 0; i < size; i++) {
            if (task.get(i).baseActivity.getClassName().equals(DefaultPageActivity.class.getName()) ||
                    task.get(i).topActivity.getClassName().equals(DefaultPageActivity.class.getName())) {
                return false;
            }
        }
        return true;
    }
}
