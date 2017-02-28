package com.edusoho.kuozhi.v3.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.v3.adapter.NofityListAdapter;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.NotificationProvider;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.model.bal.push.Notify;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.service.message.push.ESDbManager;
import com.edusoho.kuozhi.v3.service.message.push.NotifyDbHelper;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by suju on 16/11/10.
 */
public class NotifyActivity extends ActionBarBaseActivity implements NofityListAdapter.OnItemClickListener<Notify> {

    private RecyclerView mListView;
    private PtrClassicFrameLayout mPtrFrame;
    private NotifyDbHelper mNotifyDbHelper;
    private NofityListAdapter mListAdapter;
    private int mStart = 0;
    private static final int LIMIT = 10;
    private boolean canLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackMode(BACK, "通知中心");
        setContentView(R.layout.activity_nofity_layout);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getNotificationProvider().cancelNotification(Destination.NOTIFY.hashCode());
        IMClient.getClient().getConvManager().clearReadCount(Destination.NOTIFY);
    }

    private void initView() {
        mListView = (RecyclerView) findViewById(R.id.listview);
        mPtrFrame = (PtrClassicFrameLayout) findViewById(R.id.rotate_header_list_view_frame);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setReverseLayout(true);
        mListView.setLayoutManager(linearLayoutManager);
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                frame.refreshComplete();
                final List<Notify> notifyList =  mNotifyDbHelper.getNofityList(mStart, LIMIT);
                if (notifyList.isEmpty()) {
                    canLoad = false;
                    return;
                }

                mListAdapter.addDataList(notifyList);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View childView = mListView.getChildAt(0);
                        int scrollY = childView == null ? -100 : -(childView.getHeight() / 2);
                        mListView.smoothScrollBy(0, scrollY);
                        mStart += notifyList.size();
                    }
                }, 350);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return canLoad && super.checkCanDoRefresh(frame, content, header);
            }
        });

        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkCanAutoLoad(recyclerView);
                }
            }
        });
    }

    private synchronized void checkCanAutoLoad(RecyclerView recyclerView) {
        if (!canLoad || mPtrFrame.isAutoRefresh()) {
            Log.d(TAG, "auto loading");
            return;
        }
        int chileCount = recyclerView.getChildCount();
        View firstView = recyclerView.getChildAt(chileCount - 1);
        if (firstView != null && firstView.getTop() == 0) {
            Log.d(TAG, "auto load");
            mPtrFrame.autoRefresh(true, 200);
        }
    }

    private void initData() {
        School school = getAppSettingProvider().getCurrentSchool();
        mNotifyDbHelper= new NotifyDbHelper(mContext, new ESDbManager(mContext, school.getDomain()));
        List<Notify> notifyList =  mNotifyDbHelper.getNofityList(mStart, LIMIT);
        mStart += notifyList.size();

        mListAdapter = new NofityListAdapter(mContext);
        mListAdapter.addOnItemClickListener(this);
        mListView.setAdapter(mListAdapter);
        mListAdapter.addDataList(notifyList);
    }

    @Override
    public void onItemClick(View view, Notify notify, int position) {
        Map<String, String> contentData = new Gson().fromJson(notify.getContent(), LinkedHashMap.class);
        showLiveActivity(AppUtil.parseInt(contentData.get("courseId")), AppUtil.parseInt(contentData.get("lessonId")));
    }

    private void showLiveActivity(int courseId, int lessonId) {
        Bundle bundle = new Bundle();
        School school = getAppSettingProvider().getCurrentSchool();
        String url = String.format(Const.MOBILE_APP_URL, school.url + "/", String.format(Const.HTML5_LESSON, courseId, lessonId));
        bundle.putString(Const.WEB_URL, url);
        CoreEngine.create(mContext).runNormalPluginWithBundle("WebViewActivity", mContext, bundle);
    }

    public AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    protected NotificationProvider getNotificationProvider() {
        return FactoryManager.getInstance().create(NotificationProvider.class);
    }
}
