package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/9/11.
 */
public abstract class PushCommand {
    protected Pusher mPusher;

    public PushCommand(Pusher pusher) {
        mPusher = pusher;
    }

    public abstract void execute();
}
