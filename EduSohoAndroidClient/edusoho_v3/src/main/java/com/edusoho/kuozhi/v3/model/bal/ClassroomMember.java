package com.edusoho.kuozhi.v3.model.bal;

import java.io.Serializable;
import java.util.List;

/**
 * Created by JesseHuang on 15/10/28.
 */
public class ClassroomMember implements Serializable {

    public int id;
    public int classroomId;
    public int orderId;
    public int levelId;
    public int noteNum;
    public int threadNum;
    public String locked;
    public String remark;
    public String createdTime;


    public UserEntity user;
    public List<String> role;


    public static class UserEntity {
        public int id;
        public String nickname;
        public String title;
        public String avatar;

    }
}
