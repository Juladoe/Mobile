package com.edusoho.kuozhi.v3.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.shard.ListData;
import com.edusoho.kuozhi.shard.ShardDialog;

import java.util.ArrayList;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareCore;

/**
 * Created by Zhang on 2016/12/14.
 */

public class DefaultShareDialog extends Dialog implements View.OnClickListener{
    public DefaultShareDialog(Context context) {
        super(context, com.edusoho.kuozhi.shard.R.style.FullDialogTheme);
    }

    public DefaultShareDialog(Context context, int type) {
        super(context, com.edusoho.kuozhi.shard.R.style.FullDialogTheme);
    }

    private ArrayList<ListData> iconList = new ArrayList<>();
    private Context mContext;
    private View mWechat;
    private View mWeibo;
    private View mQQ;
    private View mMoment;
    private View mQzone;
    private View mCancel;
    private ShareCore mShareCore;
    private OnekeyShare mOneKeyShare;
    public String mShareTextTitle;
    public String mShareTitleUrl;
    public String mShareText;
    public String mLocalImagePath;
    public String mShareSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share);
        init();
        initEvent();
        initWindow();
    }

    private void init() {
        mContext = getContext();
        mWechat = findViewById(R.id.wechat_layout);
        mWeibo = findViewById(R.id.weibo_layout);
        mQQ = findViewById(R.id.qq_layout);
        mMoment = findViewById(R.id.moment_layout);
        mQzone = findViewById(R.id.qzone_layout);
        mCancel = findViewById(R.id.tv_cancel);
        mShareCore = new ShareCore();
    }

    private void initEvent() {
        mCancel.setOnClickListener(this);
        mWechat.setOnClickListener(this);
        mWeibo.setOnClickListener(this);
        mQQ.setOnClickListener(this);
        mMoment.setOnClickListener(this);
        mQzone.setOnClickListener(this);
    }

    private void initWindow() {
        Window window = getWindow();
        window.setWindowAnimations(com.edusoho.kuozhi.shard.R.style.ShareDialogWindowAnimation);
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.LEFT | Gravity.BOTTOM);

        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        lp.width = display.getWidth();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.x = 0;

        window.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.wechat_layout){
//            mShareCore.share(ShareSDK.getPlatform(mContext,""))
        }else if(v.getId() == R.id.weibo_layout){

        }else if(v.getId() == R.id.qq_layout){

        }else if(v.getId() == R.id.moment_layout){

        }else if(v.getId() == R.id.qzone_layout){

        }else if(v.getId() == R.id.tv_cancel){
            dismiss();
        }
    }

    private void share(String platform){
        mOneKeyShare.setPlatform(platform);
        mOneKeyShare.setSilent(false);
        mOneKeyShare.show(mContext);
        dismiss();
    }

    private void initOneKeyShare() {
        //关闭sso授权
        mOneKeyShare.disableSSOWhenAuthorize();
        mOneKeyShare.setDialogMode();
        // 分享时Notification的图标和文字
        //mOneKeyShare.setNotification(mNotification_icon, mNotification_text);
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        mOneKeyShare.setTitle(mShareTextTitle);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        mOneKeyShare.setTitleUrl(mShareTitleUrl);
        // text是分享文本，所有平台都需要这个字段
        mOneKeyShare.setText(mShareText);
//        imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        mOneKeyShare.setImagePath(mLocalImagePath);
//        imageUrl是图片的网络路径，新浪微博、人人网、QQ空间、
//        微信的两个平台、Linked-In支持此字段
//        mOneKeyShare.setImageUrl(mLocalImagePath);
        // url仅在微信（包括好友和朋友圈）中使用
        //oks.setUrl("http://sharesdk.cn");
        // site是分享此内容的网站名称，仅在QQ空间使用
        mOneKeyShare.setSite(mShareSite);
    }
}
