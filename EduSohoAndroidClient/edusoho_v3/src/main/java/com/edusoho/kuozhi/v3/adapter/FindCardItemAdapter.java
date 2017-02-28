package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryCardProperty;
import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryCourse;
import com.edusoho.kuozhi.v3.entity.lesson.Lesson;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.lesson.LessonModel;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 2016/2/19.
 */
public class FindCardItemAdapter extends BaseAdapter {

    private static final int EMPTY = 0;
    private static final int COURSE = 1;
    private static final int LIVE = 2;
    private static final int CLASSROOM = 3;
    private static final String LIVE_START = "直播中";
    private static final String LIVE_NOTE_START = "未开始";
    private static final String LIVE_FINISH = "已结束";
    private int paddingLeftRight;
    private int paddingTopBottom;

    private Context mContext;
    private List<DiscoveryCardProperty> mList;
    private DisplayImageOptions mOptions;
    private DisplayImageOptions mClassRoomOptions;
    private LessonModel mLessonModel;
    private SimpleDateFormat mLiveFormat = new SimpleDateFormat("MM-dd HH:mm");

    private SparseArray<List<Lesson>> mCourseLessonsCache = new SparseArray<>();

    public FindCardItemAdapter(Context context) {
        this(context, new ArrayList<DiscoveryCardProperty>());
        mLessonModel = new LessonModel();
        paddingLeftRight = AppUtil.dp2px(mContext, 8);
        paddingTopBottom = AppUtil.dp2px(mContext, 10);
    }

    public FindCardItemAdapter(Context context, List<DiscoveryCardProperty> list) {
        this.mContext = context;
        this.mList = list;
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true)
                .showImageForEmptyUri(R.drawable.default_course).
                        showImageOnFail(R.drawable.default_course)
                .build();
        mClassRoomOptions = new DisplayImageOptions.Builder().cacheOnDisk(true)
                .showImageForEmptyUri(R.drawable.default_classroom).
                        showImageOnFail(R.drawable.default_classroom)
                .build();
        mLessonModel = new LessonModel();
        paddingLeftRight = AppUtil.dp2px(mContext, 8);
        paddingTopBottom = AppUtil.dp2px(mContext, 10);
    }

    public void clear() {
        mList.clear();
    }

    public void setData(List<DiscoveryCardProperty> list) {
        mList = list;
        notifyDataSetChanged();
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
    public int getItemViewType(int position) {
        DiscoveryCardProperty discoveryCardEntity = mList.get(position);
        if (discoveryCardEntity.isEmpty()) {
            return EMPTY;
        }
        switch (discoveryCardEntity.getType()) {
            case "course":
                return COURSE;
            case "classroom":
                return CLASSROOM;
            case "live":
                return LIVE;
        }
        return COURSE;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    private int getItemHeight(ViewGroup parent) {
        int count = getCount();
        if (count % 2 == 0) {
            return parent.getHeight() / (count / 2);
        }

        return parent.getHeight() / (count / 2 + 1);
    }

    private View getViewByType(int position) {
        int viewType = getItemViewType(position);
        View convertView;

        if (viewType == EMPTY) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.find_card_item_empty_layout, null);
            return convertView;
        }

        ViewHolder viewHolder = new ViewHolder();
        switch (viewType) {
            case CLASSROOM:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.find_card_item_classroom_layout, null);
                break;
            case LIVE:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.find_card_item_live_layout, null);
                viewHolder.liveTimeView = (TextView) convertView.findViewById(R.id.card_live_time);
                viewHolder.liveStartLabelView = (TextView) convertView.findViewById(R.id.card_live_start_label);
                viewHolder.liveAvatarView = (ImageView) convertView.findViewById(R.id.card_user_avatar);
                viewHolder.liveNicknameView = (TextView) convertView.findViewById(R.id.card_nickname);
                break;
            case COURSE:
            default:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.find_card_item_layout, null);
        }

        viewHolder.coverView = (ImageView) convertView.findViewById(R.id.card_cover);
        viewHolder.titleView = (TextView) convertView.findViewById(R.id.card_title);
        viewHolder.priceView = (TextView) convertView.findViewById(R.id.card_price);
        viewHolder.studentNumView = (TextView) convertView.findViewById(R.id.card_num);
        convertView.setTag(viewHolder);

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = getViewByType(position);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        DiscoveryCardProperty discoveryCardEntity = mList.get(position);

        if (discoveryCardEntity.isEmpty()) {
            ViewGroup.LayoutParams lp = convertView.getLayoutParams();
            if (lp == null) {
                lp = new AbsListView.LayoutParams(
                        parent.getWidth() / 2, getItemHeight(parent));
            } else {
                lp.width = parent.getWidth() / 2;
                lp.height = getItemHeight(parent);
            }
            convertView.setLayoutParams(lp);
            return convertView;
        }

        DisplayImageOptions options = getItemViewType(position) == CLASSROOM ? mClassRoomOptions : mOptions;
        ImageLoader.getInstance().displayImage(discoveryCardEntity.getPicture(), viewHolder.coverView, options);
        viewHolder.titleView.setText(discoveryCardEntity.getTitle());

        if (position % 2 == 0) {
            convertView.setPadding(0, paddingTopBottom, paddingLeftRight, paddingTopBottom);
        } else {
            convertView.setPadding(paddingLeftRight, paddingTopBottom, 0, paddingTopBottom);
        }

        setDiscoveryCardClickListener(convertView, discoveryCardEntity.getType(), discoveryCardEntity.getId());
        if ("live".equals(discoveryCardEntity.getType())) {
            setLiveViewInfo(viewHolder, discoveryCardEntity);
            return convertView;
        }

        viewHolder.studentNumView.setText(discoveryCardEntity.getStudentNum() + " " + mContext.getString(R.string.find_card_student_num));
        if (discoveryCardEntity.getPrice() > 0) {
            viewHolder.priceView.setText(String.format("%.2f元", discoveryCardEntity.getPrice()));
        } else {
            viewHolder.priceView.setText("免费");
        }
        return convertView;
    }

    private void setLiveViewInfo(final ViewHolder viewHolder, DiscoveryCardProperty discoveryCardEntity) {
        viewHolder.studentNumView.setText(discoveryCardEntity.getStudentNum() + " " + mContext.getString(R.string.find_card_student_num));
        viewHolder.liveNicknameView.setText(discoveryCardEntity.getTeacherNickname());
        viewHolder.liveStartLabelView.setVisibility(View.GONE);
        viewHolder.liveTimeView.setVisibility(View.GONE);
        ImageLoader.getInstance().displayImage(discoveryCardEntity.getTeacherAvatar(), viewHolder.liveAvatarView, mOptions);
        try {
            final DiscoveryCourse discoveryCourse = (DiscoveryCourse) discoveryCardEntity;
            getLiveLessons(discoveryCourse.getId(), new NormalCallback<List<Lesson>>() {
                @Override
                public void success(List<Lesson> lessonList) {
                    if (lessonList != null) {
                        setLiveStatus(lessonList, viewHolder.liveStartLabelView, viewHolder.liveTimeView);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLiveLessons(final int courseId, final NormalCallback<List<Lesson>> callback) {
        if (mCourseLessonsCache.get(courseId) != null) {
            callback.success(mCourseLessonsCache.get(courseId));
        } else {
            String[] conditions = new String[]{
                    "status", "published",
                    "courseId", String.valueOf(courseId)
            };
            mLessonModel.getLessonByCourseId(conditions, new ResponseCallbackListener<List<Lesson>>() {
                @Override
                public void onSuccess(List<Lesson> data) {
                    callback.success(data);
                    mCourseLessonsCache.append(courseId, data);
                }

                @Override
                public void onFailure(String code, String message) {
                    callback.success(null);
                }
            });
        }
    }

    private void setLiveStatus(final List<Lesson> lessonList, final TextView liveStartTimeLabel, final TextView liveStartTime) {
        int lessonNum = lessonList.size();
        if (lessonNum == 0) {
            liveStartTimeLabel.setVisibility(View.GONE);
            liveStartTime.setVisibility(View.GONE);
        } else {
            liveStartTimeLabel.setVisibility(View.VISIBLE);
            Lesson lessonCursor;
            long currentTime = System.currentTimeMillis();
            long startTime = 0;
            int backgroundResourceId = R.drawable.find_card_item_image_blue_label;
            String liveTag = LIVE_NOTE_START;
            for (int i = 0; i < lessonNum; i++) {
                lessonCursor = lessonList.get(i);
                if (lessonCursor.startTime * 1000 > currentTime) {
                    startTime = lessonCursor.startTime * 1000;
                    break;
                } else if (lessonCursor.startTime * 1000 < currentTime && lessonCursor.endTime * 1000 > currentTime) {
                    liveTag = LIVE_START;
                    backgroundResourceId = R.drawable.find_card_item_image_green_label;
                    startTime = lessonCursor.startTime * 1000;
                    break;
                } else if (currentTime > lessonCursor.endTime * 1000 && i == lessonNum - 1) {
                    liveTag = LIVE_FINISH;
                    backgroundResourceId = R.drawable.find_card_item_image_gray_label;
                }
            }
            liveStartTimeLabel.setText(liveTag);
            liveStartTimeLabel.setBackgroundResource(backgroundResourceId);
            if (startTime != 0) {
                liveStartTime.setVisibility(View.VISIBLE);
                liveStartTime.setText("直播时间：" + mLiveFormat.format(startTime));
            } else {
                liveStartTime.setVisibility(View.GONE);
            }
        }
    }

    private void setDiscoveryCardClickListener(View view, String type, int id) {
        if(type.equals("classroom")){
            view.setTag(R.id.card_cover, id);
            view.setOnClickListener(mViewOnClickListener);
        }else{
            view.setTag(R.id.card_cover, id);
            view.setOnClickListener(mViewOnClickListener2);
        }
    }

    View.OnClickListener mViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int id = (int) v.getTag(R.id.card_cover);
            EdusohoApp.app.mEngine.runNormalPlugin("ClassroomActivity", mContext, new PluginRunCallback() {
                @Override
                public void setIntentDate(Intent startIntent) {
                    startIntent.putExtra(Const.CLASSROOM_ID, id);
                }
            });
        }
    };


    View.OnClickListener mViewOnClickListener2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int id = (int) v.getTag(R.id.card_cover);
            EdusohoApp.app.mEngine.runNormalPlugin("CourseActivity", mContext, new PluginRunCallback() {
                @Override
                public void setIntentDate(Intent startIntent) {
                    startIntent.putExtra(Const.COURSE_ID, id);
                }
            });
        }
    };

    class ViewHolder {
        public ImageView coverView;
        public TextView titleView;
        public TextView priceView;
        public TextView studentNumView;

        public TextView liveTimeView;
        public TextView liveStartLabelView;
        public TextView liveNicknameView;
        public ImageView liveAvatarView;
    }
}
