package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by melomelon on 15/12/21.
 */
public class PushLessonFinishedCommand extends PushCommand {
    public PushLessonFinishedCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushLessonFinished();
    }
}
