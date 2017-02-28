package com.edusoho.kuozhi.homework.model;

import java.util.List;

/**
 * Created by howzhi on 15/10/15.
 */
public class HomeWorkModel {

    private int id;

    private String courseId;

    private String lessonId;

    private String description;

    private String itemCount;

    private String courseTitle;

    private String lessonTitle;

    private List<HomeWorkQuestion> items;

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseId() {
        return this.courseId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonId() {
        return this.lessonId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setItemCount(String itemCount) {
        this.itemCount = itemCount;
    }

    public String getItemCount() {
        return this.itemCount;
    }

    public void setItems(List<HomeWorkQuestion> items) {
        this.items = items;
    }

    public List<HomeWorkQuestion> getItems() {
        return this.items;
    }

}