package com.edusoho.kuozhi.imserver.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by suju on 16/8/26.
 */
public class TimeUtil {

    public static final long ONE_WEEK = 1000 * 60 * 60 * 24 * 7;

    public static String convertMills2Date(long millis) {
        String result = "";
        if (millis <= 0) {
            return "";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
            String nowTime = sdf.format(System.currentTimeMillis());
            String showTime = sdf.format(millis);
            if (nowTime.substring(0, 8).equals(showTime.substring(0, 8))) {
                // 如果是当天
                result = showTime.substring(9);
            } else if (System.currentTimeMillis() - millis < ONE_WEEK) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(millis);
                switch (calendar.get(Calendar.DAY_OF_WEEK) - 1) {
                    case 1:
                        result = "星期一";
                        break;
                    case 2:
                        result = "星期二";
                        break;
                    case 3:
                        result = "星期三";
                        break;
                    case 4:
                        result = "星期四";
                        break;
                    case 5:
                        result = "星期五";
                        break;
                    case 6:
                        result = "星期六";
                        break;
                    default:
                        result = "星期日";
                        break;
                }
            } else {
                result = showTime.substring(0, 8);
            }
        } catch (Exception ex) {
            Log.e("convertMills2Date", ex.getMessage());
        }
        return result;
    }

    public static int getDuration(int duration) {
        return Math.round(Float.valueOf(duration) / 1000);
    }

    public static int getAudioDuration(Context context, String audioFile) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(audioFile));
        if (mediaPlayer == null) {
            return 0;
        }
        int duration = mediaPlayer.getDuration();
        mediaPlayer.release();
        return duration;
    }
}
