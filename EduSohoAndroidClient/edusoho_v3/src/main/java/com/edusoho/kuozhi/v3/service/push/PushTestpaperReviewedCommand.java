package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/9/21.
 */
public class PushTestpaperReviewedCommand extends PushCommand {

    public PushTestpaperReviewedCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushTestpaperReviewed();
    }
}
