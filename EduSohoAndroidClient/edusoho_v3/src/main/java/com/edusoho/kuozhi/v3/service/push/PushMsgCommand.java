package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/9/11.
 */
public class PushMsgCommand extends PushCommand {

    public PushMsgCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushMsg();
    }

}

