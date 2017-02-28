package com.edusoho.kuozhi.shard;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

import m.framework.utils.Utils;

/**
 * Created by howzhi on 15/10/8.
 */
public class ShardDialog extends Dialog {

    private Context mContext;
    private GridView mGridView;
    private TextView mCancelView;
    public static final int DIALOG_TYPE_NORMAL = 1;
    public static final int DIALOG_TYPE_VIDEO = 2;
    private int mStyleType = 1;
    private DismissEvent mDismissEvent;

    public ShardDialog(Context context) {
        super(context, R.style.FullDialogTheme);
        mContext = context;
        initView();
        initWindow();
    }

    public ShardDialog(Context context, int type) {
        super(context, R.style.FullDialogTheme);
        mContext = context;
        mStyleType = type;
        initView();
        initWindow();
    }

    public void setDismissEvent(DismissEvent dismissEvent) {
        mDismissEvent = dismissEvent;
    }

    private void initView() {
        if (mStyleType == DIALOG_TYPE_VIDEO) {
            setContentView(R.layout.shard_video_content_layout);
        } else {
            setContentView(R.layout.shard_content_layout);
        }
        mGridView = (GridView) findViewById(R.id.shard_gridview);
        int width = Utils.getScreenWidth(mContext);
        int height = Utils.getScreenHeight(mContext);
        if(width > height){
            mGridView.setNumColumns(6);
        }else{
            mGridView.setNumColumns(3);
        }
        mCancelView = (TextView) findViewById(R.id.shard_cancelBtn);
        mCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (mStyleType == DIALOG_TYPE_VIDEO) {
            mCancelView.setVisibility(View.GONE);
        }
    }

    private void initWindow() {
        Window window = getWindow();
        window.setWindowAnimations(R.style.ShareDialogWindowAnimation);
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.LEFT | Gravity.BOTTOM);

        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        lp.width = display.getWidth();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.x = 0;

        window.setAttributes(lp);
    }

    public void setShardDatas(ArrayList<ListData> list) {
        ShardListAdapter adapter;
        if (mStyleType == DIALOG_TYPE_VIDEO) {
            adapter = new ShardListAdapter(mContext, list, R.layout.shard_video_list_item);
            mGridView.setAdapter(adapter);
        } else if (mStyleType == DIALOG_TYPE_NORMAL) {
            adapter = new ShardListAdapter(mContext, list, R.layout.shard_list_item);
            mGridView.setAdapter(adapter);
        }
    }

    public void setShardItemClick(AdapterView.OnItemClickListener onItemClickListener) {
        mGridView.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mDismissEvent != null) {
            mDismissEvent.afterDismiss();
        }
    }

    public interface DismissEvent {
        void afterDismiss();
    }
}
