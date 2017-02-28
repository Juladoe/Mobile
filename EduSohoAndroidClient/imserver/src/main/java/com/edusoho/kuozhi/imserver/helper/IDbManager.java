package com.edusoho.kuozhi.imserver.helper;

import java.util.List;

/**
 * Created by 菊 on 2016/5/14.
 */
public interface IDbManager {

    int getVersion();

    List<String> getInitSql();

    List<String> getIncrementSql(int oldVersion);

    String getName();
}
