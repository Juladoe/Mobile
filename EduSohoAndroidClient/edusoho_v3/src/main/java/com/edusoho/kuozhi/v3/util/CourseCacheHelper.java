package com.edusoho.kuozhi.v3.util;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.course.DownloadCourse;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.UtilFactory;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8DbModel;
import com.edusoho.kuozhi.v3.service.M3U8DownService;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by suju on 17/1/18.
 */

public class CourseCacheHelper {

    private Context mContext;
    private String mDomain;
    private int mUseriId;

    public CourseCacheHelper(Context context, String domain, int userId) {
        this.mContext = context;
        this.mUseriId = userId;
        this.mDomain = domain;
    }

    private Map<Integer, List<LessonItem>> getLocalLessonListByCourseIds(int isFinish, int... courseIds) {
        List<LessonItem> lessonItems = getLocalLessonList(null);
        int[] ids = getLessonIds(lessonItems);
        SparseArray<M3U8DbModel> m3U8DbModels = M3U8Util.getM3U8ModelList(
                mContext, ids, mUseriId, mDomain, isFinish);
        Map<Integer, List<LessonItem>> localLessons = new HashMap<>();
        for (LessonItem lessonItem : lessonItems) {
            if (m3U8DbModels.indexOfKey(lessonItem.id) < 0) {
                continue;
            }
            if (!localLessons.containsKey(lessonItem.courseId)) {
                if (courseIds == null || filterCourseId(lessonItem.courseId, courseIds)) {
                    localLessons.put(lessonItem.courseId, new ArrayList<LessonItem>());
                }
            }

            List<LessonItem> lessons = localLessons.get(lessonItem.courseId);
            if (lessons != null) {
                lessons.add(lessonItem);
            }
        }

        return localLessons;
    }

    private List<LessonItem> getLocalLessonList(int[] lessonIds) {
        final ArrayList<LessonItem> lessonItems = new ArrayList<>();
        SqliteUtil.QueryParser<ArrayList<LessonItem>> queryParser;
        queryParser = new SqliteUtil.QueryParser<ArrayList<LessonItem>>() {
            @Override
            public ArrayList<LessonItem> parse(Cursor cursor) {
                String value = cursor.getString(cursor.getColumnIndex("value"));
                LessonItem item = getUtilFactory().getJsonParser().fromJson(value, LessonItem.class);
                lessonItems.add(item);
                return lessonItems;
            }
        };

        StringBuffer lessonIdQuery = new StringBuffer();
        if (lessonIds != null) {
            lessonIdQuery = new StringBuffer(" and key in (");
            for (int id : lessonIds) {
                lessonIdQuery.append(id).append(",");
            }
            if (lessonIdQuery.length() > 1) {
                lessonIdQuery.deleteCharAt(lessonIdQuery.length() - 1);
            }
            lessonIdQuery.append(")");
        }
        SqliteUtil sqliteUtil = SqliteUtil.getUtil(mContext);
        sqliteUtil.query(
                queryParser,
                "select * from data_cache where type=?" + lessonIdQuery.toString(),
                Const.CACHE_LESSON_TYPE
        );

        return lessonItems;
    }

    public List<DownloadCourse> getLocalCourseList(
            int isFinish, int[] courseIds, int[] lessonIds) {
        List<DownloadCourse> localCourses = new ArrayList<>();
        List<LessonItem> lessonItems = getLocalLessonList(lessonIds);

        if (lessonItems != null) {
            int[] ids = getLessonIds(lessonItems);
            Collections.sort(lessonItems, new Comparator<LessonItem>() {
                @Override
                public int compare(LessonItem lhs, LessonItem rhs) {
                    if (lhs.courseId > rhs.courseId) {
                        return 1;
                    } else if (lhs.courseId == rhs.courseId) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });

            SparseArray<M3U8DbModel> m3U8DbModels = M3U8Util.getM3U8ModelList(
                    mContext, ids, mUseriId, mDomain, isFinish);
            Map<Integer, List<LessonItem>> localLessons = new HashMap<>();
            for (LessonItem lessonItem : lessonItems) {
                if (m3U8DbModels.indexOfKey(lessonItem.id) < 0) {
                    continue;
                }
                if (!localLessons.containsKey(lessonItem.courseId)) {
                    if (courseIds == null || filterCourseId(lessonItem.courseId, courseIds)) {
                        DownloadCourse coruse = getLocalCourse(lessonItem.courseId);
                        if (coruse != null) {
                            localCourses.add(coruse);
                        }
                        localLessons.put(lessonItem.courseId, new ArrayList<LessonItem>());
                    }
                }

                List<LessonItem> lessons = localLessons.get(lessonItem.courseId);
                if (lessons != null) {
                    lessons.add(lessonItem);
                }
            }

            Iterator<DownloadCourse> iterable = localCourses.iterator();
            while (iterable.hasNext()) {
                DownloadCourse course = iterable.next();
                List<LessonItem> lessons = localLessons.get(course.id);
                int[] cachedLessonIds = getCachedLessonIds(lessons, m3U8DbModels);
                course.setCachedLessonNum(cachedLessonIds.length);
                course.setCachedSize(getDownloadLessonListSize(cachedLessonIds));
            }
        } else {
            localCourses.clear();
            lessonItems.clear();
        }
        return localCourses;
    }

    public void clearLocalCacheByCourseId(int... courseIds) {
        Map<Integer, List<LessonItem>> lessonMap = getLocalLessonListByCourseIds(M3U8Util.FINISH, courseIds);
        if (lessonMap.isEmpty()) {
            return;
        }
        List<Integer> lessonIds = new ArrayList<>();
        for (List<LessonItem> lessonItems : lessonMap.values()) {
            int[] tempIds = getLessonIds(lessonItems);
            for (int id : tempIds) {
                lessonIds.add(id);
            }
        }
        clearLocalCache(lessonIds);
    }

    public void clearLocalCache(List<Integer> ids) {
        SqliteUtil sqliteUtil = SqliteUtil.getUtil(mContext);

        String m3u8LessonIds = coverM3U8Ids(ids);
        String cacheLessonIds = coverLessonIds(ids);
        sqliteUtil.execSQL(String.format(
                "delete from data_cache where type='%s' and key in %s",
                Const.CACHE_LESSON_TYPE,
                cacheLessonIds.toString()
                )
        );
        sqliteUtil.execSQL(String.format(
                "delete from data_m3u8 where host='%s' and lessonId in %s",
                mDomain,
                m3u8LessonIds.toString())
        );

        sqliteUtil.execSQL(String.format(
                "delete from data_m3u8_url where lessonId in %s",
                m3u8LessonIds.toString())
        );

        M3U8DownService service = M3U8DownService.getService();
        if (service != null) {
            service.cancelAllDownloadTask();
            for (int id : ids) {
                service.cancelDownloadTask(id);
            }
        }

        clearVideoCache(ids);
    }

    private int[] getCachedLessonIds(List<LessonItem> lessons, SparseArray<M3U8DbModel> m3U8DbModels) {
        List<LessonItem> cachedLessons = new ArrayList<>();
        for (LessonItem lessonItem : lessons) {
            M3U8DbModel m3U8DbModel = m3U8DbModels.get(lessonItem.id);
            if (m3U8DbModel != null && m3U8DbModel.finish == M3U8Util.FINISH) {
                cachedLessons.add(lessonItem);
            }
        }

        return getLessonIds(cachedLessons);
    }

    private boolean filterCourseId(int courseId, int[] courseIds) {
        for (int id : courseIds) {
            if (courseId == id) {
                return true;
            }
        }
        return false;
    }

    private void clearVideoCache(List<Integer> ids) {
        File workSpace = EdusohoApp.getWorkSpace();
        if (workSpace == null) {
            return;
        }
        File videosDir = new File(workSpace, "videos/" + mUseriId + "/" + mDomain);
        for (int id : ids) {
            FileUtils.deleteFile(new File(videosDir, String.valueOf(id)).getAbsolutePath());
        }
    }

    private int[] getLessonIds(List<LessonItem> lessons) {
        int index = 0;
        int[] ids = new int[lessons.size()];
        for (LessonItem lessonItem : lessons) {
            ids[index++] = lessonItem.id;
        }
        return ids;
    }

    private static String coverM3U8Ids(List<Integer> ids) {
        StringBuffer idsStr = new StringBuffer("(");
        for (int id : ids) {
            idsStr.append(id).append(",");
        }
        if (idsStr.length() > 1) {
            idsStr.deleteCharAt(idsStr.length() - 1);
        }
        idsStr.append(")");

        return idsStr.toString();
    }

    private DownloadCourse getLocalCourse(int courseId) {
        SqliteUtil.QueryParser<DownloadCourse> queryParser;
        queryParser = new SqliteUtil.QueryParser<DownloadCourse>() {
            @Override
            public DownloadCourse parse(Cursor cursor) {
                String value = cursor.getString(cursor.getColumnIndex("value"));
                DownloadCourse course = getUtilFactory().getJsonParser().fromJson(value, DownloadCourse.class);
                return course;
            }

            @Override
            public boolean isSingle() {
                return true;
            }
        };

        DownloadCourse course = SqliteUtil.getUtil(mContext).query(
                queryParser,
                "select * from data_cache where type=? and key=?",
                Const.CACHE_COURSE_TYPE,
                String.format("course-%d", courseId)
        );
        return course;
    }

    private static String coverLessonIds(List<Integer> ids) {
        StringBuffer idsStr = new StringBuffer("(");
        for (int id : ids) {
            idsStr.append("'lesson-").append(id).append("',");
        }
        if (idsStr.length() > 1) {
            idsStr.deleteCharAt(idsStr.length() - 1);
        }
        idsStr.append(")");

        return idsStr.toString();
    }

    private long getDownloadLessonListSize(int[] lessonIds) {
        long total = 0;
        for (int i = 0; i < lessonIds.length; i++) {
            total += getDownloadLessonSize(lessonIds[i]);
        }

        return total;
    }

    private long getDownloadLessonSize(int lessonId) {
        File dir = getLocalM3U8Dir(mUseriId, mDomain, lessonId);
        if (dir == null || !dir.exists()) {
            return 0;
        }

        return getCacheSize(dir);
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

    protected UtilFactory getUtilFactory() {
        return FactoryManager.getInstance().create(UtilFactory.class);
    }
}
