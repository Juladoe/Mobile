package com.edusoho.kuozhi.v3.model.bal.push;

import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.util.PushUtil;
import java.util.HashMap;

/**
 * Created by JesseHuang on 15/12/14.
 */
public class CourseDiscussEntity extends BaseMsgEntity {
    public int discussId;
    public int courseId;
    public int fromId;
    public String nickname;
    public int belongId;
    public HashMap<String, String> headers;

    public CourseDiscussEntity() {
    }

    public CourseDiscussEntity(MessageBody messageBody) {
        super(0, messageBody.getBody(), messageBody.getSource().getImage(),
                PushUtil.MsgDeliveryType.UPLOADING, messageBody.getType(), messageBody.getCreatedTime()
        );

        fromId = messageBody.getSource().getId();
        nickname = messageBody.getSource().getNickname();
        belongId = EdusohoApp.app.loginUser.id;
    }
}
