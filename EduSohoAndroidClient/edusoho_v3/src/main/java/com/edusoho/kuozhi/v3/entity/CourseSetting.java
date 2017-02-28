package com.edusoho.kuozhi.v3.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by DF on 2016/12/21.
 */

public class CourseSetting {

    @SerializedName("custom_chapter_enabled")
    private String customChapterEnable;

    @SerializedName("chapter_name")
    private String chapterName;

    @SerializedName("part_name")
    private String partName;

    public String getCustomChapterEnable(){
        return customChapterEnable;
    }

    public String getChapterName(){
        return chapterName;
    }

    public String getPartName(){
        return partName;
    }
}
