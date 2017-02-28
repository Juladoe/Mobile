package com.edusoho.kuozhi.imserver.helper;

import android.content.ContentValues;

import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import java.util.List;

/**
 * Created by suju on 16/11/3.
 */
public interface IConvDbHelper {

    List<ConvEntity> getConvListByType(String type);

    List<ConvEntity> getConvListByUid(int uid);

    List<ConvEntity> getConvList();

    ConvEntity getConv(String convNo);

    ConvEntity getConvByConvNo(String convNo);

    ConvEntity getConvByTypeAndId(String type, int targetId);

    int deleteByConvNo(String convNo);

    int deleteByTypeAndId(String type, int targetId);

    int deleteById(int id);

    long save(ConvEntity convEntity);

    int updateUnRead(String convNo, int unRead);

    int updateByConvNo(ConvEntity convEntity);

    int update(ConvEntity convEntity);

    int updateField(String convNo, ContentValues cv);
}
