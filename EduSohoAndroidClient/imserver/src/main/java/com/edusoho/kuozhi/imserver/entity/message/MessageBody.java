package com.edusoho.kuozhi.imserver.entity.message;

import android.text.TextUtils;

import com.edusoho.kuozhi.imserver.entity.MessageEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by 菊 on 2016/5/13.
 * version | version | | type | type | | body | body | | d | destination | | i | id |
 */
public class MessageBody {

    public static final int VERSION = 1;

    /*
    MessageEntity id
     */
    private int mid;

    private String uid;

    private String msgNo;

    private String convNo;

    private String cmd;

    public int version;

    /**
     * text,image,audio,video,multi,push
     */
    public String type;

    public String body;

    public Destination destination;

    public Source sourse;

    public long createdTime;

    public int msgStatus;

    public MessageBody(String jsonStr)
    {
        JSONObject body = null;
        try {
            body = new JSONObject(jsonStr);
        } catch (Exception e) {
            body = new JSONObject();
        }
        init(body);
    }

    private void init(JSONObject body) {
        this.version = body.optInt("v");
        this.body = body.optString("b");
        this.type = body.optString("t");
        this.createdTime = body.optLong("c");
        this.uid = body.optString("i");

        JSONObject destinationBody = body.optJSONObject("d");
        this.destination = destinationBody == null ? new Destination() : new Destination(
                destinationBody.optInt("id"), destinationBody.optString("type"));

        JSONObject sourceBody = body.optJSONObject("s");
        this.sourse =  sourceBody == null ? new Source() : new Source(
                sourceBody.optInt("id"), sourceBody.optString("type"));
    }

    public MessageBody(MessageEntity messageEntity)
    {
        this(messageEntity.getMsg());
        this.setCmd(messageEntity.getCmd());
        this.getSource().setNickname(messageEntity.getFromName());
        this.getDestination().setNickname(messageEntity.getToName());
        this.setMsgNo(messageEntity.getMsgNo());
        this.setConvNo(messageEntity.getConvNo());
        this.setMessageId(TextUtils.isEmpty(messageEntity.getUid()) ? uid : messageEntity.getUid());
        this.setMsgStatus(messageEntity.getStatus());
        this.setCreatedTime(messageEntity.getTime() * 1000L);
        this.setMid(messageEntity.getId());
    }

    public MessageBody(int version, String type, String body) {
        this.version = version;
        this.type = type;
        this.body = body;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
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

    public String getMsgNo() {
        return msgNo;
    }

    public void setMsgNo(String msgNo) {
        this.msgNo = msgNo;
    }

    public String getMessageId() {
        return uid;
    }

    public void setMessageId(String uid) {
        this.uid = uid;
    }

    public Source getSource() {
        return sourse;
    }

    public void setSource(Source source) {
        this.sourse = source;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public String toJson() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("v", version);
            jsonObject.put("t", type);
            jsonObject.put("b", body);
            jsonObject.put("c", createdTime);
            jsonObject.put("i", uid);

            JSONObject destinationObj = new JSONObject();
            destinationObj.put("id", destination.getId());
            destinationObj.put("type", destination.getType());
            jsonObject.put("d", destinationObj);

            JSONObject sourceObj = new JSONObject();
            sourceObj.put("id", sourse.getId());
            sourceObj.put("type", sourse.getType());
            jsonObject.put("s", sourceObj);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonObject.toString();
    }
}