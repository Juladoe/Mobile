package com.edusoho.kuozhi.imserver.helper.impl;

import android.content.ContentValues;
import android.content.Context;

import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.factory.DbManagerFactory;
import com.edusoho.kuozhi.imserver.helper.IConvDbHelper;
import com.edusoho.kuozhi.imserver.util.DbHelper;
import com.edusoho.kuozhi.imserver.util.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 菊 on 2016/4/29.
 */
public class ConvDbHelper implements IConvDbHelper {

    private static final String TABLE = "im_conv";
    private DbHelper mDbHelper;

    public ConvDbHelper(Context context) {
        mDbHelper = new DbHelper(context, DbManagerFactory.getDefaultFactory().createIMDbManager(context));
    }

    public List<ConvEntity> getConvListByType(String type) {
        ArrayList<HashMap<String, String>> arrayList = mDbHelper.query(TABLE, "type=?", new String[]{type});
        List<ConvEntity> entityList = new ArrayList<>();
        if (arrayList == null) {
            return entityList;
        }
        for (HashMap<String, String> arrayMap : arrayList) {
            ConvEntity convEntity = createConvEntity(arrayMap);
            entityList.add(convEntity);
        }

        return entityList;
    }

    public List<ConvEntity> getConvListByUid(int uid) {
        ArrayList<HashMap<String, String>> arrayList = mDbHelper.queryBySort(TABLE, "uid=? and updatedTime>0", new String[]{String.valueOf(uid)}, "updatedTime desc");
        List<ConvEntity> entityList = new ArrayList<>();
        if (arrayList == null) {
            return entityList;
        }
        for (HashMap<String, String> arrayMap : arrayList) {
            ConvEntity convEntity = createConvEntity(arrayMap);
            entityList.add(convEntity);
        }

        return entityList;
    }

    public List<ConvEntity> getConvList() {
        ArrayList<HashMap<String, String>> arrayList = mDbHelper.queryBySort(TABLE, "convNo!=?", new String[]{""}, "updatedTime desc");
        List<ConvEntity> entityList = new ArrayList<>();
        if (arrayList == null) {
            return entityList;
        }
        for (HashMap<String, String> arrayMap : arrayList) {
            ConvEntity convEntity = createConvEntity(arrayMap);
            entityList.add(convEntity);
        }

        return entityList;
    }

    private ConvEntity createConvEntity(HashMap<String, String> arrayMap) {
        if (arrayMap == null || arrayMap.isEmpty()) {
            return null;
        }
        ConvEntity convEntity = new ConvEntity();
        convEntity.setId(MessageUtil.parseInt(arrayMap.get("id")));
        convEntity.setConvNo(arrayMap.get("convNo"));
        convEntity.setTargetName(arrayMap.get("targetName"));
        convEntity.setTargetId(MessageUtil.parseInt(arrayMap.get("targetId")));
        convEntity.setLaterMsg(arrayMap.get("laterMsg"));
        convEntity.setType(arrayMap.get("type"));
        convEntity.setUid(MessageUtil.parseInt(arrayMap.get("uid")));
        convEntity.setAvatar(arrayMap.get("avatar"));
        convEntity.setUnRead(MessageUtil.parseInt(arrayMap.get("unRead")));
        convEntity.setCreatedTime(MessageUtil.parseLong(arrayMap.get("createdTime")));
        convEntity.setUpdatedTime(MessageUtil.parseLong(arrayMap.get("updatedTime")));

        return convEntity;
    }

    public ConvEntity getConv(String convNo) {
        HashMap arrayMap = mDbHelper.querySingle(TABLE, "convNo=?", new String[]{convNo});
        return createConvEntity(arrayMap);
    }

    public ConvEntity getConvByConvNo(String convNo) {
        HashMap arrayMap = mDbHelper.querySingle(TABLE, "convNo=?", new String[]{convNo});
        return createConvEntity(arrayMap);
    }

    public ConvEntity getConvByTypeAndId(String type, int targetId) {
        HashMap arrayMap = mDbHelper.querySingle(TABLE, "type=? and targetId=?", new String[]{type, String.valueOf(targetId)});
        return createConvEntity(arrayMap);
    }

    public int deleteByConvNo(String convNo) {
        return mDbHelper.delete(TABLE, "convNo=?", new String[]{convNo});
    }

    public int deleteByTypeAndId(String type, int targetId) {
        return mDbHelper.delete(TABLE, "type=? and targetId=?", new String[]{type, String.valueOf(targetId)});
    }

    public int deleteById(int id) {
        return mDbHelper.delete(TABLE, "id=?", new String[]{String.valueOf(id)});
    }

    public long save(ConvEntity convEntity) {
        ContentValues cv = new ContentValues();
        cv.put("convNo", convEntity.getConvNo());
        cv.put("targetId", convEntity.getTargetId());
        cv.put("targetName", convEntity.getTargetName());
        cv.put("laterMsg", convEntity.getLaterMsg());
        cv.put("createdTime", convEntity.getCreatedTime());
        cv.put("updatedTime", convEntity.getUpdatedTime());
        cv.put("type", convEntity.getType());
        cv.put("uid", convEntity.getUid());
        cv.put("avatar", convEntity.getAvatar());
        cv.put("unRead", convEntity.getUnRead());
        return mDbHelper.insert(TABLE, cv);
    }

    public int updateUnRead(String convNo, int unRead) {
        ContentValues cv = new ContentValues();
        cv.put("unRead", unRead);
        return mDbHelper.update(TABLE, cv, "convNo=?", new String[]{convNo});
    }

    public int updateByConvNo(ConvEntity convEntity) {
        ContentValues cv = new ContentValues();
        cv.put("targetName", convEntity.getTargetName());
        cv.put("laterMsg", convEntity.getLaterMsg());
        cv.put("updatedTime", convEntity.getUpdatedTime());
        cv.put("avatar", convEntity.getAvatar());
        cv.put("unRead", convEntity.getUnRead());
        return mDbHelper.update(TABLE, cv, "convNo=?", new String[]{String.valueOf(convEntity.getConvNo())});
    }

    public int update(ConvEntity convEntity) {
        ContentValues cv = new ContentValues();
        cv.put("targetId", convEntity.getTargetId());
        cv.put("targetName", convEntity.getTargetName());
        cv.put("laterMsg", convEntity.getLaterMsg());
        cv.put("updatedTime", convEntity.getUpdatedTime());
        cv.put("avatar", convEntity.getAvatar());
        cv.put("type", convEntity.getType());
        cv.put("unRead", convEntity.getUnRead());
        cv.put("convNo", convEntity.getConvNo());
        return mDbHelper.update(TABLE, cv, "id=?", new String[]{String.valueOf(convEntity.getId())});
    }

    public int updateField(String convNo, ContentValues cv) {
        return mDbHelper.update(TABLE, cv, "convNo=?", new String[]{convNo});
    }
}
