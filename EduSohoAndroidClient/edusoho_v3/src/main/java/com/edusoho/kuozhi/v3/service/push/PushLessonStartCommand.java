package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by melomelon on 15/12/23.
 */
public class PushLessonStartCommand extends PushCommand {
    public PushLessonStartCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushLessonStart();
    }
}
