package com.edusoho.kuozhi.v3.entity.course;

import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.course.CourseMember;
import com.edusoho.kuozhi.v3.model.sys.Error;

import java.io.Serializable;
import java.util.List;

/**
 * Created by remilia on 2017/1/9.
 */
public class LearningCourse implements Serializable {
    public int start;
    public int limit;
    public int total;
    public List<Course> data;
    public Error error;
}
