package com.edusoho.kuozhi.v3.util.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.edusoho.kuozhi.v3.model.bal.thread.CourseThreadPostEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JesseHuang on 15/12/23.
 */
public class CourseThreadPostDataSource {
    private static final String TABLE_NAME = "COURSE_THREAD_POST";
    private SqliteChatUtil mDbHelper;
    private SQLiteDatabase mDataBase;
    private String[] allColumns = {
            "ID",
            "POSTID",
            "COURSEID",
            "LESSONID",
            "THREADID",
            "USERID",
            "NICKNAME",
            "HEADIMGURL",
            "ISELITE",
            "CONTENT",
            "TYPE",
            "DELIVERY",
            "CREATEDTIME"
    };

    public CourseThreadPostDataSource(SqliteChatUtil sqliteChatUtil) {
        mDbHelper = sqliteChatUtil;
    }

    public CourseThreadPostDataSource openWrite() throws SQLException {
        mDataBase = mDbHelper.getWritableDatabase();
        return this;
    }

    public CourseThreadPostDataSource openRead() throws SQLException {
        mDataBase = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        if (mDataBase.isOpen()) {
            mDataBase.close();
        }
        mDbHelper.close();
    }

    public CourseThreadPostEntity getPost(int id) {
        openRead();
        CourseThreadPostEntity model = new CourseThreadPostEntity();
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM COURSE_THREAD_POST WHERE ID = ?",
                new String[]{id + ""});
        while (cursor.moveToNext()) {
            model = convertCursor2CourseThreadPostEntity(cursor);
        }
        cursor.close();
        close();
        return model;
    }

    public ArrayList<CourseThreadPostEntity> getPosts(int threadId) {
        openRead();
        ArrayList<CourseThreadPostEntity> posts = new ArrayList<>();
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM COURSE_THREAD_POST WHERE THREADID = ? ORDER BY POSTID DESC",
                new String[]{threadId + ""});
        while (cursor.moveToNext()) {
            posts.add(convertCursor2CourseThreadPostEntity(cursor));
        }
        cursor.close();
        close();
        return posts;
    }

    public long create(CourseThreadPostEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], model.postId);
        cv.put(allColumns[2], model.courseId);
        cv.put(allColumns[3], model.lessonId);
        cv.put(allColumns[4], model.threadId);
        cv.put(allColumns[5], model.user.id);
        cv.put(allColumns[6], model.user.nickname);
        cv.put(allColumns[7], model.user.mediumAvatar);
        cv.put(allColumns[8], model.isElite);
        cv.put(allColumns[9], model.content);
        cv.put(allColumns[10], model.type);
        cv.put(allColumns[11], model.delivery);
        cv.put(allColumns[12], model.createdTime);
        Log.d("course thread", "create post:" + model.createdTime);
        long effectRow = mDataBase.insert(TABLE_NAME, null, cv);
        this.close();
        return effectRow;
    }

    public void create(List<CourseThreadPostEntity> posts) {
        this.openWrite();
        for (CourseThreadPostEntity model : posts) {
            ContentValues cv = new ContentValues();
            cv.put(allColumns[1], model.postId);
            cv.put(allColumns[2], model.courseId);
            cv.put(allColumns[3], model.lessonId);
            cv.put(allColumns[4], model.threadId);
            cv.put(allColumns[5], model.user.id);
            cv.put(allColumns[6], model.user.nickname);
            cv.put(allColumns[7], model.user.mediumAvatar);
            cv.put(allColumns[8], model.isElite);
            cv.put(allColumns[9], model.content);
            cv.put(allColumns[10], model.type);
            cv.put(allColumns[11], model.delivery);
            cv.put(allColumns[12], model.createdTime);
            Log.d("course thread", "create posts:" + model.createdTime);
            model.pid = (int) mDataBase.insert(TABLE_NAME, null, cv);
        }
        this.close();
    }

    public long update(CourseThreadPostEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], model.postId);
        cv.put(allColumns[2], model.courseId);
        cv.put(allColumns[3], model.lessonId);
        cv.put(allColumns[4], model.threadId);
        cv.put(allColumns[5], model.user.id);
        cv.put(allColumns[6], model.user.nickname);
        cv.put(allColumns[7], model.user.mediumAvatar);
        cv.put(allColumns[8], model.isElite);
        cv.put(allColumns[9], model.content);
        cv.put(allColumns[10], model.type);
        cv.put(allColumns[11], model.delivery);
        cv.put(allColumns[12], model.createdTime);
        Log.d("course thread", "update:" + model.createdTime);
        int effectRow = mDataBase.update(TABLE_NAME, cv, "ID = ?", new String[]{model.pid + ""});
        this.close();
        return effectRow;
    }

    public void delete(int id) {
        this.openWrite();
        mDataBase.delete(TABLE_NAME, "ID = ?",
                new String[]{id + ""});
        this.close();
    }

    public void deleteByThreadId(int threadId) {
        this.openWrite();
        mDataBase.delete(TABLE_NAME, "THREADID = ?",
                new String[]{threadId + ""});
        this.close();
    }

    public CourseThreadPostEntity convertCursor2CourseThreadPostEntity(Cursor cursor) {
        CourseThreadPostEntity model = new CourseThreadPostEntity();
        model.pid = cursor.getInt(0);
        model.postId = cursor.getInt(1);
        model.courseId = cursor.getInt(2);
        model.lessonId = cursor.getInt(3);
        model.threadId = cursor.getInt(4);
        model.user.id = cursor.getInt(5);
        model.user.nickname = cursor.getString(6);
        model.user.mediumAvatar = cursor.getString(7);
        model.isElite = cursor.getInt(8);
        model.content = cursor.getString(9);
        model.type = cursor.getString(10);
        model.delivery = cursor.getInt(11);
        model.createdTime = cursor.getString(12);
        return model;
    }
}
