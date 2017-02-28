package com.edusoho.kuozhi.v3.service.push;

import android.os.Bundle;

import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.push.V2CustomContent;
import com.edusoho.kuozhi.v3.service.EdusohoMainService;
import com.edusoho.kuozhi.v3.ui.DefaultPageActivity;
import com.edusoho.kuozhi.v3.ui.NewsCourseActivity;
import com.edusoho.kuozhi.v3.ui.fragment.FriendFragment;
import com.edusoho.kuozhi.v3.ui.fragment.NewsFragment;
import com.edusoho.kuozhi.v3.util.Const;

/**
 * Created by JesseHuang on 15/9/11.
 */
public class Pusher {
    private Bundle mBundle;
    private V2CustomContent mV2CustomContent;

    public Pusher(Bundle bundle) {
        mBundle = bundle;
    }

    public V2CustomContent getV2CustomContent() {
        return mV2CustomContent;
    }

    public void setV2CustomContent(V2CustomContent mV2CustomContent) {
        this.mV2CustomContent = mV2CustomContent;
    }

    public void pushMsg() {

    }

    public void pushVerified() {
        EdusohoMainService.getService().setNewNotification();
        mBundle.putBoolean("isNew",true);
        EdusohoApp.app.sendMsgToTarget(Const.NEW_FANS, mBundle, FriendFragment.class);
        EdusohoApp.app.sendMsgToTarget(Const.NEW_FANS, mBundle, DefaultPageActivity.class);
    }

    public void pushLessonPublish() {
        boolean isForeground = EdusohoApp.app.isForeground(NewsCourseActivity.class.getName());
        if (isForeground) {
            EdusohoApp.app.sendMsgToTarget(Const.ADD_COURSE_MSG, mBundle, NewsCourseActivity.class);
        }
        EdusohoApp.app.sendMsgToTarget(Const.ADD_COURSE_MSG, mBundle, NewsFragment.class);
    }

    public void pushCourseAnnouncement() {
    }

    public void pushGlobalAnnouncement() {
    }

    public void pushTestpaperReviewed() {
    }

    public void pushHomeworkReviewed() {
    }

    public void pushQuestionAnswered() {
        EdusohoApp.app.sendMsgToTarget(Const.ADD_COURSE_MSG, mBundle, NewsFragment.class);
    }

    public void pushQuestionCreated() {
    }

    public void pushLessonFinished() {
    }

    public void pushLessonStart() {
    }

    public void pushDiscountPass() {
    }

    public void pushLiveLessonStartNotify() {
    }

    public void pushArticleCreate() {
    }

    public void pushClassroomMsg() {
    }


    public void pushCourseDiscussMsg() {
    }
}
