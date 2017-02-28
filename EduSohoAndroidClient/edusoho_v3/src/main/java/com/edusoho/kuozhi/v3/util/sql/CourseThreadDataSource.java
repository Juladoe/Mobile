package com.edusoho.kuozhi.v3.util.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.edusoho.kuozhi.v3.model.bal.thread.CourseThreadEntity;
import com.edusoho.kuozhi.v3.util.CommonUtil;

/**
 * Created by JesseHuang on 15/12/23.
 */
public class CourseThreadDataSource {
    private static final String TABLE_NAME = "COURSE_THREAD";
    private SqliteChatUtil mDbHelper;
    private SQLiteDatabase mDataBase;
    private String[] allColumns = {
            "ID",
            "COURSEID",
            "LESSONID",
            "USERID",
            "NICKNAME",
            "HEADIMGURL",
            "TYPE",
            "ISSTICK",
            "ISELITE",
            "POSTNUM",
            "TITLE",
            "CONTENT",
            "CREATEDTIME"
    };

    public CourseThreadDataSource(SqliteChatUtil mDbHelper) {
        this.mDbHelper = mDbHelper;
    }

    public CourseThreadDataSource openWrite() throws SQLException {
        mDataBase = mDbHelper.getWritableDatabase();
        return this;
    }

    public CourseThreadDataSource openRead() throws SQLException {
        mDataBase = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        if (mDataBase.isOpen()) {
            mDataBase.close();
        }
        mDbHelper.close();
    }

    public CourseThreadEntity get(int threadId) {
        openRead();
        CourseThreadEntity model = null;
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM COURSE_THREAD WHERE ID = ?",
                new String[]{threadId + ""});
        while (cursor.moveToNext()) {
            model = convertCursor2CourseThreadEntity(cursor);
        }
        cursor.close();
        close();
        return model;
    }

    public long create(CourseThreadEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[0], model.id);
        cv.put(allColumns[1], model.courseId);
        cv.put(allColumns[2], model.lessonId);
        cv.put(allColumns[3], model.user.id);
        cv.put(allColumns[4], model.user.nickname);
        cv.put(allColumns[5], model.user.mediumAvatar);
        cv.put(allColumns[6], model.type);
        cv.put(allColumns[7], model.isStick);
        cv.put(allColumns[8], model.isElite);
        cv.put(allColumns[9], model.postNum);
        cv.put(allColumns[10], model.title);
        cv.put(allColumns[11], model.content);
        cv.put(allColumns[12], model.createdTime);
        long effectRow = mDataBase.insert(TABLE_NAME, null, cv);
        this.close();
        return effectRow;
    }

    public long update(CourseThreadEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], model.courseId);
        cv.put(allColumns[2], model.lessonId);
        cv.put(allColumns[3], model.user.id);
        cv.put(allColumns[4], model.user.nickname);
        cv.put(allColumns[5], model.user.mediumAvatar);
        cv.put(allColumns[6], model.type);
        cv.put(allColumns[7], model.isStick);
        cv.put(allColumns[8], model.isElite);
        cv.put(allColumns[9], model.postNum);
        cv.put(allColumns[10], model.title);
        cv.put(allColumns[11], model.content);
        cv.put(allColumns[12], CommonUtil.convertMilliSec(model.createdTime));
        int effectRow = mDataBase.update(TABLE_NAME, cv, "ID = ?", new String[]{model.id + ""});
        this.close();
        return effectRow;
    }

    public void delete(int threadId) {
        this.openWrite();
        mDataBase.delete(TABLE_NAME, "ID = ?",
                new String[]{threadId + ""});
        this.close();
    }

    public CourseThreadEntity convertCursor2CourseThreadEntity(Cursor cursor) {
        CourseThreadEntity model = new CourseThreadEntity();
        model.id = cursor.getInt(0);
        model.courseId = cursor.getInt(1);
        model.lessonId = cursor.getInt(2);
        model.user.id = cursor.getInt(3);
        model.user.nickname = cursor.getString(4);
        model.user.mediumAvatar = cursor.getString(5);
        model.type = cursor.getString(6);
        model.isStick = cursor.getInt(7);
        model.isElite = cursor.getInt(8);
        model.postNum = cursor.getInt(9);
        model.title = cursor.getString(10);
        model.content = cursor.getString(11);
        model.createdTime = cursor.getString(12);
        return model;
    }
}
