package com.edusoho.kuozhi.homework.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.v3.util.AppUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by howzhi on 14-10-8.
 */
public class EssayImageSelectAdapter extends BaseAdapter {

    public final static int SEL_IMG = 0010;
    public final static int CAMERA_IMG = 0011;
    public final static int SHOW_IMG = 0012;

    private LayoutInflater inflater;
    private Context mContext;
    private List<GridViewItem> mList;

    public EssayImageSelectAdapter(Context context)
    {
        mContext = context;
        mList = getDefaultItems();
        inflater = LayoutInflater.from(context);
    }

    private List<GridViewItem> getDefaultItems() {
        ArrayList<GridViewItem> list = new ArrayList<GridViewItem>();

        GridViewItem item = new GridViewItem();
        item.icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.hw_essay_imgselect);
        item.type = SEL_IMG;
        item.path = "hw_essay_imgselect";
        list.add(item);
        item = new GridViewItem();
        item.icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.hw_essay_imgcamera);
        item.type = CAMERA_IMG;
        item.path = "hw_essay_imgcamera";
        list.add(item);
        return list;
    }

    public void insertItem(Bitmap bitmap, String path) {
        GridViewItem item = new GridViewItem();
        item.icon = bitmap;
        item.type = SHOW_IMG;
        item.path = path;

        mList.add(0, item);
        notifyDataSetChanged();
    }

    public List<GridViewItem> getItems() {
        return mList;
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
            ImageView imageView = (ImageView) inflater.inflate(R.layout.homework_essay_imagesel_item, null);
            view = imageView;
            holder = new ViewHolder();
            holder.itemView = imageView;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        GridViewItem item = mList.get(index);
        holder.itemView.setImageBitmap(item.icon);
        int size = AppUtil.dp2px(mContext, 40);
        GridView.LayoutParams layoutParams = new AbsListView.LayoutParams(size, size);
        holder.itemView.setLayoutParams(layoutParams);
        return view;
    }

    private class ViewHolder
    {
        public ImageView itemView;
    }

    public static class GridViewItem {

        public String path;
        public Bitmap icon;
        public int type;
    }
}