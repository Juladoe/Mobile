package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/9/11.
 */
public class PushVerifiedCommand extends PushCommand {
    public PushVerifiedCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushVerified();
    }
}
