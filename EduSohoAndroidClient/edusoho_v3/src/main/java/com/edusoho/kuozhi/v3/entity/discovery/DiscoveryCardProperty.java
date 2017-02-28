package com.edusoho.kuozhi.v3.entity.discovery;

/**
 * Created by JesseHuang on 16/3/4.
 */
public interface DiscoveryCardProperty {
    int getId();

    String getPicture();

    String getTitle();

    double getPrice();

    int getStudentNum();

    String getType();

    String getTeacherAvatar();

    String getTeacherNickname();

    boolean isEmpty();
}
