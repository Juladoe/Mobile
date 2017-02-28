package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/11/4.
 */
public class PushLiveLessonStartNotify extends PushCommand {
    public PushLiveLessonStartNotify(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushLiveLessonStartNotify();
    }
}
