package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.NotificationProvider;
import com.edusoho.kuozhi.v3.factory.UtilFactory;
import com.edusoho.kuozhi.v3.model.bal.push.Bulletin;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.ui.fragment.NewsFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by JesseHuang on 15/7/14.
 * 公告
 */
public class BulletinActivity extends ActionBarBaseActivity {

    public static final String CONV_NO = "conv_no";
    private ListView mListView;
    private PtrClassicFrameLayout mPtrFrame;
    private View mEmptyView;
    private TextView tvEmpty;
    private BulletinAdapter mBulletinAdapter;
    private static final int LIMIT = 15;
    private int mStart = 0;
    private String mConvNo;
    private boolean mCanLoadMore;

    private static long TIME_INTERVAL = 60 * 5;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin);
        initView();
        initData();
    }

    private void initView() {
        mHandler = new Handler();
        mListView = (ListView) findViewById(R.id.lv_bulletin);
        mPtrFrame = (PtrClassicFrameLayout) findViewById(R.id.rotate_header_list_view_frame);
        mEmptyView = findViewById(R.id.view_empty);
        tvEmpty = (TextView) findViewById(R.id.tv_empty_text);
        tvEmpty.setText(getResources().getString(R.string.announcement_empty_text));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getNotificationProvider().cancelNotification(Destination.GLOBAL.hashCode());
        IMClient.getClient().getConvManager().clearReadCount(Destination.GLOBAL);
    }

    private void initData() {
        setBackMode(BACK, "网校公告");
        mCanLoadMore = true;
        mConvNo = Destination.GLOBAL;
        List<Bulletin> bulletinList = getBulletins(mStart);
        mStart += bulletinList.size();
        mBulletinAdapter = new BulletinAdapter(bulletinList);
        mListView.setAdapter(mBulletinAdapter);
        mListView.post(new ScrollRunnable(mStart));
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
                List<Bulletin> bulletinList = getBulletins(mStart);
                mBulletinAdapter.addItems(bulletinList);
                mPtrFrame.refreshComplete();
                if (bulletinList.isEmpty()) {
                    mCanLoadMore = false;
                }
                int total = mStart + bulletinList.size();
                mListView.postDelayed(new ScrollRunnable(total > mStart ? total - mStart - 1 : 0), 100);
                mStart = total;
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return mCanLoadMore && super.checkCanDoRefresh(frame, content, header);
            }
        });
        mHandler.postDelayed(mNotifyNewFragment2UpdateItemBadgeRunnable, 500);
        setListVisibility(mBulletinAdapter.getCount() == 0);
    }

    private List<Bulletin> getBulletins(int start) {
        List<MessageEntity> messageEntityList = IMClient.getClient().getChatRoom(mConvNo).getMessageList(start);
        ArrayList<Bulletin> bulletinList = new ArrayList<>();
        if (messageEntityList == null || messageEntityList.isEmpty()) {
            return bulletinList;
        }

        Role role = IMClient.getClient().getRoleManager().getRole(mConvNo, 1);
        for (MessageEntity messageEntity : messageEntityList) {
            MessageBody messageBody = new MessageBody(messageEntity);
            Bulletin bulletin = getUtilFactory().getJsonParser().fromJson(messageBody.getBody(), Bulletin.class);
            bulletin.setCreateTime(messageBody.getCreatedTime());
            if (role.getRid() != 0) {
                bulletin.setAvatar(role.getAvatar());
            }
            bulletinList.add(bulletin);
        }
        return bulletinList;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mBulletinAdapter.clear();
        mStart = 0;
        List<Bulletin> bulletinList = getBulletins(mStart);
        mBulletinAdapter.addItems(bulletinList);
        mStart += bulletinList.size();
        mListView.post(new ScrollRunnable(mStart));
        mHandler.postDelayed(mNotifyNewFragment2UpdateItemBadgeRunnable, 500);
    }

    private Runnable mNotifyNewFragment2UpdateItemBadgeRunnable = new Runnable() {
        @Override
        public void run() {
            Bundle bundle = new Bundle();
            bundle.putInt(Const.FROM_ID, 0);
            app.sendMsgToTarget(NewsFragment.UPDATE_UNREAD_BULLETIN, bundle, NewsFragment.class);
        }
    };

    @Override
    public void invoke(WidgetMessage message) {
        MessageType messageType = message.type;
        switch (messageType.code) {
            case Const.ADD_BULLETIN_MSG:

                break;
        }
        setListVisibility(mBulletinAdapter.getCount() == 0);
    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = this.getClass().getSimpleName();
        return new MessageType[]{new MessageType(Const.ADD_BULLETIN_MSG, source)};
    }

    public class BulletinAdapter extends BaseAdapter {
        private List<Bulletin> mList;
        private DisplayImageOptions mOptions;

        public BulletinAdapter(List<Bulletin> list) {
            mList = list;
            mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).showImageForEmptyUri(R.drawable.default_avatar).
                    showImageOnFail(R.drawable.default_avatar).build();
        }

        public void addItems(List<Bulletin> list) {
            mList.addAll(0, list);
            notifyDataSetChanged();
        }

        public void addItem(Bulletin bulletin) {
            mList.add(bulletin);
            notifyDataSetChanged();
        }

        public void clear() {
            if (mList.size() > 0) {
                mList.clear();
                notifyDataSetChanged();
            }
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            BulletinActivity.this.setListVisibility(getCount() == 0);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Bulletin getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_bulletin, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Bulletin bulletin = mList.get(position);
            holder.tvCreatedTime.setVisibility(View.GONE);
            if (position > 0) {
                if (bulletin.getCreateTime() - mList.get(position - 1).getCreateTime() > TIME_INTERVAL) {
                    holder.tvCreatedTime.setVisibility(View.VISIBLE);
                    holder.tvCreatedTime.setText(AppUtil.convertMills2Date((bulletin.getCreateTime())));
                }
            } else {
                holder.tvCreatedTime.setVisibility(View.VISIBLE);
                holder.tvCreatedTime.setText(AppUtil.convertMills2Date((bulletin.getCreateTime())));
            }
            holder.tvContent.setText(bulletin.title);
            ImageLoader.getInstance().displayImage(bulletin.getAvatar(), holder.ivHeadImageUrl, mOptions);
            return convertView;
        }
    }

    public static class ViewHolder {
        public ImageView ivHeadImageUrl;
        public TextView tvContent;
        public TextView tvCreatedTime;

        public ViewHolder(View view) {
            ivHeadImageUrl = (ImageView) view.findViewById(R.id.ci_send_pic);
            tvContent = (TextView) view.findViewById(R.id.tv_send_content);
            tvCreatedTime = (TextView) view.findViewById(R.id.tv_send_time);
        }
    }

    /**
     * 设置空数据背景ICON
     *
     * @param visibility 是否空数据
     */
    private void setListVisibility(boolean visibility) {
        mListView.setVisibility(visibility ? View.GONE : View.VISIBLE);
        mEmptyView.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    protected UtilFactory getUtilFactory() {
        return FactoryManager.getInstance().create(UtilFactory.class);
    }

    protected NotificationProvider getNotificationProvider() {
        return FactoryManager.getInstance().create(NotificationProvider.class);
    }

    private class ScrollRunnable implements Runnable {

        private int position;

        public ScrollRunnable(int position) {
            this.position = position;
        }

        @Override
        public void run() {
            mListView.setSelection(position);
        }
    }
}
