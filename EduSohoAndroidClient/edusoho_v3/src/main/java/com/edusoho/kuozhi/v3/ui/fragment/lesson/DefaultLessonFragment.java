package com.edusoho.kuozhi.v3.ui.fragment.lesson;


/**
 * Created by howzhi on 15/4/3.
 */
public class DefaultLessonFragment extends WebVideoLessonFragment {

    @Override
    protected void loadContent() {
        mWebView.loadUrl(mUri);
    }
}