package com.edusoho.kuozhi.imserver.ui.listener;

import android.os.Bundle;
import com.edusoho.kuozhi.imserver.entity.Role;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suju on 16/8/27.
 */
public interface MessageControllerListener {

    void onShowImage(int index, ArrayList<String> imageList);

    void onShowUser(Role role);

    void onShowWebPage(String url);

    void onShowActivity(Bundle bundle);

    void selectPhoto();

    void takePhoto();

    /*
        callback
     */

    interface PhotoSelectCallback {

        void onSelected(List<String> pathList);
    }

}
