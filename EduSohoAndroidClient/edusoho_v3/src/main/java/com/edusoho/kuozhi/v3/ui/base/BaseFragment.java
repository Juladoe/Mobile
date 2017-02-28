package com.edusoho.kuozhi.v3.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.UtilFactory;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.ViewUtil;
import com.edusoho.kuozhi.v3.view.EduSohoAnimWrap;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by JesseHuang on 15/4/24.
 */
public abstract class BaseFragment extends Fragment implements MessageEngine.MessageCallback {

    protected BaseActivity mActivity;
    protected EdusohoApp app;
    protected int mViewId;
    protected View mContainerView;
    public String mTitle;
    protected int mRunStatus;
    protected Context mContext;
    private Queue<WidgetMessage> mUIMessageQueue;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
        mContext = mActivity.getBaseContext();
        app = mActivity.app;
        mUIMessageQueue = new ArrayDeque<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (app == null) {
            app = EdusohoApp.app;
        }
        app.registMsgSource(this);
    }

    protected void invokeUIMessage() {
        WidgetMessage message = null;
        while ((message = mUIMessageQueue.poll()) != null) {
            invoke(message);
        }
    }

    @Override
    public void invoke(WidgetMessage message) {
    }

    @Override
    public void onResume() {
        super.onResume();
        mRunStatus = MSG_RESUME;
    }

    @Override
    public void onPause() {
        super.onPause();
        mRunStatus = MSG_PAUSE;
    }

    protected int getRunStatus() {
        return mRunStatus;
    }

    protected void saveMessage(WidgetMessage message) {
        mUIMessageQueue.add(message);
    }

    @Override
    public int getMode() {
        return REGIST_CLASS;
    }

    @Override
    public com.edusoho.kuozhi.v3.model.sys.MessageType[] getMsgTypes() {
        return new com.edusoho.kuozhi.v3.model.sys.MessageType[0];
    }

    protected void changeTitle(String title) {
        mTitle = title;
        mActivity.setTitle(title);
    }

    public String getTitle() {
        return "";
    }

    @Override
    public void onDestroy() {
        app.unRegistMsgSource(this);
        super.onDestroy();

        MessageType[] messageTypes = getMsgTypes();
        if (messageTypes == null) {
            return;
        }
        for (MessageType messageType : messageTypes) {
            if (messageType.code == MessageType.NONE) {
                app.unRegistPubMsg(messageType, this);
            }
        }
    }

    protected void setContainerView(int viewId) {
        mViewId = viewId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        saveViewState(savedInstanceState);
        if (mContainerView == null) {
            mContainerView = inflater.inflate(mViewId, null);
            initView(mContainerView);
        }

        ViewGroup parent = (ViewGroup) mContainerView.getParent();
        if (parent != null) {
            parent.removeView(mContainerView);
        }
        return mContainerView;
    }

    protected void viewBind(ViewGroup contentView) {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                ViewUtil viewUtil = field.getAnnotation(ViewUtil.class);
                if (viewUtil != null) {
                    int id = getResources().getIdentifier(
                            viewUtil.value(), "id", mContext.getPackageName());
                    Log.d(null, "viewUtil->id " + id);
                    field.set(this, contentView.findViewById(id));
                }
            }

        } catch (Exception e) {
            //nothing
        }
    }


    protected void viewInject(View contentView) {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                ViewUtil viewUtil = field.getAnnotation(ViewUtil.class);
                if (viewUtil != null) {
                    int id = getResources().getIdentifier(
                            viewUtil.value(), "id", mContext.getPackageName());
                    field.set(this, contentView.findViewById(id));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showProgress(boolean isShow) {
        mActivity.setProgressBarIndeterminateVisibility(isShow);
    }

    protected void showBtnLayout(View view) {
        view.measure(0, 0);
        int height = view.getMeasuredHeight();
        CommonUtil.animForHeight(new EduSohoAnimWrap(view), 0, height, 240);
    }

    protected void startActivityWithBundle(String activityName, Bundle bundle) {
        app.mEngine.runNormalPluginWithBundle(activityName, mActivity, bundle);
    }

    protected void startActivityWithBundleAndResult(String activityName, int request, final Bundle bundle) {
        app.mEngine.runPluginFromFragmentFroResult(activityName, this, request, bundle);
    }

    protected void startActivity(String activityName, PluginRunCallback callback) {
        app.mEngine.runNormalPlugin(activityName, mActivity, callback);
    }

    protected void saveViewState(Bundle savedInstanceState) {
    }

    protected void initView(View view) {
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    protected UtilFactory getUtilFactory() {
        return FactoryManager.getInstance().create(UtilFactory.class);
    }
}
