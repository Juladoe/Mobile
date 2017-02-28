package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.course.ClassroomReview;
import com.edusoho.kuozhi.v3.model.bal.course.ClassroomReviewDetail;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.ReviewStarView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DF on 2017/2/16.
 */

public class ClassroomReviewAdapter extends BaseAdapter implements View.OnClickListener{

    private int mId;
    private int mPage = 0;
    private Context mContext;
    private List<ClassroomReview> mList;
    private ViewHolder viewHolder;
    private boolean mCanLoad;

    public ClassroomReviewAdapter(Context mContext, int mId) {
        this.mContext = mContext;
        this.mId = mId;
        mList = new ArrayList<>();
    }

    public void setData(List<ClassroomReview> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setCanLoad(boolean mCanLoad) {
        this.mCanLoad = mCanLoad;
    }

    public void addData(List<ClassroomReview> list) {
        this.mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mCanLoad && (position == getCount() - 1)) {
            mPage++;
            mCanLoad = false;
            addItemData();
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_detail_review, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ClassroomReview review = mList.get(position);
        viewHolder.mDesc.setText(review.getContent());
        viewHolder.mName.setText(review.getUser().nickname);
        viewHolder.mTime.setText(CommonUtil.convertWeekTime(review.getCreatedTime()));
        viewHolder.mStar.setRating((int) Double.parseDouble(review.getRating()));
        ImageLoader.getInstance().displayImage(review.getUser().getMediumAvatar(), viewHolder.mIcon,
                ((EdusohoApp) mContext.getApplicationContext()).mAvatarOptions);
        viewHolder.mIcon.setTag(review.getUser().id);
        viewHolder.mIcon.setOnClickListener(this);
        return convertView;
    }

    private void addItemData() {
        CourseDetailModel.getClassroomReviews(mId, String.valueOf(10)
                , String.valueOf(mPage*10), new ResponseCallbackListener<ClassroomReviewDetail>() {
                    @Override
                    public void onSuccess(ClassroomReviewDetail data) {
                        if (data.getData().size() < 10) {
                            mCanLoad = false;
                        } else {
                            mCanLoad = true;
                        }
                        addData(data.getData());
                    }

                    @Override
                    public void onFailure(String code, String message) {

                    }
                });
    }

    public static class ViewHolder{
        private ImageView mIcon;
        private TextView mName;
        private TextView mTime;
        private TextView mDesc;
        private ReviewStarView mStar;

        public ViewHolder(View view) {
            this.mDesc = (TextView) view.findViewById(R.id.tv_review_desc);
            this.mName = (TextView) view.findViewById(R.id.tv_review_name);
            this.mTime = (TextView) view.findViewById(R.id.tv_review_time);
            this.mIcon = (ImageView) view.findViewById(R.id.iv_review_icon);
            this.mStar = (ReviewStarView) view.findViewById(R.id.v_review_star);
        }
    }

    @Override
    public void onClick(View v) {
        String id = v.getTag().toString();
        jumpToMember(id);
    }

    private void jumpToMember(String id) {
        final String url = String.format(
                Const.MOBILE_APP_URL,
                EdusohoApp.app.schoolHost,
                String.format("main#/userinfo/%s",
                        id)
        );
        CoreEngine.create(mContext).runNormalPlugin("WebViewActivity"
                , mContext, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(Const.WEB_URL, url);
                    }
                });
    }
}
