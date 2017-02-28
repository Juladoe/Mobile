package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.adapter.FindCardItemAdapter;
import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryCardProperty;
import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryColumn;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * Created by su on 2016/2/19.
 */
public class FindCardView extends LinearLayout {

    private Context mContext;
    private GridView mGridView;
    private TextView mTitleView;
    private TextView tvMore;
    private FindCardItemAdapter mAdapter;
    private SparseArray<Integer> mChildHeightArray;
    private int mChildId;

    public FindCardView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public FindCardView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
        initView();
    }

    protected void initView() {
        setOrientation(LinearLayout.VERTICAL);
        int padding = AppUtil.dp2px(getContext(), 16);
        setPadding(padding, padding, padding, padding);
        setBackgroundColor(Color.WHITE);
        View headView = LayoutInflater.from(getContext()).inflate(R.layout.view_find_card_head_layout, null);
        mTitleView = (TextView) headView.findViewById(R.id.card_title);
        tvMore = (TextView) headView.findViewById(R.id.tv_more);
        addView(headView);

        mGridView = createGridView();
        addView(mGridView);
        mChildHeightArray = new SparseArray<>();
    }

    public void setDiscoveryCardEntity(DiscoveryColumn discoveryColumn) {
        this.mChildId = discoveryColumn.id;
        mTitleView.setText(discoveryColumn.title);
        setData(discoveryColumn.data);
    }

    public void setMoreClickListener(String orderType, String type, int categoryId) {
        final String url;
        switch (type) {
            case "course":
                url = String.format(
                        Const.MOBILE_APP_URL,
                        EdusohoApp.app.schoolHost,
                        String.format(Const.MOBILE_WEB_COURSES, categoryId, orderType)
                );
                break;
            case "live":
                url = String.format(
                        Const.MOBILE_APP_URL,
                        EdusohoApp.app.schoolHost,
                        String.format(Const.MOBILE_WEB_LIVE_COURSES, categoryId, orderType)
                );
                break;
            case "classroom":
            default:
                url = String.format(
                        Const.MOBILE_APP_URL,
                        EdusohoApp.app.schoolHost,
                        String.format(Const.MOBILE_WEB_CLASSROOMS, categoryId, orderType)
                );
        }

        tvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "find_moreButton");
                EdusohoApp.app.mEngine.runNormalPlugin("WebViewActivity", mContext, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(Const.WEB_URL, url);
                    }
                });
            }
        });
    }

    protected GridView createGridView() {
        GridView gridView = new GridView(getContext());

        gridView.setColumnWidth(getContext().getResources().getDimensionPixelSize(R.dimen.card_grid_width));
        gridView.setNumColumns(2);
        gridView.setVerticalSpacing(0);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.setHorizontalScrollBarEnabled(false);
        return gridView;
    }

    public void setData(List<DiscoveryCardProperty> data) {
        mAdapter.setData(data);
        drawGridView();
    }

    private void drawGridView() {
        int totalHeight = 0, childHeight = 0;
        totalHeight = mChildHeightArray.get(mChildId, 0);
        if (totalHeight == 0) {
            int count = mAdapter.getCount();
            count = count % 2 == 0 ? count / 2 : count / 2 + 1;

            View child = mAdapter.getView(0, null, mGridView);
            child.measure(0, 0);
            childHeight = child.getMeasuredHeight();
            totalHeight = childHeight * count;
            mChildHeightArray.put(mChildId, totalHeight);
        }

        ViewGroup.LayoutParams lp = mGridView.getLayoutParams();
        lp.height = totalHeight;
        mGridView.setLayoutParams(lp);
    }

    public void setAdapter(ListAdapter adapter) {
        mAdapter = (FindCardItemAdapter) adapter;
        mGridView.setAdapter(adapter);
    }

    public int getCardViewListSize() {
        return mAdapter.getCount();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mChildHeightArray.clear();
    }
}
