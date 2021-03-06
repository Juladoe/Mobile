package com.edusoho.kuozhi.v3.model.bal.course;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Zhang on 2016/12/18.
 */

public class CourseReviewDetail implements Serializable{

    private int start;
    private int limit;
    private String total;
    private List<CourseReview> data;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<CourseReview> getData() {
        return data;
    }

    public void setData(List<CourseReview> data) {
        this.data = data;
    }
}
