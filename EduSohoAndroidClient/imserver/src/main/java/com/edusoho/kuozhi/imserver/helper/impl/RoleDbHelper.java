package com.edusoho.kuozhi.imserver.helper.impl;

import android.content.ContentValues;
import android.content.Context;

import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.factory.DbManagerFactory;
import com.edusoho.kuozhi.imserver.util.DbHelper;
import com.edusoho.kuozhi.imserver.util.DbUtil;
import com.edusoho.kuozhi.imserver.util.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 菊 on 2016/4/29.
 */
public class RoleDbHelper {

    private static final String TABLE = "im_role";
    private DbHelper mDbHelper;

    public RoleDbHelper(Context context) {
        mDbHelper = new DbHelper(context, DbManagerFactory.getDefaultFactory().createIMDbManager(context));
    }

    public Role getRoleByType(String type, int rid) {
        HashMap arrayMap = mDbHelper.querySingle(TABLE, "type=? and rid=?", new String[]{type, String.valueOf(rid)});
        if (arrayMap == null || arrayMap.isEmpty()) {
            return new Role();
        }

        return createRole(arrayMap);
    }

    public Map<Integer, Role> getRoleMap(int[] rid) {
        String selectStr = String.format("id in (%s)", DbUtil.makePlaceholders(rid, ","));
        ArrayList<HashMap<String, String>> arrayList = mDbHelper.query(TABLE, selectStr, DbUtil.intArrayToStringArray(rid));
        Map<Integer, Role> roleMap = new HashMap<>();
        if (arrayList == null) {
            return roleMap;
        }
        for (HashMap<String, String> arrayMap : arrayList) {
            Role role = createRole(arrayMap);
            roleMap.put(role.getRid(), role);
        }

        return roleMap;
    }

    private Role createRole(HashMap<String, String> arrayMap) {
        if (arrayMap == null || arrayMap.isEmpty()) {
            return null;
        }
        Role role = new Role();
        role.setType(arrayMap.get("type"));
        role.setRid(MessageUtil.parseInt(arrayMap.get("rid")));
        role.setAvatar(arrayMap.get("avatar"));
        role.setNickname(arrayMap.get("nickname"));

        return role;
    }

    public long save(Role role) {
        ContentValues cv = new ContentValues();
        cv.put("rid", role.getRid());
        cv.put("type", role.getType());
        cv.put("nickname", role.getNickname());
        cv.put("avatar", role.getAvatar());
        return mDbHelper.insert(TABLE, cv);
    }

    public int update(Role role) {
        ContentValues cv = new ContentValues();
        cv.put("type", role.getType());
        cv.put("nickname", role.getNickname());
        cv.put("avatar", role.getAvatar());
        return mDbHelper.update(TABLE, cv, "rid=? and type=?", new String[]{String.valueOf(role.getRid()), role.getType()});
    }
}
