package com.edusoho.kuozhi.v3.model.bal.push;

import java.io.Serializable;

/**
 * Created by JesseHuang on 15/9/15.
 */
public class V2CustomContent implements Serializable {
    private int msgId;
    private int v;
    private FromEntity from;
    private ToEntity to;
    private BodyEntity body;
    private int createdTime;

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTo(ToEntity to) {
        this.to = to;
    }

    public void setBody(BodyEntity body) {
        this.body = body;
    }

    public void setV(int v) {
        this.v = v;
    }

    public void setCreatedTime(int createdTime) {
        this.createdTime = createdTime;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public void setFrom(FromEntity from) {
        this.from = from;
    }

    public ToEntity getTo() {
        return to;
    }

    public BodyEntity getBody() {
        return body;
    }

    public int getV() {
        return v;
    }

    public int getCreatedTime() {
        return createdTime;
    }

    public int getMsgId() {
        return msgId;
    }

    public FromEntity getFrom() {
        return from;
    }

    public static class FromEntity {

        private int id;
        private String nickname;
        private String image;
        private String type;

        public void setId(int id) {
            this.id = id;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public String getNickname() {
            return nickname;
        }

        public String getImage() {
            return image;
        }

        public String getType() {
            return type;
        }
    }

    public static class ToEntity {
        /**
         * id : 268
         * type : user
         */
        private int id;
        private String type;
        private String image;

        public void setId(int id) {
            this.id = id;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }

    public static class BodyEntity {

        private int id;
        private String content;
        private String type;
        private String lessonType;
        private String title;
        private String image;
        private int threadId;
        private int postId;
        private int courseId;

        private int lessonId;
        private int homeworkResultId;
        private int questionId;
        private boolean isLessonFinished;
        private int learnStartTime;
        private int learnFinishTime;

        public int getLearnStartTime() {
            return learnStartTime;
        }

        public void setLearnStartTime(int learnStartTime) {
            this.learnStartTime = learnStartTime;
        }

        public int getLearnFinishTime() {
            return learnFinishTime;
        }

        public void setLearnFinishTime(int learnFinishTime) {
            this.learnFinishTime = learnFinishTime;
        }

        public boolean getIsLessonFinished() {
            return isLessonFinished;
        }

        public void setIsLessonFinished(boolean isLessonFinished) {
            this.isLessonFinished = isLessonFinished;
        }

        public int getHomeworkResultId() {
            return homeworkResultId;
        }

        public void setHomeworkResultId(int homeworkResultId) {
            this.homeworkResultId = homeworkResultId;
        }

        public int getQuestionId() {
            return questionId;
        }

        public void setQuestionId(int questionId) {
            this.questionId = questionId;
        }

        public int getLessonId() {
            return lessonId;
        }

        public void setLessonId(int lessonId) {
            this.lessonId = lessonId;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public int getThreadId() {
            return threadId;
        }

        public void setThreadId(int threadId) {
            this.threadId = threadId;
        }

        public int getPostId() {
            return postId;
        }

        public void setPostId(int postId) {
            this.postId = postId;
        }

        public int getCourseId() {
            return courseId;
        }

        public void setCourseId(int courseId) {
            this.courseId = courseId;
        }


        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLessonType() {
            return lessonType;
        }

        public void setLessonType(String lessonType) {
            this.lessonType = lessonType;
        }
    }
}
