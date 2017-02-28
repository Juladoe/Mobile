package com.edusoho.kuozhi.v3.model.bal.course;

import com.edusoho.kuozhi.v3.model.bal.Member;
import com.edusoho.kuozhi.v3.model.bal.Vip;
import com.edusoho.kuozhi.v3.model.bal.VipLevel;

import java.io.Serializable;

/**
 * Created by howzhi on 14-8-26.
 */
public class CourseDetailsResult implements Serializable {
    public Course course;
    public boolean userFavorited;
    public Member member;
    public Vip vip;
    public VipLevel[] vipLevels;
}
