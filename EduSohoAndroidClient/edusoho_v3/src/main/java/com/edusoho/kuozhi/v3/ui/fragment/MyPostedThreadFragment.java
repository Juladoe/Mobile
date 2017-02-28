package com.edusoho.kuozhi.v3.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.MyThreadAdapter;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PromiseCallback;
import com.edusoho.kuozhi.v3.model.bal.thread.MyThreadEntity;
import com.edusoho.kuozhi.v3.model.provider.MyThreadProvider;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.Promise;
import com.edusoho.kuozhi.v3.view.EduSohoDivederLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by melomelon on 16/2/26.
 */
public class MyPostedThreadFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private MyThreadAdapter mAdapter;
    private EduSohoDivederLine mDividerLine;
    private TextView mEmptyTv;
    private FrameLayout mLoading;

    private MyThreadProvider mProvider;
    private List mDataList;

    public MyPostedThreadFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.my_posted_thread_fragment_layout);
    }

    @Override
    protected void initView(View view) {
        mEmptyTv = (TextView) view.findViewById(R.id.empty_posted_thread);
        mLoading = (FrameLayout) view.findViewById(R.id.my_posted_thread_loading);
        mLoading.setVisibility(View.VISIBLE);

        mDividerLine = new EduSohoDivederLine(EduSohoDivederLine.VERTICAL);
        mDividerLine.setColor(getResources().getColor(R.color.material_grey));
        mDividerLine.setSize(1);
        mDividerLine.setMarginLeft(24);
        mDividerLine.setMarginRight(24);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_posted_thread_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(mDividerLine);

        initData();
    }

    public void initData() {
        mDataList = new ArrayList();

        loadPostedThread().then(new PromiseCallback() {
            @Override
            public Promise invoke(Object obj) {

                if (mDataList.size() == 0) {
                    mRecyclerView.setVisibility(View.GONE);
                    mEmptyTv.setVisibility(View.VISIBLE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEmptyTv.setVisibility(View.GONE);
                }

                mLoading.setVisibility(View.GONE);
                return null;
            }
        });

        mAdapter = new MyThreadAdapter(mContext, app, MyThreadAdapter.POST_THREAD);
        mRecyclerView.setAdapter(mAdapter);
    }

    public Promise loadPostedThread() {
        final Promise promise = new Promise();

        RequestUrl requestUrl = app.bindNewUrl(Const.MY_POSTED_THREADS, true);
        StringBuffer stringBuffer = new StringBuffer(requestUrl.url);
        stringBuffer.append("?start=0&limit=10000/");
        requestUrl.url = stringBuffer.toString();

        mProvider = new MyThreadProvider(mContext);
        mProvider.getMyPostedThread(requestUrl).success(new NormalCallback<MyThreadEntity[]>() {
            @Override
            public void success(MyThreadEntity[] entities) {

                mDataList.addAll(Arrays.asList(entities));
                mAdapter.addDataList(mDataList);
                promise.resolve(mDataList);
            }

        });

        return promise;
    }
}
