package com.edusoho.kuozhi.v3.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.managar.IMConvManager;
import com.edusoho.kuozhi.imserver.managar.IMRoleManager;
import com.edusoho.kuozhi.imserver.ui.IMessageListPresenter;
import com.edusoho.kuozhi.imserver.ui.IMessageListView;
import com.edusoho.kuozhi.imserver.ui.MessageListFragment;
import com.edusoho.kuozhi.imserver.ui.MessageListPresenterImpl;
import com.edusoho.kuozhi.imserver.ui.data.DefautlMessageDataProvider;
import com.edusoho.kuozhi.imserver.ui.data.IMessageDataProvider;
import com.edusoho.kuozhi.imserver.ui.helper.MessageResourceHelper;
import com.edusoho.kuozhi.imserver.ui.listener.MessageControllerListener;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.NotificationProvider;
import com.edusoho.kuozhi.v3.factory.UtilFactory;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.PromiseCallback;
import com.edusoho.kuozhi.v3.model.bal.push.RedirectBody;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;
import com.edusoho.kuozhi.v3.ui.fragment.ChatSelectFragment;
import com.edusoho.kuozhi.v3.ui.fragment.ViewPagerFragment;
import com.edusoho.kuozhi.v3.util.ActivityUtil;
import com.edusoho.kuozhi.v3.util.ApiTokenUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.Promise;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by suju on 16/9/6.
 */
public abstract class AbstractIMChatActivity extends AppCompatActivity {

    public static final int SEND_IMAGE = 1;
    public static final int SEND_CAMERA = 2;

    public static final String BACK = "返回";
    public static final String TAG = "ChatActivity";
    public static final String FROM_ID = "from_id";
    public static final String TARGET_TYPE = "targer_type";
    public static final String FROM_NAME = "from_name";
    public static final String CONV_NO = "conv_no";
    public static final String HEAD_IMAGE_URL = "head_image_url";

    protected int mTargetId;
    protected String mTargetName;
    protected String mTargetType;
    protected String mConversationNo;
    protected IMessageListPresenter mIMessageListPresenter;
    protected MessageListFragment mMessageListFragment;
    protected Context mContext;
    protected TextView mTitleTextView;
    protected View mTitleLayoutView;

    private ActionBar mActionBar;
    private LoadDialog loadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mActionBar = getSupportActionBar();
        mContext = getBaseContext();
        setContentView(createView());
        initParams();
        setBackMode(BACK, TextUtils.isEmpty(mTargetName) ? "聊天" : mTargetName);
        attachMessageListFragment();
        ActivityUtil.setStatusBarTranslucent(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        ActivityUtil.setRootViewFitsWindow(this, getResources().getColor(R.color.primary));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mConversationNo != null) {
            getNotificationProvider().cancelNotification(mConversationNo.hashCode());
        }
    }

    public void setBackMode(String backTitle, String title) {
        mTitleLayoutView = getLayoutInflater().inflate(R.layout.actionbar_custom_title, null);
        mTitleTextView = (TextView) mTitleLayoutView.findViewById(R.id.tv_action_bar_title);
        mTitleTextView.setText(title);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        mActionBar.setCustomView(mTitleLayoutView, layoutParams);

        if (backTitle != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected NotificationProvider getNotificationProvider() {
        return FactoryManager.getInstance().create(NotificationProvider.class);
    }

    protected void attachMessageListFragment() {
        Log.d(TAG, "attachMessageListFragment");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("im_container");

        if (fragment != null) {
            mMessageListFragment = (MessageListFragment) fragment;
        } else {
            mMessageListFragment = createFragment();
            fragmentTransaction.add(R.id.chat_content, mMessageListFragment, "im_container");
            fragmentTransaction.commitAllowingStateLoss();
        }

        mIMessageListPresenter = createProsenter();
        mIMessageListPresenter.addMessageControllerListener(getMessageControllerListener());
    }

    protected IMessageListPresenter createProsenter() {
        Bundle bundle = new Bundle();
        bundle.putString(MessageListFragment.CONV_NO, mConversationNo);
        bundle.putInt(MessageListFragment.TARGET_ID, mTargetId);
        bundle.putString(MessageListFragment.TARGET_TYPE, getTargetType());

        return new ChatMessageListPresenterImpl(
                bundle,
                IMClient.getClient().getConvManager(),
                IMClient.getClient().getRoleManager(),
                IMClient.getClient().getResourceHelper(),
                new DefautlMessageDataProvider(),
                mMessageListFragment
        );
    }

    protected MessageListFragment createFragment() {
        MessageListFragment messageListFragment = (MessageListFragment) Fragment.instantiate(mContext, MessageListFragment.class.getName());
        return messageListFragment;
    }

    protected String getTargetType() {
        return TextUtils.isEmpty(mTargetType) ? Destination.USER : mTargetType;
    }

    protected void initParams() {
        Intent dataIntent = getIntent();
        mTargetId = dataIntent.getIntExtra(FROM_ID, 0);
        mConversationNo = dataIntent.getStringExtra(CONV_NO);
        mTargetType = dataIntent.getStringExtra(TARGET_TYPE);
        mTargetName = dataIntent.getStringExtra(FROM_NAME);

        if (TextUtils.isEmpty(mConversationNo)) {
            ConvEntity convEntity = new IMConvManager(mContext).getConvByTypeAndId(mTargetType, mTargetId);
            mConversationNo = convEntity == null ? null : convEntity.getConvNo();
        }
    }

    private void handlerUrlAction(String url) {
        Pattern WEB_URL_PAT = Pattern.compile("(http://)?(.+)/(course|classroom)/(\\d+)", Pattern.DOTALL);
        Matcher matcher = WEB_URL_PAT.matcher(url);
        if (matcher.find()) {
            String type = matcher.group(3);
            if (!TextUtils.isEmpty(type)) {
                Bundle bundle = new Bundle();
                switch (type) {
                    case "course":
                        bundle.putInt(Const.COURSE_ID, Integer.parseInt(matcher.group(4)));
                        CoreEngine.create(mContext).runNormalPluginWithBundle("CourseActivity", mContext, bundle);
                        return;
                    case "classroom":
                        bundle.putInt(Const.CLASSROOM_ID, Integer.parseInt(matcher.group(4)));
                        CoreEngine.create(mContext).runNormalPluginWithBundle("ClassroomActivity", mContext, bundle);
                        return;
                }
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString(Const.WEB_URL, url);
        CoreEngine.create(mContext).runNormalPluginWithBundle("WebViewActivity", mContext, bundle);
    }

    protected MessageControllerListener getMessageControllerListener() {
        return new MessageControllerListener() {

            @Override
            public void onShowImage(int index, ArrayList<String> imageList) {
                Bundle bundle = new Bundle();
                bundle.putInt("index", index);
                bundle.putStringArrayList("imageList", imageList);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
                viewPagerFragment.setArguments(bundle);
                fragmentTransaction.setCustomAnimations(R.anim.window_zoom_open, R.anim.window_zoom_exit);
                viewPagerFragment.show(fragmentTransaction, "viewpager");
            }

            @Override
            public void onShowUser(Role role) {
                Bundle bundle = new Bundle();
                School school = getAppSettingProvider().getCurrentSchool();
                bundle.putString(Const.WEB_URL, String.format(
                        Const.MOBILE_APP_URL,
                        school.url + "/",
                        String.format(Const.USER_PROFILE, role.getRid()))
                );
                CoreEngine.create(mContext).runNormalPluginWithBundle("WebViewActivity", mContext, bundle);
            }

            @Override
            public void onShowWebPage(String url) {
                handlerUrlAction(url);
            }

            @Override
            public void selectPhoto() {
                openPictureFromLocal();
            }

            @Override
            public void takePhoto() {
                openPictureFromCamera();
            }

            @Override
            public void onShowActivity(Bundle bundle) {
                String activityName = bundle.getString("activityName");
                switch (activityName) {
                    case "ThreadDiscussActivity":
                        CoreEngine.create(mContext).runNormalPluginWithBundle("ThreadDiscussActivity", mContext, bundle);
                        break;
                    case "ChatSelectFragment":
                        try {
                            JSONObject data = new JSONObject(bundle.getString("data"));
                            final RedirectBody redirectBody = RedirectBody.createByJsonObj(data);
                            PluginRunCallback pluginRunCallback = new PluginRunCallback() {
                                @Override
                                public void setIntentDate(Intent startIntent) {
                                    startIntent.putExtra(Const.ACTIONBAR_TITLE, "选择");
                                    startIntent.putExtra(ChatSelectFragment.BODY, redirectBody);
                                    startIntent.putExtra(FragmentPageActivity.FRAGMENT, "ChatSelectFragment");
                                }
                            };
                            CoreEngine.create(mContext).runNormalPluginForResult(
                                    "FragmentPageActivity",
                                    AbstractIMChatActivity.this,
                                    ChatSelectFragment.REQUEST_SELECT,
                                    pluginRunCallback
                            );
                        } catch (JSONException e) {
                        }
                        break;
                }
            }
        };
    }

    protected abstract void createTargetRole(String type, int rid, MessageListPresenterImpl.RoleUpdateCallback callback);

    protected abstract Promise createChatConvNo();

    protected View createView() {
        FrameLayout frameLayout = new FrameLayout(getBaseContext());
        frameLayout.setId(R.id.chat_content);

        return frameLayout;
    }

    /**
     * 从图库获取图片
     */
    protected void openPictureFromLocal() {
        Intent intent = new Intent(getBaseContext(), MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 5);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        startActivityForResult(intent, SEND_IMAGE);
    }

    protected void openPictureFromCamera() {
        Intent intent = new Intent(getBaseContext(), MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_TAKE_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        startActivityForResult(intent, SEND_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatSelectFragment.REQUEST_SELECT && resultCode == ChatSelectFragment.RESULT_SEND_OK) {
            mIMessageListPresenter.refresh();
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == SEND_IMAGE) {
            ArrayList<String> pathList = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            int size = getSupportFragmentManager().getFragments().size();
            data.removeExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            data.putStringArrayListExtra("ImageList", pathList);
            for (int i = 0; i < size; i++) {
                getSupportFragmentManager().getFragments().get(i).onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    protected UtilFactory getUtilFactory() {
        return FactoryManager.getInstance().create(UtilFactory.class);
    }

    protected class ChatMessageListPresenterImpl extends MessageListPresenterImpl {

        public ChatMessageListPresenterImpl(Bundle params,
                                            IMConvManager convManager,
                                            IMRoleManager roleManager,
                                            MessageResourceHelper messageResourceHelper,
                                            IMessageDataProvider mIMessageDataProvider,
                                            IMessageListView messageListView) {
            super(params, convManager, roleManager, messageResourceHelper, mIMessageDataProvider, messageListView);
            setClientInfo(IMClient.getClient().getClientId(), IMClient.getClient().getClientName());
        }

        @Override
        protected Map<String, String> getRequestHeaders() {
            HashMap<String, String> map = new HashMap();
            String token = ApiTokenUtil.getApiToken(mContext);
            map.put("Auth-Token", TextUtils.isEmpty(token) ? "" : token);
            return map;
        }

        @Override
        protected void createRole(String type, int rid, MessageListPresenterImpl.RoleUpdateCallback callback) {
            createTargetRole(type, rid, callback);
        }

        @Override
        protected void createConvNo(final MessageListPresenterImpl.ConvNoCreateCallback convNoCreateCallback) {
            loadDialog = LoadDialog.create(AbstractIMChatActivity.this);
            loadDialog.show();
            createChatConvNo().then(new PromiseCallback<String>() {
                @Override
                public Promise invoke(String convNo) {
                    loadDialog.dismiss();
                    if (!TextUtils.isEmpty(convNo)) {
                        convNoCreateCallback.onCreateConvNo(convNo);
                    }
                    return null;
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadDialog != null) {
            loadDialog.dismiss();
        }
        loadDialog = null;
    }
}
