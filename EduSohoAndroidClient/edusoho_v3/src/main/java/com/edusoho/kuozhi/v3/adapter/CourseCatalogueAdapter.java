package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.entity.lesson.CourseCatalogue;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.view.EduSohoNewIconView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by DF on 2016/12/14.
 */
public class CourseCatalogueAdapter extends RecyclerView.Adapter<CourseCatalogueAdapter.ViewHolder> {

    public int mSelect = -1;
    private static CourseCatalogue courseCatalogue;
    public Context mContext;
    private static boolean isJoin;
    private String chapterTitle;
    private String unitTitle;
    private static final int TYPE_CHAPTER = 0;
    private static final int TYPE_SECTION = 1;
    private static final int TYPE_LESSON = 2;
    private static final int TYPE_FOOTER = 3;
    private final LayoutInflater mInflater;
    private static Map<String, String> learnStatuses;

    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, CourseCatalogue.LessonsBean lessonsBean);
    }

    public CourseCatalogueAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        CourseCatalogueAdapter.courseCatalogue = null;

    }

    public void setData(CourseCatalogue courseCatalogue, boolean isJoin, String chapterTitle, String unitTitle) {
        this.chapterTitle = chapterTitle;
        this.unitTitle = unitTitle;
        CourseCatalogueAdapter.courseCatalogue = courseCatalogue;
        CourseCatalogueAdapter.learnStatuses = courseCatalogue.getLearnStatuses();
        CourseCatalogueAdapter.isJoin = isJoin;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener (OnRecyclerViewItemClickListener listener) {
        this.onRecyclerViewItemClickListener = listener;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (getItemCount() == 1 || (position == getItemCount() - 1)) {
            return;
        }
        holder.render(courseCatalogue.getLessons().get(position), chapterTitle, unitTitle, position);
        holder.itemView.setTag(courseCatalogue.getLessons().get(position));
        if (holder.getItemViewType() == TYPE_LESSON) {
            if (holder.itemView.hasOnClickListeners()) {
                return;
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelect == holder.getAdapterPosition()) {
                        return;
                    }
                    if (mSelect == -1) {
                        mSelect = 0;
                    }
                    courseCatalogue.getLessons().get(mSelect).isSelect = false;
                    notifyItemChanged(mSelect);
                    mSelect = holder.getAdapterPosition();
                    courseCatalogue.getLessons().get(mSelect).isSelect = true;
                    notifyItemChanged(mSelect);
                    if (onRecyclerViewItemClickListener != null) {
                        onRecyclerViewItemClickListener.onItemClick(v, courseCatalogue.getLessons().get(mSelect));
                    }
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_FOOTER:
                View view = new View(mContext);
                view.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AppUtil.dp2px(mContext, 80)));
                return new FooterViewHolder(view);
            case TYPE_CHAPTER:
                return new ChatperViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter_catalog, parent, false));
            case TYPE_SECTION:
                return new UnitViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_catalog, parent, false));
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson_catalog, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (courseCatalogue == null) {
            return 1;
        }
        return courseCatalogue.getLessons() == null ? 1 : courseCatalogue.getLessons().size() + 1;
    }

    protected static abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
        }

        protected abstract void render(CourseCatalogue.LessonsBean lesson, String customChapter, String customUnit, int position);
    }

    private class FooterViewHolder extends ViewHolder {

        public FooterViewHolder(View view) {
            super(view);
        }

        @Override
        protected void render(CourseCatalogue.LessonsBean lesson, String customChapter, String customUnit, int position) {

        }
    }

    private class LessonViewHolder extends ViewHolder {

        private ImageView lessonState;
        private EduSohoNewIconView lessonKind;
        private TextView lessonTitle;
        private TextView lessonFree;
        private TextView lessonTime;
        private TextView liveState;
        private View lessonUp;
        private View lessonDown;
        private CourseCatalogue.LessonsBean lessonsBean;

        public LessonViewHolder(View view) {
            super(view);
            lessonState = (ImageView) itemView.findViewById(R.id.lesson_state);
            lessonKind = (EduSohoNewIconView) itemView.findViewById(R.id.lesson_kind);
            lessonTitle = (TextView) itemView.findViewById(R.id.lesson_title);
            lessonFree = (TextView) itemView.findViewById(R.id.lesson_free);
            lessonTime = (TextView) itemView.findViewById(R.id.lesson_time);
            liveState = (TextView) itemView.findViewById(R.id.live_state);
            lessonUp = itemView.findViewById(R.id.lesson_up);
            lessonDown = itemView.findViewById(R.id.lesson_down);
        }

        @Override
        protected void render(CourseCatalogue.LessonsBean lesson, String customChapter, String customUnit,int position) {
            lessonTitle.setText(lesson.getTitle());
            initView(position);
        }

        private void initView(int position) {
            lessonsBean = courseCatalogue.getLessons().get(position);
            if (!isJoin) {
                lessonState.setVisibility(View.GONE);
                lessonUp.setVisibility(View.GONE);
                lessonDown.setVisibility(View.GONE);
            } else {
                //判断课时学习状态
                decideStatu();
                lessonUp.setVisibility(View.VISIBLE);
                lessonDown.setVisibility(View.VISIBLE);
                if (position != 0) {
                    if (!"lesson".equals(courseCatalogue.getLessons().get(position - 1).getItemType())) {
                        lessonUp.setVisibility(View.INVISIBLE);
                    }
                    if (position == courseCatalogue.getLessons().size() - 1) {
                        lessonDown.setVisibility(View.INVISIBLE);
                    }
                }
                if (position < courseCatalogue.getLessons().size() - 1) {
                    if (!"lesson".equals(courseCatalogue.getLessons().get(position + 1).getItemType())) {
                        lessonDown.setVisibility(View.INVISIBLE);
                    }
                    if (position == 0) {
                        lessonUp.setVisibility(View.INVISIBLE);
                    }
                }
                if (courseCatalogue.getLessons().size() == 1) {
                    lessonUp.setVisibility(View.INVISIBLE);
                    lessonDown.setVisibility(View.INVISIBLE);
                }
            }
            decideKind();
            if (courseCatalogue.getLessons().get(position).isSelect) {
                lessonKind.setTextColor(mContext.getResources().getColor(R.color.primary));
                lessonTitle.setTextColor(mContext.getResources().getColor(R.color.primary));
                lessonTime.setTextColor(mContext.getResources().getColor(R.color.primary));
            } else {
                lessonKind.setTextColor(mContext.getResources().getColor(R.color.secondary2_font_color));
                lessonTitle.setTextColor(mContext.getResources().getColor(R.color.secondary_font_color));
                lessonTime.setTextColor(mContext.getResources().getColor(R.color.secondary_font_color));
            }
            lessonTime.setText(lessonsBean.getLength());
            lessonTitle.setText(String.format("%s、%s", lessonsBean.getNumber(), lessonsBean.getTitle()));
            lessonFree.setVisibility(View.INVISIBLE);
            if ("1".equals(lessonsBean.getFree()) && !isJoin) {
                lessonFree.setVisibility(View.VISIBLE);
            }
            if ("live".equals(lessonsBean.getType())) {
                initLiveState();
            }
            lessonTitle.measure(0, 0);
            RelativeLayout.LayoutParams down = (RelativeLayout.LayoutParams) lessonDown.getLayoutParams();
            down.height = AppUtil.dp2px(mContext, 30) > lessonTitle.getMeasuredHeight() - AppUtil.dp2px(mContext, 12f) ? AppUtil.dp2px(mContext, 30)
                    : lessonTitle.getMeasuredHeight();
            lessonDown.setLayoutParams(down);
        }

        private void initLiveState() {
            lessonTitle.setMaxEms(8);
            long time = System.currentTimeMillis() / 1000;
            String start = lessonsBean.getStartTime();
            String end = lessonsBean.getEndTime();
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(Long.parseLong(end) * 1000);
            String startTime = sf.format(date);
            liveState.setVisibility(View.VISIBLE);
            if (time <= Long.parseLong(start)) {
                startTime = startTime.split("-", 2)[1].substring(0, startTime.split("-", 2)[1].lastIndexOf(":")).replace("-", "月").replace(" ", "号 ");
                liveState.setTextColor(mContext.getResources().getColor(R.color.secondary_font_color));
                liveState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                liveState.setText(startTime);
            } else {
                if (time > Long.parseLong(end)) {
                    if ("ungenerated".equals(lessonsBean.getReplayStatus())) {
                        liveState.setText(R.string.live_state_finish);
                        liveState.setTextColor(ContextCompat.getColor(mContext, R.color.secondary2_font_color));
                        liveState.setBackground(ContextCompat.getDrawable(mContext, R.drawable.live_state_finish));
                    } else {
                        liveState.setText(R.string.live_state_replay);
                        liveState.setTextColor(mContext.getResources().getColor(R.color.secondary2_color));
                        liveState.setBackground(ContextCompat.getDrawable(mContext, R.drawable.live_state_replay));
                    }
                } else {
                    liveState.setText(R.string.live_state_ing);
                    liveState.setTextColor(mContext.getResources().getColor(R.color.primary_color));
                    liveState.setBackground(ContextCompat.getDrawable(mContext, R.drawable.live_state_ing));
                }
            }
        }

        private void decideStatu() {
            lessonState.setImageResource(R.drawable.lesson_status);
            if (learnStatuses != null && learnStatuses.containsKey(lessonsBean.getId())) {
                if ("learning".equals(learnStatuses.get(lessonsBean.getId()))) {
                    lessonState.setImageResource(R.drawable.lesson_status_learning);
                } else if ("finished".equals(learnStatuses.get(lessonsBean.getId()))) {
                    lessonState.setImageResource(R.drawable.lesson_status_finish);
                }
            }
        }

        private void decideKind() {
            switch (lessonsBean.getType()) {
                case "ppt":
                    lessonKind.setText(R.string.catalog_lesson_ppt);
                    break;
                case "audio":
                    lessonKind.setText(R.string.catalog_lesson_audio);
                    break;
                case "text":
                    lessonKind.setText(R.string.catalog_lesson_text);
                    break;
                case "flash":
                    lessonKind.setText(R.string.catalog_lesson_flash);
                    break;
                case "live":
                    lessonKind.setText(R.string.catalog_lesson_live);
                    break;
                case "video":
                    lessonKind.setText(R.string.catalog_lesson_video);
                    break;
                case "document":
                    lessonKind.setText(R.string.catalog_lesson_doucument);
                    break;
                case "testpaper":
                    lessonKind.setText(R.string.catalog_lesson_testPaper);
                    break;
            }
        }
    }

    private static class ChatperViewHolder extends ViewHolder {

        private TextView chapterTitle;

        public ChatperViewHolder(View view) {
            super(view);
            chapterTitle = (TextView) itemView.findViewById(R.id.chapter_title);
        }

        @Override
        protected void render(CourseCatalogue.LessonsBean lesson, String customChapter, String customUnite, int position) {
            if (!TextUtils.isEmpty(customChapter)) {
                chapterTitle.setText(String.format("第%s%s:%s", lesson.getNumber(), customChapter, lesson.getTitle()));
            }else {
                chapterTitle.setText(String.format("%s", lesson.getTitle()));
            }
        }
    }

    private static class UnitViewHolder extends ViewHolder {

        private TextView sectionTitle;

        public UnitViewHolder(View view) {
            super(view);
            sectionTitle = (TextView) itemView.findViewById(R.id.section_title);
        }

        @Override
        protected void render(CourseCatalogue.LessonsBean lesson, String customChapter, String customUnite, int positon) {
            if (!TextUtils.isEmpty(customUnite)) {
                sectionTitle.setText(String.format("第%s%s:%s", lesson.getNumber(), customUnite, lesson.getTitle()));
            }else {
                sectionTitle.setText(String.format("%s", lesson.getTitle()));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() == 1 || (position == getItemCount() - 1)) {
            return TYPE_FOOTER;
        }
        if ("chapter".equals(courseCatalogue.getLessons().get(position).getType())) {
            return TYPE_CHAPTER;
        } else if ("unit".equals(courseCatalogue.getLessons().get(position).getType())) {
            return TYPE_SECTION;
        } else {
            return TYPE_LESSON;
        }
    }

    public void setLearnStatuses(Map<String, String> learnStatuses) {
        CourseCatalogueAdapter.learnStatuses = learnStatuses;
        notifyDataSetChanged();
    }

}