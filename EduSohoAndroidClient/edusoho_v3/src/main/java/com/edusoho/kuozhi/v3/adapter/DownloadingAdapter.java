package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8DbModel;
import com.edusoho.kuozhi.v3.service.M3U8DownService;
import com.edusoho.kuozhi.v3.ui.base.BaseActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.M3U8Util;
import com.edusoho.kuozhi.v3.view.EduSohoIconView;
import com.edusoho.kuozhi.v3.view.EduSohoNewIconView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by JesseHuang on 15/6/16.
 */
public class DownloadingAdapter extends BaseExpandableListAdapter {

    private Context mContex;
    private SparseArray<M3U8DbModel> m3u8ModelList;
    private List<Course> mGroupItems;
    private List<List<LessonItem>> mChildItems;
    private boolean mSelectedShow = false;
    private DownloadType mType;
    private int mChildLayoutId;
    private DisplayImageOptions mOptions;

    public DownloadingAdapter(Context ctx, BaseActivity activity, SparseArray<M3U8DbModel> m3u8List,
                              List<Course> groupItems, HashMap<Integer, List<LessonItem>> mLocalLessons, DownloadType type, int childResId) {
        mContex = ctx;
        m3u8ModelList = m3u8List;
        mGroupItems = groupItems;

        List<List<LessonItem>> lessonItems = new ArrayList<>();
        for (Course course : groupItems) {
            lessonItems.add(mLocalLessons.get(course.id));
        }
        mChildItems = lessonItems;
        mType = type;
        mChildLayoutId = childResId;
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).showImageForEmptyUri(R.drawable.defaultpic).
                showImageOnFail(R.drawable.defaultpic).build();
    }

    public void updateLocalData(List<Course> groupItems, HashMap<Integer, List<LessonItem>> mLocalLessons) {
        mGroupItems = groupItems;
        List<List<LessonItem>> lessonItems = new ArrayList<>();
        for (Course course : groupItems) {
            lessonItems.add(mLocalLessons.get(course.id));
        }
        mChildItems = lessonItems;
        notifyDataSetChanged();
    }

    public void setCourseExpired(Course expiredCourse) {
        for (Course localCourse : mGroupItems) {
            if (localCourse.id == expiredCourse.id) {
                localCourse.title = localCourse.title + "--(已过期)";
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void updateProgress(int lessonId, M3U8DbModel model) {
        m3u8ModelList.put(lessonId, model);
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mGroupItems.size();
    }

    public List<LessonItem> getChildrenItemsByCourseId(int courseId) {
        int index = 0;
        for (Course course : mGroupItems) {
            if (course.id == courseId) {
                break;
            }
            index++;
        }
        return mChildItems.get(index);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildItems.get(groupPosition).size();
    }

    @Override
    public Course getGroup(int groupPosition) {
        return mGroupItems.get(groupPosition);
    }

    @Override
    public LessonItem getChild(int groupPosition, int childPosition) {
        return mChildItems.isEmpty() ? null : mChildItems.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupPanel groupPanel;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContex).inflate(R.layout.item_download_manager_course_group, null);
            groupPanel = new GroupPanel(convertView);
            convertView.setTag(groupPanel);
        } else {
            groupPanel = (GroupPanel) convertView.getTag();
        }

        final Course course = mGroupItems.get(groupPosition);
        ImageLoader.getInstance().displayImage(course.middlePicture, groupPanel.ivAvatar, mOptions);
        groupPanel.tvCourseTitle.setText(course.title);
        groupPanel.ivVideoSum.setText(String.format("视频 %s", mChildItems.get(groupPosition).size()));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ChildPanel childPanel;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContex).inflate(mChildLayoutId, null);
            childPanel = new ChildPanel(convertView, mType);
            convertView.setTag(childPanel);
        } else {
            childPanel = (ChildPanel) convertView.getTag();
        }
        final LessonItem lessonItem = mChildItems.get(groupPosition).get(childPosition);
        childPanel.tvLessonTitle.setText(lessonItem.title);
        if (mType == DownloadType.DOWNLOADED) {
            childPanel.tvVideoLength.setText(AppUtil.convertCNTime(lessonItem.length));
        } else {
            M3U8DbModel model = m3u8ModelList.get(lessonItem.id);
            //childPanel.tvProgress.setText((int) (model.downloadNum / (float) model.totalNum * 100) + "%");

            int downStatus = getDownloadStatus(lessonItem.id);
            int downStatusIconRes = downStatus == M3U8Util.DOWNING ? R.string.font_downloading : R.string.font_stop_downloading;
            if (model.finish == M3U8Util.DOWNLOAD_ERROR) {
                //childPanel.tvProgress.setText("下载失败");
            }
            //childPanel.setDownloasState(downStatus);
            //childPanel.ivDownloadSign.setText(mContex.getResources().getString(downStatusIconRes));
        }
        //选择框是否显示
        if (mSelectedShow) {
            childPanel.ivDownloadSelected.setVisibility(View.VISIBLE);
            if (lessonItem.isSelected) {
                childPanel.ivDownloadSelected.setText(mContex.getString(R.string.font_download_select));
            } else {
                childPanel.ivDownloadSelected.setText(mContex.getString(R.string.font_download_unselect));
            }
            childPanel.ivDownloadSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (childPanel.ivDownloadSelected.getText().equals(mContex.getString(R.string.font_download_unselect))) {
                        childPanel.ivDownloadSelected.setText(mContex.getString(R.string.font_download_select));
                        lessonItem.isSelected = true;
                    } else {
                        childPanel.ivDownloadSelected.setText(mContex.getString(R.string.font_download_unselect));
                        lessonItem.isSelected = false;
                    }
                }
            });
        } else {
            childPanel.ivDownloadSelected.setVisibility(View.GONE);
        }

        return convertView;
    }

    protected int getDownloadStatus(int lessonId) {
        M3U8DownService service = M3U8DownService.getService();
        if (service == null) {
            return M3U8Util.NONE;
        }
        return service.getTaskStatus(lessonId);
    }

    public void setItemDownloadStatus(int groupPosition, int childPosition) {
        LessonItem lessonItem = mChildItems.get(groupPosition).get(childPosition);
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
        for (List<LessonItem> lessonItems : mChildItems) {
            for (LessonItem item : lessonItems) {
                item.isSelected = b;
            }
        }
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getSelectLessonId() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (List<LessonItem> lessonItems : mChildItems) {
            for (LessonItem item : lessonItems) {
                if (item.isSelected) {
                    ids.add(item.id);
                }
            }
        }
        return ids;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public static class GroupPanel {
        public ImageView ivAvatar;
        public TextView tvCourseTitle;
        public EduSohoIconView ivIndicator;
        public TextView ivVideoSum;
        public TextView ivVideoSizes;

        public GroupPanel(View view) {
            ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
            tvCourseTitle = (TextView) view.findViewById(R.id.tv_course_title);
            ivIndicator = (EduSohoIconView) view.findViewById(R.id.iv_indicator);
            ivVideoSum = (TextView) view.findViewById(R.id.tv_video_sum);
            ivVideoSizes = (TextView) view.findViewById(R.id.tv_video_size);
        }
    }

    public static class ChildPanel {
        public EduSohoNewIconView ivDownloadSelected;
        public TextView tvLessonTitle;
        public View viewDownloadProgress;
        public TextView ivDownloadSign;
        public ProgressBar tvProgress;
        public TextView tvVideoLength;

        public ChildPanel(View view, DownloadType type) {
            ivDownloadSelected = (EduSohoNewIconView) view.findViewById(R.id.iv_download_selected);
            tvLessonTitle = (TextView) view.findViewById(R.id.tv_lesson_content);
            viewDownloadProgress = view.findViewById(R.id.rl_progress);
            //ivDownloadSign = (TextView) view.findViewById(R.id.iv_download_sign);
            tvProgress = (ProgressBar) view.findViewById(R.id.tv_progress);
            tvVideoLength = (TextView) view.findViewById(R.id.tv_video_length);

            if (DownloadType.DOWNLOADED == type) {
                tvVideoLength.setVisibility(View.VISIBLE);
                tvProgress.setVisibility(View.GONE);
            } else {
                tvVideoLength.setVisibility(View.GONE);
                tvProgress.setVisibility(View.VISIBLE);
            }
        }
    }

    public enum DownloadType {
        DOWNLOADING, DOWNLOADED
    }
}
