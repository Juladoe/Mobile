package com.edusoho.kuozhi.v3.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.plugin.ShareTool;
import com.edusoho.kuozhi.v3.ui.fragment.mine.MineFragment;
import com.edusoho.kuozhi.v3.ui.fragment.mine.MyFavoriteFragment;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.CourseUtil;
import com.edusoho.kuozhi.v3.view.dialog.MoreDialog;
import com.edusoho.kuozhi.v3.view.dialog.SureDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JesseHuang on 2017/2/7.
 */

public class MyFavoriteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY = 0;
    private static final int NOT_EMPTY = 1;
    private int mCurrentDataStatus;

    private List<Course> courseList;
    private Context mContext;

    public MyFavoriteAdapter(Context context) {
        courseList = new ArrayList<>();
        mContext = context;
    }

    public void setData(List<Course> list) {
        courseList.clear();
        courseList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mCurrentDataStatus;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == NOT_EMPTY) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_collect, parent, false);
            return new MyFavoriteFragment.FavoriteViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_empty, parent, false);
            return new MineFragment.EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (mCurrentDataStatus == NOT_EMPTY) {
            final Course course = courseList.get(position);
            MyFavoriteFragment.FavoriteViewHolder favoriteViewHolder = (MyFavoriteFragment.FavoriteViewHolder) viewHolder;
            ImageLoader.getInstance().displayImage(course.getLargePicture()
                    , favoriteViewHolder.ivPic, EdusohoApp.app
                            .mOptions);
            favoriteViewHolder.tvAddNum.setText(String.format("%s人参与", course.studentNum));
            favoriteViewHolder.tvTitle.setText(String.valueOf(course.title));
            favoriteViewHolder.recyclerViewItem.setTag(course.id);
            favoriteViewHolder.recyclerViewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EdusohoApp.app.mEngine.runNormalPlugin("CourseActivity"
                            , mContext, new PluginRunCallback() {
                                @Override
                                public void setIntentDate(Intent startIntent) {
                                    startIntent.putExtra(Const.COURSE_ID, course.id);
                                }
                            });
                }
            });
            favoriteViewHolder.tvMore.setTag(course);
            favoriteViewHolder.tvMore.setOnClickListener(mMoreClickListener);
            if (course.type.equals("live")) {
                favoriteViewHolder.layoutLive.setVisibility(View.VISIBLE);
                if (course.liveState == 1) {
                    favoriteViewHolder.tvLive.setText(R.string.lesson_living);
                    favoriteViewHolder.tvLiveIcon.setVisibility(View.VISIBLE);
                } else {
                    favoriteViewHolder.tvLive.setText("直播");
                    favoriteViewHolder.tvLiveIcon.setVisibility(View.GONE);
                }
            } else {
                favoriteViewHolder.layoutLive.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (courseList != null && courseList.size() != 0) {
            mCurrentDataStatus = NOT_EMPTY;
            return courseList.size();
        }
        mCurrentDataStatus = EMPTY;
        return 1;
    }

    private View.OnClickListener mMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Course course = (Course) v.getTag();
            MoreDialog dialog = new MoreDialog(mContext);
            dialog.init("取消收藏", new MoreDialog.MoreCallBack() {
                @Override
                public void onMoveClick(View v, final Dialog dialog) {
                    new SureDialog(mContext).init("是否确定取消收藏！", new SureDialog.CallBack() {
                        @Override
                        public void onSureClick(View v, final Dialog dialog2) {
                            CourseUtil.uncollectCourse(course.id
                                    , new CourseUtil.OnCollectSuccessListener() {
                                        @Override
                                        public void onCollectSuccess() {
                                            CommonUtil.shortToast(mContext, "取消收藏成功");
                                            courseList.remove(course);
                                            notifyDataSetChanged();
                                            dialog.dismiss();
                                            dialog2.dismiss();
                                        }
                                    }
                            );
                        }

                        @Override
                        public void onCancelClick(View v, Dialog dialog2) {
                            dialog2.dismiss();
                        }
                    }).show();
                }

                @Override
                public void onShareClick(View v, Dialog dialog) {
                    final ShareTool shareTool =
                            new ShareTool(mContext
                                    , EdusohoApp.app.host + "/course/" + course.id
                                    , course.title
                                    , course.about.length() > 20 ?
                                    course.about.substring(0, 20)
                                    : course.about
                                    , course.middlePicture);
                    new Handler((mContext.getMainLooper())).post(new Runnable() {
                        @Override
                        public void run() {
                            shareTool.shardCourse();
                        }
                    });
                    dialog.dismiss();
                }

                @Override
                public void onCancelClick(View v, Dialog dialog) {
                    dialog.dismiss();
                }
            }).show();
        }
    };
}
