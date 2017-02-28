package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;

import java.util.ArrayList;

/**
 * Created by Melomelon on 2015/6/15.
 */
public class ChooseClassDialogFragment extends DialogFragment{

    public static final int TYPE_LESSEN = 0;
    public static final int TYPE_CLASS = 1;
    private int mType;
    private LayoutInflater mInflater;

    private ActionBarBaseActivity mActivity;
    private EdusohoApp mApp;

    private TextView mText;
    private ListView mList;

    private ArrayList<Course> courseList;
//    班级
//    private ArrayList<> classList;

    private View view;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt("Type");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (ActionBarBaseActivity) activity;
        mApp = mActivity.app;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        view = inflater.inflate(R.layout.choose_class_dialog_layout,container,false);
        mText = (TextView) view.findViewById(R.id.choose_class_text);
        mList = (ListView) view.findViewById(R.id.choose_class_list);
        if(mType == TYPE_LESSEN){
            mText.setText("选择在学的课程");
            courseList = new ArrayList<Course>();
        }else {
            mText.setText("选择所在的班级");
            courseList = new ArrayList<Course>();
            //TODO
//            classList = new ArrayList<>()
        }
        MyAdapter mAdapter = new MyAdapter();
        mList.setAdapter(mAdapter);
        int i = 0;
        while (i<9){
            Course course = new Course();
            course.title = "课程"+i;
            course.about = "简介"+i+" "+"简介"+i+" "+"简介"+i+" "
                    +"简介"+i+" "+"简介"+i+" "+"简介"+i+" "
                    +"简介"+i+" "+"简介"+i+" "+"简介"+i+" "
                    +"简介"+i+" "+"简介"+i+" "+"简介"+i;
            int id = R.drawable.default_avatar;
            course.middlePicture = ""+id;
            mAdapter.addItem(course);
            i++;
        }

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                    bundle.putString("type","课程");
                    mApp.mEngine.runNormalPluginWithBundle("AddNormalFriend", mActivity, bundle);

            }
        });
        return view;
    }

    @Override
    public void onResume() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        getDialog().getWindow().setLayout(displayMetrics.widthPixels*5/6,displayMetrics.heightPixels*5/9);
        super.onResume();
    }



    public class MyAdapter extends BaseAdapter{

        public void addItem(Course course){
            courseList.add(course);
        }

        @Override
        public int getCount() {
            return courseList.size();
        }

        @Override
        public Object getItem(int position) {
            return courseList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if(convertView == null){
                holder = new Holder();
                convertView = getActivity().getLayoutInflater().inflate(R.layout.course_list_item, null);
                holder.pic = (ImageView) convertView.findViewById(R.id.course_pic);
                holder.title = (TextView) convertView.findViewById(R.id.course_title);
                holder.info = (TextView) convertView.findViewById(R.id.course_info);
                convertView.setTag(holder);
            }else {
                holder = (Holder) convertView.getTag();
            }
            holder.title.setText(courseList.get(position).title);
            holder.info.setText(courseList.get(position).about);
            holder.pic.setImageResource(Integer.parseInt(courseList.get(position).middlePicture));

            return convertView;
        }

        class Holder {
            TextView title;
            TextView info;
            ImageView pic;
        }
    }
}
