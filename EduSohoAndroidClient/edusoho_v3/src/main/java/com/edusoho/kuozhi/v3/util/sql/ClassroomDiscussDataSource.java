package com.edusoho.kuozhi.v3.util.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.edusoho.kuozhi.v3.model.bal.push.ClassroomDiscussEntity;

import java.util.ArrayList;

/**
 * Created by JesseHuang on 15/10/15.
 */
public class ClassroomDiscussDataSource {
    private static final String TABLE_NAME = "CLASSROOM_DISCUSS";
    private SqliteChatUtil mDbHelper;
    private SQLiteDatabase mDataBase;
    private String[] allColumns = {"DISCUSSID", "ID", "CLASSROOMID", "FROMID", "NICKNAME", "HEADIMGURL", "CONTENT", "BELONGID", "TYPE", "DELIVERY", "CREATEDTIME"};

    public ClassroomDiscussDataSource(SqliteChatUtil sqliteChatUtil) {
        mDbHelper = sqliteChatUtil;
    }

    public ClassroomDiscussDataSource openWrite() throws SQLException {
        mDataBase = mDbHelper.getWritableDatabase();
        return this;
    }

    public ClassroomDiscussDataSource openRead() throws SQLException {
        mDataBase = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        if (mDataBase.isOpen()) {
            mDataBase.close();
        }
        mDbHelper.close();
    }

    public ArrayList<ClassroomDiscussEntity> getLists(int classroomId, int belongId, int start, int limit) {
        openRead();
        ArrayList<ClassroomDiscussEntity> news = new ArrayList<>();
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM CLASSROOM_DISCUSS WHERE CLASSROOMID = ? AND BELONGID = ? ORDER BY CREATEDTIME DESC LIMIT ?, ? ",
                new String[]{classroomId + "", belongId + "", start + "", limit + ""});
        while (cursor.moveToNext()) {
            news.add(convertCursor2ClassroomDiscussEntity(cursor));
        }
        cursor.close();
        close();
        return news;
    }

    public long create(ClassroomDiscussEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], model.id);
        cv.put(allColumns[2], model.classroomId);
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

    public long update(ClassroomDiscussEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], model.id);
        cv.put(allColumns[2], model.classroomId);
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

    public void delete(int classroomId, int belongId) {
        this.openWrite();
        mDataBase.delete(TABLE_NAME, "CLASSROOMID = ? AND BELONGID = ?",
                new String[]{classroomId + "", belongId + ""});
        this.close();
    }

    public ClassroomDiscussEntity convertCursor2ClassroomDiscussEntity(Cursor cursor) {
        ClassroomDiscussEntity model = new ClassroomDiscussEntity();
        model.id = cursor.getInt(1);
        model.classroomId = cursor.getInt(2);
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
