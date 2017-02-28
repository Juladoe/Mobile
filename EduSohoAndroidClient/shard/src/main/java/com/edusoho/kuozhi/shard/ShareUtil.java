package com.edusoho.kuozhi.shard;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.onekeyshare.OnekeyShare;

import static com.mob.tools.utils.R.getBitmapRes;


/**
 * Created by onewoman on 14-11-6.
 */
public class ShareUtil {
    public int mNotification_icon;
    public String mNotification_text;
    public String mShareTextTitle;
    public String mShareTitleUrl;
    public String mShareText;
    public String mLocalImagePath;
    public String mShareSite;
    private OnekeyShare mOneKeyShare;
    private Context mContext;
    private ShardDialog mAlertDialog;
    private ShareHandler mShareHandler;
    private ShareSDKUtil mShareSDKUtil;
    private List<ListData> mCustomList;
    private ArrayList<ListData> mList;
    private ShardDialog.DismissEvent mDismissEvent;

    private ShareUtil(Context context) {
        //添加应用信息
        mShareSDKUtil = new ShareSDKUtil();
        mShareSDKUtil.initSDK(context);
        mContext = context;
        initPlatformList();
    }

    public static ShareUtil getShareUtil(Context context) {
        return new ShareUtil(context);
    }

    public void setCustomList(List<ListData> dataList) {
        this.mCustomList = dataList;
        if (mCustomList != null) {
            mList.addAll(mCustomList);
        }
    }

    public List<ListData> getDataList() {
        return mList;
    }

    public ShareUtil initShareParams(
            int icon, String shareTextTitle, String shareTitleUrl, String shareText, File imageFile, String ShareSite, int type
    ) {
        mOneKeyShare = new OnekeyShare();
        mNotification_icon = icon;
        mShareTextTitle = shareTextTitle;
        mShareTitleUrl = shareTitleUrl;
        mShareText = shareText;
        if (imageFile.exists()) {
            mLocalImagePath = imageFile.getAbsolutePath();
            mOneKeyShare.setImagePath(mLocalImagePath);
        } else {

        }
        mShareSite = ShareSite;
        initOneKeyShare();
        initDialog(type);
        return this;
    }

    public ShareUtil initShareParams(
            int icon, String shareTextTitle, String shareTitleUrl, String shareText, Uri imageFile, String ShareSite, int type
    ) {
        mOneKeyShare = new OnekeyShare();
        mNotification_icon = icon;
        mShareTextTitle = shareTextTitle;
        mShareTitleUrl = shareTitleUrl;
        mShareText = shareText;
        if (imageFile != null) {
            mOneKeyShare.setImagePath(imageFile.toString());
        } else {

        }
        mShareSite = ShareSite;
        initOneKeyShare();
        initDialog(type);
        return this;
    }

    public void setDismissEvent(ShardDialog.DismissEvent dismissEvent) {
        mDismissEvent = dismissEvent;
    }

    private boolean filterPlat(String name) {
        String[] filters = mContext.getResources().getStringArray(R.array.shard_filter);
        for (String filter : filters) {
            if (filter.equals(name)) {
                return true;
            }
        }

        return false;
    }

    private void initPlatformList() {
        Platform[] platforms = mShareSDKUtil.getPlatformList();
        mList = new ArrayList<>();

        for (Platform platform : platforms) {
            String name = platform.getName();
            if (filterPlat(name)) {
                continue;
            }
            String resName = ("logo_" + name).toLowerCase();
            int resId = getBitmapRes(mContext, resName);
            ListData data = new ListData(mContext.getResources().getDrawable(resId), name, mContext);
            mList.add(data);
        }

        Collections.sort(mList, new Comparator<ListData>() {
            @Override
            public int compare(ListData lhs, ListData rhs) {
                return rhs.type.compareToIgnoreCase(lhs.type);
            }
        });
    }

    public void initDialog(int type) {
        mAlertDialog = new ShardDialog(mContext, type);
        mAlertDialog.setDismissEvent(mDismissEvent);
        mAlertDialog.setShardDatas(mList);
        mAlertDialog.setShardItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListData data = (ListData) parent.getItemAtPosition(position);

                if (mShareHandler != null && mShareHandler.handler(data.type)) {
                    mAlertDialog.dismiss();
                    return;
                }

                mOneKeyShare.setPlatform(data.type);
                mOneKeyShare.setSilent(false);
                mOneKeyShare.show(mContext);
                mAlertDialog.dismiss();
            }
        });
    }

    public void show(ShareHandler shareHandler) {
        this.mShareHandler = shareHandler;
        mAlertDialog.show();
    }

    private void initOneKeyShare() {
        //关闭sso授权
        mOneKeyShare.disableSSOWhenAuthorize();
        mOneKeyShare.setDialogMode();
        mOneKeyShare.setTitle(mShareTextTitle);
        mOneKeyShare.setTitleUrl(mShareTitleUrl);
        mOneKeyShare.setText(mShareText);
        mOneKeyShare.setSite(mShareSite);
    }

}
