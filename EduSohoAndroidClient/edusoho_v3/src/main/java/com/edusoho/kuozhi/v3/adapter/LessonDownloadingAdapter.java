package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8DbModel;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.service.M3U8DownService;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.M3U8Util;
import com.edusoho.kuozhi.v3.view.EduSohoNewIconView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suju on 17/1/10.
 */

public class LessonDownloadingAdapter extends BaseAdapter {

    private Context mContex;
    private SparseArray<M3U8DbModel> m3u8ModelList;
    private List<LessonItem> mChildItems;
    private boolean mSelectedShow = false;
    private DownloadingAdapter.DownloadType mType;
    private int mChildLayoutId;
    private DisplayImageOptions mOptions;

    public LessonDownloadingAdapter(Context ctx, SparseArray<M3U8DbModel> m3u8List, List<LessonItem> localLessons, DownloadingAdapter.DownloadType type, int childResId) {
        mContex = ctx;
        m3u8ModelList = m3u8List;
        mChildItems = new ArrayList<>();
        mType = type;
        mChildLayoutId = childResId;
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).showImageForEmptyUri(R.drawable.default_course).
                showImageOnFail(R.drawable.default_course).build();
        if (localLessons != null) {
            mChildItems.addAll(localLessons);
        }
    }

    public void updateLocalData(List<LessonItem> localLessons) {
        mChildItems.clear();
        if (localLessons == null || localLessons.isEmpty()) {
            return;
        }
        mChildItems.addAll(localLessons);
        notifyDataSetChanged();
    }

    public void updateProgress(int lessonId, M3U8DbModel model) {
        m3u8ModelList.put(lessonId, model);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mChildItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChildPanel childPanel;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContex).inflate(mChildLayoutId, null);
            childPanel = new ChildPanel(convertView, mType);
            convertView.setTag(childPanel);
        } else {
            childPanel = (ChildPanel) convertView.getTag();
        }
        final LessonItem lessonItem = mChildItems.get(position);
        childPanel.tvLessonTitle.setText(lessonItem.title);
        if (mType == DownloadingAdapter.DownloadType.DOWNLOADED) {
            childPanel.tvVideoLength.setText(getDownloadLessonSize(lessonItem.id));
        } else {
            M3U8DbModel model = m3u8ModelList.get(lessonItem.id);
            childPanel.tvProgress.setMax(model.totalNum);
            childPanel.tvProgress.setProgress(model.downloadNum);

            int downStatus = getDownloadStatus(lessonItem.id);
            if (downStatus == M3U8Util.ERROR) {
                childPanel.tvLessonTitle.setText(AppUtil.getColorTextAfter(lessonItem.title, " 下载失败", Color.RED));
            }
            childPanel.setDownloasState(downStatus);
        }
        //选择框是否显示
        if (mSelectedShow) {
            childPanel.ivDownloadSelected.setVisibility(View.VISIBLE);
            String icon = getDownloadSelectedIcon(lessonItem.isSelected);
            int iconColor = getDownloadSelectedColor(lessonItem.isSelected);

            childPanel.ivDownloadSelected.setText(icon);
            childPanel.ivDownloadSelected.setTextColor(iconColor);
            childPanel.ivDownloadSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isSelected = childPanel.ivDownloadSelected.getText().equals(
                            mContex.getString(R.string.font_download_unselect));
                    String icon = getDownloadSelectedIcon(isSelected);
                    int iconColor = getDownloadSelectedColor(isSelected);

                    lessonItem.isSelected = isSelected;
                    childPanel.ivDownloadSelected.setText(icon);
                    childPanel.ivDownloadSelected.setTextColor(iconColor);
                }
            });
        } else {
            childPanel.ivDownloadSelected.setVisibility(View.GONE);
        }

        return convertView;
    }

    private String getDownloadSelectedIcon(boolean isSelected) {
        return isSelected ? mContex.getString(R.string.font_download_select) :
                mContex.getString(R.string.font_download_unselect);
    }

    private int getDownloadSelectedColor(boolean isSelected) {
        return isSelected ? mContex.getResources().getColor(R.color.primary_color) :
                mContex.getResources().getColor(R.color.secondary2_font_color);
    }

    private String getDownloadLessonSize(int lessonId) {
        AppSettingProvider settingProvider = getAppSettingProvider();
        User user = settingProvider.getCurrentUser();
        School school = settingProvider.getCurrentSchool();
        if (user == null || school == null) {
            return "";
        }
        File dir = getLocalM3U8Dir(user.id, school.getDomain(), lessonId);
        if (dir == null || !dir.exists()) {
            return "";
        }

        float size = getCacheSize(dir) / 1024.0f / 1024.0f;
        if (size == 0) {
            return "0M";
        } else {
            return String.format("%.1f%s", size, "M");
        }
    }

    private long getCacheSize(File dir) {
        long totalSize = 0;
        for (File file : dir.listFiles()) {
            if (!file.isDirectory()) {
                totalSize = totalSize + file.length();
            } else {
                totalSize = totalSize + getCacheSize(file);
            }
        }
        return totalSize;
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    private File getLocalM3U8Dir(int userId, String host, int lessonId) {
        File workSpace = EdusohoApp.getWorkSpace();
        if (workSpace == null) {
            return null;
        }

        StringBuffer dirBuilder = new StringBuffer(workSpace.getAbsolutePath());
        dirBuilder.append("/videos/")
                .append(userId)
                .append("/")
                .append(host)
                .append("/")
                .append(lessonId);

        File lessonDir = new File(dirBuilder.toString());
        if (!lessonDir.exists()) {
            lessonDir.mkdirs();
        }

        return lessonDir;
    }

    protected int getDownloadStatus(int lessonId) {
        M3U8DownService service = M3U8DownService.getService();
        if (service == null) {
            return M3U8Util.PAUSE;
        }
        return service.getTaskStatus(lessonId);
    }

    public void setItemDownloadStatus(int position) {
        LessonItem lessonItem = mChildItems.get(position);
        lessonItem.isSelected = !lessonItem.isSelected;
        notifyDataSetChanged();
    }

    public boolean isSelectedShow() {
        return this.mSelectedShow;
    }

    public void setSelectShow(boolean b) {
        this.mSelectedShow = b;
        notifyDataSetChanged();
    }

    public void isSelectAll(boolean b) {
        for (LessonItem item : mChildItems) {
            item.isSelected = b;
        }
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getSelectLessonId() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (LessonItem item : mChildItems) {
            if (item.isSelected) {
                ids.add(item.id);
            }
        }
        return ids;
    }

    public static class ChildPanel {
        public EduSohoNewIconView ivDownloadSelected;
        public TextView tvLessonTitle;
        public View viewDownloadProgress;
        public ProgressBar tvProgress;
        public TextView tvVideoLength;

        public ChildPanel(View view, DownloadingAdapter.DownloadType type) {
            ivDownloadSelected = (EduSohoNewIconView) view.findViewById(R.id.iv_download_selected);
            tvLessonTitle = (TextView) view.findViewById(R.id.tv_lesson_content);
            viewDownloadProgress = view.findViewById(R.id.rl_progress);
            tvProgress = (ProgressBar) view.findViewById(R.id.tv_progress);
            tvVideoLength = (TextView) view.findViewById(R.id.tv_video_length);

            if (DownloadingAdapter.DownloadType.DOWNLOADED == type) {
                tvVideoLength.setVisibility(View.VISIBLE);
                tvProgress.setVisibility(View.GONE);
            } else {
                tvVideoLength.setVisibility(View.GONE);
                tvProgress.setVisibility(View.VISIBLE);
            }
        }

        public void setDownloasState(int downStatus) {
            switch (downStatus) {
                case M3U8Util.ERROR:
                case M3U8Util.PAUSE:
                    tvProgress.setBackgroundResource(R.drawable.icon_download_start);
                    break;
                case M3U8Util.NONE:
                case M3U8Util.DOWNING:
                default:
                    tvProgress.setBackgroundResource(R.drawable.icon_download_pause);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public LessonItem getItem(int position) {
        return mChildItems.get(position);
    }
}
