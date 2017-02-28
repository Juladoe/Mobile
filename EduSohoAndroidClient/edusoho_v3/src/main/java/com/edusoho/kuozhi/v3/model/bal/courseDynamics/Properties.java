package com.edusoho.kuozhi.v3.model.bal.courseDynamics;

import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.test.Testpaper;

/**
 * Created by melomelon on 16/2/18.
 */
public class Properties {
    private Course course;
    private LessonItem lesson;
    private DynamicsHomeworkItem homework;
    private DynamicsHomeworkResult homeworkResult;
    private Testpaper testpaper;
    private DynamicsTestpaperResult result;
    private DynamicsThread thread;
    private DynamicsPost post;
    private String lessonLearnStartTime;

    public String getLessonLearnStartTime() {
        return lessonLearnStartTime;
    }

    public Course getCourse() {
        return course;
    }

    public LessonItem getLesson() {
        return lesson;
    }

    public DynamicsHomeworkItem getHomework() {
        return homework;
    }

    public DynamicsHomeworkResult getHomeworkResult() {
        return homeworkResult;
    }

    public Testpaper getTestpaper() {
        return testpaper;
    }

    public DynamicsTestpaperResult getResult() {
        return result;
    }

    public DynamicsThread getThread() {
        return thread;
    }

    public DynamicsPost getPost() {
        return post;
    }
}
