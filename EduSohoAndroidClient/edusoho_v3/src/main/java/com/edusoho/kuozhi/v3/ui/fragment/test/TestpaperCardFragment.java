package com.edusoho.kuozhi.v3.ui.fragment.test;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.adapter.test.TestpaperCardAdapter;
import com.edusoho.kuozhi.v3.model.bal.Answer;
import com.edusoho.kuozhi.v3.model.bal.test.PaperResult;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionTypeSeq;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;
import com.edusoho.kuozhi.v3.ui.test.TestpaperActivity;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.EduSohoButton;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by howzhi on 14-9-19.
 */
public class TestpaperCardFragment extends DialogFragment {

    private LinearLayout mCardLayout;
    private EduSohoButton mSubmitBtn;
    private boolean mIsShowDlg;
    private TestpaperActivity mTestpaperActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.PopDialogTheme);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTestpaperActivity = (TestpaperActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.testpaper_card_fragment, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(mTestpaperActivity);
        mSubmitBtn = (EduSohoButton) view.findViewById(R.id.testpaper_card_submit);
        mCardLayout = (LinearLayout) view.findViewById(R.id.testpaper_card_layout);

        HashMap<QuestionType, ArrayList<QuestionTypeSeq>> questionTypeArrayListHashMap =
                mTestpaperActivity.getAllQuestions();

        HashMap<QuestionType, ArrayList<Answer>> answerMap = mTestpaperActivity.getAnswer();
        for (QuestionType type : questionTypeArrayListHashMap.keySet()) {
            View cardView = layoutInflater.inflate(R.layout.testpaper_card_layout, null);

            TextView label = (TextView) cardView.findViewById(R.id.testpaper_card_label);
            GridView cardGridView = (GridView) cardView.findViewById(R.id.testpaper_card_gridview);

            ArrayList<QuestionTypeSeq> questionTypeSeqs = questionTypeArrayListHashMap.get(type);
            if (type == QuestionType.material) {
                questionTypeSeqs = getMaterialItems(questionTypeSeqs);
            }

            TestpaperCardAdapter adapter = new TestpaperCardAdapter(
                    mTestpaperActivity,
                    questionTypeSeqs,
                    answerMap.get(type),
                    R.layout.testpaper_card_gridview_item
            );

            cardGridView.setAdapter(adapter);
            label.setText(type.title());
            mCardLayout.addView(cardView);
        }

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LoadDialog loadDialog = LoadDialog.create(mTestpaperActivity);
                loadDialog.setMessage("提交试卷");
                loadDialog.show();

                PaperResult paperResult = mTestpaperActivity.getTestpaperResult();
                RequestUrl requestUrl = mTestpaperActivity.app.bindUrl(
                        Const.FINISH_TESTPAPER, true);
                Map<String, String> params = requestUrl.getParams();
                params.put("usedTime", mTestpaperActivity.getUsedTime() + "");
                params.put("id", paperResult.id + "");

                HashMap<QuestionType, ArrayList<QuestionTypeSeq>> questionMap = mTestpaperActivity.getTestpaperQuestions();
                HashMap<QuestionType, ArrayList<Answer>> answerMap = mTestpaperActivity.getAnswer();

                for (QuestionType qt : questionMap.keySet()) {
                    ArrayList<QuestionTypeSeq> questionTypeSeqs = questionMap.get(qt);
                    ArrayList<Answer> answers = answerMap.get(qt);
                    int length = questionTypeSeqs.size();
                    for (int i = 0; i < length; i++) {
                        QuestionTypeSeq questionTypeSeq = questionTypeSeqs.get(i);
                        Answer answer = answers.get(i);
                        if (!answer.isAnswer) {
                            continue;
                        }

                        int position = 0;
                        for (Object object : answer.data) {
                            params.put(
                                    String.format("data[%d][%d]", questionTypeSeq.questionId, position++),
                                    object.toString());
                        }
                    }
                }

                mTestpaperActivity.ajaxPost(requestUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadDialog.dismiss();
                        boolean result = mTestpaperActivity.parseJsonValue(
                                response, new TypeToken<Boolean>() {
                                });
                        if (result) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FragmentPageActivity.FRAGMENT, "TestpaperResultFragment");
                            bundle.putString(Const.ACTIONBAR_TITLE, " 考试结果");

                            PaperResult paperResult = mTestpaperActivity.getTestpaperResult();
                            bundle.putInt(TestpaperResultFragment.RESULT_ID, paperResult.id);

                            mTestpaperActivity.app.mEngine.runNormalPluginWithBundle(
                                    "FragmentPageActivity", mTestpaperActivity, bundle);
                            mTestpaperActivity.finish();
                        }
                    }
                }, null);
            }
        });
    }

    private ArrayList<QuestionTypeSeq> getMaterialItems(
            ArrayList<QuestionTypeSeq> questionTypeSeqs) {
        ArrayList<QuestionTypeSeq> list = new ArrayList<QuestionTypeSeq>();
        for (QuestionTypeSeq questionTypeSeq : questionTypeSeqs) {

            list.addAll(questionTypeSeq.items);
        }
        return list;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.BOTTOM);

        lp.width = EdusohoApp.screenW;
        lp.height = (int) (EdusohoApp.screenH * 0.8f);

        dialogWindow.setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsShowDlg) {
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    return i == keyEvent.KEYCODE_BACK;
                }
            });
            showSubmitDialog();
        }
    }

    public void setNotCancel() {
        mIsShowDlg = true;
        setCancelable(false);
    }

    private PopupDialog popupDialog;

    private void showSubmitDialog() {
        if (popupDialog == null) {
            popupDialog = PopupDialog.createNormal(
                    getActivity(), "考试结束", "考试时间结束，请交卷");
            popupDialog.setOkText("查看答题卡");
            popupDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    popupDialog = null;
                }
            });
            popupDialog.show();
        }
    }

}
