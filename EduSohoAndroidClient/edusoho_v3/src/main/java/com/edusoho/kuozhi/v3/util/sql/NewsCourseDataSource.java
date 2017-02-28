package com.edusoho.kuozhi.v3.util.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.edusoho.kuozhi.v3.model.bal.push.NewsCourseEntity;

import java.util.ArrayList;

/**
 * Created by JesseHuang on 15/9/16.
 */
public class NewsCourseDataSource {
    public static final String TABLE_NAME = "NEWS_COURSE";
    public String[] allColumns = {"ID", "COURSEID", "OBJECTID", "TITLE", "CONTENT", "FROMTYPE", "BODYTYPE", "LESSONTYPE", "USERID", "CREATEDTIME", "LESSONID", "HOMEWORKRESULTID", "QUESTIONID", "LEARNSTARTTIME", "LEARNFINISHTIME"};
    private SqliteChatUtil mDbHelper;
    private SQLiteDatabase mDataBase;

    public NewsCourseDataSource(SqliteChatUtil sqliteChatUtil) {
        mDbHelper = sqliteChatUtil;
    }

    public NewsCourseDataSource openWrite() throws SQLException {
        mDataBase = mDbHelper.getWritableDatabase();
        return this;
    }

    public NewsCourseDataSource openRead() throws SQLException {
        mDataBase = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        if (mDataBase.isOpen()) {
            mDataBase.close();
        }
        mDbHelper.close();
    }

    public ArrayList<NewsCourseEntity> getNewsCourses(int start, int limit, int courseId, int userId) {
        this.openRead();
        ArrayList<NewsCourseEntity> list = null;
        try {
            list = new ArrayList<>();
            String sql = String.format("COURSEID = %d AND USERID = %d", courseId, userId);
            Cursor cursor = mDataBase.query(TABLE_NAME, allColumns, sql, null, null, null, "CREATEDTIME DESC",
                    String.format("%d, %d", start, limit));
            while (cursor.moveToNext()) {
                list.add(cursorToEntity(cursor));
            }
            cursor.close();
        } catch (Exception ex) {
            Log.d("-->", ex.getMessage());
        }
        this.close();
        return list;
    }

    public NewsCourseEntity cursorToEntity(Cursor cursor) {
        NewsCourseEntity entity = new NewsCourseEntity();
        entity.setId(cursor.getInt(0));
        entity.setCourseId(cursor.getInt(1));
        entity.setObjectId(cursor.getInt(2));
        entity.setTitle(cursor.getString(3));
        entity.setContent(cursor.getString(4));
        entity.setFromType(cursor.getString(5));
        entity.setBodyType(cursor.getString(6));
        entity.setLessonType(cursor.getString(7));
        entity.setUserId(cursor.getInt(8));
        entity.setCreatedTime(cursor.getInt(9));
        entity.setLessonId(cursor.getInt(10));
        entity.setHomworkResultId(cursor.getInt(11));
        entity.setThreadId(cursor.getInt(12));
        entity.setLearnStartTime(cursor.getInt(13));
        entity.setLearnFinishTime(cursor.getInt(14));
        return entity;
    }

    public long create(NewsCourseEntity newsCourseEntity) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[0], newsCourseEntity.getId());
        cv.put(allColumns[1], newsCourseEntity.getCourseId());
        cv.put(allColumns[2], newsCourseEntity.getObjectId());
        cv.put(allColumns[3], newsCourseEntity.getTitle());
        cv.put(allColumns[4], newsCourseEntity.getContent());
        cv.put(allColumns[5], newsCourseEntity.getFromType());
        cv.put(allColumns[6], newsCourseEntity.getBodyType());
        cv.put(allColumns[7], newsCourseEntity.getLessonType());
        cv.put(allColumns[8], newsCourseEntity.getUserId());
        cv.put(allColumns[9], newsCourseEntity.getCreatedTime());
        cv.put(allColumns[10], newsCourseEntity.getLessonId());
        cv.put(allColumns[11], newsCourseEntity.getHomworkResultId());
        cv.put(allColumns[12], newsCourseEntity.getThreadId());
        cv.put(allColumns[13], newsCourseEntity.getLearnStartTime());
        cv.put(allColumns[14], newsCourseEntity.getLearnFinishTime());
        long effectRow = mDataBase.insert(TABLE_NAME, null, cv);
        this.close();
        return effectRow;
    }

    public void create(ArrayList<NewsCourseEntity> list) {
        this.openWrite();
        for (NewsCourseEntity newsCourseEntity : list) {
            ContentValues cv = new ContentValues();
            cv.put(allColumns[0], newsCourseEntity.getId());
            cv.put(allColumns[1], newsCourseEntity.getCourseId());
            cv.put(allColumns[2], newsCourseEntity.getObjectId());
            cv.put(allColumns[3], newsCourseEntity.getTitle());
            cv.put(allColumns[4], newsCourseEntity.getContent());
            cv.put(allColumns[5], newsCourseEntity.getFromType());
            cv.put(allColumns[6], newsCourseEntity.getBodyType());
            cv.put(allColumns[7], newsCourseEntity.getLessonType());
            cv.put(allColumns[8], newsCourseEntity.getUserId());
            cv.put(allColumns[9], newsCourseEntity.getCreatedTime());
            cv.put(allColumns[10], newsCourseEntity.getLessonId());
            cv.put(allColumns[11], newsCourseEntity.getHomworkResultId());
            cv.put(allColumns[12], newsCourseEntity.getThreadId());
            cv.put(allColumns[13], newsCourseEntity.getLearnStartTime());
            cv.put(allColumns[14], newsCourseEntity.getLearnFinishTime());
            mDataBase.insert(TABLE_NAME, null, cv);
        }
        this.close();
    }

    public int update(NewsCourseEntity newsCourseEntity) {
        this.openWrite();
        ContentValues cv = new ContentValues();
        cv.put(allColumns[1], newsCourseEntity.getCourseId());
        cv.put(allColumns[2], newsCourseEntity.getObjectId());
        cv.put(allColumns[3], newsCourseEntity.getTitle());
        cv.put(allColumns[4], newsCourseEntity.getContent());
        cv.put(allColumns[5], newsCourseEntity.getFromType());
        cv.put(allColumns[6], newsCourseEntity.getBodyType());
        cv.put(allColumns[7], newsCourseEntity.getLessonType());
        cv.put(allColumns[8], newsCourseEntity.getUserId());
        cv.put(allColumns[9], newsCourseEntity.getCreatedTime());
        cv.put(allColumns[10], newsCourseEntity.getLessonId());
        cv.put(allColumns[11], newsCourseEntity.getHomworkResultId());
        cv.put(allColumns[12], newsCourseEntity.getThreadId());
        cv.put(allColumns[13], newsCourseEntity.getLearnStartTime());
        cv.put(allColumns[14], newsCourseEntity.getLearnFinishTime());
        int effectRow = mDataBase.update(TABLE_NAME, cv, "ID = ?", new String[]{newsCourseEntity.getId() + ""});
        this.close();
        return effectRow;
    }

    public long delete(int courseId, int userId) {
        this.openWrite();
        long effectRow = mDataBase.delete(TABLE_NAME, "COURSEID = ? AND USERID = ?",
                new String[]{courseId + "", userId + ""});
        this.close();
        return effectRow;
    }
}
