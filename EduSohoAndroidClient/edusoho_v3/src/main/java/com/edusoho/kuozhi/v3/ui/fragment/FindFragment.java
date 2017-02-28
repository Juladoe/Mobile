package com.edusoho.kuozhi.v3.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.adapter.FindListAdapter;
import com.edusoho.kuozhi.v3.adapter.SchoolBannerAdapter;
import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryColumn;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.discovery.DiscoveryModel;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.provider.SystemProvider;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.SchoolBanner;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.DiscoveryLoadHelper;
import com.edusoho.kuozhi.v3.view.EduSohoBanner;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by JesseHuang on 15/4/26.
 */
public class FindFragment extends BaseFragment {

    private static final String DEFAULT_DISCOVERY_TITLE = "推荐课程";

    private SystemProvider mSystemProvider;

    private ListView mListView;
    private View mLoadView;
    private PtrClassicFrameLayout mFindContentLayout;
    private EduSohoBanner mFindBannerView;
    private FindListAdapter mFindListAdapter;
    private DiscoveryModel mDiscoveryModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_find_layout);
        mSystemProvider = ModelProvider.initProvider(mContext, SystemProvider.class);
        mDiscoveryModel = new DiscoveryModel();
    }

    private float getViewScale() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth() / 750f;
    }

    private void addBannerView() {
        mFindBannerView = (EduSohoBanner) LayoutInflater.from(mContext).inflate(R.layout.find_listview_head_layout, null);
        int bannerHeight = (int) (300 * getViewScale());
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bannerHeight);
        mFindBannerView.setLayoutParams(lp);
        mListView.addHeaderView(mFindBannerView);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mFindContentLayout = (PtrClassicFrameLayout) view.findViewById(R.id.find_content);
        mListView = (ListView) view.findViewById(R.id.listview);
        mLoadView = view.findViewById(R.id.find_load_layout);
        addBannerView();
        getDiscoveryData();
        initSchoolBanner(false);
        mFindListAdapter = new FindListAdapter(mContext);
        mFindContentLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                initSchoolBanner(true);
                mFindListAdapter.clear();
                getDiscoveryData();
                mFindContentLayout.refreshComplete();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return super.checkCanDoRefresh(frame, content, header);
            }
        });
    }

    protected void initSchoolBanner(final boolean isUpdate) {
        RequestUrl requestUrl = app.bindUrl(Const.SCHOOL_BANNER, false);
        mSystemProvider.getSchoolBanners(requestUrl).success(new NormalCallback<List<SchoolBanner>>() {
            @Override
            public void success(List<SchoolBanner> schoolBanners) {
                SchoolBannerAdapter adapter;
                if (isUpdate) {
                    mFindBannerView.update(schoolBanners);
                } else {
                    adapter = new SchoolBannerAdapter(
                            mActivity, schoolBanners);
                    mFindBannerView.setAdapter(adapter);
                    mFindBannerView.setCurrentItem(1, false);
                    mFindBannerView.setupAutoPlay();
                }
            }
        });
    }

    private void getDiscoveryData() {
        mLoadView.setVisibility(View.VISIBLE);
        mDiscoveryModel.getDiscoveryColumns(new ResponseCallbackListener<List<DiscoveryColumn>>() {
            @Override
            public void onSuccess(final List<DiscoveryColumn> discoveryColumnList) {
                mFindListAdapter = new FindListAdapter(mContext);
                mListView.setAdapter(mFindListAdapter);
                DiscoveryLoadHelper loadHelper = new DiscoveryLoadHelper();
                if (discoveryColumnList.size() != 0) {
                    int size = discoveryColumnList.size();
                    for (int i = 0; i < size; i++) {
                        loadHelper.addTask(discoveryColumnList.get(i));
                    }
                } else {
                    DiscoveryColumn discoveryColumn = new DiscoveryColumn();
                    discoveryColumn.title = DEFAULT_DISCOVERY_TITLE;
                    discoveryColumn.type = "course";
                    loadHelper.addTask(discoveryColumn);
                }
                loadHelper.invoke(new DiscoveryLoadHelper.ResultCallback() {
                    @Override
                    public void onResult(List<DiscoveryColumn> discoveryColumns) {
                        mLoadView.setVisibility(View.GONE);
                        mFindListAdapter.addDataList(discoveryColumns);
                    }
                });
            }

            @Override
            public void onFailure(String code, String message) {
                mLoadView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.find_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.find_search) {
            MobclickAgent.onEvent(mContext, "find_searchButton");
            EdusohoApp.app.mEngine.runNormalPlugin("WebViewActivity", mContext, new PluginRunCallback() {
                @Override
                public void setIntentDate(Intent startIntent) {
                    String url = String.format(Const.MOBILE_APP_URL, EdusohoApp.app.schoolHost, Const.MOBILE_SEARCH);
                    startIntent.putExtra(Const.WEB_URL, url);
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}
