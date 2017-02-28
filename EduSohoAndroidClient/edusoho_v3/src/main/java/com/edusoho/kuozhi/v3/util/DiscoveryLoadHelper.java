package com.edusoho.kuozhi.v3.util;

import android.util.Log;
import android.util.SparseArray;

import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryColumn;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suju on 16/9/10.
 */
public class DiscoveryLoadHelper
{
    private int mTaskCount;
    private SparseArray<DiscoveryColumn> mDiscoveryCardPropertieList;
    private List<DiscoveryLoadTask> mTaskList;
    private ResultCallback mResultCallback;

    public DiscoveryLoadHelper() {
        mTaskList = new ArrayList<>();
        mDiscoveryCardPropertieList = new SparseArray<>();
    }

    public void addTask(DiscoveryColumn discoveryColumn) {
        mTaskList.add(new DiscoveryLoadTask(discoveryColumn));
        mTaskCount = mTaskList.size();
    }

    public void invoke(ResultCallback resultCallback) {
        this.mResultCallback = resultCallback;
        for (int i = 0; i < mTaskList.size(); i++) {
            DiscoveryLoadTask loadTask = mTaskList.get(i);
            loadTask.exectue(new DiscoveryLoadTaskCallback(i));
        }
    }

    private class DiscoveryLoadTaskCallback implements DiscoveryLoadTask.TaskCallback {

        private int mIndex;
        public DiscoveryLoadTaskCallback(int index) {
            mIndex = index;
        }

        @Override
        public void onResult(DiscoveryColumn discoveryColumn) {
            if (discoveryColumn != null && discoveryColumn.data != null && !discoveryColumn.data.isEmpty()) {
                mDiscoveryCardPropertieList.put(mIndex, discoveryColumn);
            }
            mTaskCount --;
            if (mTaskCount <= 0) {
                mResultCallback.onResult(sortDataList());
            }
        }

        private List<DiscoveryColumn> sortDataList() {
            List<DiscoveryColumn> list = new ArrayList<>();
            int size = mTaskList.size();
            for (int i = 0; i < size; i++) {
                DiscoveryColumn dc = mDiscoveryCardPropertieList.get(i);
                if (dc != null) {
                    list.add(dc);
                }
            }

            return list;
        }
    }

    public interface ResultCallback {
        void onResult(List<DiscoveryColumn> discoveryCardProperties);
    }
}