package com.edusoho.kuozhi.v3.service;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.broadcast.ResourceDownStatusReceiver;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.entity.lesson.LessonMaterial;
import com.edusoho.kuozhi.v3.entity.lesson.LessonResource;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import cn.trinea.android.common.util.DigestUtils;
import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ToastUtils;


/**
 * Created by howzhi on 14-10-11.
 */
public class DownLoadService extends Service {

    private String mNotifiTitle;
    private int mFileSize;
    private String mFileUrl;
    private Context mContext;
    private EdusohoApp app;
    private static DownLoadService instance;

    public static final int UPDATE = 0001;

    private SparseArray<HttpHead> mResourceDownloadTask;
    private HashMap<Long, Notification> notificationHashMap;
    private Timer mTimer;
    private DownloadManager downloadManager;
    private NotificationManager notificationManager;
    private ScheduledThreadPoolExecutor mThreadPoolExecutor;
    private SqliteUtil sqliteUtil;

    private HttpClient mHttpClient;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        Log.d(null, "download_service destroy");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        app = (EdusohoApp) getApplication();
        mContext = this;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        notificationHashMap = new HashMap<Long, Notification>();
        mResourceDownloadTask = new SparseArray<HttpHead>();
        sqliteUtil = SqliteUtil.getUtil(mContext);

        mThreadPoolExecutor = new ScheduledThreadPoolExecutor(3);
        mThreadPoolExecutor.setMaximumPoolSize(5);
        initHttpClient();
    }

    private void initHttpClient() {
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 100);
        //超时
        HttpConnectionParams.setConnectionTimeout(params, 3000);
        HttpConnectionParams.setSoTimeout(params, 3000);
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        mHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schReg), params);
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, DownLoadService.class);
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startDown(Context context, LessonMaterial lessonMaterial) {
        Intent intent = new Intent();
        intent.putExtra("LessonMaterial", lessonMaterial);
        intent.setClass(context, DownLoadService.class);
        context.startService(intent);
    }

    public static DownLoadService getService() {
        return instance;
    }

    public void cancelDownTask(LessonMaterial lessonMaterial) {
        HttpHead httpHead = mResourceDownloadTask.get(lessonMaterial.id);
        if (httpHead != null && !httpHead.isAborted()) {
            httpHead.abort();
            mResourceDownloadTask.remove(lessonMaterial.id);
            Log.d(null, "cancelDownload " + lessonMaterial.id);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            LessonMaterial lessonMaterial = (LessonMaterial) intent.getSerializableExtra("LessonMaterial");
            startDownTask(lessonMaterial);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startTimer() {
        if (mTimer != null) {
            return;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(null, "notificationHashMap->" + notificationHashMap.size());
                if (notificationHashMap.isEmpty()) {
                    mTimer.cancel();
                    mTimer = null;
                    return;
                }
                for (Long index : notificationHashMap.keySet()) {
                    queryDownloadStatus(index);
                }
            }
        }, 0, 100);
    }

    public static LessonResource queryDownTask(EdusohoApp app, int materialId) {
        SqliteUtil.QueryParser<LessonResource> queryParser = new SqliteUtil.QueryParser<LessonResource>() {
            @Override
            public LessonResource parse(Cursor cursor) {
                LessonResource resource = new LessonResource();
                resource.lessonId = cursor.getInt(cursor.getColumnIndex("lessonId"));
                resource.host = cursor.getString(cursor.getColumnIndex("host"));
                resource.userId = cursor.getInt(cursor.getColumnIndex("userId"));
                resource.download = cursor.getInt(cursor.getColumnIndex("download"));
                resource.total = cursor.getInt(cursor.getColumnIndex("total"));
                resource.finish = cursor.getInt(cursor.getColumnIndex("finish"));
                return resource;
            }
        };

        SqliteUtil sqliteUtil = SqliteUtil.getUtil(app);
        return sqliteUtil.query(
                queryParser,
                "select * from lesson_resource where materialId=? and userId=? and host=?",
                String.valueOf(materialId),
                String.valueOf(app.loginUser.id),
                String.valueOf(app.domain)
        );

    }

    private void addLessonResource(LessonMaterial lessonMaterial) {
        ContentValues cv = new ContentValues();
        cv.put("finish", 0);
        cv.put("host", app.domain);
        cv.put("lessonId", lessonMaterial.lessonId);
        cv.put("materialId", lessonMaterial.id);
        cv.put("userId", app.loginUser.id);
        cv.put("total", 0);
        cv.put("download", 0);
        sqliteUtil.insert("lesson_resource", cv);
    }

    private void updateLessonResource(int materialId, long download, int finish) {
        ContentValues cv = new ContentValues();
        cv.put("finish", finish);
        cv.put("download", download);
        sqliteUtil.update("lesson_resource", cv, "materialId=?", new String[]{String.valueOf(materialId)});
    }

    public void startDownTask(final LessonMaterial lessonMaterial) {
        LessonResource lessonResource = queryDownTask(app, lessonMaterial.id);
        if (lessonResource == null) {
            addLessonResource(lessonMaterial);
        }
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                downLoadFileByClient(lessonMaterial);
            }
        });
    }

    public static SparseArray<LessonResource> queryAllDownloadStatus(EdusohoApp app, int lessonId) {
        final SparseArray<LessonResource> list = new SparseArray<LessonResource>();
        SqliteUtil.QueryParser<LessonResource> queryParser = new SqliteUtil.QueryParser<LessonResource>() {
            @Override
            public LessonResource parse(Cursor cursor) {
                LessonResource resource = new LessonResource();
                resource.lessonId = cursor.getInt(cursor.getColumnIndex("lessonId"));
                resource.materialId = cursor.getInt(cursor.getColumnIndex("materialId"));
                resource.host = cursor.getString(cursor.getColumnIndex("host"));
                resource.userId = cursor.getInt(cursor.getColumnIndex("userId"));
                resource.download = cursor.getInt(cursor.getColumnIndex("download"));
                resource.total = cursor.getInt(cursor.getColumnIndex("total"));
                resource.finish = cursor.getInt(cursor.getColumnIndex("finish"));
                list.put(resource.materialId, resource);
                return resource;
            }
        };
        SqliteUtil sqliteUtil = SqliteUtil.getUtil(app);
        sqliteUtil.query(
                queryParser,
                "select * from lesson_resource where lessonId=? and host=? and userId=?",
                String.valueOf(lessonId),
                app.domain,
                String.valueOf(app.loginUser.id)
        );
        return list;
    }

    public static String getLocalResourceFileName(String name) {
        StringBuilder stringBuilder = new StringBuilder(DigestUtils.md5(name));
        String fileExt = CommonUtil.getFileExt(name);
        return stringBuilder.append(fileExt).toString();
    }

    private void downLoadFileByClient(final LessonMaterial lessonMaterial) {
        File resourceFile = null;
        HttpHead httpGet = new HttpHead(lessonMaterial.fileUri);
        try {
            Log.d(null, "start download -> " + lessonMaterial.title);
            File dir = getLocalResourceDir(mContext);

            resourceFile = new File(dir, getLocalResourceFileName(lessonMaterial.title));
            final long offset = resourceFile.length() > 0 ? resourceFile.length() - 1 : 0;
            httpGet.addHeader("Range", "bytes=" + offset + "-");
            Log.d(null, "download Range-> " + offset);
            HttpResponse response = mHttpClient.execute(httpGet);
            mResourceDownloadTask.put(lessonMaterial.id, httpGet);
            Log.d(null, "lessonMaterial.fileUri-> " + lessonMaterial.fileUri);
            InputStream inputStream = response.getEntity().getContent();

            NormalCallback<long[]> callback = new NormalCallback<long[]>() {
                @Override
                public void success(long[] offsets) {
                    //发送下载广播
                    Intent intent = new Intent(ResourceDownStatusReceiver.ACTION);
                    intent.putExtra("materialId", lessonMaterial.id);
                    intent.putExtra("download", offsets[0]);
                    intent.putExtra(Const.ACTIONBAR_TITLE, lessonMaterial.title);
                    mContext.sendBroadcast(intent);
                }
            };
            if (CommonUtil.writeRandomFile(resourceFile, inputStream, "rw", callback)) {
                Log.d(null, "end download -> " + lessonMaterial.title);
                updateLessonResource(lessonMaterial.id, resourceFile.length(), 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (resourceFile != null) {
                Log.d(null, "end update offset -> " + lessonMaterial.id);
                updateLessonResource(lessonMaterial.id, resourceFile.length(), 0);
            }
        } finally {
            httpGet.abort();
        }
    }

    public static File getLocalResourceDir(Context context) {
        File workSpace = EdusohoApp.getWorkSpace();
        if (workSpace == null) {
            ToastUtils.show(context, "没有内存卡，不能下载资料文件!");
            return null;
        }

        EdusohoApp app = EdusohoApp.app;
        if (app == null) {
            return null;
        }

        StringBuffer dirBuilder = new StringBuffer(workSpace.getAbsolutePath());
        dirBuilder.append("/lesson_resource/")
                .append(app.loginUser.id)
                .append("/")
                .append(app.domain);

        File resourceDir = new File(dirBuilder.toString());
        if (!resourceDir.exists()) {
            resourceDir.mkdirs();
        }

        return resourceDir;
    }

    private void downLoadFile(int lessonId) {
        Uri uri = Uri.parse(mFileUrl);
        if (uri == null) {
            Toast.makeText(mContext, "资源下载地址不存在！", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(false);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(mNotifiTitle));
        request.setMimeType(mimeString);

        //在通知栏中显示
        request.setShowRunningNotification(false);
        request.setVisibleInDownloadsUi(false);
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            request.setDestinationInExternalPublicDir("edusoho", mNotifiTitle);
        }

        long id = downloadManager.enqueue(request);
        createNotification(id);
        startTimer();

    }

    private void createNotification(long id) {
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        // 定义Notification的各种属性
        Notification notification = new Notification(R.drawable.notification_download_icon,
                "正在下载 " + mNotifiTitle, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_LIGHTS;

        Intent notificationIntent = new Intent();
        PendingIntent contentItent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = contentItent;
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.download_notification_layout);

        remoteViews.setTextViewText(R.id.notify_title, mNotifiTitle);
        remoteViews.setProgressBar(R.id.notify_progress, mFileSize, 0, false);
        remoteViews.setTextViewText(R.id.notify_percent, "0%");
        notification.contentView = remoteViews;

        // 把Notification传递给NotificationManager
        notificationManager.notify((int) id, notification);
        notificationHashMap.put(id, notification);
    }

    private void showComplteNotification(File file) {
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        // 定义Notification的各种属性
        Notification notification = new Notification(R.drawable.notification_download_icon,
                "下载完成 " + mNotifiTitle, System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.tickerText = "下载完成";

        Intent notificationIntent = CommonUtil.getViewFileIntent(file);
        PendingIntent contentItent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        notification.contentIntent = contentItent;

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.download_notification_layout);

        remoteViews.setTextViewText(R.id.notify_title, mNotifiTitle);
        remoteViews.setViewVisibility(R.id.notify_progress, View.GONE);
        remoteViews.setViewVisibility(R.id.notify_finish, View.VISIBLE);
        remoteViews.setTextViewText(R.id.notify_finish, "下载完成");
        remoteViews.setTextViewText(R.id.notify_percent, "100%");
        notification.contentView = remoteViews;

        // 把Notification传递给NotificationManager
        notificationManager.notify(0, notification);
    }

    private void finishNotification(long id, String filename) {
        Notification notification = notificationHashMap.get(id);
        if (notification == null) {
            return;
        }
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.tickerText = "下载完成";

        Intent notificationIntent = CommonUtil.getViewFileIntent(new File(filename));
        PendingIntent contentItent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        notification.contentIntent = contentItent;

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.download_notification_layout);

        remoteViews.setTextViewText(R.id.notify_title, mNotifiTitle);
        remoteViews.setViewVisibility(R.id.notify_progress, View.GONE);
        remoteViews.setViewVisibility(R.id.notify_finish, View.VISIBLE);
        remoteViews.setTextViewText(R.id.notify_finish, "下载完成");
        remoteViews.setTextViewText(R.id.notify_percent, "100%");
        notification.contentView = remoteViews;
        notificationManager.notify((int) id, notification);
        //EdusohoApp.app.sendMsgToTarget(LessonResourceActivity.INIT_STATUS, null, LessonResourceActivity.class);
    }

    private void updateNotification(long id, int total, int download) {
        Notification notification = notificationHashMap.get(id);
        if (notification == null) {
            return;
        }

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.download_notification_layout);

        remoteViews.setTextViewText(R.id.notify_title, mNotifiTitle);
        remoteViews.setProgressBar(R.id.notify_progress, total, download, false);
        float percent = (download / (float) total);
        remoteViews.setTextViewText(R.id.notify_percent, (int) (percent * 100) + "%");
        notification.contentView = remoteViews;
        notificationManager.notify((int) id, notification);
    }

    private void queryDownloadStatus(long id) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.v("down", "STATUS_PAUSED");
                case DownloadManager.STATUS_PENDING:
                    Log.v("down", "STATUS_PENDING");
                case DownloadManager.STATUS_RUNNING:
                    //正在下载，不做任何事情
                    int total = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    int download = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

                    Log.v("down", "total->" + total + " download->" + download);
                    updateNotification(id, total, download);
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    //完成
                    String localUrl = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    String fileName = getFilePathFromUri(Uri.parse(localUrl));
                    //String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    Log.v("down", "下载完成->" + fileName);
                    moveToCache(new File(fileName));
                    finishNotification(id, fileName);
                    notificationHashMap.remove(id);
                    downloadManager.remove(id);

                    break;
                case DownloadManager.STATUS_FAILED:
                    //清除已下载的内容，重新下载
                    //String reason = c.getString(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                    Log.v("down", "STATUS_FAILED");
                    downloadManager.remove(id);
                    notificationManager.cancel((int) id);
                    notificationHashMap.remove(id);
                    break;
            }

            c.close();
        }
    }

    private void moveToCache(File file) {
        String fileExt = CommonUtil.getFileExt(file.getName());
        String fileName = Base64.encodeToString(file.getName().getBytes(), Base64.NO_WRAP);
        File cacheDir = CommonUtil.getCacheFileDir();
        File targetFile = new File(cacheDir, fileName + fileExt);
        try {
            FileUtils.copyFile(file.getAbsolutePath(), targetFile.getAbsolutePath());
            file.delete();

            Log.v("move 移动完成->", targetFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if ("content".equals(uri.getScheme())) {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(uri, filePathColumn, null,
                    null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        } else if ("file".equals(uri.getScheme())) {
            filePath = new File(uri.getPath()).getAbsolutePath();
        }
        Log.d(null, "filePath=" + filePath);
        return filePath;
    }
}
