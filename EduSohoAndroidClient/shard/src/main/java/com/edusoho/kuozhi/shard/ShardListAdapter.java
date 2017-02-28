package com.edusoho.kuozhi.shard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by howzhi on 14-10-8.
 */
public class ShardListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private int mResouce;
    private Context mContext;
    private List<ListData> mList;

    public ShardListAdapter(
            Context context, List<ListData> list, int resource)
    {
        mList = list;
        mContext = context;
        mResouce = resource;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int index) {
        return mList.get(index);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int index, View view, ViewGroup vg) {
        final ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(mResouce, null);
            holder = new ViewHolder();

            holder.shardIcon = (ImageView) view.findViewById(R.id.shard_icon);
            holder.shardText = (TextView) view.findViewById(R.id.shard_text);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ListData listItem = mList.get(index);

        holder.shardIcon.setImageDrawable(listItem.icon);
        holder.shardText.setText(listItem.text);
        return view;
    }

    private class ViewHolder
    {
        public ImageView shardIcon;
        public TextView shardText;
    }
}
