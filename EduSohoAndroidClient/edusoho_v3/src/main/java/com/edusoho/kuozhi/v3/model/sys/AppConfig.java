package com.edusoho.kuozhi.v3.model.sys;

/**
 * Created by JesseHuang on 15/4/23.
 */
public class AppConfig {

    public static final String IM_OPEN = "1";

    public boolean startWithSchool;//进入时是否有网校信息
    public boolean showSplash;//是否是第一次安装，是否有引导界面
    public boolean isPublicRegistDevice;
    public int offlineType;

    /**
     * 默认0，开启1
     */
    public int msgSound;
    /**
     * 默认0，开启2
     */
    public int msgVibrate;

    public boolean newVerifiedNotify;

    public boolean isEnableIMChat;

}
