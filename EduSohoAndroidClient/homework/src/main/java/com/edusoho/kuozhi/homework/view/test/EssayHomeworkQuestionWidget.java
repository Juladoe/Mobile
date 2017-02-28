package com.edusoho.kuozhi.homework.view.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.edusoho.kuozhi.homework.ExerciseActivity;
import com.edusoho.kuozhi.homework.HomeworkActivity;
import com.edusoho.kuozhi.homework.HomeworkSummaryActivity;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.adapter.EssayImageSelectAdapter;
import com.edusoho.kuozhi.homework.model.HomeWorkItemResult;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.homework.ui.fragment.HomeWorkQuestionFragment;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.html.EduHtml;
import com.edusoho.kuozhi.v3.util.html.EduTagHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by howzhi on 14-9-29.
 */
public class EssayHomeworkQuestionWidget extends BaseHomeworkQuestionWidget {

    private EditText contentEdt;
    private ArrayList<String> mRealImageList;
    private View mToolsLayout;
    private GridView mImageGridView;
    private EssayImageSelectAdapter mImageGridViewAdapter;
    private static final int IMAGE_SIZE = 500;

    /**
     * 图片数量
     */
    private int mImageCount = 1;

    public EssayHomeworkQuestionWidget(Context context) {
        super(context);
    }

    public EssayHomeworkQuestionWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private AdapterView.OnItemClickListener mClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            EssayImageSelectAdapter.GridViewItem item = (EssayImageSelectAdapter.GridViewItem)
                    parent.getItemAtPosition(position);
            if (item.type == EssayImageSelectAdapter.SHOW_IMG) {
                prevSelectImage(item.path);
                return;
            }

            if (mWorkMode == PARSE) {
                return;
            }

            if (mImageGridViewAdapter.getCount() >= 7) {
                CommonUtil.longToast(mContext, "上传图片仅限5张!");
                return;
            }
            switch (item.type) {
                case EssayImageSelectAdapter.SEL_IMG:
                    MessageEngine.getInstance().sendMsgToTagetForCallback(
                            HomeWorkQuestionFragment.PHOTO, null, HomeWorkQuestionFragment.class, new NormalCallback() {
                                @Override
                                public void success(Object obj) {
                                    addImageToGridView((Bundle) obj);
                                }
                            });
                    break;
                case EssayImageSelectAdapter.CAMERA_IMG:
                    MessageEngine.getInstance().sendMsgToTagetForCallback(
                            HomeWorkQuestionFragment.CAMERA, null, HomeWorkQuestionFragment.class, new NormalCallback() {
                                @Override
                                public void success(Object obj) {
                                    addImageToGridView((Bundle) obj);
                                }
                            });
                    break;

            }
        }
    };

    private void prevSelectImage(String url) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", 0);
        String[] imgPaths = new String[]{
                url
        };
        bundle.putStringArray("images", imgPaths);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setComponent(new ComponentName(mContext.getPackageName(), "ViewPagerActivity"));
        mContext.startActivity(intent);
    }

    @Override
    protected void restoreResult(List<String> resultData) {
        if (resultData == null) {
            return;
        }
        initQuestionAnswer(resultData);
    }

    @Override
    protected void parseQuestionAnswer() {
        HomeWorkItemResult itemResult = mQuestion.getResult();
        if (itemResult != null) {
            initQuestionAnswer(itemResult.answer);
            mWorkMode = PARSE;
            contentEdt.setEnabled(false);
            mAnalysisVS = (ViewStub) this.findViewById(R.id.hw_quetion_analysis);
            mAnalysisVS.setOnInflateListener(new ViewStub.OnInflateListener() {
                @Override
                public void onInflate(ViewStub viewStub, View view) {
                    initResultAnalysis(view);
                }
            });
            mAnalysisVS.inflate();
        }
    }

    private void initQuestionAnswer(List<String> answer) {
        String answerContent = answer == null || answer.isEmpty()
                ? "" : answer.get(0);

        Pattern imageFilterPat = EduHtml.IMAGE_URL_FILTER;
        Matcher matcher = imageFilterPat.matcher(answerContent);
        StringBuffer newContent = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(newContent, "");
            if (mRealImageList.size() > 5) {
                continue;
            }
            mRealImageList.add(matcher.group(1));
            ImageLoader.getInstance().loadImage(matcher.group(1), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    mImageGridViewAdapter.insertItem(loadedImage, imageUri);
                }
            });
        }
        matcher.appendTail(newContent);
        contentEdt.setText(newContent);
    }

    protected void initResultAnalysis(View view) {
        TextView myRightText = (TextView) view.findViewById(R.id.hw_question_right_anwer);
        TextView AnalysisText = (TextView) view.findViewById(R.id.hw_question_analysis);
        TextView teacherSayView = (TextView) view.findViewById(R.id.hw_question_teachersay);

        String html = coverHtmlTag("正确答案:<p></p>" + listToStr(mQuestion.getAnswer()));
        myRightText.setText(Html.fromHtml(
                html, new NetImageGetter(myRightText, html), new EduTagHandler()));

        int rightColor = mContext.getResources().getColor(R.color.primary);
        SpannableString teacherSayText = AppUtil.getColorTextAfter(
                "老师评语:\n", TextUtils.isEmpty(mQuestion.getResult().teacherSay) ? "无" : mQuestion.getResult().teacherSay, rightColor);
        teacherSayView.setText(teacherSayText);

        String analysis = mQuestion.getAnalysis();
        AnalysisText.setText(
                TextUtils.isEmpty(analysis) ? "解析:无" : Html.fromHtml("解析<p></p>" + coverHtmlTag(analysis)));
    }

    private void addImageToGridView(Bundle bundle) {
        String filePath = bundle.getString("file");
        String realImagePath = bundle.getString("image");
        Bitmap bitmap = AppUtil.getBitmapFromFile(new File(filePath), AppUtil.dp2px(mContext, 40));
        mRealImageList.add(realImagePath);
        mImageGridViewAdapter.insertItem(bitmap, filePath);
        updateAnswerData(getRealImageStr().append(contentEdt.getText()).toString());
    }

    private TextWatcher onTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            updateAnswerData(getRealImageStr().append(charSequence).toString());

        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    public StringBuilder getRealImageStr() {
        StringBuilder builder = new StringBuilder();
        for (String image : mRealImageList) {
            builder.append(String.format("<img src='%s' />", image));
        }

        return builder;
    }

    private void updateAnswerData(String answerStr) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", mIndex - 1);
        bundle.putString("QuestionType", QuestionType.material.name());
        ArrayList<String> data = new ArrayList<String>();
        data.add(answerStr);
        bundle.putStringArrayList("data", data);
        if (HomeworkSummaryActivity.HOMEWORK.equals(mType)) {
            MessageEngine.getInstance().sendMsgToTaget(
                    HomeworkActivity.CHANGE_ANSWER, bundle, HomeworkActivity.class);
        } else {
            MessageEngine.getInstance().sendMsgToTaget(
                    ExerciseActivity.CHANGE_ANSWER, bundle, ExerciseActivity.class);
        }
    }

    @Override
    protected void invalidateData() {
        super.invalidateData();
        mToolsLayout = this.findViewById(R.id.hw_essay_tools_layout);
        contentEdt = (EditText) this.findViewById(R.id.hw_essay_content);
        mImageGridView = (GridView) findViewById(R.id.hw_essay_select_gridview);
        contentEdt.addTextChangedListener(onTextChangedListener);

        mImageGridViewAdapter = new EssayImageSelectAdapter(mContext);
        mImageGridView.setAdapter(mImageGridViewAdapter);
        mImageGridView.setOnItemClickListener(mClickListener);
        mRealImageList = new ArrayList<>(5);

        restoreResult(mQuestion.getAnswer());
        parseQuestionAnswer();
    }

    private class NetImageGetter implements Html.ImageGetter {
        private TextView mTextView;
        private String html;

        public NetImageGetter(TextView textView, String html) {
            this.html = html;
            mTextView = textView;
        }

        @Override
        public Drawable getDrawable(String s) {
            if (TextUtils.isEmpty(s)) {
                return getResources().getDrawable(R.drawable.html_image_fail);
            }
            Drawable drawable = getResources().getDrawable(R.drawable.load);
            File file = ImageLoader.getInstance().getDiskCache().get(s);

            if (file != null && file.exists()) {
                Bitmap bitmap = AppUtil.getBitmapFromFile(file);
                drawable = new BitmapDrawable(bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            }

            ImageLoader.getInstance().loadImage(s, EdusohoApp.app.mOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    mTextView.setText(Html.fromHtml(html, new NetImageGetter(mTextView, html), null));
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                }
            });

            return drawable;
        }
    }

    @Override
    protected void initView(AttributeSet attrs) {
    }

    @Override
    public void setData(HomeWorkQuestion questionSeq, int index) {
        super.setData(questionSeq, index);
        invalidateData();
    }

    /**
     * 光标处插入图片
     *
     * @param imageName
     * @param image
     */
    private void insertImage(EditText editText, String imageName, Bitmap image, String imageTag) {
        Editable eb = editText.getEditableText();
        //获得光标所在位置
        int qqPosition = editText.getSelectionStart();
        SpannableString ss = new SpannableString(imageTag);
        if (image.getWidth() > 500) {
            image = scaleImage(image);
        }

        //定义插入图片
        Drawable drawable = new BitmapDrawable(image);
        ss.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        drawable.setBounds(2, 0, drawable.getIntrinsicWidth() + 2, drawable.getIntrinsicHeight() + 2);

        //插入图片
        eb.insert(qqPosition, ss);
    }

    /**
     * 图片缩小
     *
     * @param bitmap
     * @return
     */
    private Bitmap scaleImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = AppUtil.dp2px(mContext, IMAGE_SIZE);

        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);

        return scaledBitmap;
    }
}
