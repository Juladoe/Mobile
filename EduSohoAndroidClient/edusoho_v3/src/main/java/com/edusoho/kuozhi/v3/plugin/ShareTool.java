package com.edusoho.kuozhi.v3.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.shard.ListData;
import com.edusoho.kuozhi.shard.ShardDialog;
import com.edusoho.kuozhi.shard.ShareHandler;
import com.edusoho.kuozhi.shard.ShareUtil;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.push.RedirectBody;
import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;
import com.edusoho.kuozhi.v3.ui.fragment.ChatSelectFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JesseHuang on 15/6/19.
 */
public class ShareTool {

    private static final String SHARE_COURSE_TO_USER = "http://%s/mapi_v2/mobile/main#/%s/%s";

    private Context mContext;
    private String mUrl = "";
    private String mTitle = "";
    private String mAbout = "";
    private String mPic = "";
    private int mDialogType = 1;
    private ShardDialog.DismissEvent mDismissEvent;

    public ShareTool(Context ctx, String url, String title, String about, String pic) {
        mContext = ctx;
        mUrl = url;
        mTitle = title;
        mAbout = fromHtml(about);
        mPic = pic;
    }

    public ShareTool(Context ctx, String url, String title, String about, String pic, int type) {
        mContext = ctx;
        mUrl = url;
        mTitle = title;
        mAbout = fromHtml(about);
        mPic = pic;
        mDialogType = type;
    }

    private String fromHtml(String htmlCode) {
        return Html.fromHtml(htmlCode).toString();
    }

    public void shardCourse() {
        ImageLoader.getInstance().loadImage(mPic, EdusohoApp.app.mOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                startShare(imageUri);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                CommonUtil.longToast(mContext, "课程图片获取失败");
                startShare(AppUtil.getUriById(mContext, R.drawable.default_classroom));
            }
        });
    }

    private void startShare(String imageUri) {
        ShareUtil shareUtil = ShareUtil.getShareUtil(mContext);
        shareUtil.setDismissEvent(mDismissEvent);
        File file = ImageLoader.getInstance().getDiskCache().get(imageUri);
        List<ListData> listDatas = shareUtil.getDataList();
        listDatas.addAll(0, getCustomListData());
        shareUtil.initShareParams(
                R.mipmap.ic_launcher,
                mTitle,
                mUrl,
                coverShareContent(mAbout, mUrl),
                file,
                EdusohoApp.app.host
                , mDialogType
        );
        shareUtil.show(getShareHandler());
    }

    private void startShare(Uri imageUri) {
        ShareUtil shareUtil = ShareUtil.getShareUtil(mContext);
        shareUtil.setDismissEvent(mDismissEvent);
        List<ListData> listDatas = shareUtil.getDataList();
        listDatas.addAll(0, getCustomListData());
        shareUtil.initShareParams(
                R.mipmap.ic_launcher,
                mTitle,
                mUrl,
                coverShareContent(mAbout, mUrl),
                imageUri,
                EdusohoApp.app.host
                , mDialogType
        );
        shareUtil.show(getShareHandler());
    }

    private String coverShareContent(String content, String url) {
        StringBuilder stringBuilder = new StringBuilder();

        String formatString = Html.fromHtml(content).toString();
        int contentSize = 140 - url.length() - 3;
        if (formatString.length() > contentSize) {
            formatString = formatString.substring(0, contentSize);
        }
        stringBuilder.append(formatString);
        stringBuilder.append(" \r\n").append(url);

        return stringBuilder.toString();
    }

    private ShareHandler getShareHandler() {
        return new ShareHandler() {
            @Override
            public boolean handler(String type) {
                if (type.startsWith("Wechat")) {
                    //朋友圈
                    int wxType;
                    switch (type) {
                        case "Wechat":
                            wxType = SendMessageToWX.Req.WXSceneSession;
                            break;
                        case "WechatMoments":
                            wxType = SendMessageToWX.Req.WXSceneTimeline;
                            break;
                        default:
                            wxType = SendMessageToWX.Req.WXSceneFavorite;
                            break;
                    }
                    shardToMM(mContext, wxType);
                    return true;
                }

                if ("shareToUser".equals(type)) {
                    shareToUser(mUrl, mTitle, mAbout, mPic);
                    return true;
                }

                return false;
            }
        };
    }

    private String coverWebUrl(String url) {

        if (TextUtils.isEmpty(url)) {
            return "";
        }

        Pattern WEB_URL_PAT = Pattern.compile("(http://)?(.+)/(course|classroom|article)/(\\d+)", Pattern.DOTALL);
        Matcher matcher = WEB_URL_PAT.matcher(url);
        if (matcher.find()) {
            return String.format(SHARE_COURSE_TO_USER, matcher.group(2), matcher.group(3), matcher.group(4));
        }
        return url;
    }

    private void shareToUser(String url, String title, String about, String pic) {
        RedirectBody redirectBody = RedirectBody.createByShareContent(
                coverWebUrl(url), title, about, pic);

        Intent startIntent = new Intent();
        startIntent.putExtra(Const.ACTIONBAR_TITLE, "选择");
        startIntent.putExtra(ChatSelectFragment.BODY, redirectBody);
        startIntent.putExtra(FragmentPageActivity.FRAGMENT, "ChatSelectFragment");
        startIntent.setComponent(new ComponentName(mContext, FragmentPageActivity.class));
        mContext.startActivity(startIntent);
    }

    private List<ListData> getCustomListData() {
        List<ListData> listDatas = new ArrayList<>();
        if (EdusohoApp.app.loginUser == null) {
            return listDatas;
        }

        //校友修改位置
        ListData data = new ListData(
                mContext.getResources().getDrawable(R.drawable.share_user),
                "shareToUser",
                mContext
        );
        listDatas.add(data);
        return listDatas;
    }

    private boolean shardToMM(Context context, int type) {
        String APP_ID = mContext.getResources().getString(R.string.app_id);
        IWXAPI wxApi;
        wxApi = WXAPIFactory.createWXAPI(context, APP_ID, true);
        wxApi.registerApp(APP_ID);
        WXTextObject wXTextObject = new WXTextObject();
        wXTextObject.text = "分享课程";
        WXWebpageObject wxobj = new WXWebpageObject();

        wxobj.webpageUrl = mUrl;
        WXMediaMessage wXMediaMessage = new WXMediaMessage();
        wXMediaMessage.mediaObject = wxobj;
        wXMediaMessage.description = AppUtil.coverCourseAbout(mAbout);
        wXMediaMessage.title = mTitle;
        //具体尺寸new ImageSize(100, 99)待修改
        if (!TextUtils.isEmpty(mPic)) {
            wXMediaMessage.setThumbImage(ImageLoader.getInstance().loadImageSync(mPic,
                    new ImageSize(100, 99), EdusohoApp.app.mOptions));
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = type;
        req.transaction = System.currentTimeMillis() + "";
        req.message = wXMediaMessage;
        return wxApi.sendReq(req);
    }

}
