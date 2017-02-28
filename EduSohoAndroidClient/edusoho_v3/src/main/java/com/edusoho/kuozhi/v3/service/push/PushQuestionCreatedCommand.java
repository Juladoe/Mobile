package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 16/2/23.
 */
public class PushQuestionCreatedCommand extends PushCommand {

    public PushQuestionCreatedCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushQuestionCreated();
    }
}
