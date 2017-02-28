package com.edusoho.kuozhi.imserver.helper.impl;

import android.content.ContentValues;
import android.content.Context;

import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.helper.IConvDbHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 菊 on 2016/4/29.
 */
public class ConvMemHelper implements IConvDbHelper {

    private static final String TABLE = "im_conv";

    public ConvMemHelper(Context context) {
    }

    public List<ConvEntity> getConvListByType(String type) {
        List<ConvEntity> entityList = new ArrayList<>();
        return entityList;
    }

    public List<ConvEntity> getConvListByUid(int uid) {
        List<ConvEntity> entityList = new ArrayList<>();
        return entityList;
    }

    public List<ConvEntity> getConvList() {
        List<ConvEntity> entityList = new ArrayList<>();
        return entityList;
    }

    public ConvEntity getConv(String convNo) {
        return null;
    }

    public ConvEntity getConvByConvNo(String convNo) {
        return null;
    }

    public ConvEntity getConvByTypeAndId(String type, int targetId) {
        return null;
    }

    public int deleteByConvNo(String convNo) {
        return 0;
    }

    public int deleteByTypeAndId(String type, int targetId) {
        return 0;
    }

    public int deleteById(int id) {
        return 0;
    }

    public long save(ConvEntity convEntity) {
        return 0;
    }

    public int updateUnRead(String convNo, int unRead) {
        return 0;
    }

    public int updateByConvNo(ConvEntity convEntity) {
        return 0;
    }

    public int update(ConvEntity convEntity) {
        return 0;
    }

    public int updateField(String convNo, ContentValues cv) {
        return 0;
    }
}
