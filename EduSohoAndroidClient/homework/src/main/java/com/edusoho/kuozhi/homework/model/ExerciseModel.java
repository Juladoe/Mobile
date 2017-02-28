package com.edusoho.kuozhi.homework.model;

import java.util.List;

/**
 * Created by Melomelon on 2015/10/26.
 */
public class ExerciseModel {
    private int id;
    private String courseId;
    private String lessonId;
    private String description;
    private String itemCount;
    private String courseTitle;
    private String lessonTitle;
    private List<HomeWorkQuestion> items;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemCount() {
        return itemCount;
    }

    public void setItemCount(String itemCount) {
        this.itemCount = itemCount;
    }

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

    public List<HomeWorkQuestion> getItems() {
        return items;
    }

    public void setItems(List<HomeWorkQuestion> items) {
        this.items = items;
    }
}
