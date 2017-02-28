package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.push.NewsCourseEntity;
import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;
import com.edusoho.kuozhi.v3.ui.ThreadDiscussActivity;
import com.edusoho.kuozhi.v3.ui.ThreadDiscussChatActivity;
import com.edusoho.kuozhi.v3.ui.chat.AbstractIMChatActivity;
import com.edusoho.kuozhi.v3.ui.fragment.test.TestpaperResultFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by melomelon on 15/12/10.
 */
public class StudyProcessRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int COURSE_SUMMARY = 0;
    public static final int LESSON_TITLE = 1;
    public static final int COST_TIME = 2;
    public static final int NORMAL_NOTI = 3;
    public static final int INTENT_NOTI = 4;

    private List<NewsCourseEntity> mDataList;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private EdusohoApp mApp;

    private View.OnClickListener summaryListener;


    public StudyProcessRecyclerAdapter(Context context, List list, EdusohoApp app) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mDataList = list;
        this.mApp = app;
    }

    @Override
    public int getItemViewType(int position) {
        NewsCourseEntity entity = mDataList.get(position);
        String type = entity.getBodyType();

        if (type.equals("testpaper.reviewed") || type.equals("homework.reviewed") || type.equals("question.answered")) {
            return INTENT_NOTI;
        } else if (type.equals("course.lessonTitle")) {
            return LESSON_TITLE;
        } else if (type.equals("course.summary")) {
            return COURSE_SUMMARY;
        } else if (type.equals("lesson.costTime")) {
            return COST_TIME;
        } else {
            return NORMAL_NOTI;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case COURSE_SUMMARY:
                return new LessonSummaryViewHolder(mLayoutInflater.inflate(R.layout.item_study_process_lesson_summary, parent, false));

            case LESSON_TITLE:
                return new LessonTitleViewHolder(mLayoutInflater.inflate(R.layout.item_study_process_lesson_title, parent, false));

            case COST_TIME:
                return new CostTimeViewHolder(mLayoutInflater.inflate(R.layout.item_study_process_cost_time, parent, false));

            case NORMAL_NOTI:
                return new NormalNotificationViewHolder(mLayoutInflater.inflate(R.layout.item_study_process_notification, parent, false));

            case INTENT_NOTI:
                return new IntentNotificationViewHolder(mLayoutInflater.inflate(R.layout.item_study_process_notification, parent, false));

            default:
                return null;

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof LessonSummaryViewHolder) {
            NewsCourseEntity entity = mDataList.get(position);
            ((LessonSummaryViewHolder) holder).summaryCourseIntroduction.setText("课程简介：" + AppUtil.removeHtmlSpace(AppUtil.removeHtmlSpan(entity.getContent())));
            ImageLoader.getInstance().displayImage(entity.getImage(), ((LessonSummaryViewHolder) holder).summaryCourseImage);
            ((LessonSummaryViewHolder) holder).summaryCourseTitle.setText(entity.getTitle());
            ((LessonSummaryViewHolder) holder).summaryCourseTeacher.setText(entity.getTeacher());
            if (entity.getTeacher().equals("暂无教师")) {
                ((LessonSummaryViewHolder) holder).summaryTeacherTag.setVisibility(View.GONE);
            } else {
                ((LessonSummaryViewHolder) holder).summaryTeacherTag.setVisibility(View.VISIBLE);
            }
            ((LessonSummaryViewHolder) holder).summaryFrame.setOnClickListener(summaryListener);
        }

        if (holder instanceof LessonTitleViewHolder) {
            final NewsCourseEntity entity = mDataList.get(position);
            String lessonTitle = entity.getContent();
            if (entity.getIsLessonfinished()) {
                ((LessonTitleViewHolder) holder).lessonState.setImageResource(R.drawable.icon_lesson_state_finished);
            } else {
                ((LessonTitleViewHolder) holder).lessonState.setImageResource(R.drawable.icon_lesson_state_half);
            }
            ((LessonTitleViewHolder) holder).lessonTitle.setText(lessonTitle);
            ((LessonTitleViewHolder) holder).lessonTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mApp.mEngine.runNormalPlugin("WebViewActivity", mContext, new PluginRunCallback() {
                        @Override
                        public void setIntentDate(Intent startIntent) {
                            String url = String.format(Const.MOBILE_APP_URL, mApp.schoolHost, String.format(Const.HTML5_LESSON, entity.getCourseId(), entity.getLessonId()));
                            startIntent.putExtra(Const.WEB_URL, url);
                        }
                    });
                }
            });

        }
        if (holder instanceof CostTimeViewHolder) {
            NewsCourseEntity entity = mDataList.get(position);
            String costTime = entity.getContent();
            if (costTime.equals("")) {
                ((CostTimeViewHolder) holder).costTime.setVisibility(View.GONE);
            } else {
                ((CostTimeViewHolder) holder).costTime.setText(entity.getContent());
                ((CostTimeViewHolder) holder).costTime.setVisibility(View.VISIBLE);
            }
        }
        if (holder instanceof NormalNotificationViewHolder) {

            final NewsCourseEntity entity = mDataList.get(position);
            if (entity.getBodyType().equals("lesson.finish")) {
                ((NormalNotificationViewHolder) holder).notificationType.setBackgroundResource(R.drawable.process_lesson_start_bg);
                ((NormalNotificationViewHolder) holder).notificationType.setTextColor(mContext.getResources().getColor(R.color.process_lesson_start));
                ((NormalNotificationViewHolder) holder).notificationType.setText("课时完成");
                String textContont = AppUtil.cutString(entity.getContent(), 30);
                ((NormalNotificationViewHolder) holder).notificationContent.setText(String.format("恭喜您已经完成了课时『%s』的学习", textContont));

            }
            if (entity.getBodyType().equals("lesson.start")) {
                ((NormalNotificationViewHolder) holder).notificationType.setBackgroundResource(R.drawable.process_lesson_start_bg);
                ((NormalNotificationViewHolder) holder).notificationType.setTextColor(mContext.getResources().getColor(R.color.process_lesson_start));
                ((NormalNotificationViewHolder) holder).notificationType.setText("课时开始");
                String textContont = AppUtil.cutString(entity.getContent(), 30);
                ((NormalNotificationViewHolder) holder).notificationContent.setText(String.format("您已经开始了课时『%s』的学习", textContont));
            }
            if (entity.getBodyType().equals("question.created")) {
                NormalNotificationViewHolder normalNotificationViewHolder = (NormalNotificationViewHolder) holder;
                normalNotificationViewHolder.notificationType.setText(String.format("[问答]%s", entity.getTitle()));
                normalNotificationViewHolder.notificationContent.setText(entity.getContent());
                normalNotificationViewHolder.notificationContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CoreEngine.create(mContext).runNormalPlugin("ThreadDiscussActivity", mContext, new PluginRunCallback() {
                            @Override
                            public void setIntentDate(Intent startIntent) {
                                startIntent.putExtra(ThreadDiscussActivity.TARGET_ID, entity.getCourseId());
                                startIntent.putExtra(ThreadDiscussActivity.TARGET_TYPE, "course");
                                startIntent.putExtra(ThreadDiscussActivity.THREAD_ID, entity.getThreadId());
                                startIntent.putExtra(ThreadDiscussActivity.ACTIVITY_TYPE, PushUtil.ThreadMsgType.THREAD_POST);
                            }
                        });
                    }
                });
            }
            ((NormalNotificationViewHolder) holder).notificationTime.setText("系统 发布于" + AppUtil.timeStampToDate(String.valueOf(entity.getCreatedTime()), null));
            ((NormalNotificationViewHolder) holder).TVMoreInfo.setVisibility(View.GONE);
        }

        if (holder instanceof IntentNotificationViewHolder) {

            final NewsCourseEntity entity = mDataList.get(position);
            ((IntentNotificationViewHolder) holder).notificationTime.setText("系统 发布于" + AppUtil.timeStampToDate(String.valueOf(entity.getCreatedTime()), null));
            ((IntentNotificationViewHolder) holder).TVMoreInfo.setVisibility(View.VISIBLE);

            if (entity.getBodyType().equals("testpaper.reviewed")) {
                ((IntentNotificationViewHolder) holder).notificationType.setBackgroundResource(R.drawable.process_testpaper_bg);
                ((IntentNotificationViewHolder) holder).notificationType.setTextColor(mContext.getResources().getColor(R.color.process_testpaper));
                ((IntentNotificationViewHolder) holder).notificationType.setText("试卷批阅完成");
                String textContont = AppUtil.cutString(entity.getContent(), 30);
                ((IntentNotificationViewHolder) holder).notificationContent.setText(String.format("您的课时『%s』，试卷已经批阅完成", textContont));
                ((IntentNotificationViewHolder) holder).notificationContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mApp.mEngine.runNormalPlugin("FragmentPageActivity", mContext, new PluginRunCallback() {
                            @Override
                            public void setIntentDate(Intent startIntent) {
                                startIntent.putExtra(FragmentPageActivity.FRAGMENT, "TestpaperResultFragment");
                                startIntent.putExtra(Const.ACTIONBAR_TITLE, entity.getTitle() + "考试结果");
                                startIntent.putExtra(TestpaperResultFragment.RESULT_ID, entity.getObjectId());
                                startIntent.putExtra(Const.STATUS, "finished");
                            }
                        });
                    }
                });
            } else if (entity.getBodyType().equals("homework.reviewed")) {
                ((IntentNotificationViewHolder) holder).notificationType.setBackgroundResource(R.drawable.process_testpaper_bg);
                ((IntentNotificationViewHolder) holder).notificationType.setTextColor(mContext.getResources().getColor(R.color.process_testpaper));
                ((IntentNotificationViewHolder) holder).notificationType.setText("作业批阅完成");
                String textContont = AppUtil.cutString(entity.getContent(), 30);
                ((IntentNotificationViewHolder) holder).notificationContent.setText(String.format("课时『%s』的作业已经批阅完成", textContont));
                ((IntentNotificationViewHolder) holder).notificationContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(Const.LESSON_ID, entity.getLessonId());
                        mApp.mEngine.runNormalPluginWithBundle("HomeworkSummaryActivity", mContext, bundle);
                    }
                });
            } else if (entity.getBodyType().equals("question.answered")) {
                ((IntentNotificationViewHolder) holder).notificationType.setBackgroundResource(R.drawable.process_question_bg);
                ((IntentNotificationViewHolder) holder).notificationType.setTextColor(mContext.getResources().getColor(R.color.process_question));
                ((IntentNotificationViewHolder) holder).notificationType.setText("问题回复");
                String textContont = AppUtil.cutString(entity.getContent(), 30);
                ((IntentNotificationViewHolder) holder).notificationContent.setText(String.format("您的问题『%s』有新的老师回复", textContont));
                ((IntentNotificationViewHolder) holder).notificationContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mApp.mEngine.runNormalPlugin("ThreadDiscussActivity", mContext, new PluginRunCallback() {
                            @Override
                            public void setIntentDate(Intent startIntent) {
                                startIntent.putExtra(AbstractIMChatActivity.FROM_ID, entity.getThreadId());
                                startIntent.putExtra(ThreadDiscussChatActivity.THREAD_TYPE, "question");
                                startIntent.putExtra(ThreadDiscussChatActivity.THREAD_TARGET_ID, entity.getCourseId());
                                startIntent.putExtra(ThreadDiscussChatActivity.THREAD_TARGET_TYPE, "course");
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class LessonTitleViewHolder extends RecyclerView.ViewHolder {

        private TextView lessonTitle;
        private ImageView lessonState;

        public LessonTitleViewHolder(View itemView) {
            super(itemView);
            lessonState = (ImageView) itemView.findViewById(R.id.studyprocess_lesson_state_icon);
            lessonTitle = (TextView) itemView.findViewById(R.id.study_process_lesson_title);
            TextPaint textPaint = lessonTitle.getPaint();
            textPaint.setFakeBoldText(true);
        }
    }

    public void addItem(NewsCourseEntity entity) {
        mDataList.add(entity);
    }

    private class LessonSummaryViewHolder extends RecyclerView.ViewHolder {

        private TextView summaryCourseTitle;
        private ImageView summaryCourseImage;
        private TextView summaryCourseIntroduction;
        private TextView summaryCourseTeacher;
        private TextView summaryTeacherTag;
        private RelativeLayout summaryFrame;

        public LessonSummaryViewHolder(View itemView) {
            super(itemView);
            summaryFrame = (RelativeLayout) itemView.findViewById(R.id.process_lesson_summary_frame);
            summaryCourseTitle = (TextView) itemView.findViewById(R.id.study_process_lesson_summary_title);
            summaryCourseImage = (ImageView) itemView.findViewById(R.id.study_process_lesson_summary_image);
            summaryCourseIntroduction = (TextView) itemView.findViewById(R.id.study_process_lesson_summary_introduction);
            summaryTeacherTag = (TextView) itemView.findViewById(R.id.study_process_lesson_teacher_tag);
            summaryCourseTeacher = (TextView) itemView.findViewById(R.id.study_process_lesson_summary_teacher);
        }
    }

    private class CostTimeViewHolder extends RecyclerView.ViewHolder {
        private TextView costTime;

        public CostTimeViewHolder(View itemView) {
            super(itemView);
            costTime = (TextView) itemView.findViewById(R.id.study_process_cost_time);
        }
    }

    private class NormalNotificationViewHolder extends RecyclerView.ViewHolder {

        private TextView notificationContent;
        private TextView notificationTime;
        private TextView notificationType;
        private TextView TVMoreInfo;

        public NormalNotificationViewHolder(View itemView) {
            super(itemView);
            notificationContent = (TextView) itemView.findViewById(R.id.study_process_notification_content);
            notificationTime = (TextView) itemView.findViewById(R.id.study_process_notification_time);
            notificationType = (TextView) itemView.findViewById(R.id.notification_type);
            TVMoreInfo = (TextView) itemView.findViewById(R.id.study_process_more_info);
        }

    }

    private class IntentNotificationViewHolder extends RecyclerView.ViewHolder {

        private TextView notificationContent;
        private TextView notificationTime;
        private TextView notificationType;
        private TextView TVMoreInfo;

        public IntentNotificationViewHolder(View itemView) {
            super(itemView);
            notificationContent = (TextView) itemView.findViewById(R.id.study_process_notification_content);
            notificationTime = (TextView) itemView.findViewById(R.id.study_process_notification_time);
            notificationType = (TextView) itemView.findViewById(R.id.notification_type);
            TVMoreInfo = (TextView) itemView.findViewById(R.id.study_process_more_info);
        }
    }

    public void setSummaryListene(View.OnClickListener listener) {
        this.summaryListener = listener;
    }

    public void setmDataList(List<NewsCourseEntity> mDataList) {
        this.mDataList = mDataList;
        notifyItemRangeChanged(0, getItemCount());
    }
}
