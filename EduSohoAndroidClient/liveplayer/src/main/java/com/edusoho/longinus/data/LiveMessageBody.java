package com.edusoho.longinus.data;

import android.text.TextUtils;

import com.edusoho.kuozhi.imserver.entity.MessageEntity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suju on 16/10/13.
 */
public class LiveMessageBody {

    private String convNo;
    private String type;
    private String clientId;
    private String clientName;
    private String role;
    private String data;
    private long time;
    private int msgStatus;

    public LiveMessageBody(MessageEntity messageEntity) {
        this(messageEntity.getMsg());
        if (TextUtils.isEmpty(type)) {
            setType(messageEntity.getCmd());
        }
        if (time == 0) {
            setTime(messageEntity.getTime());
        }
    }

    public LiveMessageBody(String body) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(body);
        } catch (JSONException e) {
            jsonObject = new JSONObject();
        }
        setClientId(jsonObject.optString("clientId"));
        setClientName(jsonObject.optString("clientName"));
        setRole(jsonObject.optString("role"));
        setType(jsonObject.optString("type"));
        setConvNo(jsonObject.optString("convNo"));
        setData(jsonObject.optString("data"));
        setTime(jsonObject.optLong("time"));
    }

    public int getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(int msgStatus) {
        this.msgStatus = msgStatus;
    }

    public String getConvNo() {
        return convNo;
    }

    public void setConvNo(String convNo) {
        this.convNo = convNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
