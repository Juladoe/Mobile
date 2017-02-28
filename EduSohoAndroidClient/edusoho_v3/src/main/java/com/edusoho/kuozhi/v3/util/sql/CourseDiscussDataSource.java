package com.edusoho.kuozhi.v3.util.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.edusoho.kuozhi.v3.model.bal.push.CourseDiscussEntity;

import java.util.ArrayList;

/**
 * Created by JesseHuang on 15/12/14.
 */
public class CourseDiscussDataSource {
    private static final String TABLE_NAME = "COURSE_DISCUSS";
    private SqliteChatUtil mDbHelper;
    private SQLiteDatabase mDataBase;
    private String[] allColumns = {"DISCUSSID", "ID", "COURSEID", "FROMID", "NICKNAME", "HEADIMGURL", "CONTENT", "BELONGID", "TYPE", "DELIVERY", "CREATEDTIME"};

    public CourseDiscussDataSource(SqliteChatUtil sqliteChatUtil) {
        mDbHelper = sqliteChatUtil;
    }

    public CourseDiscussDataSource openWrite() throws SQLException {
        mDataBase = mDbHelper.getWritableDatabase();
        return this;
    }

    public CourseDiscussDataSource openRead() throws SQLException {
        mDataBase = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        if (mDataBase.isOpen()) {
            mDataBase.close();
        }
        mDbHelper.close();
    }

    public ArrayList<CourseDiscussEntity> getLists(int courseId, int belongId, int start, int limit) {
        openRead();
        ArrayList<CourseDiscussEntity> news = new ArrayList<>();
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM COURSE_DISCUSS WHERE COURSEID = ? AND BELONGID = ? ORDER BY CREATEDTIME DESC LIMIT ?, ? ",
                new String[]{courseId + "", belongId + "", start + "", limit + ""});
        while (cursor.moveToNext()) {
            news.add(convertCursor2CourseDiscussEntity(cursor));
        }
        cursor.close();
        close();
        return news;
    }

    public CourseDiscussEntity get(String whereSql, String[] whereArgs) {
        CourseDiscussEntity model = null;
        openRead();
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM COURSE_DISCUSS WHERE " + whereSql, whereArgs);
        while (cursor.moveToNext()) {
            model = convertCursor2CourseDiscussEntity(cursor);
        }
        cursor.close();
        close();
        return model;
    }

    public long create(CourseDiscussEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], model.id);
        cv.put(allColumns[2], model.courseId);
        cv.put(allColumns[3], model.fromId);
        cv.put(allColumns[4], model.nickname);
        cv.put(allColumns[5], model.headImgUrl);
        cv.put(allColumns[6], model.content);
        cv.put(allColumns[7], model.belongId);
        cv.put(allColumns[8], model.type);
        cv.put(allColumns[9], model.delivery);
        cv.put(allColumns[10], model.createdTime);
        long effectRow = mDataBase.insert(TABLE_NAME, null, cv);
        this.close();
        return effectRow;
    }

    public long update(CourseDiscussEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], model.id);
        cv.put(allColumns[2], model.courseId);
        cv.put(allColumns[3], model.fromId);
        cv.put(allColumns[4], model.nickname);
        cv.put(allColumns[5], model.headImgUrl);
        cv.put(allColumns[6], model.content);
        cv.put(allColumns[7], model.belongId);
        cv.put(allColumns[8], model.type);
        cv.put(allColumns[9], model.delivery);
        cv.put(allColumns[10], model.createdTime);
        int effectRow = mDataBase.update(TABLE_NAME, cv, "DISCUSSID = ?", new String[]{model.discussId + ""});
        this.close();
        return effectRow;
    }

    public void delete(int courseId, int belongId) {
        this.openWrite();
        mDataBase.delete(TABLE_NAME, "COURSEID = ? AND BELONGID = ?",
                new String[]{courseId + "", belongId + ""});
        this.close();
    }

    public CourseDiscussEntity convertCursor2CourseDiscussEntity(Cursor cursor) {
        CourseDiscussEntity model = new CourseDiscussEntity();
        model.id = cursor.getInt(1);
        model.courseId = cursor.getInt(2);
        model.fromId = cursor.getInt(3);
        model.nickname = cursor.getString(4);
        model.headImgUrl = cursor.getString(5);
        model.content = cursor.getString(6);
        model.belongId = cursor.getInt(7);
        model.type = cursor.getString(8);
        model.delivery = cursor.getInt(9);
        model.createdTime = cursor.getInt(10);
        return model;
    }
}
