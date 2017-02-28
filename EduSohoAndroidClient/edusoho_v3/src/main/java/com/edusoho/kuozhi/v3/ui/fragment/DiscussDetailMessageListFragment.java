package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.ui.MessageListFragment;
import com.edusoho.kuozhi.v3.entity.lesson.QuestionAnswerAdapter;
import com.edusoho.kuozhi.v3.view.NewTextMessageInputView;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Created by DF on 2017/1/8.
 */

public class DiscussDetailMessageListFragment extends MessageListFragment {

    private View inflate;

    @Override
    protected void initView(View view) {
        mPtrFrame = (PtrClassicFrameLayout) view.findViewById(com.edusoho.kuozhi.imserver.R.id.rotate_header_list_view_frame);
        mMessageListView = (RecyclerView) view.findViewById(com.edusoho.kuozhi.imserver.R.id.listview);
        ViewGroup inputViewGroup = (ViewGroup) view.findViewById(com.edusoho.kuozhi.imserver.R.id.message_input_view);
        mMessageInputView = new NewTextMessageInputView(getActivity());
        inputViewGroup.addView(((View) mMessageInputView));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mMessageListView.setLayoutManager(mLayoutManager);
        mMessageListView.setAdapter(mListAdapter);
        ((QuestionAnswerAdapter) mListAdapter).addHeaderView(inflate, getArguments());
        mMessageListView.setItemAnimator(null);
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mPtrFrame.refreshComplete();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIMessageListPresenter.insertMessageList();
                    }
                }, 350);
            }
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                boolean canDoRefresh = PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
                return canRefresh() && canDoRefresh;
            }
        });
        setEnable(true);
        mMessageInputView.setMessageSendListener(getMessageSendListener());
        mMessageInputView.setMessageControllerListener(getMessageControllerListener());
    }

    @Override
    public void onAttach(Activity activity) {
        mListAdapter = new QuestionAnswerAdapter(getActivity());
        inflate = LayoutInflater.from(getActivity()).inflate(R.layout.thread_discuss_head_layout, null);
        mListAdapter.setMessageListItemController(getMessageListItemClickListener());
        super.onAttach(activity);
    }

}
