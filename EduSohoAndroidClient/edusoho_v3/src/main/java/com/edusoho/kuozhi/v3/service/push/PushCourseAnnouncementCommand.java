package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/9/21.
 */
public class PushCourseAnnouncementCommand extends PushCommand {

    public PushCourseAnnouncementCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushCourseAnnouncement();
    }
}
