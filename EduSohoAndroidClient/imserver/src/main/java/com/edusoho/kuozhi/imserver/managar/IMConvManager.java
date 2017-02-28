package com.edusoho.kuozhi.imserver.managar;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.helper.impl.ConvDbHelper;
import java.util.List;

/**
 * Created by 菊 on 2016/5/15.
 */
public class IMConvManager {

    private Context mContext;

    public IMConvManager(Context context) {
        this.mContext = context;
    }

    public List<ConvEntity> getConvList() {
        return new ConvDbHelper(mContext).getConvList();
    }

    public List<ConvEntity> getConvListByType(String type) {
        return new ConvDbHelper(mContext).getConvListByType(type);
    }

    public ConvEntity getConvByConvNo(String convNo) {
        return new ConvDbHelper(mContext).getConvByConvNo(convNo);
    }

    public ConvEntity getConvByTypeAndId(String type, int targetId) {
        if (TextUtils.isEmpty(type)) {
            return null;
        }
        return new ConvDbHelper(mContext).getConvByTypeAndId(type, targetId);
    }

    public int updateConvByConvNo(ConvEntity convEntity) {
        return new ConvDbHelper(mContext).updateByConvNo(convEntity);
    }

    public int clearLaterMsg(String convNo) {
        ContentValues cv = new ContentValues();
        cv.put("laterMsg", "");
        return new ConvDbHelper(mContext).updateField(convNo, cv);
    }

    public int updateConvField(String convNo, ContentValues cv) {
        return new ConvDbHelper(mContext).updateField(convNo, cv);
    }

    public int clearReadCount(String convNo) {
        return new ConvDbHelper(mContext).updateUnRead(convNo, 0);
    }

    public int deleteConv(String convNo) {
        return new ConvDbHelper(mContext).deleteByConvNo(convNo);
    }

    public int deleteConvByTypeAndId(String type, int targetId) {
        return new ConvDbHelper(mContext).deleteByTypeAndId(type, targetId);
    }

    public int deleteById(int id) {
        return new ConvDbHelper(mContext).deleteById(id);
    }

    public long createConv(ConvEntity convEntity) {
        return new ConvDbHelper(mContext).save(convEntity);
    }
}
