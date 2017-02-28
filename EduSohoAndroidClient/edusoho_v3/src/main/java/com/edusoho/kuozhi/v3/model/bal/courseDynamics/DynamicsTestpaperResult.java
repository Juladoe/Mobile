package com.edusoho.kuozhi.v3.model.bal.courseDynamics;

/**
 * Created by melomelon on 16/2/19.
 */
public class DynamicsTestpaperResult {
    private String id;
    private String userId;
    private String score;
    private String objectiveScore;
    private String subjectiveScore;
    private String teacherSay;
    private String passedStatus;

    public String getId() {
        return id;
    }

    public String getScore() {
        return score;
    }

    public String getTeacherSay() {
        return teacherSay;
    }

    public String getPassedStatus() {
        return passedStatus;
    }
}
