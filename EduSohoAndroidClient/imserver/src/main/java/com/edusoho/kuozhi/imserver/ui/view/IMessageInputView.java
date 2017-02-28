package com.edusoho.kuozhi.imserver.ui.view;

import com.edusoho.kuozhi.imserver.ui.listener.InputViewControllerListener;
import com.edusoho.kuozhi.imserver.ui.listener.MessageSendListener;

/**
 * Created by suju on 16/10/19.
 */
public interface IMessageInputView {

    int INPUT_IMAGE_AND_VOICE = 0;
    int INPUT_TEXT = 1;

    void setEnabled(boolean isEnable);

    void setMessageSendListener(MessageSendListener listener);

    void setMessageControllerListener(InputViewControllerListener listener);
}
