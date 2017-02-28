package com.edusoho.kuozhi.v3.entity.discovery;

import java.io.Serializable;
import java.util.List;

/**
 * Created by su on 2016/2/24.
 */
public class DiscoveryColumn<T extends DiscoveryCardProperty> implements Serializable {

    public int id;
    public String title;
    public String type;
    public int categoryId;
    public String orderType;
    public int showCount;
    public String seq;
    public String createTime;
    public String updateTime;

    public List<T> data;
}
