package com.edusoho.kuozhi.v3.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.edusoho.kuozhi.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by howzhi on 15/9/28.
 */
public class RedirectPreViewDialog extends Dialog {

    private TextView mTitleView;
    private TextView mBodyView;
    private ImageView mIconView;

    private View mCancelBtn;
    private View mOkBtn;

    private PopupDialog.PopupClickListener mPopupClickListener;

    public RedirectPreViewDialog(Context context)
    {
        super(context, R.style.loadDlgTheme);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initView();
    }

    private TextView getTitleView() {
        return mTitleView;
    }

    private TextView getBodyView() {
        return mBodyView;
    }

    private ImageView getIconView() {
        return mIconView;
    }

    private void initView() {
        mTitleView = (TextView) findViewById(R.id.popup_title);
        mBodyView = (TextView) findViewById(R.id.popup_message);
        mIconView = (ImageView) findViewById(R.id.popup_icon);
        mCancelBtn = findViewById(R.id.popup_cancel_btn);
        mOkBtn = findViewById(R.id.popup_ok_btn);

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupClickListener != null) {
                    mPopupClickListener.onClick(PopupDialog.CANCEL);
                }
                dismiss();
            }
        });
    }

    public void setButtonClickListener(PopupDialog.PopupClickListener popupClickListener) {

        this.mPopupClickListener = popupClickListener;
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupClickListener.onClick(PopupDialog.OK);
                dismiss();
            }
        });
    }

    public static RedirectPreViewDialogBuilder getBuilder(Context context) {
        return new RedirectPreViewDialogBuilder(context);
    }

    public static class RedirectPreViewDialogBuilder {

        private DisplayImageOptions mOptions;
        private RedirectPreViewDialog mDialog;

        public RedirectPreViewDialogBuilder(Context context)
        {
            this.mDialog = new RedirectPreViewDialog(context);
            mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).
                    showImageForEmptyUri(R.drawable.default_avatar).
                    showImageOnFail(R.drawable.default_avatar).build();
        }

        public RedirectPreViewDialogBuilder setLayout(int layout) {
            mDialog.setContentView(layout);
            return this;
        }

        public RedirectPreViewDialogBuilder setTitle(String title) {
            mDialog.getTitleView().setText(title);
            return this;
        }

        public RedirectPreViewDialogBuilder setBody(String body) {
            mDialog.getBodyView().setText(body);
            return this;
        }

        public RedirectPreViewDialogBuilder setIcon(int icon) {
            mDialog.getIconView().setImageResource(icon);
            return this;
        }

        public RedirectPreViewDialogBuilder setIconByUri(String uri) {
            ImageLoader.getInstance().displayImage(uri, mDialog.getIconView(), mOptions);
            return this;
        }

        public RedirectPreViewDialog build() {
            return mDialog;
        }
    }
}
