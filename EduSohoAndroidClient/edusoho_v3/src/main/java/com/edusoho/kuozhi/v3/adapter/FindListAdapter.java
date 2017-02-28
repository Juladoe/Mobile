package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryColumn;
import com.edusoho.kuozhi.v3.view.FindCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by su on 2016/2/24.
 */
public class FindListAdapter extends BaseAdapter {

    private Context mContext;
    private List<DiscoveryColumn> mList;

    public FindListAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
    }

    public void addDataList(List<DiscoveryColumn> findCardEntity) {
        mList.addAll(findCardEntity);

        notifyDataSetChanged();
    }

    public void addData(DiscoveryColumn findCardEntity) {
        mList.add(findCardEntity);
        notifyDataSetChanged();
    }

    public void clear() {
        mList.clear();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new FindCardView(mContext);
            ((FindCardView) convertView).setAdapter(new FindCardItemAdapter(mContext));
        }

        DiscoveryColumn discoveryColumn = mList.get(position);
        FindCardView findCardView = (FindCardView) convertView;
        findCardView.setDiscoveryCardEntity(discoveryColumn);
        findCardView.setMoreClickListener(discoveryColumn.orderType == null ? "recommend" : discoveryColumn.orderType, discoveryColumn.type, discoveryColumn.categoryId);
        return convertView;
    }
}
