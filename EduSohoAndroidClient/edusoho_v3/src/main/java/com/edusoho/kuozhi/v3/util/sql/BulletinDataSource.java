package com.edusoho.kuozhi.v3.util.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.edusoho.kuozhi.v3.model.bal.push.Bulletin;

import java.util.ArrayList;

/**
 * Created by JesseHuang on 15/7/7.
 */
public class BulletinDataSource {
    private static final String TABLE_NAME = "BULLETIN";
    public String[] allColumns = {"ID", "CONTENT", "SCHOOLDOMAIN", "CREATEDTIME"};
    private SqliteChatUtil mDbHelper;
    private SQLiteDatabase mDataBase;

    public BulletinDataSource(SqliteChatUtil sqliteChatUtil) {
        mDbHelper = sqliteChatUtil;
    }

    public BulletinDataSource openWrite() throws SQLException {
        mDataBase = mDbHelper.getWritableDatabase();
        return this;
    }

    public BulletinDataSource openRead() throws SQLException {
        mDataBase = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public ArrayList<Bulletin> getBulletins(int start, int limit, String sql, String order) {
        this.openRead();
        ArrayList<Bulletin> list = null;
        try {
            list = new ArrayList<>();
            if (TextUtils.isEmpty(sql)) {
                sql = null;
            }
            Cursor cursor = mDataBase.query(TABLE_NAME, allColumns, sql, null, null, null, order,
                    String.format("%d, %d", start, limit));
            while (cursor.moveToNext()) {
                list.add(cursorToBulletin(cursor));
            }
            cursor.close();
        } catch (Exception ex) {
            Log.d("-->", ex.getMessage());
        }
        this.close();
        return list;
    }

    public long create(Bulletin bulletin) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[0], bulletin.id);
        long insertId = mDataBase.insert(TABLE_NAME, null, cv);
        this.close();
        return insertId;
    }

    public int getMaxId() {
        openRead();
        Cursor cursor = mDataBase.rawQuery("SELECT ID FROM BULLETIN ORDER BY CREATEDTIME DESC LIMIT 0,1", null);
        int maxId = 0;
        if (cursor.moveToNext()) {
            maxId = cursor.getInt(0);
        }
        cursor.close();
        close();
        return maxId;
    }

    private Bulletin cursorToBulletin(Cursor cursor) {
        Bulletin bulletin = new Bulletin();
        bulletin.id = cursor.getInt(0);
        return bulletin;
    }

    public int delete() {
        this.openWrite();
        int bulletinId = mDataBase.delete(TABLE_NAME, null, null);
        this.close();
        return bulletinId;
    }
}
