package com.edusoho.kuozhi.v3.ui.friend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PromiseCallback;
import com.edusoho.kuozhi.v3.model.bal.SchoolApp;
import com.edusoho.kuozhi.v3.model.provider.FriendProvider;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.ui.ServiceProviderActivity;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.Promise;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Melomelon on 2015/9/10.
 */
public class ServiceListActivity extends ActionBarBaseActivity {

    private ListView serviceList;

    private LayoutInflater mLayoutInflater;
    private ServiceListAdapter mAdapter;
    private FriendProvider mProvider;
    private FrameLayout mLoading;

    private ArrayList<SchoolApp> mServiceList = new ArrayList<SchoolApp>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackMode(BACK, "服务号");
        setContentView(R.layout.service_list_layout);
        serviceList = (ListView) findViewById(R.id.service_list);
        mLoading = (FrameLayout) findViewById(R.id.service_list_loading);
        mAdapter = new ServiceListAdapter();
        serviceList.setAdapter(mAdapter);
        mProvider = new FriendProvider(mContext);

        if (mServiceList.size() != 0) {
            mAdapter.clearList();
        }
        if (!app.getNetIsConnect()) {
            mLoading.setVisibility(View.GONE);
            Toast.makeText(mContext, "无网络连接", Toast.LENGTH_LONG).show();
        }
        loadSchoolApps().then(new PromiseCallback() {
            @Override
            public Promise invoke(Object obj) {
                mLoading.setVisibility(View.GONE);
                return null;
            }
        });

        serviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SchoolApp schoolApp = (SchoolApp) adapterView.getItemAtPosition(i);
                switch (schoolApp.code) {
                    case PushUtil.AnnouncementType.GLOBAL:
                        MobclickAgent.onEvent(mContext, "alumni_serviceBulletin_information");
                        app.mEngine.runNormalPlugin("BulletinActivity", mActivity, null);
                        break;
                    case PushUtil.ArticleType.TYPE:
                        Bundle bundle = new Bundle();
                        bundle.putString(ServiceProviderActivity.SERVICE_TYPE, PushUtil.ArticleType.TYPE);
                        bundle.putInt(ServiceProviderActivity.SERVICE_ID, schoolApp.id);
                        bundle.putString(Const.ACTIONBAR_TITLE, "资讯");
                        MobclickAgent.onEvent(mContext, "alumni_serviceBulletin_schoolNotice");
                        app.mEngine.runNormalPluginWithBundle("ServiceProviderActivity", mActivity, bundle);
                        break;
                }
            }
        });
    }

    public Promise loadSchoolApps() {
        mAdapter.clearList();

        final Promise promise = new Promise();
        mProvider.getSchoolApps()
                .success(new NormalCallback<List<SchoolApp>>() {
                    @Override
                    public void success(List<SchoolApp> schoolAppResult) {
                        if (schoolAppResult.size() != 0) {
                            mAdapter.addSchoolAppList(schoolAppResult);
                            new IMProvider(mContext).updateRoles(schoolAppResult);
                        }
                        promise.resolve(schoolAppResult);
                    }
                });

        return promise;
    }

    public class ServiceListAdapter extends BaseAdapter {

        public ServiceListAdapter() {
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mServiceList.size();
        }

        @Override
        public Object getItem(int i) {
            return mServiceList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void clearList() {
            mServiceList.clear();
            notifyDataSetChanged();
        }

        public void addSchoolAppList(List<SchoolApp> list) {
            mServiceList.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            final SchoolAppHolder schoolAppHolder;
            if (view == null) {
                view = mLayoutInflater.inflate(R.layout.item_type_school_app, null);
                schoolAppHolder = new SchoolAppHolder();
                schoolAppHolder.SchoolAppName = (TextView) view.findViewById(R.id.friend_name);
                schoolAppHolder.schoolAppAvatar = (RoundedImageView) view.findViewById(R.id.friend_avatar);
                schoolAppHolder.dividerLine = view.findViewById(R.id.divider_line);
                view.setTag(schoolAppHolder);
            } else {
                schoolAppHolder = (SchoolAppHolder) view.getTag();
            }

            final SchoolApp schoolApp = mServiceList.get(position);

            ImageLoader.getInstance().displayImage(schoolApp.avatar, schoolAppHolder.schoolAppAvatar, app.mOptions);
            schoolAppHolder.SchoolAppName.setText(schoolApp.name);
            if (position != mServiceList.size() - 1) {
                schoolAppHolder.dividerLine.setVisibility(View.VISIBLE);
            } else {
                schoolAppHolder.dividerLine.setVisibility(View.GONE);
            }

            return view;
        }

        private class SchoolAppHolder {
            private RoundedImageView schoolAppAvatar;
            private TextView SchoolAppName;
            private View dividerLine;
        }
    }
}
