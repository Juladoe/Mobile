package com.edusoho.kuozhi.v3.util;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.broadcast.DownloadStatusReceiver;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.model.bal.m3u8.DownloadModel;
import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8DbModel;
import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8File;
import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8ListItem;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.trinea.android.common.util.DigestUtils;
import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by howzhi on 14/12/10.∂
 */
public class M3U8Util {

    public static final int DOWNING = 0;
    public static final int NONE = 1;
    public static final int ERROR = 2;
    public static final int PAUSE = 3;

    public static final int FINISH = 1;
    public static final int UN_FINISH = 0;
    public static final int ALL = 2;
    public static final int START = -1;
    public static final int DOWNLOAD_ERROR = 3;

    private static final String TAG = "M3U8Util";
    private static Pattern M3U8_STREAM_PAT = Pattern.compile(
            "#EXT-X-STREAM-INF:PROGRAM-ID=(\\d+),BANDWIDTH=(\\d+)", Pattern.DOTALL);
    private static Pattern M3U8_EXTINF_PAT = Pattern.compile(
            "#EXTINF:([\\d\\.]+),", Pattern.DOTALL);
    private static Pattern M3U8_EXT_X_KEY_PAT = Pattern.compile(
            "#EXT-X-KEY:METHOD=AES-128,URI=\"([^,\"]+)\",IV=(\\w+)", Pattern.DOTALL);
    private static Pattern URL_PAT = Pattern.compile("(#EXT-X-KEY:[^\n]+)?(http://[^\"\n]+)", Pattern.DOTALL);
    private Context mContext;
    private int mLessonId;
    private int mCourseId;
    private int mUserId;
    private String mLessonTitle;
    private String mLessonMediaUrl;
    private EdusohoApp app;
    private SqliteUtil mSqliteUtil;
    private String mTargetHost;
    private boolean isCancel;
    private int mDownloadStatus;
    private Hashtable<String, Integer> mTimeOutList;
    private ArrayList<Future> mFutures;
    private ScheduledThreadPoolExecutor mThreadPoolExecutor;
    private Queue<DownloadItem> mDownloadQueue;

    public M3U8Util(Context context) {
        this.mContext = context;
        this.app = EdusohoApp.app;
        this.mUserId = app.loginUser.id;
        this.mDownloadStatus = NONE;

        Uri hostUri = Uri.parse(app.host);
        if (hostUri != null) {
            this.mTargetHost = hostUri.getHost();
        }

        initDownloadEnv();
        mSqliteUtil = SqliteUtil.getUtil(mContext);
    }

    public int getCourseId() {
        return mCourseId;
    }

    public int getLessonId() {
        return mLessonId;
    }

    public int getUserId() {
        return mUserId;
    }

    private static M3U8DbModel parseM3U8Model(Cursor cursor) {
        M3U8DbModel m3U8DbModel = new M3U8DbModel();
        m3U8DbModel.id = cursor.getInt(cursor.getColumnIndex("id"));
        m3U8DbModel.finish = cursor.getInt(cursor.getColumnIndex("finish"));
        m3U8DbModel.downloadNum = cursor.getInt(cursor.getColumnIndex("download_num"));
        m3U8DbModel.totalNum = cursor.getInt(cursor.getColumnIndex("total_num"));
        m3U8DbModel.lessonId = cursor.getInt(cursor.getColumnIndex("lessonId"));
        m3U8DbModel.host = cursor.getString(cursor.getColumnIndex("host"));
        m3U8DbModel.playList = cursor.getString(cursor.getColumnIndex("play_list"));

        return m3U8DbModel;
    }

    /**
     * 获取视频缓存
     */
    public static M3U8DbModel queryM3U8Model(
            Context context, int userId, int lessonId, String host, int isFinish) {
        SqliteUtil.QueryParser<M3U8DbModel> queryCallBack =
                new SqliteUtil.QueryParser<M3U8DbModel>() {
                    @Override
                    public M3U8DbModel parse(Cursor cursor) {
                        return parseM3U8Model(cursor);
                    }
                };

        String finishQuery = isFinish == ALL ? "" : " and finish=" + isFinish;
        M3U8DbModel m3U8DbModel = SqliteUtil.getUtil(context).query(
                queryCallBack,
                "select * from data_m3u8 where userId=? and host=? and lessonId=?" + finishQuery,
                String.valueOf(userId), host, String.valueOf(lessonId)
        );
        if (m3U8DbModel == null) {
            return null;
        }

        Map<String, Integer> m3u8ItemMap = getDownloadM3U8ItemMap(context, lessonId);
        m3U8DbModel.downloadNum = m3u8ItemMap == null ? 0 : m3u8ItemMap.size();

        return m3U8DbModel;
    }

    public static ArrayList<M3U8DbModel> queryM3U8DownTasks(Context context, String host, int userId) {
        final ArrayList<M3U8DbModel> list = new ArrayList<>();
        SqliteUtil.QueryParser<M3U8DbModel> queryCallBack =
                new SqliteUtil.QueryParser<M3U8DbModel>() {
                    @Override
                    public M3U8DbModel parse(Cursor cursor) {
                        M3U8DbModel m3U8DbModel = parseM3U8Model(cursor);
                        list.add(m3U8DbModel);
                        return null;
                    }
                };

        SqliteUtil.getUtil(context).query(
                queryCallBack,
                "select * from data_m3u8 where userId=? and host=? and finish in (0, -1)",
                String.valueOf(userId),
                host
        );

        return list;
    }

    public static SparseArray<M3U8DbModel> getM3U8ModelList(
            Context context, int[] ids, int userId, String host, int isFinish) {
        final StringBuffer lessonIds = new StringBuffer("(");
        for (int id : ids) {
            lessonIds.append(id).append(",");
        }
        if (lessonIds.length() > 1) {
            lessonIds.deleteCharAt(lessonIds.length() - 1);
        }
        lessonIds.append(")");

        final SparseArray<M3U8DbModel> list = new SparseArray<>();
        SqliteUtil.QueryParser<M3U8DbModel> queryCallBack =
                new SqliteUtil.QueryParser<M3U8DbModel>() {
                    @Override
                    public M3U8DbModel parse(Cursor cursor) {
                        M3U8DbModel m3U8DbModel = parseM3U8Model(cursor);
                        list.put(m3U8DbModel.lessonId, m3U8DbModel);
                        return null;
                    }
                };

        if (isFinish == ALL) {
            SqliteUtil.getUtil(context).query(
                    queryCallBack,
                    "select * from data_m3u8 where userId=? and host=? and lessonId in " + lessonIds,
                    String.valueOf(userId),
                    host
            );
        } else {
            String finishQuery = isFinish == FINISH ? "finish=" + isFinish : "finish in (0, -1, 3)";
            SqliteUtil.getUtil(context).query(
                    queryCallBack,
                    "select * from data_m3u8 where userId=? and host=? and " + finishQuery + " and lessonId in " + lessonIds,
                    String.valueOf(userId),
                    host
            );
        }
        return list;
    }

    private static void clearM3U8Db(SqliteUtil sqliteUtil, int lessonId) {
        Log.d(TAG, "clear m3u8 db");
        sqliteUtil.delete(
                "data_m3u8",
                "lessonId = ?",
                new String[]{String.valueOf(lessonId)}
        );
        //清除以前的数据库缓存项
        sqliteUtil.delete(
                "data_m3u8_url",
                "lessonId=?",
                new String[]{String.valueOf(lessonId)}
        );
    }

    public boolean downloadQueueIsEmpty() {
        return mDownloadQueue.isEmpty();
    }

    public static M3U8DbModel saveM3U8Model(
            Context context, int lessonId, String host, int userId) {
        Log.d(TAG, "saveM3U8Model");
        SqliteUtil sqliteUtil = SqliteUtil.getUtil(context);
        clearM3U8Db(sqliteUtil, lessonId);
        ContentValues cv = new ContentValues();
        cv.put("finish", START);
        cv.put("total_num", 0);
        cv.put("download_num", 0);
        cv.put("userId", userId);
        cv.put("lessonId", lessonId);
        cv.put("host", host);
        cv.put("play_list", "");
        sqliteUtil.insert("data_m3u8", cv);

        M3U8DbModel m3U8DbModel = new M3U8DbModel();
        m3U8DbModel.finish = START;
        m3U8DbModel.lessonId = lessonId;
        m3U8DbModel.host = host;
        m3U8DbModel.userId = userId;

        return m3U8DbModel;
    }

    public int getDownloadStatus() {
        return mDownloadStatus;
    }

    public String getLessonTitle() {
        return mLessonTitle;
    }

    private InputStream readStreamFromNet(String url) {
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            Log.d(TAG, "get url " + url);
            HttpResponse response = client.execute(httpGet);
            return response.getEntity().getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void download(int lessonId, int courseId, int userId) {
        mLessonId = lessonId;
        mCourseId = courseId;
        mUserId = userId;

        setDownloadStatus(DOWNING);

        isCancel = false;
        initDownloadEnv();
        M3U8DbModel localM3U8DbModel = checkHasLocalM3U8Task(mLessonId, mUserId);
        if (localM3U8DbModel != null) {
            Log.d(TAG, "continue M3U8DbModle");
            if (localM3U8DbModel.downloadNum == localM3U8DbModel.totalNum) {
                completeTask(localM3U8DbModel);
                return;
            }
            M3U8File m3U8File = getM3U8FileFromModel(localM3U8DbModel);
            addM3U8SourceToQueue(m3U8File);
            for (int i = 0; i < 5; i++) {
                prepareDownload();
            }
            LessonItem lessonItem = mSqliteUtil.queryForObj(
                    new TypeToken<LessonItem>() {
                    },
                    "where type=? and key=?",
                    Const.CACHE_LESSON_TYPE,
                    "lesson-" + mLessonId
            );

            if (lessonItem != null) {
                mLessonTitle = lessonItem.title;
            }
            return;
        }

        Log.d(TAG, "load lesson " + lessonId);
        loadLessonUrl(lessonId, courseId);
    }

    public void setDownloadStatus(int status) {
        this.mDownloadStatus = status;
        ContentValues cv = new ContentValues();
        cv.put("finish", status);
        if (status == ERROR) {
            cv.put("finish", DOWNLOAD_ERROR);
            updateM3U8Model(cv, mLessonId, mTargetHost);
            clearDownloadEnv();
            return;
        }
        updateM3U8Model(cv, mLessonId, mTargetHost);
        sendBroadcast(status);
    }

    private M3U8DbModel checkHasLocalM3U8Task(int lessonId, int userId) {
        M3U8DbModel m3U8DbModel = queryM3U8Model(mContext, userId, lessonId, mTargetHost, ALL);
        if (m3U8DbModel == null) {
            return null;
        }

        if (m3U8DbModel.finish == FINISH
                || TextUtils.isEmpty(m3U8DbModel.playList)
                || (m3U8DbModel.downloadNum > 0 && m3U8DbModel.downloadNum == m3U8DbModel.totalNum)) {
            return null;
        }
        return m3U8DbModel;
    }

    private void loadLessonUrl(final int lessonId, int courseId) {
        RequestUrl requestUrl = app.bindUrl(Const.COURSELESSON, true);
        requestUrl.setParams(new String[]{
                "courseId", String.valueOf(courseId),
                "lessonId", String.valueOf(lessonId)
        });

        app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LessonItem lessonItem = null;
                try {
                    lessonItem = app.gson.fromJson(
                            response, new TypeToken<LessonItem>() {
                            }.getType()
                    );
                } catch (Exception e) {
                }
                if (lessonItem == null) {
                    setDownloadStatus(ERROR);
                    ToastUtils.show(mContext, "下载视频失败,请重新尝试下载!");
                    return;
                }

                mLessonMediaUrl = lessonItem.mediaUri;
                mLessonTitle = lessonItem.title;
                mThreadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mLessonMediaUrl != null && mLessonMediaUrl.contains("getLocalVideo")) {
                            downloadLocalVideos(mLessonMediaUrl);
                        } else {
                            parseM3U8(mLessonMediaUrl);
                        }
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setDownloadStatus(ERROR);
                ToastUtils.show(mContext, "下载视频失败,请重新尝试下载!");
            }
        });
    }

    private void sendBroadcast(int status) {
        Intent intent = new Intent(DownloadStatusReceiver.ACTION);
        intent.putExtra(Const.LESSON_ID, mLessonId);
        intent.putExtra(Const.COURSE_ID, mCourseId);
        intent.putExtra(Const.ACTIONBAR_TITLE, mLessonTitle);
        intent.putExtra(Const.STATUS, status);
        mContext.sendBroadcast(intent);
    }

    private void downloadLocalVideos(String videoUrl) {
        insertM3U8SourceToDb(mLessonId, videoUrl);
        FileOutputStream fos;
        InputStream is;
        try {
            java.net.URL url = new java.net.URL(videoUrl);
            URLConnection conn = url.openConnection();
            conn.connect();
            is = conn.getInputStream();
            int fileSize = conn.getContentLength();
            if (fileSize < 1 || is == null) {
            } else {
                ContentValues cv = new ContentValues();
                cv.put("finish", 0);
                cv.put("total_num", 100);
                cv.put("play_list", videoUrl);
                mSqliteUtil.update(
                        "data_m3u8",
                        cv,
                        "lessonId=? and host=?",
                        new String[]{String.valueOf(mLessonId), mTargetHost}
                );
                sendBroadcast(DOWNING);

                String key = DigestUtils.md5(videoUrl);
                File file = createLocalM3U8SourceFile(key);
                if (file == null) {
                    return;
                }
                fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                int downloadSize = 0;
                int downloadPercent = 1;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    downloadSize = downloadSize + len;
                    float percent = downloadSize / (float) fileSize;
                    if ((int) (percent * 100) == downloadPercent && downloadPercent <= 100) {
                        String updateSql = "update data_m3u8 set download_num = %d where userId = %d and host = '%s' and lessonId = %d";
                        mSqliteUtil.execSQL(String.format(updateSql, downloadPercent, mUserId, mTargetHost, mLessonId));
                        downloadPercent = downloadPercent + 5;
                        sendBroadcast(DOWNING);
                    }
                }
                fos.close();
                is.close();
                String updateSql = "update data_m3u8 set download_num = %d, finish = %d where userId = %d and host = '%s' and lessonId = %d";
                mSqliteUtil.execSQL(String.format(updateSql, 100, 1, mUserId, mTargetHost, mLessonId));
                String updateM3U8DataSql = "update data_m3u8_url set finish = %d and lessonId = %d";
                mSqliteUtil.execSQL(String.format(updateM3U8DataSql, 1, mLessonId));
                sendBroadcast(DOWNING);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, Integer> getDownloadM3U8ItemMap(Context context, int lessonId) {
        final Map<String, Integer> filters = new HashMap<>();
        SqliteUtil.QueryParser<HashMap<String, Integer>> queryCallBack =
                new SqliteUtil.QueryParser<HashMap<String, Integer>>() {
                    @Override
                    public HashMap<String, Integer> parse(Cursor cursor) {
                        String url = cursor.getString(cursor.getColumnIndex("url"));
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        filters.put(url, id);
                        return null;
                    }
                };

        SqliteUtil.getUtil(context).query(
                queryCallBack,
                "select * from data_m3u8_url where finish=? and lessonId=?",
                "1",
                String.valueOf(lessonId)
        );

        return filters;
    }

    private M3U8File getM3U8FileFromModel(M3U8DbModel m3U8DbModel) {
        StringReader reader = new StringReader(m3U8DbModel.playList);

        Map<String, Integer> filters = getDownloadM3U8ItemMap(mContext, m3U8DbModel.lessonId);
        M3U8File m3U8File = parseM3u8ListFile(new BufferedReader(reader), filters);
        return m3U8File;
    }

    /**
     * 获取网络的m3u8 file
     */
    public M3U8File getM3U8FileFromUrl(String url) {
        M3U8File m3U8File = null;

        int type = M3U8File.STREAM_LIST;
        while (type == M3U8File.STREAM_LIST) {
            if (m3U8File != null) {
                ArrayList<M3U8ListItem> m3u8List = m3U8File.m3u8List;
                int pos = m3u8List.size() > 2 ? 1 : 0;
                M3U8ListItem m3U8ListItem = m3u8List.get(pos);
                url = m3U8ListItem.url;
            }

            Log.d(TAG, "start parse m3u8 file " + m3U8File);
            InputStream inputStream = readStreamFromNet(url);
            if (inputStream == null) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            m3U8File = parseM3u8ListFile(reader, null);
            type = m3U8File.type;
        }

        return m3U8File;
    }

    /**
     * 解析m3u8
     */
    private void parseM3U8(String url) {
        M3U8File m3U8File = getM3U8FileFromUrl(url);

        Log.d(TAG, "m3U8File " + m3U8File);
        if (m3U8File == null || m3U8File.isEmpty()) {
            setDownloadStatus(ERROR);
        } else {
            if (m3U8File.type == M3U8File.STREAM) {
                setDownloadStatus(ERROR);
                ToastUtils.show(mContext, "视频格式不正确,不能下载!");
                return;
            }
            initM3U8DataToDb(m3U8File);
            addM3U8SourceToQueue(m3U8File);
            for (int i = 0; i < 5; i++) {
                prepareDownload();
            }
        }
    }

    private int updateM3U8Model(ContentValues cv, int lessonId, String host) {
        return mSqliteUtil.update(
                "data_m3u8",
                cv,
                "lessonId=? and host=?",
                new String[]{String.valueOf(lessonId), host}
        );
    }

    /*
        初始化需要下载的m3u8列表
    */
    private void initM3U8DataToDb(M3U8File m3U8File) {
        Log.d(TAG, "initM3U8DataToDb");
        ContentValues cv = new ContentValues();
        cv.put("finish", UN_FINISH);
        cv.put("total_num", m3U8File.urlList.size() + m3U8File.keyList.size());
        cv.put("play_list", m3U8File.content);
        updateM3U8Model(cv, mLessonId, mTargetHost);

        //插入需要下载的任务
        for (String key : m3U8File.keyList) {
            Log.d(TAG, "insert m3u8 key " + key);
            insertM3U8SourceToDb(mLessonId, key);
        }
        for (String key : m3U8File.urlList) {
            Log.d(TAG, "insert m3u8 src " + key);
            insertM3U8SourceToDb(mLessonId, key);
        }
    }

    /**
     * 插入每个需要下载的资源 url md5保存
     */
    private void insertM3U8SourceToDb(int lessonId, String url) {
        ContentValues cv = new ContentValues();
        cv.put("lessonId", lessonId);
        cv.put("finish", UN_FINISH);
        cv.put("url", DigestUtils.md5(url));
        mSqliteUtil.insert("data_m3u8_url", cv);
    }

    private void insertM3U8SourceDownloadId(long reference, String url, String type, int lessonId) {
        ContentValues cv = new ContentValues();
        cv.put("reference", reference);
        cv.put("targetId", lessonId);
        cv.put("url", url);
        cv.put("type", type);
        mSqliteUtil.insert("download_item", cv);
    }

    private String queryDownloadUriPath(long reference) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(reference);

        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(query);
        Uri fileUri = null;
        if (cursor.moveToFirst()) {
            int localURIIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            Log.d(TAG, "COLUMN_LOCAL_FILENAME:" + cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)));
            fileUri = Uri.parse(cursor.getString(localURIIndex));
        }
        cursor.close();

        if (fileUri == null) {
            return "";
        }
        return fileUri.getPath();
    }

    public int queryDownloadUriStatus(long reference) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(reference);

        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(query);
        int status = DownloadManager.STATUS_RUNNING;
        if (cursor.moveToFirst()) {
            int localStatusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            status = cursor.getInt(localStatusIndex);
        }
        cursor.close();

        return status;
    }

    /*
    status STATUS_SUCCESSFUL, STATUS_FAILED
     */
    public void updateDownloadStatus(DownloadModel downloadModel, int status) {
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            updateDownloadFinish(downloadModel);
            return;
        }
        if (status == DownloadManager.STATUS_FAILED) {
            setDownloadStatus(ERROR);
            return;
        }
        mDownloadQueue.add(new DownloadItem(downloadModel.url, downloadModel.type));
        prepareDownload();
    }

    private void removeSystemDownloadTask(DownloadManager downloadManager, String url, String type) {
        DownloadModel downloadModel = getDownloadModel(url, type);
        if (downloadModel != null) {
            Log.d(TAG, "removeRepeatTask:" + url);
            downloadManager.remove(downloadModel.reference);
            mSqliteUtil.delete("download_item", "url=? and type=?", new String[]{url, type});
        }
    }

    private DownloadModel getDownloadModel(String url, String type) {
        SqliteUtil.QueryParser<DownloadModel> queryCallBack =
                new SqliteUtil.QueryParser<DownloadModel>() {
                    @Override
                    public DownloadModel parse(Cursor cursor) {
                        DownloadModel downloadModel = new DownloadModel();
                        downloadModel.url = cursor.getString(cursor.getColumnIndex("url"));
                        downloadModel.type = cursor.getString(cursor.getColumnIndex("type"));
                        downloadModel.targetId = cursor.getInt(cursor.getColumnIndex("targetId"));
                        downloadModel.reference = cursor.getInt(cursor.getColumnIndex("reference"));
                        downloadModel.id = cursor.getInt(cursor.getColumnIndex("id"));
                        return downloadModel;
                    }
                };

        return SqliteUtil.getUtil(mContext).query(
                queryCallBack,
                "select * from download_item where url=? and type=?",
                url,
                type
        );
    }

    private void updateDownloadFinish(DownloadModel downloadModel) {
        try {
            saveDownloadItem(downloadModel);
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            removeSystemDownloadTask(downloadManager, downloadModel.url, downloadModel.type);
        } catch (FileNotFoundException fnfe) {
            Log.d(TAG, "copy file error:" + downloadModel.url);
            prepareDownload();
            return;
        }
        Log.d("updateDownloadStatus:", downloadModel.url + " finished");
        ContentValues cv = new ContentValues();
        cv.put("finish", FINISH);
        int result = mSqliteUtil.update(
                "data_m3u8_url",
                cv,
                "url=?",
                new String[]{DigestUtils.md5(downloadModel.url)}
        );

        if (result > 0) {
            sendSuccessBroadcast();
            //更新总计数器
            M3U8DbModel m3U8DbModel = updateM3U8DownloadNum();
            if (m3U8DbModel != null && m3U8DbModel.downloadNum == m3U8DbModel.totalNum) {
                M3U8DbModel localM3U8DbModel = checkHasLocalM3U8Task(mLessonId, mUserId);
                if (localM3U8DbModel != null) {
                    Log.d(TAG, "resume donwload m3u8");
                    M3U8File m3U8File = getM3U8FileFromModel(localM3U8DbModel);
                    addM3U8SourceToQueue(m3U8File);
                    prepareDownload();
                    return;
                }
                completeTask(m3U8DbModel);
            }
        }

        prepareDownload();
        Log.d(TAG, "update m3u8 src status " + downloadModel.url);
    }

    private void completeTask(M3U8DbModel m3U8DbModel) {
        ContentValues cv = new ContentValues();
        cv.put("finish", FINISH);
        try {
            String playListStr = createLocalM3U8File(m3U8DbModel);
            Log.d(TAG, "finish");
            cv.put("play_list", playListStr);
            mSqliteUtil.update(
                    "data_m3u8",
                    cv,
                    "host=? and lessonId=? and userId=?",
                    new String[]{
                            mTargetHost,
                            String.valueOf(mLessonId),
                            String.valueOf(mUserId)
                    }
            );
            mDownloadQueue.clear();
            sendSuccessBroadcast();
            Log.d(TAG, "completeTask");
        } catch (FileNotFoundException fe) {
            Log.d(TAG, fe.getMessage());
        }
    }

    private void saveKey(String key, String content) {
        ContentValues cv = new ContentValues();
        cv.put("type", Const.CACHE_KEY_TYPE);
        cv.put("key", "ext_x_key/" + key);
        cv.put("value", content);
        mSqliteUtil.insert("data_cache", cv);
    }

    private void saveDownloadItem(DownloadModel downloadModel) throws FileNotFoundException {
        String path = queryDownloadUriPath(downloadModel.reference);
        File targetFile = new File(path);
        Log.d(TAG, "targetFile:" + targetFile.exists());
        if ("key".equals(downloadModel.type)) {
            StringBuilder stringBuilder = FileUtils.readFile(path, "utf-8");
            if (stringBuilder != null) {
                saveKey(DigestUtils.md5(downloadModel.url), stringBuilder.toString());
            }
        }
        copyFile(DigestUtils.md5(downloadModel.url), targetFile);
        targetFile.delete();
    }

    private void sendSuccessBroadcast() {
        //发送下载广播
        Intent intent = new Intent(DownloadStatusReceiver.ACTION);
        intent.putExtra(Const.LESSON_ID, mLessonId);
        intent.putExtra(Const.COURSE_ID, mCourseId);
        intent.putExtra(Const.ACTIONBAR_TITLE, mLessonTitle);
        mContext.sendBroadcast(intent);
    }

    private synchronized M3U8DbModel updateM3U8DownloadNum() {
        //更新总计数器
        M3U8DbModel m3U8DbModel = queryM3U8Model(mContext, mUserId, mLessonId, mTargetHost, UN_FINISH);
        if (m3U8DbModel == null || m3U8DbModel.finish == FINISH) {
            return null;
        }
        if (m3U8DbModel.downloadNum == m3U8DbModel.totalNum) {
            return m3U8DbModel;
        }
        ContentValues cv = new ContentValues();
        Log.d(TAG, String.format("down:%d totalL%d", m3U8DbModel.downloadNum, m3U8DbModel.totalNum));
        cv.put("download_num", m3U8DbModel.downloadNum);
        mSqliteUtil.update(
                "data_m3u8",
                cv,
                "host=? and lessonId=? and userId=?",
                new String[]{
                        mTargetHost,
                        String.valueOf(mLessonId),
                        String.valueOf(mUserId)
                }
        );

        return m3U8DbModel;
    }

    public void cancelDownload() {
        isCancel = true;

        for (Future future : mFutures) {
            future.cancel(true);
        }
        clearDownloadEnv();
    }

    private void clearDownloadEnv() {
        if (mDownloadQueue != null) {
            mDownloadQueue.clear();
        }

        if (mTimeOutList != null) {
            mTimeOutList.clear();
        }
        if (mThreadPoolExecutor != null) {
            mThreadPoolExecutor.purge();
            mThreadPoolExecutor.shutdown();
        }

        mThreadPoolExecutor = null;
        mDownloadQueue = null;
        mTimeOutList = null;
    }

    private void initDownloadEnv() {
        if (mFutures != null
                && mTimeOutList != null
                && mDownloadQueue != null
                && mThreadPoolExecutor != null) {
            return;
        }
        mFutures = new ArrayList<>();
        mTimeOutList = new Hashtable<>();
        mDownloadQueue = new ArrayDeque<>();
        mThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        mThreadPoolExecutor.setMaximumPoolSize(1);
    }

    private boolean removeLocalKeyFile(File dir, String key) {
        File keyFile = new File(dir, key);
        return keyFile != null && keyFile.delete();
    }

    private String createLocalM3U8File(M3U8DbModel m3U8DbModel) throws FileNotFoundException {
        String playList = m3U8DbModel.playList;
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = URL_PAT.matcher(playList);

        String replaceStr = "http://localhost:8800/" + mLessonId + "/";
        File m3u8Dir = getLocalM3U8Dir();
        while (matcher.find()) {
            String url = matcher.group(2);
            String type = matcher.group(1);
            String key = DigestUtils.md5(url);
            if (type != null) {
                matcher.appendReplacement(
                        stringBuffer, type + "http://localhost:8800/ext_x_key/" + key);
                removeLocalKeyFile(m3u8Dir, key);
            } else {
                if (!new File(m3u8Dir, key).exists()) {
                    throw new FileNotFoundException(key + "file not exists");
                }

                matcher.appendReplacement(
                        stringBuffer, replaceStr + key + "?" + url.split("[?]")[1]);
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private void saveM3U8FileToStorage(String m3u8Str) {
        File dir = getLocalM3U8Dir();
        File m3u8File = new File(dir, "play.m3u8");
        FileUtils.writeFile(m3u8File.getAbsolutePath(), m3u8Str);
    }

    private void getResourceFromNet(String url, String type) {
        if (isCancel) {
            return;
        }
        Future future = mThreadPoolExecutor.schedule(new DownloadRunnable(type, url), 1, TimeUnit.MILLISECONDS);
        mFutures.add(future);
    }

    private void processTimeout(String type, String key, String url) {
        //超时处理
        int count = mTimeOutList.containsKey(key) ? mTimeOutList.get(key) : 0;
        Log.d(TAG, "timeiout count " + count);
        if (count < 30) {
            getResourceFromNet(url, type);
            mTimeOutList.put(key, ++count);
        }
    }

    /*
        开始下载m3u8资源
    */
    private void addM3U8SourceToQueue(M3U8File m3U8File) {
        Log.d(TAG, "start addM3U8SourceToQueue");

        ArrayList<String> keyList = m3U8File.keyList;
        ArrayList<String> urlList = m3U8File.urlList;

        for (String url : keyList) {
            mDownloadQueue.add(new DownloadItem("key", url));
        }

        for (String url : urlList) {
            mDownloadQueue.add(new DownloadItem("url", url));
        }
    }

    private void prepareDownload() {
        if (mDownloadQueue.isEmpty()) {
            return;
        }

        DownloadItem downloadItem = mDownloadQueue.poll();
        if (downloadItem == null) {
            return;
        }
        getResourceFromNet(downloadItem.url, downloadItem.type);
    }

    private File getLocalM3U8Dir() {
        File workSpace = EdusohoApp.getWorkSpace();
        if (workSpace == null) {
            ToastUtils.show(mContext, "没有内存卡，不能下载视频文件!");
            return null;
        }

        StringBuffer dirBuilder = new StringBuffer(workSpace.getAbsolutePath());
        dirBuilder.append("/videos/")
                .append(mUserId)
                .append("/")
                .append(mTargetHost)
                .append("/")
                .append(mLessonId);

        File lessonDir = new File(dirBuilder.toString());
        if (!lessonDir.exists()) {
            lessonDir.mkdirs();
        }

        return lessonDir;
    }

    private File createLocalM3U8SourceFile(String name) {
        File m3u8FileDir = getLocalM3U8Dir();
        if (m3u8FileDir == null) {
            return null;
        }

        return new File(m3u8FileDir, name);
    }

    /*
        解析m3u8列表
    */
    private M3U8File parseM3u8ListFile(
            BufferedReader reader, Map<String, Integer> filters) {
        M3U8File m3U8File = new M3U8File();
        StringBuilder stringBuilder = new StringBuilder();
        int pos = -1;
        String oldKey = null;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
                Matcher matcher = M3U8_STREAM_PAT.matcher(line);
                if (matcher.find()) {
                    if (m3U8File.m3u8List == null) {
                        m3U8File.type = M3U8File.STREAM_LIST;
                        m3U8File.m3u8List = new ArrayList<>();
                    }

                    M3U8ListItem item = new M3U8ListItem();
                    item.bandwidth = Integer.parseInt(matcher.group(2));
                    item.programId = Integer.parseInt(matcher.group(1));
                    m3U8File.m3u8List.add(item);
                    pos = 0;
                    continue;
                }

                matcher = M3U8_EXT_X_KEY_PAT.matcher(line);
                if (matcher.find()) {
                    m3U8File.type = M3U8File.PLAY_LIST;
                    String key = matcher.group(1);
                    if (!key.equals(oldKey)) {
                        oldKey = key;
                        if (filters == null || !filters.containsKey(DigestUtils.md5(key))) {
                            m3U8File.keyList.add(key);
                        }
                    }
                    pos = 1;
                    continue;
                }

                matcher = M3U8_EXTINF_PAT.matcher(line);
                if (matcher.find()) {
                    m3U8File.type = M3U8File.PLAY_LIST;
                    pos = 2;
                    continue;
                }
                //判断url类型
                switch (pos) {
                    case 0:
                        M3U8ListItem m3U8ListItem = m3U8File.m3u8List.get(
                                m3U8File.m3u8List.size() - 1);
                        m3U8ListItem.url = line;
                        pos = -1;
                        break;
                    case 1:
                        break;
                    case 2:
                        if (filters == null || !filters.containsKey(DigestUtils.md5(line))) {
                            m3U8File.urlList.add(line);
                        }
                        pos = -1;
                        break;
                    default:
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        m3U8File.content = stringBuilder.toString();
        Log.d(TAG, "end parse m3u8 file ");
        return m3U8File;
    }

    public static class DigestInputStream extends InputStream {

        private InputStream mTargetInputStream;
        private int mCurrentDigestIndex;
        private byte[] mDigestKey;

        public DigestInputStream(InputStream target, String host) {
            initDigestKey(host, true);
            this.mTargetInputStream = target;
        }

        public DigestInputStream(InputStream target, String host, boolean isMd5) {
            initDigestKey(host, isMd5);
            this.mTargetInputStream = target;
        }

        private void initDigestKey(String host, boolean isMd5) {

            String digestStr = host;
            if (isMd5) {
                if (!TextUtils.isEmpty(host)) {
                    digestStr = DigestUtils.md5(host);
                }
            }

            this.mCurrentDigestIndex = 0;
            this.mDigestKey = digestStr.getBytes();
        }

        @Override
        public int read() throws IOException {
            int length = mTargetInputStream.read();
            byte[] buffer = new byte[1];
            buffer[0] = (byte) length;
            processorByteArray(1, buffer);
            return buffer[0];
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            int length = mTargetInputStream.read(buffer);
            processorByteArray(length, buffer);
            return length;
        }

        @Override
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            int length = mTargetInputStream.read(buffer, byteOffset, byteCount);
            processorByteArray(length, buffer);

            return length;
        }

        @Override
        public void close() throws IOException {
            super.close();
            mTargetInputStream.close();
        }

        private void processorByteArray(int length, byte[] buffer) {
            if (length <= 0 || this.mDigestKey.length == 0) {
                return;
            }

            int keyLength = mDigestKey.length - 1;
            for (int i = 0; i < length; i++) {
                byte b = buffer[i];
                mCurrentDigestIndex = mCurrentDigestIndex > keyLength ? 0 : mCurrentDigestIndex;
                b = (byte) (b ^ mDigestKey[mCurrentDigestIndex++]);
                buffer[i] = b;
            }
        }
    }

    class DownloadItem {
        public String url;
        public String type;

        public DownloadItem(String type, String url) {
            this.type = type;
            this.url = url;
        }
    }

    private void copyFile(String key, File targetFile) throws FileNotFoundException {
        File file = createLocalM3U8SourceFile(key);
        if (file == null) {
            Log.d(TAG, "file download error" + key);
            return;
        }

        boolean isSave = FileUtils.writeFile(
                file,
                new DigestInputStream(new FileInputStream(targetFile), mTargetHost)
        );

        Log.d(TAG, "isSave " + isSave);
        if (!isSave) {
            file.delete();
            throw new RuntimeException("down error");
        }
    }

    class DownloadRunnable implements Runnable {
        public String url;
        public String type;

        public DownloadRunnable(String type, String url) {
            this.type = type;
            this.url = url;
        }

        @Override
        public void run() {
            if (!AppUtil.isWiFiConnect(mContext) && EdusohoApp.app.config.offlineType == 0) {
                ToastUtils.show(mContext, R.string.download_no_network);
                setDownloadStatus(ERROR);
                return;
            }
            String key = DigestUtils.md5(url);

            DownloadManager downloadManager;
            downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

            request.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, key);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.addRequestHeader("Android", "Android-kuozhi v3");
            if (EdusohoApp.app.config.offlineType == 1) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            }

            long reference = downloadManager.enqueue(request);
            insertM3U8SourceDownloadId(reference, url, type, mLessonId);
        }
    }
}
