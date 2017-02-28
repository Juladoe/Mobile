package com.edusoho.kuozhi.imserver.error;

/**
 * Created by suju on 16/8/29.
 */
public class MessageSaveFailException extends Throwable {

    public MessageSaveFailException() {
        super();
    }

    @Override
    public String getMessage() {
        return "message save fail";
    }
}
