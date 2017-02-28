package com.edusoho.kuozhi.v3.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.Classroom;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.sys.Cache;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.plugin.ShareTool;
import com.edusoho.kuozhi.v3.ui.fragment.mine.MineFragment;
import com.edusoho.kuozhi.v3.ui.fragment.mine.MyStudyFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.CourseCacheHelper;
import com.edusoho.kuozhi.v3.util.CourseUtil;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import com.edusoho.kuozhi.v3.view.dialog.MoreDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JesseHuang on 2017/2/10.
 */

public class MyClassroomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int EMPTY = 0;
    private static final int NOT_EMPTY = 1;
    private int mCurrentDataStatus;
    private Context mContext;
    private List<Classroom> mClassroomList;

    public MyClassroomAdapter(Context context) {
        this.mContext = context;
        mClassroomList = new ArrayList<>();
    }

    public void setClassrooms(List<Classroom> list) {
        mClassroomList = list;
        notifyDataSetChanged();
    }

    public void clear() {
        mClassroomList.clear();
    }

    @Override
    public int getItemViewType(int position) {
        return mCurrentDataStatus;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mCurrentDataStatus == NOT_EMPTY) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_classroom, parent, false);
            return new MyStudyFragment.ClassroomViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_empty, parent, false);
            return new MineFragment.EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (mCurrentDataStatus == NOT_EMPTY) {
            MyStudyFragment.ClassroomViewHolder classroomViewHolder = (MyStudyFragment.ClassroomViewHolder) viewHolder;
            final Classroom classroom = mClassroomList.get(position);
            classroomViewHolder.tvTitle.setText(String.valueOf(classroom.title));
            ImageLoader.getInstance().displayImage(classroom.getLargePicture(), classroomViewHolder.ivPic, EdusohoApp.app.mOptions);
            classroomViewHolder.rLayoutItem.setTag(classroom.id);
            classroomViewHolder.rLayoutItem.setOnClickListener(getClassroomViewClickListener());
            classroomViewHolder.tvMore.setTag(classroom);
            classroomViewHolder.tvMore.setOnClickListener(getMoreClickListener());
        }
    }

    @Override
    public int getItemCount() {
        if (mClassroomList != null && mClassroomList.size() != 0) {
            mCurrentDataStatus = NOT_EMPTY;
            return mClassroomList.size();
        }
        mCurrentDataStatus = EMPTY;
        return 1;
    }

    private View.OnClickListener getClassroomViewClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int classroomId = (int) v.getTag();
                CoreEngine.create(mContext).runNormalPlugin("ClassroomActivity", mContext, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(Const.CLASSROOM_ID, classroomId);
                    }
                });
            }
        };
    }

    private View.OnClickListener getMoreClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Classroom classroom = (Classroom) v.getTag();
                final MoreDialog dialog = new MoreDialog(mContext);
                dialog.init("退出班级", new MoreDialog.MoreCallBack() {
                    @Override
                    public void onMoveClick(View v, final Dialog dialog) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("确认退出班级")
                                .setMessage(R.string.delete_classroom)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dlg, int which) {
                                        CourseUtil.deleteClassroom(classroom.id, new CourseUtil.CallBack() {
                                            @Override
                                            public void onSuccess(String response) {
                                                CommonUtil.shortToast(mContext, "退出成功");
                                                mClassroomList.remove(classroom);
                                                notifyDataSetChanged();
                                                dlg.dismiss();
                                                dialog.dismiss();
                                                clearClassRoomCoursesCache(classroom.id);
                                            }

                                            @Override
                                            public void onError(String response) {
                                                dlg.dismiss();
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .create()
                                .show();
                    }

                    @Override
                    public void onShareClick(View v, Dialog dialog) {
                        final ShareTool shareTool = new ShareTool(mContext
                                , EdusohoApp.app.host + "/classroom/" + classroom.id
                                , classroom.title
                                , classroom.about.length() > 20 ? classroom.about.substring(0, 20) : classroom.about
                                , classroom.largePicture);
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

    private void clearClassRoomCoursesCache(int classRoomId) {
        Cache cache = SqliteUtil.getUtil(mContext).query(
                "select * from data_cache where key=? and type=?",
                "classroom-" + classRoomId,
                Const.CACHE_CLASSROOM_COURSE_IDS_TYPE
        );
        if (cache != null && cache.get() != null) {
            int[] ids = splitIntArrayByString(cache.get());
            if (ids.length <= 0) {
                return;
            }

            clearCoursesCache(ids);
        }
    }

    private int[] splitIntArrayByString(String idsString) {
        List<Integer> ids = new ArrayList<>();
        String[] splitArray = idsString.split(",");
        for (String item : splitArray) {
            int id = AppUtil.parseInt(item);
            if (id > 0) {
                ids.add(id);
            }
        }
        int[] idArray = new int[ids.size()];
        for (int i = 0; i < idArray.length; i++) {
            idArray[i] = ids.get(i);
        }
        return idArray;
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    private void clearCoursesCache(int... courseIds) {
        School school = getAppSettingProvider().getCurrentSchool();
        User user = getAppSettingProvider().getCurrentUser();
        new CourseCacheHelper(mContext, school.getDomain(), user.id).clearLocalCacheByCourseId(courseIds);
    }
}
