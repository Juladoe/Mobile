package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/9/23.
 */
public class PushDiscountPassCommand extends PushCommand {

    public PushDiscountPassCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushDiscountPass();
    }
}
