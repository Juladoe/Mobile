package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/12/21.
 */
public class PushCourseDiscussCommand extends PushCommand {

    public PushCourseDiscussCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushCourseDiscussMsg();
    }
}
