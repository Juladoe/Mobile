package com.edusoho.kuozhi.v3.model.bal.push;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by JesseHuang on 15/10/18.
 */
public class BaseMsgEntity implements Serializable {

    public int id;
    public String content;
    public String headImgUrl;
    public int delivery = 2;
    public String type;
    public long createdTime;
    public String upyunMediaPutUrl;
    public String upyunMediaGetUrl;
    public HashMap<String, String> headers;

    public BaseMsgEntity() {
    }

    public BaseMsgEntity(int id, String content, String headImgUrl, int delivery, String type, long createdTime) {
        this.id = id;
        this.content = content;
        this.headImgUrl = headImgUrl;
        this.type = type;
        this.delivery = delivery;
        this.createdTime = createdTime;
    }

}
