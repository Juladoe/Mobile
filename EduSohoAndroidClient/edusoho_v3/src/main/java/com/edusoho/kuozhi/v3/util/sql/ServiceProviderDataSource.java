package com.edusoho.kuozhi.v3.util.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.edusoho.kuozhi.v3.model.bal.push.ServiceProviderModel;
import java.util.ArrayList;

/**
 * Created by howzhi on 15/9/24.
 */
public class ServiceProviderDataSource {

    public static final String TAG = "ServiceProvider";

    private static final String TABLE_NAME = "SP_MSG";
    private SqliteChatUtil mDbHelper;
    private SQLiteDatabase mDataBase;
    private String[] allColumns = {"ID", "SP_ID", "TITLE", "CONTENT", "TYPE", "BODY", "CREATEDTIME", "TOID"};

    public ServiceProviderDataSource(SqliteChatUtil sqliteChatUtil) {
        mDbHelper = sqliteChatUtil;
    }

    public ServiceProviderDataSource openWrite() throws SQLException {
        mDataBase = mDbHelper.getWritableDatabase();
        return this;
    }

    public ServiceProviderDataSource openRead() throws SQLException {
        mDataBase = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        if (mDataBase.isOpen()) {
            mDataBase.close();
        }
        mDbHelper.close();
    }

    public ServiceProviderModel getServiceProviderMsg(int id) {
        openRead();
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM SP_MSG WHERE id = ?", new String[]{ id + "" });
        ServiceProviderModel newCourse = null;
        if (cursor.moveToNext()) {
            newCourse = convertCursor2SPModel(cursor);
        }
        cursor.close();
        close();
        return newCourse;
    }

    public ArrayList<ServiceProviderModel> getServiceProviderMsgs(int toId, int start, int limit) {
        this.openRead();
        ArrayList<ServiceProviderModel> list = null;
        try {
            list = new ArrayList<>();
            String sql = String.format("toId = %d", toId);
            Cursor cursor = mDataBase.query(TABLE_NAME, allColumns, sql, null, null, null, "CREATEDTIME DESC",
                    String.format("%d, %d", start, limit));
            while (cursor.moveToNext()) {
                list.add(convertCursor2SPModel(cursor));
            }
            cursor.close();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
        this.close();
        return list;
    }

    public long create(ServiceProviderModel spModel) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], spModel.spId);
        cv.put(allColumns[2], spModel.title);
        cv.put(allColumns[3], spModel.content);
        cv.put(allColumns[4], spModel.type);
        cv.put(allColumns[5], spModel.body);
        cv.put(allColumns[6], spModel.createdTime);
        cv.put(allColumns[7], spModel.toId);
        long insertId = mDataBase.insert(TABLE_NAME, null, cv);
        close();
        return insertId;
    }

    public long update(ServiceProviderModel spModel) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], spModel.spId);
        cv.put(allColumns[2], spModel.title);
        cv.put(allColumns[3], spModel.content);
        cv.put(allColumns[4], spModel.type);
        cv.put(allColumns[5], spModel.body);
        cv.put(allColumns[6], spModel.createdTime);
        cv.put(allColumns[7], spModel.toId);
        long id = mDataBase.update(TABLE_NAME, cv, "id = ?", new String[]{spModel.id + ""});
        close();
        return id;
    }

    public long delete(String whereSql, String... whereArgs) {
        openWrite();
        long id = mDataBase.delete(TABLE_NAME, whereSql, whereArgs);
        close();
        return id;
    }

    public long deleteBySPId(int spId) {
        this.openWrite();
        long effectRow = mDataBase.delete(TABLE_NAME, "SP_ID = ?",
                new String[]{ spId + "" });
        this.close();
        return effectRow;
    }

    public ServiceProviderModel convertCursor2SPModel(Cursor cursor) {
        ServiceProviderModel newModel = new ServiceProviderModel();
        newModel.id = cursor.getInt(0);
        newModel.spId = cursor.getInt(1);
        newModel.title = cursor.getString(2);
        newModel.content = cursor.getString(3);
        newModel.type = cursor.getString(4);
        newModel.body = cursor.getString(5);
        newModel.createdTime = cursor.getInt(6);
        newModel.toId = cursor.getInt(7);
        return newModel;
    }

    public long delete(int id) {
        openWrite();
        long newId = mDataBase.delete(TABLE_NAME, "id = ?", new String[]{ id + ""});
        close();
        return newId;
    }
}
