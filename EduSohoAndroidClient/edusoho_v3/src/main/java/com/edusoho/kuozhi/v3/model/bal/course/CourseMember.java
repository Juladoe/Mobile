package com.edusoho.kuozhi.v3.model.bal.course;

import java.io.Serializable;

/**
 * Created by JesseHuang on 15/12/11.
 */
public class CourseMember implements Serializable {

    public static final int NONE = 0;
    public static final int MEMBER = 1;
    public static final int EXPIRE = 3;

    public int id;
    public int courseId;
    public int classroomId;
    public String joinedType;
    public int orderId;
    public String deadline;
    public int levelId;
    public int learnedNum;
    public String credit;
    public int noteNum;
    public String noteLastUpdateTime;
    public String isLearned;
    public String seq;
    public String remark;
    public String isVisible;
    public String role;
    public String locked;
    public String deadlineNotified;
    public String createdTime;

    public UserEntity user;
    public Course course;

    public static class UserEntity {
        public int id;
        public String nickname;
        public String title;

        public String avatar;

        public String getAvatar() {
            int schemIndex = avatar.lastIndexOf("http://");
            if (schemIndex != -1) {
                return avatar.substring(schemIndex);
            }
            return avatar;
        }
    }

    public static class Course{
        public int id;
        public String title;
        public String picture;
        public String convNo;
    }
}
