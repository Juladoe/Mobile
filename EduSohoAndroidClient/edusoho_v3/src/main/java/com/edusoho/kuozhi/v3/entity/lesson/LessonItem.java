package com.edusoho.kuozhi.v3.entity.lesson;


import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8DbModel;

import java.io.Serializable;

/**
 * Created by howzhi on 14-5-24.
 */
public class LessonItem<T> implements Serializable {
    public static final int FREE = 1;

    public int id;
    public int courseId;
    public int chapterId;
    public int number;
    public int seq;
    public int free;
    public String status;
    public String title;
    public String summary;
    public String tag;
    public String type;
    public T content;
    private int giveCredit;
    private int requireCredit;
    public int mediaId;
    public String mediaSource;
    public String mediaName;
    public String mediaUri;
    public String headUrl;
    public String length;
    public int materialNum;
    public int quizNum;
    public int learnedNum;
    public int viewedNum;
    public int userId;
    public String remainTime;
    public String createdTime;
    public String itemType;
    public String startTime;
    public String endTime;
    public String replayStatus;
    public String mediaStorage;
    public String mediaConvertStatus;

    public M3U8DbModel m3u8Model;
    public boolean isSelected;
    public int groupId;

    public UploadFile uploadFile;

    public enum ItemType {
        LESSON, CHAPTER, UNIT, EMPTY;

        public static ItemType cover(String name) {
            ItemType type = EMPTY;
            try {
                type = valueOf(name.toUpperCase());
            } catch (Exception e) {
                return EMPTY;
            }
            return type;
        }
    }

    public enum MediaSourceType {
        YOUKU, SELF, TUDOU, EMPTY, QQVIDEO, FALLBACK, NETEASEOPENCOURSE;

        public static MediaSourceType cover(String name) {
            MediaSourceType type = EMPTY;
            try {
                type = valueOf(name.toUpperCase());
            } catch (Exception e) {
                return EMPTY;
            }
            return type;
        }
    }
}
