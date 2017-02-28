package com.edusoho.kuozhi.v3.entity.discovery;

import com.edusoho.kuozhi.v3.model.bal.Classroom;

/**
 * Created by JesseHuang on 16/3/4.
 */
public class DiscoveryClassroom extends Classroom implements DiscoveryCardProperty {

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
        return "classroom";
    }

    @Override
    public String getTeacherAvatar() {
        return null;
    }

    @Override
    public String getTeacherNickname() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return mEmpty;
    }

    public DiscoveryClassroom() {

    }

    public DiscoveryClassroom(boolean isEmpty) {
        mEmpty = isEmpty;
    }
}
