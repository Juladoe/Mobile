package com.edusoho.kuozhi;

import com.edusoho.kuozhi.imserver.BuildConfig;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

/**
 * Created by 菊 on 2016/5/18.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MessageManagerTest {

    @Before
    public void setUp() {
        IMClient.getClient().init(ShadowApplication.getInstance().getApplicationContext());
    }

    private MessageEntity createMessage(String fromId, String fromName, String toId, String toName, String msg) {
        MessageEntity entity = new MessageEntityBuildr()
                .addCmd("message")
                .addFromId(fromId)
                .addFromName(fromName)
                .addToId(toId)
                .addMsg(msg)
                .addConvNo("convNo1")
                .addTime(1)
                .addStatus(MessageEntity.StatusType.SUCCESS)
                .addToName(toName)
                .builder();
        return entity;
    }

    @Test
    public void testCreateMessage() {
        MessageEntity messageEntity = createMessage("1", "test1", "2", "test2", "hello");
        messageEntity.setUid("uid1");
        IMClient.getClient().getMessageManager().createMessage(messageEntity);

        messageEntity = IMClient.getClient().getMessageManager().getMessageByUID("uid1");
        Assert.assertEquals("1", messageEntity.getFromId());
        Assert.assertEquals("test1", messageEntity.getFromName());
        Assert.assertEquals("2", messageEntity.getToId());
        Assert.assertEquals("test2", messageEntity.getToName());
        Assert.assertEquals("hello", messageEntity.getMsg());
    }

    @Test
    public void testGetMessageById() {
        MessageEntity messageEntity = createMessage("1", "test1", "2", "test2", "hello");
        messageEntity.setUid("uid1");
        IMClient.getClient().getMessageManager().createMessage(messageEntity);

        messageEntity = IMClient.getClient().getMessageManager().getMessageByUID("uid1");
        messageEntity = IMClient.getClient().getMessageManager().getMessage(messageEntity.getId());

        Assert.assertEquals("1", messageEntity.getFromId());
        Assert.assertEquals("test1", messageEntity.getFromName());
        Assert.assertEquals("2", messageEntity.getToId());
        Assert.assertEquals("test2", messageEntity.getToName());
        Assert.assertEquals("hello", messageEntity.getMsg());
    }
}
