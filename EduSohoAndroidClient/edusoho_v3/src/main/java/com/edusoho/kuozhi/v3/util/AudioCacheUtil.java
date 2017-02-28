package com.edusoho.kuozhi.v3.util;

import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.sys.AudioCacheEntity;
import com.edusoho.kuozhi.v3.util.sql.AudioCacheDataSource;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;

/**
 * Created by JesseHuang on 15/12/29.
 */
public class AudioCacheUtil {
    private static AudioCacheUtil mAudioCacheUtil;
    private static AudioCacheDataSource mAudioCacheDataSource;

    private AudioCacheUtil(AudioCacheDataSource dataSource) {
        mAudioCacheDataSource = dataSource;
    }

    public static AudioCacheUtil getInstance() {
        if (mAudioCacheUtil == null) {
            mAudioCacheUtil = new AudioCacheUtil(new AudioCacheDataSource(SqliteUtil.getUtil(EdusohoApp.app.getApplicationContext())));
        }
        return mAudioCacheUtil;
    }

    public AudioCacheEntity getAudioCacheByPath(String path) {
        return mAudioCacheDataSource.getAudio(path);
    }

    public AudioCacheEntity getAudioCache(String localPath, String onlinePath) {
        return mAudioCacheDataSource.getAudio(localPath, onlinePath);
    }

    public AudioCacheEntity create(String local, String online) {
        return create(new AudioCacheEntity(local, online));
    }

    public AudioCacheEntity create(AudioCacheEntity model) {
        AudioCacheEntity cacheLocal = mAudioCacheDataSource.getAudio(model.localPath);
        AudioCacheEntity cacheOnline = mAudioCacheDataSource.getAudio(model.onlinePath);
        AudioCacheEntity result = null;
        if (cacheLocal == null && cacheOnline == null) {
            result = mAudioCacheDataSource.create(model);
        } else if (cacheOnline == null) {
            model.id = cacheLocal.id;
            model.localPath = cacheLocal.localPath;
            mAudioCacheDataSource.update(model);
            result = model;
        } else if (cacheLocal == null) {
            model.id = cacheOnline.id;
            model.onlinePath = cacheOnline.onlinePath;
            mAudioCacheDataSource.update(model);
            result = model;
        }
        return result;
    }

    public void update(AudioCacheEntity model) {
        mAudioCacheDataSource.update(model);
    }
}
