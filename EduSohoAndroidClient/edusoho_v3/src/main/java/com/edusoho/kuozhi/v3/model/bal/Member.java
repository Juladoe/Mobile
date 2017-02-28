package com.edusoho.kuozhi.v3.model.bal;

import java.io.Serializable;

/**
 * Created by howzhi on 14-9-15.
 */
public class Member implements Serializable {

    public int id;
    public int courseId;
    public int userId;
    public int orderId;
    public long deadline;
    public String levelId;
    public String learnedNum;
    public String credit;
    public String noteNum;
    public String noteLastUpdateTime;
    public String isLearned;
    public String seq;
    public String remark;
    public int isVisible;
    public Object role;
    public int locked;
    public int createdTime;

//    public enum Role
//    {
//        teacher, student, admin
//    }
}
