package com.edusoho.kuozhi.v3.model.bal.m3u8;

/**
 * Created by howzhi on 14/12/10.
 */
public class M3U8DbModel {

    public int id;
    public int userId;
    public int finish;
    public String host;
    public int lessonId;
    public int totalNum;
    public int downloadNum;
    public String playList;

    @Override
    public String toString() {
        return "M3U8DbModle {" +
                "id=" + id +
                ", finish=" + finish +
                ", userId=" + userId +
                ", host='" + host + '\'' +
                ", lessonId=" + lessonId +
                ", totalNum=" + totalNum +
                ", downloadNum=" + downloadNum +
                ", playList='" + playList + '\'' +
                '}';
    }
}
