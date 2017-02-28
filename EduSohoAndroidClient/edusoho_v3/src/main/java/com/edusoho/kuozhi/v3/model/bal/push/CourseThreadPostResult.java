package com.edusoho.kuozhi.v3.model.bal.push;

import com.edusoho.kuozhi.v3.model.bal.thread.CourseThreadPostEntity;
import java.io.Serializable;
import java.util.List;

/**
 * Created by JesseHuang on 15/12/27.
 */
public class CourseThreadPostResult implements Serializable {

    public int start;
    public int limit;
    public int total;

    public List<CourseThreadPostEntity> resources;
}
