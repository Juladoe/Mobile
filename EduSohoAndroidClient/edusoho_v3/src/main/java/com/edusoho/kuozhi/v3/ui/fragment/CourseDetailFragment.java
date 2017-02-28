package com.edusoho.kuozhi.v3.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.entity.course.CourseDetail;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.Teacher;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.model.bal.course.CourseMember;
import com.edusoho.kuozhi.v3.model.bal.course.CourseReview;
import com.edusoho.kuozhi.v3.model.bal.course.CourseReviewDetail;
import com.edusoho.kuozhi.v3.ui.AllReviewActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.EduHtmlHttpImageGetter;
import com.edusoho.kuozhi.v3.view.ReviewStarView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhang on 2016/12/8.
 */

public class CourseDetailFragment extends BaseDetailFragment {

    private int mCourseId;
    private CourseDetail mCourseDetail;
    private List<CourseReview> mReviews = new ArrayList<>();
    private ReviewAdapter mAdapter;

    public CourseDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourseId = getArguments().getInt(Const.COURSE_ID);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mAdapter = new ReviewAdapter();
        mLvReview.setAdapter(mAdapter);
        mTvStudent1.setText(R.string.txt_course_student);
        mTvReview1.setText(R.string.txt_course_review);
        mTvPeople1.setText(R.string.txt_suit_people);
        initData();
    }

    protected void initData() {
        setLoadViewStatus(View.VISIBLE);
        CourseDetailModel.getCourseDetail(mCourseId, new ResponseCallbackListener<CourseDetail>() {
            @Override
            public void onSuccess(CourseDetail data) {
                mCourseDetail = data;
                if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                    Log.d("CourseDetailFragment", "activity is finish");
                    return;
                }
                refreshView();
                setLoadViewStatus(View.GONE);
            }

            @Override
            public void onFailure(String code, String message) {
                setLoadViewStatus(View.GONE);
            }
        });
        refreshReview();
        CourseDetailModel.getCourseMember(mCourseId,
                new ResponseCallbackListener<List<CourseMember>>() {
                    @Override
                    public void onSuccess(List<CourseMember> data) {
                        if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                            Log.d("CourseDetailFragment", "activity is finish");
                            return;
                        }
                        initStudent(data);
                    }

                    @Override
                    public void onFailure(String code, String message) {
                    }
                });
    }

    private void initStudent(List<CourseMember> data) {
        View.OnClickListener onClickListener =
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id = String.valueOf(v.getTag());
                        jumpToMember(id);
                    }
                };
        if (data.size() == 0) {
            mTvStudentNone.setVisibility(View.VISIBLE);
        } else {
            mTvStudentNone.setVisibility(View.GONE);
        }
        for (int i = 0; i < 5; i++) {
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_detail_avatar, null, false);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, -1);
            params.weight = 1;
            view.setLayoutParams(params);
            ImageView image = (ImageView) view.findViewById(R.id.iv_avatar_icon);
            TextView txt = (TextView) view.findViewById(R.id.tv_avatar_name);
            if (data.size() > i && data.get(i).user != null) {
                image.setTag(data.get(i).user.id);
                image.setOnClickListener(onClickListener);
                txt.setText(data.get(i).user.nickname);
                ImageLoader.getInstance().displayImage(data.get(i).user.getAvatar(), image, EdusohoApp.app.mAvatarOptions);
            } else {
                txt.setText("");
                image.setImageAlpha(0);
            }
            mStudentIconLayout.addView(view);
        }
    }

    @Override
    protected void refreshView() {
        super.refreshView();
        Course course = mCourseDetail.getCourse();
        mTvTitle.setText(course.title);
        mTvStudentNum.setText(String.format("(%s)", mCourseDetail.getCourse().studentNum));
        mTvTitleDesc.setText(Html.fromHtml(course.about, new EduHtmlHttpImageGetter(mTvTitleDesc, null, true), null));
        if (mCourseDetail.getMember() == null) {
            mPriceLayout.setVisibility(View.VISIBLE);
            if (mCourseDetail.getCourse().vipLevelId == 0) {
                mVipLayout.setVisibility(View.GONE);
            } else {
                mVipLayout.setVisibility(View.VISIBLE);
                mTvVipDesc.setText(String.format("加入%s，免费学习更多课程",
                        mCourseDetail.getVipLevels().size() > course.vipLevelId - 1 ?
                                mCourseDetail.getVipLevels().get(course.vipLevelId - 1).name : ""));
            }
            if (course.price == 0) {
                mTvPriceNow.setText("免费");
                mTvPriceNow.setTextSize(18);
                mTvPriceNow.setTextColor(getResources().getColor(R.color.primary_color));
                mTvPrice1.setVisibility(View.GONE);
            } else {
                mTvPriceNow.setText(String.valueOf(course.price));
                mTvPriceNow.setTextSize(24);
                mTvPriceNow.setTextColor(getResources().getColor(R.color.secondary_color));
                mTvPrice1.setVisibility(View.VISIBLE);
            }
            if (course.originPrice == 0) {
                mTvPriceOld.setVisibility(View.GONE);
            } else {
                if (course.originPrice == course.price) {
                    mTvPriceOld.setVisibility(View.GONE);
                } else {
                    mTvPriceOld.setVisibility(View.VISIBLE);
                    mTvPriceOld.setText("¥" + course.originPrice);
                }
            }
        } else {
            mPriceLayout.setVisibility(View.GONE);
            mVipLayout.setVisibility(View.GONE);
        }
        mTvTitleStudentNum.setText(String.format("%s名学生",
                course.studentNum));
        mReviewStar.setRating((int) course.rating);
        StringBuilder sb = new StringBuilder();
        int length = course.audiences.length;
        if (length == 0) {
            mPeopleLayout.setVisibility(View.GONE);
        } else {
            mPeopleLayout.setVisibility(View.VISIBLE);
            for (int i = 0; i < length; i++) {
                sb.append(course.audiences[i]);
                if (i != length - 1) {
                    sb.append("；");
                }
            }
            mTvPeopleDesc.setText(sb.toString());
        }
        if (course.teachers.length == 0) {
            mTeacherLayout.setVisibility(View.GONE);
        } else {
            mTeacherLayout.setVisibility(View.VISIBLE);
            Teacher teacher = course.teachers[0];
            mTeacherId = String.valueOf(teacher.id);
            ImageLoader.getInstance().displayImage(teacher.getAvatar(), mIvTeacherIcon);
            mTvTeacherName.setText(teacher.nickname);
            mTvTeacherDesc.setText(teacher.title);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void moreStudent() {
        MobclickAgent.onEvent(getActivity(), "courseDetailsPage_introduction_moreCoursesParticipants");
        final String url = String.format(
                Const.MOBILE_APP_URL,
                EdusohoApp.app.schoolHost,
                String.format("main#/studentlist/%s/%s",
                        "courses", mCourseId)
        );
        CoreEngine.create(getContext()).runNormalPlugin("WebViewActivity"
                , getContext(), new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(Const.WEB_URL, url);
                    }
                });
    }

    @Override
    protected void moreReview() {
        EdusohoApp.app.mEngine.runNormalPlugin("AllReviewActivity"
                , getContext(), new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(AllReviewActivity.ID, Integer.valueOf(mCourseId));
                        startIntent.putExtra(AllReviewActivity.TYPE,AllReviewActivity.TYPE_COURSE);
                    }
                });
    }

    class ReviewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mReviews.size() > 5 ? 5 : mReviews.size();
        }

        @Override
        public Object getItem(int position) {
            return mReviews.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_detail_review, null, false);
                viewHolder = new ViewHolder();
                viewHolder.mDesc = (TextView) convertView.findViewById(R.id.tv_review_desc);
                viewHolder.mName = (TextView) convertView.findViewById(R.id.tv_review_name);
                viewHolder.mTime = (TextView) convertView.findViewById(R.id.tv_review_time);
                viewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_review_icon);
                viewHolder.mStar = (ReviewStarView) convertView.findViewById(R.id.v_review_star);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            CourseReview review = mReviews.get(position);
            viewHolder.mDesc.setText(review.getContent());
            viewHolder.mName.setText(review.getUser().nickname);
            viewHolder.mTime.setText(CommonUtil.convertWeekTime(review.getCreatedTime()));
            viewHolder.mStar.setRating((int) Double.parseDouble(review.getRating()));
            ImageLoader.getInstance().displayImage(review.getUser().getMediumAvatar(), viewHolder.mIcon);
            viewHolder.mIcon.setTag(review.getUser().id);
            viewHolder.mIcon.setOnClickListener(mOnClickListener);
            return convertView;
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = v.getTag().toString();
                jumpToMember(id);
            }
        };
    }

    private static ViewHolder viewHolder;

    class ViewHolder {
        ImageView mIcon;
        TextView mName;
        TextView mTime;
        TextView mDesc;
        ReviewStarView mStar;
    }

    private void jumpToMember(String id) {
        final String url = String.format(
                Const.MOBILE_APP_URL,
                EdusohoApp.app.schoolHost,
                String.format("main#/userinfo/%s",
                        id)
        );
        CoreEngine.create(getContext()).runNormalPlugin("WebViewActivity"
                , getActivity(), new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(Const.WEB_URL, url);
                    }
                });
    }

    public void refreshReview() {
        CourseDetailModel.getCourseReviews(mCourseId, "10", "0",
                new ResponseCallbackListener<CourseReviewDetail>() {
                    @Override
                    public void onSuccess(CourseReviewDetail data) {
                        mReviews.clear();
                        int length = data.getData().size();
                        for (int i = 0; i < length; i++) {
                            if (!data.getData().get(i).parentId.equals("0")) {
                                data.getData().remove(i);
                                i--;
                                length--;
                            }
                        }
                        mTvReviewNum.setText(String.format("(%s)", data.getTotal()));
                        if (data.getData().size() == 0) {
                            mReviewNoneLayout.setVisibility(View.VISIBLE);
                            mTvReviewMore.setVisibility(View.GONE);
                        } else {
                            mReviewNoneLayout.setVisibility(View.GONE);
                            mReviews.addAll(data.getData());
                            if (mReviews.size() < 5) {
                                mTvReviewMore.setVisibility(View.GONE);
                            } else {
                                mTvReviewMore.setVisibility(View.VISIBLE);
                                mTvReviewMore.setText("更多评价");
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(String code, String message) {
                    }
                });
    }
}
