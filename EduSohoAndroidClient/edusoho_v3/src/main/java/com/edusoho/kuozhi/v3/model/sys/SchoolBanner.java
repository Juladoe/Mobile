package com.edusoho.kuozhi.v3.model.sys;

/**
 * Created by su on 2016/2/19.
 */
public class SchoolBanner {
    public String url;
    public String action;
    public String params;

    public static SchoolBanner def()
    {
        SchoolBanner schoolBanner = new SchoolBanner();
        schoolBanner.action = "";
        schoolBanner.url = "localRes";
        schoolBanner.params = "";
        return schoolBanner;
    }
}