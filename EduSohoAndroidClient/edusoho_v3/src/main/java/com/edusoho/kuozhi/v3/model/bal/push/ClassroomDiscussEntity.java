package com.edusoho.kuozhi.v3.model.bal.push;

import java.util.HashMap;

/**
 * Created by JesseHuang on 15/10/15.
 */
public class ClassroomDiscussEntity extends BaseMsgEntity {
    public int discussId;
    public int classroomId;
    public int fromId;
    public String nickname;
    public int belongId;

    public String upyunMediaPutUrl;
    public String upyunMediaGetUrl;
    public HashMap<String, String> headers;

    public ClassroomDiscussEntity() {
    }

    public ClassroomDiscussEntity(int id, int classroomId, int fromId, String nickname, String headImgUrl, String content, int belongId,
                                  String type, int delivery, int createdTime) {
        super(id, content, headImgUrl, delivery, type, createdTime);
        this.classroomId = classroomId;
        this.fromId = fromId;
        this.nickname = nickname;
        this.belongId = belongId;
        this.type = type;
    }

}
