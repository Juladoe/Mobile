package com.edusoho.kuozhi.adapter;

import android.content.Context;
import android.view.View;
import com.edusoho.kuozhi.BuildConfig;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.adapter.ChatAdapter;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.bal.push.Chat;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import java.util.ArrayList;

/**
 * Created by howzhi on 15/11/24.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatAdapterTest {

    private Context mContext;
    private User mUser;

    @Before
    public void setUp() {

        ActionBarBaseActivity actionBarBaseActivity = Robolectric.buildActivity(ActionBarBaseActivity.class).get();
        EdusohoApp.app = new EdusohoApp();
        EdusohoApp.app.domain = "";
        mUser = new User();

        mContext = actionBarBaseActivity.getApplicationContext();

        ImageLoaderConfiguration loaderConfiguration = new ImageLoaderConfiguration
                .Builder(mContext)
                .memoryCacheExtraOptions((int) (1280), (int) (720))
                .diskCache(new UnlimitedDiscCache(AppUtil.getAppCacheDir())).imageDownloader(new BaseImageDownloader(mContext, Const.TIMEOUT, Const.TIMEOUT))
                .build();
        ImageLoader.getInstance().init(loaderConfiguration);
    }

    @Test
    public void testAdapterAddItem() {
        ChatAdapter<Chat> adapter = new ChatAdapter<Chat>(mContext, new ArrayList<Chat>(), mUser);

        Chat chat = new Chat();
        chat.chatId = 1;
        chat.custom = "custom";
        chat.fromId = 1;
        chat.nickname = "test";
        chat.toId = 1;
        chat.userId = 1;
        chat.content = "content";
        adapter.addItem(chat);

        Assert.assertEquals(1, adapter.getCount());
        Chat chat1 = (Chat) adapter.getItem(0);
        Assert.assertEquals(chat.nickname, chat1.nickname);
        Assert.assertEquals(chat.chatId, chat1.chatId);
        Assert.assertEquals(chat.userId, chat1.userId);
    }

    @Test
    public void testInitAdatper() {
        ArrayList<Chat> list = new ArrayList<Chat>();
        Chat chat = new Chat();
        chat.chatId = 1;
        chat.custom = "custom";
        chat.fromId = 1;
        chat.nickname = "test";
        chat.toId = 1;
        chat.userId = 1;
        chat.content = "content";
        list.add(chat);

        ChatAdapter<Chat> adapter = new ChatAdapter<Chat>(mContext, list, mUser);

        Assert.assertEquals(1, adapter.getCount());
        Chat chat1 = (Chat) adapter.getItem(0);
        Assert.assertEquals(chat.nickname, chat1.nickname);
        Assert.assertEquals(chat.chatId, chat1.chatId);
        Assert.assertEquals(chat.userId, chat1.userId);
    }

    @Test
    public void testAdapterView() {
        ArrayList<Chat> list = new ArrayList<Chat>();
        Chat chat = new Chat();
        chat.chatId = 1;
        chat.custom = "custom";
        chat.fromId = 1;
        chat.nickname = "test";
        chat.toId = 1;
        chat.userId = 1;
        chat.content = "content";
        chat.type = PushUtil.ChatMsgType.TEXT;
        list.add(chat);

        ChatAdapter<Chat> adapter = new ChatAdapter<Chat>(mContext, list, mUser);

        View view = adapter.getView(0, null, null);
        Assert.assertNotNull(view);
        ChatAdapter.ViewHolder holder = (ChatAdapter.ViewHolder) view.getTag();
        Assert.assertNotNull(holder.tvSendContent);
        Assert.assertEquals(chat.content, holder.tvSendContent.getText());
    }
}
