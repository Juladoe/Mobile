package com.edusoho.kuozhi.v3.util.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.edusoho.kuozhi.v3.model.sys.AudioCacheEntity;

/**
 * Created by JesseHuang on 15/12/29.
 */
public class AudioCacheDataSource {

    private static final String TABLE_NAME = "AUDIO_CACHE";
    private SqliteUtil mDbHelper;
    private SQLiteDatabase mDataBase;
    private String[] allColumns = {
            "ID",
            "LOCALPATH",
            "ONLINEPATH"
    };

    public AudioCacheDataSource(SqliteUtil sqliteUtil) {
        mDbHelper = sqliteUtil;
    }

    public AudioCacheDataSource openWrite() throws SQLException {
        mDataBase = mDbHelper.getWritableDatabase();
        return this;
    }

    public AudioCacheDataSource openRead() throws SQLException {
        mDataBase = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        if (mDataBase.isOpen()) {
            mDataBase.close();
        }
        mDbHelper.close();
    }

    public AudioCacheEntity getAudio(String path) {
        openRead();
        AudioCacheEntity model = null;
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM AUDIO_CACHE WHERE LOCALPATH = ? OR ONLINEPATH = ?",
                new String[]{path + "", path + ""});
        while (cursor.moveToNext()) {
            model = convertCursor2Model(cursor);
        }
        cursor.close();
        close();
        return model;
    }

    public AudioCacheEntity getAudio(String localPath, String onlinePath) {
        openRead();
        AudioCacheEntity model = null;
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM AUDIO_CACHE WHERE LOCALPATH = ? AND ONLINEPATH = ?",
                new String[]{localPath + "", onlinePath + ""});
        while (cursor.moveToNext()) {
            model = convertCursor2Model(cursor);
        }
        cursor.close();
        close();
        return model;
    }

    public AudioCacheEntity getAudio(int id) {
        openRead();
        AudioCacheEntity model = null;
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM AUDIO_CACHE WHERE ID = ?",
                new String[]{id + ""});
        while (cursor.moveToNext()) {
            model = convertCursor2Model(cursor);
        }
        cursor.close();
        close();
        return model;
    }

    public long create(String local, String online) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], local);
        cv.put(allColumns[2], online);
        long effectRow = mDataBase.insert(TABLE_NAME, null, cv);
        this.close();
        return effectRow;
    }

    public AudioCacheEntity create(AudioCacheEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], model.localPath);
        cv.put(allColumns[2], model.onlinePath);
        long id = mDataBase.insert(TABLE_NAME, null, cv);
        AudioCacheEntity result = getAudio((int) id);
        this.close();
        return result;
    }

    public void update(AudioCacheEntity model) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[0], model.id);
        cv.put(allColumns[1], model.localPath);
        cv.put(allColumns[2], model.onlinePath);
        mDataBase.update(TABLE_NAME, cv, "ID = ?", new String[]{model.id + ""});
        this.close();
    }

    private AudioCacheEntity convertCursor2Model(Cursor cursor) {
        AudioCacheEntity model = new AudioCacheEntity();
        model.id = cursor.getInt(0);
        model.localPath = cursor.getString(1);
        model.onlinePath = cursor.getString(2);
        return model;
    }

}
