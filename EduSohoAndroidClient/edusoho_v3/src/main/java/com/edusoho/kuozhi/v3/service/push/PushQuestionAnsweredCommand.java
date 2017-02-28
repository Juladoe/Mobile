package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by melomelon on 15/12/21.
 */
public class PushQuestionAnsweredCommand extends PushCommand {
    public PushQuestionAnsweredCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushQuestionAnswered();
    }
}
