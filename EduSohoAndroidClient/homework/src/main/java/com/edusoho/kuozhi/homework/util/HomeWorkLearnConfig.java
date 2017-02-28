package com.edusoho.kuozhi.homework.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by suju on 16/12/28.
 */

public class HomeWorkLearnConfig {

    public static boolean getHomeworkLocalLearnConfig(Context context, String type, int targetId) {
        if (targetId == 0) {
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences("homework_local_learn_config", Context.MODE_PRIVATE);
        return sp.getBoolean(String.format("%s-%d", type, targetId), false);
    }

    public static void saveHomeworkLocalLearnConfig(Context context, String type, int targetId, boolean isLearn) {
        SharedPreferences sp = context.getSharedPreferences("homework_local_learn_config", Context.MODE_PRIVATE);
        sp.edit().putBoolean(String.format("%s-%d", type, targetId), isLearn).commit();
    }
}
