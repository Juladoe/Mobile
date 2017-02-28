package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/9/21.
 */
public class PushGlobalAnnouncementCommand extends PushCommand {

    public PushGlobalAnnouncementCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushGlobalAnnouncement();
    }
}
