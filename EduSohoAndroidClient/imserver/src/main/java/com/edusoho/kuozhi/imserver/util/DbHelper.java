package com.edusoho.kuozhi.imserver.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.edusoho.kuozhi.imserver.helper.IDbManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 菊 on 2016/4/29.
 */
public class DbHelper extends SQLiteOpenHelper {

    private IDbManager mIMDbManager;

    public DbHelper(Context context, IDbManager dbManager) {
        super(context, dbManager.getName(), null, dbManager.getVersion());
        this.mIMDbManager = dbManager;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        List<String> sqlList = mIMDbManager.getInitSql();
        for (String sql : sqlList) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        List<String> sqlList = mIMDbManager.getIncrementSql(oldVersion);
        for (String sql : sqlList) {
            db.execSQL(sql);
        }
    }

    private String getRawQuerySql(String table, String selection, String orderBy, String limit) {
        StringBuilder stringBuilder = new StringBuilder("select * from ");
        stringBuilder.append(table);
        if (!TextUtils.isEmpty(selection)) {
            stringBuilder.append(" where ").append(selection);
        }
        if (!TextUtils.isEmpty(orderBy)) {
            stringBuilder.append(" order by ").append(orderBy);
        }

        if (!TextUtils.isEmpty(limit)) {
            stringBuilder.append(" limit ").append(limit);
        }

        return stringBuilder.toString();
    }

    public ArrayList<HashMap<String, String>> queryBySortAndLimit(String table, String selection, String[] selectionArgs, String orderBy, String limit) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(getRawQuerySql(table, selection, orderBy, limit), selectionArgs);
        ArrayList<HashMap<String, String>> resultList = new ArrayList<>();
        while (cursor.moveToNext()) {
            int columnCount = cursor.getColumnCount();
            HashMap<String, String> arrayMap = new HashMap<>();
            for (int i = 0; i < columnCount; i++) {
                arrayMap.put(cursor.getColumnName(i), cursor.getString(i));
            }
            resultList.add(arrayMap);
        }

        cursor.close();
        db.close();
        return resultList;
    }

    public ArrayList<HashMap<String, String>> queryBySort(String table, String selection, String[] selectionArgs, String orderBy) {
        return queryBySortAndLimit(table, selection, selectionArgs, orderBy, null);
    }

    /**
     * #1 is replace
     *
     * @param sql
     * @param selectionArgs
     * @return
     */
    public ArrayList<HashMap> rawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        ArrayList<HashMap> resultList = new ArrayList<>();
        while (cursor.moveToNext()) {
            int columnCount = cursor.getColumnCount();
            HashMap arrayMap = new HashMap();
            for (int i = 0; i < columnCount; i++) {
                arrayMap.put(cursor.getColumnName(i), cursor.getString(i));
            }
            resultList.add(arrayMap);
        }

        cursor.close();
        db.close();
        return resultList;
    }

    public ArrayList<HashMap<String, String>> query(String table, String selection, String[] selectionArgs) {
        return queryBySort(table, selection, selectionArgs, null);
    }

    public HashMap querySingleBySort(String table, String selection, String[] selectionArgs, String order) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table, null, selection, selectionArgs, null, null, order);

        HashMap result = null;
        if (cursor.moveToNext()) {
            result = new HashMap();
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                result.put(cursor.getColumnName(i), cursor.getString(i));
            }
        }

        cursor.close();
        db.close();
        return result;
    }

    public HashMap querySingle(String table, String selection, String[] selectionArgs) {
        return querySingleBySort(table, selection, selectionArgs, null);
    }

    public int delete(String table, String selection, String[] selectionArgs) {
        SQLiteDatabase db = getWritableDatabase();
        int resultId = db.delete(table, selection, selectionArgs);
        db.close();
        return resultId;
    }

    public long insert(String table, ContentValues cv) {
        SQLiteDatabase db = getWritableDatabase();
        long resultId = db.insert(table, null, cv);
        db.close();
        return resultId;
    }

    public int update(String table, ContentValues cv, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        int resultId = db.update(table, cv, whereClause, whereArgs);
        db.close();
        return resultId;
    }
}
