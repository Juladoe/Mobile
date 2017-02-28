package com.edusoho.kuozhi.v3.model.sys;

/**
 * Created by JesseHuang on 15/12/29.
 */
public class AudioCacheEntity {
    public int id;
    public String localPath;
    public String onlinePath;

    public AudioCacheEntity() {

    }

    public AudioCacheEntity(String localPath, String onlinePath) {
        this.localPath = localPath;
        this.onlinePath = onlinePath;
    }
}
