package com.edusoho.kuozhi.v3.entity.discovery;

import com.edusoho.kuozhi.v3.model.bal.course.Course;

import java.io.Serializable;

/**
 * Created by JesseHuang on 16/3/4.
 */
public class DiscoveryCourse extends Course implements DiscoveryCardProperty, Serializable {
    private boolean mEmpty = false;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getPicture() {
        return middlePicture;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public int getStudentNum() {
        return studentNum;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getTeacherAvatar() {
        if (hasTeachers()) {
            return teachers[0].getSmallAvatar();
        }
        return "";
    }

    @Override
    public String getTeacherNickname() {
        if (hasTeachers()) {
            return teachers[0].nickname;
        }
        return "";
    }

    private boolean hasTeachers() {
        return teachers.length > 0;
    }

    @Override
    public boolean isEmpty() {
        return mEmpty;
    }

    public DiscoveryCourse() {
    }

    public DiscoveryCourse(boolean isEmpty) {
        this.mEmpty = isEmpty;
    }
}
