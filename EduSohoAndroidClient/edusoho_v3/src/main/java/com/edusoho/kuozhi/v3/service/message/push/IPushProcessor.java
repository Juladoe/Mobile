package com.edusoho.kuozhi.v3.service.message.push;

import android.content.Intent;

/**
 * Created by suju on 16/11/10.
 */
public interface IPushProcessor {

    Intent getNotifyIntent();

    String[] getNotificationContent(String body);

    void processor();
}
