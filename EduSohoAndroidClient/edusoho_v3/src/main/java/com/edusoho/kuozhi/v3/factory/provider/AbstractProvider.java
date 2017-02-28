package com.edusoho.kuozhi.v3.factory.provider;

import android.content.Context;
import com.edusoho.kuozhi.v3.factory.IService;

/**
 * Created by su on 2015/12/30.
 */
public abstract class AbstractProvider implements IService {

    protected Context mContext;

    public AbstractProvider(Context context)
    {
        this.mContext = context;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }
}
