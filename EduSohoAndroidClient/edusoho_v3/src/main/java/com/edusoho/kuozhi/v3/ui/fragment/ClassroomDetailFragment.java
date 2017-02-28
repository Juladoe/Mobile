package com.edusoho.kuozhi.v3.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.course.ClassroomDetail;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.Classroom;
import com.edusoho.kuozhi.v3.model.bal.Teacher;
import com.edusoho.kuozhi.v3.model.bal.course.ClassroomMember;
import com.edusoho.kuozhi.v3.model.bal.course.ClassroomReview;
import com.edusoho.kuozhi.v3.model.bal.course.ClassroomReviewDetail;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.ui.AllReviewActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.EduHtmlHttpImageGetter;
import com.edusoho.kuozhi.v3.view.ReviewStarView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhang on 2016/12/8.
 */

public class ClassroomDetailFragment extends BaseDetailFragment {

    private int mClassroomId;
    private ClassroomDetail mClassroomDetail;
    private List<ClassroomReview> mReviews = new ArrayList<>();
    private ReviewAdapter mAdapter;

    public ClassroomDetailFragment() {
    }

    public void setClassroomId(int classroomId) {
        this.mClassroomId = classroomId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClassroomId = getArguments().getInt(Const.CLASSROOM_ID);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mAdapter = new ReviewAdapter();
        mVipLayout.setVisibility(View.GONE);
        mLvReview.setAdapter(mAdapter);
        mTvTeacher.setText(R.string.classroom_teacher_txt);
        mTvStudent1.setText(R.string.txt_classroom_student);
        mTvReview1.setText(R.string.txt_classroom_review);
        mTvPeople1.setText(R.string.txt_provision_services);
        initData();
    }

    protected void initData() {
        setLoadViewStatus(View.VISIBLE);
        CourseDetailModel.getClassroomDetail(mClassroomId, new ResponseCallbackListener<ClassroomDetail>() {
            @Override
            public void onSuccess(ClassroomDetail data) {
                setLoadViewStatus(View.GONE);
                mClassroomDetail = data;
                refreshView();
            }

            @Override
            public void onFailure(String code, String message) {
                setLoadViewStatus(View.GONE);
            }
        });
        CourseDetailModel.getClassroomReviews(mClassroomId, "5", "0",
                new ResponseCallbackListener<ClassroomReviewDetail>() {
                    @Override
                    public void onSuccess(ClassroomReviewDetail data) {
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
        CourseDetailModel.getClassroomMember(mClassroomId,
                new ResponseCallbackListener<List<ClassroomMember>>() {
                    @Override
                    public void onSuccess(List<ClassroomMember> data) {
                        initStudent(data);
                    }

                    @Override
                    public void onFailure(String code, String message) {

                    }
                });
    }

    private void initStudent(List<ClassroomMember> data) {
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
            if (data.size() > i) {
                image.setTag(data.get(i).userId);
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
        Classroom classRoom = mClassroomDetail.getClassRoom();
        mTvTitle.setText(classRoom.title);
        mTvStudentNum.setText(String.format("(%s)", mClassroomDetail.getClassRoom().studentNum));
        mTvTitleDesc.setText(Html.fromHtml(classRoom.about, new EduHtmlHttpImageGetter(mTvTitleDesc, null, true), null));
        mTvStudentNum.setText(String.format("(%s)", mClassroomDetail.getClassRoom().studentNum));
        if (mClassroomDetail.getMember() == null) {
            mPriceLayout.setVisibility(View.VISIBLE);
            if (classRoom.vipLevelId == 0) {
                mVipLayout.setVisibility(View.GONE);
            } else {
                mVipLayout.setVisibility(View.VISIBLE);
                mTvVipDesc.setText(String.format("加入%s，免费学习更多课程",
                        mClassroomDetail.getVipLevels().size() > classRoom.vipLevelId - 1 ?
                                mClassroomDetail.getVipLevels().get(classRoom.vipLevelId - 1).name : ""));
            }
            if (classRoom.price == 0) {
                mTvPriceNow.setText(R.string.txt_free);
                mTvPriceNow.setTextSize(18);
                mTvPriceNow.setTextColor(getResources().getColor(R.color.primary_color));
                mTvPrice1.setVisibility(View.GONE);
            } else {
                mTvPriceNow.setText(String.valueOf(classRoom.price));
                mTvPriceNow.setTextSize(24);
                mTvPriceNow.setTextColor(getResources().getColor(R.color.secondary_color));
                mTvPrice1.setVisibility(View.VISIBLE);
            }
            mTvPriceOld.setVisibility(View.GONE);
        } else {
            mPriceLayout.setVisibility(View.GONE);
            mVipLayout.setVisibility(View.GONE);
        }
        mTvTitleStudentNum.setText(String.format("%s名学生",
                classRoom.studentNum));
        try {
            mReviewStar.setRating((int) Double.parseDouble(classRoom.rating));
        } catch (Exception e) {

        }
        if (classRoom.service == null || classRoom.service.length == 0) {
            mPeopleLayout.setVisibility(View.GONE);
        } else {
            mPeopleLayout.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (String str : classRoom.service) {
                switch (str) {
                    case "homeworkReview":
                        sb.append(getResources().getString(R.string.txt_services_homeworkreview));
                        break;
                    case "testpaperReview":
                        sb.append(getResources().getString(R.string.txt_services_testpaper));
                        break;
                    case "teacherAnswer":
                        sb.append(getResources().getString(R.string.txt_services_teacheranswer));
                        break;
                    case "liveAnswer":
                        sb.append(getResources().getString(R.string.txt_services_liveanswer));
                        break;
                    case "event":
                        sb.append(getResources().getString(R.string.txt_services_event));
                        break;
                    case "workAdvise":
                        sb.append(getResources().getString(R.string.txt_services_workadvise));
                        break;
                }
                sb.append("\n");
            }
            mTvPeopleDesc.setText(sb.substring(0, sb.length() - 1));
        }
        getTeacherView(mClassroomId);
    }

    public void getTeacherView(int headTeacherId) {
        CourseDetailModel.getTeacher(headTeacherId, new ResponseCallbackListener<Teacher[]>() {
            @Override
            public void onSuccess(Teacher[] data) {
                if (data.length == 0) {
                    mTeacherLayout.setVisibility(View.GONE);
                } else {
                    mTeacherLayout.setVisibility(View.VISIBLE);
                    mTeacherId = String.valueOf(data[0].id);
                    ImageLoader.getInstance().displayImage(data[0].smallAvatar.split("\\?")[0], mIvTeacherIcon, ((EdusohoApp) getActivity().getApplication()).mAvatarOptions);
                    mTvTeacherName.setText(data[0].nickname);
                    mTvTeacherDesc.setText(data[0].title);
                }
            }

            @Override
            public void onFailure(String code, String message) {
                mTeacherLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void moreStudent() {
        final String url = String.format(
                Const.MOBILE_APP_URL,
                EdusohoApp.app.schoolHost,
                String.format("main#/studentlist/%s/%s",
                        "classroom", mClassroomId)
        );
        EdusohoApp.app.mEngine.runNormalPlugin("WebViewActivity"
                , EdusohoApp.app.mActivity, new PluginRunCallback() {
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
                        startIntent.putExtra(AllReviewActivity.ID, Integer.valueOf(mClassroomId));
                        startIntent.putExtra(AllReviewActivity.TYPE, AllReviewActivity.TYPE_CLASSROOM);
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
            ClassroomReview review = mReviews.get(position);
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
        EdusohoApp.app.mEngine.runNormalPlugin("WebViewActivity"
                , EdusohoApp.app.mActivity, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(Const.WEB_URL, url);
                    }
                });
    }
}
