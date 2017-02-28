package com.edusoho.test.friend;

import android.app.Activity;
import android.content.Intent;
import android.test.UiThreadTest;
import android.widget.ListView;

import com.edusoho.kuozhi.v3.ui.friend.SearchFriendActivity;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by Melomelon on 2015/8/26.
 */
public class SearchFriendActivityTest extends BaseActivityUnitTestCase<SearchFriendActivity> {
    public SearchFriendActivityTest() {
        super(SearchFriendActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(), SearchFriendActivity.class);
    }

    @UiThreadTest
    public void testGetActivity() {
        SearchFriendActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testListView(){
        SearchFriendActivity mActivity = getActivity();
        ListView listView = (ListView) mActivity.findViewById(com.edusoho.kuozhi.R.id.search_friend_list);
        assertNotNull(listView);
    }
}
