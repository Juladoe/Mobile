package com.edusoho.kuozhi.v3.view.swipemenulistview;

import android.widget.BaseAdapter;

/**
 * Created by JesseHuang on 16/2/1.
 */
public abstract class BaseSwipeListAdapter extends BaseAdapter {

    public boolean getSwipEnableByPosition(int position){
        return true;
    }
}
