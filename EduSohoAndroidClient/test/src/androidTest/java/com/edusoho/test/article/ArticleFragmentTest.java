package com.edusoho.test.article;

import android.app.Activity;
import android.test.UiThreadTest;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.edusoho.kuozhi.v3.ui.ServiceProviderActivity;
import com.edusoho.kuozhi.v3.ui.fragment.article.ArticleFragment;
import com.edusoho.test.base.BaseFragmentTestCase;

import java.lang.reflect.Field;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Created by howzhi on 15/9/9.
 */
public class ArticleFragmentTest extends BaseFragmentTestCase<ArticleFragment> {

    public ArticleFragmentTest()
    {
        super(ArticleFragment.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent.putExtra(ServiceProviderActivity.SERVICE_ID, 0);
    }

    @UiThreadTest
    public void testInitView() {
        ArticleFragment articleFragment = getFragment();
        assertNotNull(articleFragment);

        Activity activity = getActivity();
        ViewGroup mMenuLayout = (ViewGroup) activity.findViewById(com.edusoho.kuozhi.R.id.message_menu_layout);
        ExpandableListView mMessageListView = (ExpandableListView) activity.findViewById(com.edusoho.kuozhi.R.id.message_list);
        PtrClassicFrameLayout mMessageLayout = (PtrClassicFrameLayout) activity.findViewById(com.edusoho.kuozhi.R.id.message_list_layout);

        assertNotNull(mMenuLayout);
        assertNotNull(mMessageListView);
        assertNotNull(mMessageLayout);
    }

    @UiThreadTest
    public void testInitData() {
        ArticleFragment articleFragment = getFragment();

        assertNotNull(getField(articleFragment, "mArticleProvider"));
        assertNotNull(getField(articleFragment, "mSPDataSource"));
    }

    private Object getField(Object target, String name) {
        Field field = null;
        try {
           field = target.getClass().getDeclaredField(name);
           field.setAccessible(true);
           return field.get(target);
       } catch (Exception e) {

       }

       return null;
    }
}
