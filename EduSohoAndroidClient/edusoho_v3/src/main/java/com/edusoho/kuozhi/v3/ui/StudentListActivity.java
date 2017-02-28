package com.edusoho.kuozhi.v3.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.entity.lesson.StudentList;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by DF on 2016/12/19.
 */

public class StudentListActivity extends ActionBarBaseActivity {

    private StudentList studentList;
    private GridView glStudentList;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_student_list);
        initData();
        initView();
    }

    private void initData() {
        final LoadDialog loadDialog = LoadDialog.create(StudentListActivity.this);
        loadDialog.show();
        String path = String.format(Const.STUDENT_LIST, "");
        RequestUrl requestUrl = app.bindNewUrl(path, true);
        mActivity.ajaxGet(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadDialog.dismiss();
                studentList = parseJsonValue(response, new TypeToken<StudentList>(){});
                if (studentList != null && studentList.getPrevious().size() != 0) {
                    initCatalog();
                } else {
                    CommonUtil.shortCenterToast(StudentListActivity.this, "该班级没有课程");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

    }


    private void initView() {
        TextView tvBack = (TextView) findViewById(R.id.tv_back);
        glStudentList = (GridView) findViewById(R.id.gv_student_list);
        tvBack.setOnClickListener(getBackOnclickListener());
    }

    private void initCatalog() {
        ArrayList<HashMap<String, String>> imagelist = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < studentList.getPrevious().size(); i++) {

        }
//        glStudentList.setAdapter(new SimpleAdapter(StudentListActivity.this, imagelist, R.layout.item_student_list, new));
    }

    public View.OnClickListener getBackOnclickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StudentListActivity.this.finish();
            }
        };
    }
}
