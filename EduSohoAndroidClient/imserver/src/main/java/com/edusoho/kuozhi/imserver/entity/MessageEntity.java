package com.edusoho.kuozhi.imserver.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 菊 on 2016/4/27.
 */
public class MessageEntity implements Parcelable {

    private String fromId;
    private String fromName;
    private String toName;
    private int time;
    private String msgNo;
    private String toId;
    private String convNo;
    private String msg;
    private String cmd;
    private int status;
    private int id;
    private String uid;

    public MessageEntity() {
    }

    public MessageEntity(Parcel in)
    {
        this.fromId = in.readString();
        this.fromName = in.readString();
        this.toName = in.readString();
        this.time = in.readInt();
        this.msgNo = in.readString();
        this.toId = in.readString();
        this.convNo = in.readString();
        this.msg = in.readString();
        this.cmd = in.readString();
        this.status = in.readInt();
        this.id = in.readInt();
        this.uid = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getConvNo() {
        return convNo;
    }

    public void setConvNo(String convNo) {
        this.convNo = convNo;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getMsgNo() {
        return msgNo;
    }

    public void setMsgNo(String msgNo) {
        this.msgNo = msgNo;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fromId);
        dest.writeString(fromName);
        dest.writeString(toName);
        dest.writeInt(time);
        dest.writeString(msgNo);
        dest.writeString(toId);
        dest.writeString(convNo);
        dest.writeString(msg);
        dest.writeString(cmd);
        dest.writeInt(status);
        dest.writeInt(id);
        dest.writeString(uid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MessageEntity> CREATOR = new Parcelable.Creator<MessageEntity>() {

        public MessageEntity createFromParcel(Parcel in) {
            return new MessageEntity(in);
        }

        public MessageEntity[] newArray(int size) {
            return new MessageEntity[size];
        }
    };

    public static class StatusType {
        public static final int SUCCESS = 1;
        public static final int FAILED = 0;
        public static final int UPLOADING = 2;
        public static final int NONE = -1;
        public static final int UNREAD = 4;
    }
}
