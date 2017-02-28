package com.edusoho.kuozhi.v3.entity.lesson;

import com.edusoho.kuozhi.v3.util.AppUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by DF on 2016/12/14.
 */

public class CourseCatalogue {

    private Map<String, String> learnStatuses;

    private List<LessonsBean> lessons;

    public Map<String, String> getLearnStatuses() {
        return learnStatuses;
    }

    public void setLearnStatuses(Map<String, String> learnStatuses) {
        this.learnStatuses = learnStatuses;
    }

    public List<LessonsBean> getLessons() {
        return lessons;
    }

    public void setLessons(List<LessonsBean> lessons) {
        this.lessons = lessons;
    }

    public static class LessonsBean implements Serializable {

        private String content;
        private String courseId;
        private String id;
        private String itemType;
        private String length;
        private String number;
        private String title;
        private String type;
        private String free;
        private String mediaSource;
        private String seq;
        private String startTime;
        private String endTime;
        private String replayStatus;
        public boolean isSelect;

        public LessonItem toLessonItem() {
            LessonItem lessonItem = new LessonItem();
            lessonItem.courseId = AppUtil.parseInt(courseId);
            lessonItem.id = AppUtil.parseInt(id);
            lessonItem.itemType = itemType;
            lessonItem.length = length;
            lessonItem.number = AppUtil.parseInt(number);
            lessonItem.title = title;
            lessonItem.type = type;
            lessonItem.free = AppUtil.parseInt(free);
            lessonItem.mediaSource = mediaSource;
            lessonItem.seq = AppUtil.parseInt(seq);
            lessonItem.startTime = startTime;
            lessonItem.endTime = endTime;
            lessonItem.replayStatus = replayStatus;
            return lessonItem;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String createdTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getReplayStatus() {
            return replayStatus;
        }

        public void setReplayStatus(String replayStatus) {
            this.replayStatus = replayStatus;
        }

        public String getSeq() {
            return seq;
        }
        public void setSeq(String seq) {
            this.seq = seq;
        }

        public String getMediaSource() {
            return mediaSource;
        }

        public void setMediaSource(String mediaSource) {
            this.mediaSource = mediaSource;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public String getLength() {
            return length;
        }

        public void setLength(String length) {
            this.length = length;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFree() {
            return free;
        }

        public void setFree(String free) {
            this.free = free;
        }

        public static class UploadFileBean {

            private String id;
            private String length;


            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getLength() {
                return length;
            }

            public void setLength(String length) {
                this.length = length;
            }
        }
    }
}
