package com.edusoho.kuozhi.v3.model.bal;

import java.io.Serializable;

/**
 * Created by JesseHuang on 16/3/4.
 */
public class Classroom implements Serializable {

    public int id;
    public String title;
    public String status;
    public String about = "";
    public int categoryId;
    public String description;
    public double price;
    public String middlePicture;
    public String largePicture;
    public int studentNum;
    public int vipLevelId;
    public String postNum;
    public String rating;
    public String buyable;
    public String createdTime;
    public String[] service;
    public Teacher[] teachers;
    public String convNo;
    public int headTeacherId;

    public String getLargePicture() {
        int schemIndex = largePicture.lastIndexOf("http://");
        if (schemIndex != -1) {
            return largePicture.substring(schemIndex);
        }
        if (largePicture.startsWith("//")) {
            return "http:" + largePicture;
        }
        return largePicture;
    }
}
