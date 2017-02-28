package com.edusoho.kuozhi.v3.model.bal;

import java.io.Serializable;

/**
 * Created by howzhi on 14-8-31.
 */
public class VipLevel implements Serializable {

    public int id;
    public int seq;
    public String name;
    public String icon;
    public String picture;
    public double monthPrice;
    public double yearPrice;
    public String description;
    public int freeLearned;
    public int enabled;
    public String createdTime;
}
