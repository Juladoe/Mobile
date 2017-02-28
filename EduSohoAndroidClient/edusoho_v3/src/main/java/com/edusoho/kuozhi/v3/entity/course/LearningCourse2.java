package com.edusoho.kuozhi.v3.entity.course;

import com.edusoho.kuozhi.v3.model.bal.course.Course;

import java.io.Serializable;
import java.util.List;

/**
 * Created by remilia on 2017/1/9.
 */
public class LearningCourse2 implements Serializable {
    private String total;
    private List<Course> resources;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<Course> getResources() {
        return resources;
    }

    public void setResources(List<Course> resources) {
        this.resources = resources;
    }
}
