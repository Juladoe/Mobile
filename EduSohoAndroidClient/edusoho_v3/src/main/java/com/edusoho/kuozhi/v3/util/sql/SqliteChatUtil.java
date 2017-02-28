package com.edusoho.kuozhi.v3.util.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.util.AssetsUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JesseHuang on 15/7/1.
 */
public class SqliteChatUtil extends SQLiteOpenHelper {
    private static final int VERSION = 10;
    private static String mCurDbName;
    private static SqliteChatUtil instance;
    private Context mContext;

    public SqliteChatUtil(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, null, VERSION);
        mContext = context;
        mCurDbName = name;
    }

    public static SqliteChatUtil getSqliteChatUtil(Context context, String dbName) {
        if (TextUtils.isEmpty(mCurDbName)) {
            mCurDbName = EdusohoApp.app.domain;
        }
        if (!mCurDbName.equals(dbName)) {
            instance = new SqliteChatUtil(context, dbName, null);
            return instance;
        }
        if (instance == null) {
            instance = new SqliteChatUtil(context, dbName, null);
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        List<String> sqlList = getInitSql("db_init_chat.sql");
        for (String sql : sqlList) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("SqliteChatUtil", String.format("ov:%d, nv:%d", oldVersion, newVersion));
        List<String> sqlList = getInitSql(String.format("db_init_chat.%s.sql", newVersion + ""));
        for (int i = oldVersion; i < newVersion; i++) {
            sqlList.addAll(getInitSql(String.format("db_init_chat.%s.sql", i + "")));
        }
        try {
            for (String sql : sqlList) {
                db.execSQL(sql);
            }
        } catch (Exception ex) {
            Log.d("SqliteChatUtil", "onUpgrade: " + ex.getMessage());
        }
    }

    private ArrayList<String> getInitSql(String name) {
        ArrayList<String> sqlList = new ArrayList<>();
        InputStream inputStream = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            inputStream = AssetsUtil.open(mContext, name);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                if (line.endsWith(";")) {
                    sqlList.add(stringBuilder.toString());
                    stringBuilder.delete(0, stringBuilder.length());
                }
            }
        } catch (Exception e) {
            Log.d("sqlchat", e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                Log.d("sqlchat", e.getMessage());
            }
        }

        return sqlList;
    }

    public void close() {
        getReadableDatabase().close();
        getWritableDatabase().close();
    }
}
