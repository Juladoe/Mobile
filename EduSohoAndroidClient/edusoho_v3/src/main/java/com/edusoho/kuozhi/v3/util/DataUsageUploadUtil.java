package com.edusoho.kuozhi.v3.util;

import android.content.Context;
import android.database.Cursor;

import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8File;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by JesseHuang on 2016/11/9.
 */

public class DataUsageUploadUtil {

    private static Pattern URL_PAT = Pattern.compile("(#EXT-X-KEY:[^\n]+)?(http://[^\"\n]+)", Pattern.DOTALL);
    private int mWatchTimer;
    private long mStartTime;
    private long mEndTime;
    private Context mContext;
    /**
     * 视频课时播放时长
     */
    private int playLength;
    private int mLessonId;

    public DataUsageUploadUtil(int lessonId, int length, Context context) {
        playLength = length;
        mLessonId = lessonId;
        mContext = context;
    }

    public void startTimer() {
        mStartTime = System.currentTimeMillis();
    }

    public void pauseTimer() {

    }

    public void stopTimer() {
        mEndTime = System.currentTimeMillis();
    }

    private int getWatchTime() {
        return (int) ((mEndTime - mStartTime) / 1000);
    }

    private int getVideoSize() {
        File workSpace = EdusohoApp.getWorkSpace();
        StringBuffer dirBuilder = new StringBuffer(workSpace.getAbsolutePath());
        dirBuilder.append("/videos/")
                .append(EdusohoApp.app.loginUser.id)
                .append("/")
                .append(EdusohoApp.app.domain)
                .append("/")
                .append(mLessonId);
        File videoFiles = new File(dirBuilder.toString());
        if (!videoFiles.exists()) {
            return 0;
        }
        return (int) FileUtils.getTotalSizeOfFilesInDir(videoFiles);
    }

    public int getDataUsageSave() {
        return getWatchTime() * getVideoSize() / playLength;
    }

    public String getUploadUrl() {
        final String[] uploadUrl = new String[1];
        SqliteUtil.QueryParser<HashMap<String, Integer>> queryCallBack =
                new SqliteUtil.QueryParser<HashMap<String, Integer>>() {
                    @Override
                    public HashMap<String, Integer> parse(Cursor cursor) {
                        uploadUrl[0] = cursor.getString(cursor.getColumnIndex("play_list"));
                        return null;
                    }
                };
        SqliteUtil.getUtil(mContext).query(
                queryCallBack,
                "select * from data_m3u8 where finish=? and lessonId=?",
                "1",
                String.valueOf(mLessonId)
        );
        Matcher matcher = URL_PAT.matcher(uploadUrl[0]);
        String url = "";
        while (matcher.find()) {
            url = matcher.group(2);
        }
        return url;
    }
}
