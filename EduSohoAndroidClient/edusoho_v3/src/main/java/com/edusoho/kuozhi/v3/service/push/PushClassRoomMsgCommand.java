package com.edusoho.kuozhi.v3.service.push;

/**
 * Created by JesseHuang on 15/10/15.
 */
public class PushClassRoomMsgCommand extends PushCommand {
    public PushClassRoomMsgCommand(Pusher pusher) {
        super(pusher);
    }

    @Override
    public void execute() {
        mPusher.pushClassroomMsg();
    }
}
