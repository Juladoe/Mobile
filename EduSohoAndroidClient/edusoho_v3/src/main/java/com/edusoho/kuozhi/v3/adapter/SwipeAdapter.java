package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.listener.AvatarLoadingListener;
import com.edusoho.kuozhi.v3.model.bal.push.New;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.view.EduBadgeView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by JesseHuang on 15/6/7.
 */
public class SwipeAdapter extends BaseAdapter {
    private Context mContext;
    private int mLayoutId;
    private List<New> mList;
    private DisplayImageOptions mOptions;
    private int mTitleRestWidth;

    public SwipeAdapter(Context ctx, int id) {
        mContext = ctx;
        mLayoutId = id;
        mList = new CopyOnWriteArrayList<>();
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).showImageForEmptyUri(R.drawable.user_avatar).
                showImageOnFail(R.drawable.user_avatar).showImageOnLoading(null).build();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Bundle bundle = new Bundle();
        bundle.putInt("badge", getAllUnreadNum());
        MessageEngine.getInstance().sendMsg(Const.BADGE_UPDATE, bundle);
    }

    private int getAllUnreadNum() {
        int total = 0;
        for (New item : mList) {
            total += item.unread;
        }
        return total;
    }

    public void update(List<New> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItem(New newModel) {
        mList.add(0, newModel);
        notifyDataSetChanged();
    }

    public void updateItem(New newModel) {
        int size = mList.size();
        for (int i = 0; i < size; i++) {
            New item = mList.get(i);
            if (item.fromId == newModel.fromId) {
                mList.remove(i);
                mList.add(i, newModel);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void setItemToTop(New newModel) {
        Iterator<New> iterator = mList.iterator();
        while (iterator.hasNext()) {
            New item = iterator.next();
            if (item.fromId == newModel.fromId && item.type.equals(newModel.type)) {
                mList.remove(item);
                mList.add(0, newModel);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public New getItem(int position) {
        if (position < 0 || position > mList.size()) {
            return null;
        }
        return mList.get(position) != null ? mList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mLayoutId, null);
            viewHolder = new ViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final New item = mList.get(position);
        ImageLoader.getInstance().displayImage(item.imgUrl, viewHolder.ivAvatar, mOptions, new AvatarLoadingListener(item.type));
        viewHolder.bvUnread.setBadgeCount(item.unread);
        calculateTitleMaxWidth();
        viewHolder.tvTitle.setMaxWidth(mTitleRestWidth);
        switch (item.type) {
            case PushUtil.ChatUserType.TEACHER:
                viewHolder.tvRole.setVisibility(View.VISIBLE);
                viewHolder.tvRole.setText("老师");
                viewHolder.tvRole.setBackgroundResource(R.drawable.role_teacher_bg);
                break;
            case PushUtil.ChatUserType.CLASSROOM:
                viewHolder.tvRole.setVisibility(View.VISIBLE);
                viewHolder.tvRole.setText("班级");
                viewHolder.tvRole.setBackgroundResource(R.drawable.role_classroom_bg);
                break;
            case PushUtil.ChatUserType.COURSE:
                viewHolder.tvRole.setVisibility(View.VISIBLE);
                viewHolder.tvRole.setText("课程");
                viewHolder.tvRole.setBackgroundResource(R.drawable.role_course_bg);
                break;
            default:
                viewHolder.tvRole.setVisibility(View.GONE);
        }
        viewHolder.tvParent.setVisibility(item.parentId == 0 ? View.GONE : View.VISIBLE);
        viewHolder.tvTitle.setText(item.title);
        viewHolder.tvContent.setText(item.content);
        viewHolder.tvPostTime.setText(AppUtil.convertMills2Date(item.createdTime));
        return convertView;
    }

    public void removeItem(int position) {
        mList.remove(position);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        public ImageView ivAvatar;
        public EduBadgeView bvUnread;
        public TextView tvTitle;
        public TextView tvContent;
        public TextView tvPostTime;
        public View viewAvatar;
        public TextView tvRole;
        public TextView tvParent;

        public ViewHolder(View view) {
            ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
            viewAvatar = view.findViewById(R.id.view_avatar);
            bvUnread = (EduBadgeView) view.findViewById(R.id.bv_unread);
            bvUnread.setTargetView(viewAvatar);
            bvUnread.setBadgeGravity(Gravity.RIGHT | Gravity.TOP);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvContent = (TextView) view.findViewById(R.id.tv_content);
            tvPostTime = (TextView) view.findViewById(R.id.tv_post_time);
            tvRole = (TextView) view.findViewById(R.id.tv_role);
            tvParent = (TextView) view.findViewById(R.id.tv_parent);
            view.setTag(this);
        }
    }

    private void calculateTitleMaxWidth() {
        if (mTitleRestWidth == 0) {
            //时间
            float timeWidth = 5 * mContext.getResources().getDimension(R.dimen.new_item_time_size);
            //标签
            float roleWidth = 2 * mContext.getResources().getDimensionPixelSize(R.dimen.x_small_font_size);
            //头像
            float avatarWidth = mContext.getResources().getDimension(R.dimen.head_icon_news_item);
            //根据layout计算  10*2+6*2+4+2*6+2*8
            float marginWidth = CommonUtil.dip2px(mContext, 2 * 6 + 2 * 8 + 4 + 2 * 6 + 2 * 10);
            mTitleRestWidth = EdusohoApp.screenW - (int) Math.ceil(timeWidth + roleWidth + avatarWidth + marginWidth);
        }
    }
}


