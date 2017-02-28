package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.ViewPagerAdapter;
import com.edusoho.kuozhi.v3.view.photo.HackyViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suju on 16/9/14.
 */
public class ViewPagerFragment extends DialogFragment implements ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private TextView mViewPaperLabel;
    private int mIndex;
    private int mTotalCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.edusohoTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photo_layout, null);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView(View view) {
        mViewPaperLabel = (TextView) view.findViewById(R.id.images_label);
        mViewPager = (HackyViewPager) view.findViewById(R.id.images_pager);
        Bundle bundle = getArguments();
        mIndex = bundle.getInt("index", 0);
        String[] images;
        if (bundle.containsKey("imageList")) {
            ArrayList<String> list = bundle.getStringArrayList("imageList");
            images = getImageUrls(list);
        } else {
            images = (String[]) bundle.getSerializable("images");
        }

        mTotalCount = images.length;
        if (images != null && images.length > 0) {
            ViewPagerAdapter adapter = new ViewPagerAdapter(images, new ViewPagerAdapter.ViewPagerAdapterListener() {
                @Override
                public void onFinish() {
                    //getActivity().finish();
                    getDialog().dismiss();
                }
            });
            mViewPager.setAdapter(adapter);
            mViewPager.setCurrentItem(mIndex);
            mViewPager.setOnPageChangeListener(this);
            updateViewPaperLabel(mIndex);
        }
    }

    private String[] getImageUrls(List<String> list) {
        String[] imageUrls = new String[list.size()];
        list.toArray(imageUrls);

        return imageUrls;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = R.style.WindowZoomAnimation;
        return dialog;
    }

    protected void updateViewPaperLabel(int position) {
        mViewPaperLabel.setText(String.format("%d/%d", position + 1, mTotalCount));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mIndex = position;
        updateViewPaperLabel(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
