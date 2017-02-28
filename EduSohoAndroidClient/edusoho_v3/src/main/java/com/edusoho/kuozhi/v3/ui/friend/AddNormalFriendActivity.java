package com.edusoho.kuozhi.v3.ui.friend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.bal.Friend;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.Const;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Melomelon on 2015/6/9.
 *
 * 添加课程好友
 * 添加班级好友
 *
 */
public class AddNormalFriendActivity extends ActionBarBaseActivity{

    public final String mTitle = "添加班级好友";
    public boolean isClass = false;
    private ArrayList<Friend> mTeacherList;
    private ArrayList<Friend> mStudentList;
    private ArrayList<Friend> mFriendsList;

    private AddNormalFriendAdapter mAdapter;
    private ListView mList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_normal_friend_layout);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String string = bundle.getString("type");

        /**选择课程还是课时**/
        if(string.equals("班级")){
            setBackMode(BACK,mTitle);
            isClass = true;
        }else {
            setBackMode(BACK,"添加课程好友");
            isClass = false;
        }

        mList = (ListView) findViewById(R.id.add_normal_friend_list);
        mAdapter = new AddNormalFriendAdapter(R.layout.add_friend_item);

        mStudentList = new ArrayList();
        mTeacherList = new ArrayList();
        mFriendsList = new ArrayList();

        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
            }
        });

        loadFriends();
        mAdapter.addItems(mTeacherList);

        mAdapter.addItems(mStudentList);

    }

    public void loadFriends(){

    }




    public class AddNormalFriendAdapter extends BaseAdapter{

        private int mResource;

        public AddNormalFriendAdapter(int mResource) {
            this.mResource = mResource;
        }

        @Override
        public int getCount() {
            return mFriendsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFriendsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addItem(Friend friend){
            if(friend.isTeacher){
                mTeacherList.add(friend);
            }else {
                mStudentList.add(friend);
            }

        }

        public void addItems(ArrayList<Friend> list){
            mFriendsList.addAll(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder holder;
            if(convertView == null){
                holder = new ItemHolder();
                convertView = getLayoutInflater().inflate(mResource,null);
                holder.divider = convertView.findViewById(R.id.tpye_divider);
                holder.typeText = (TextView) convertView.findViewById(R.id.type_text);
                holder.friendImage = (CircleImageView) convertView.findViewById(R.id.add_friend_image);
                holder.friendName = (TextView) convertView.findViewById(R.id.add_friend_name);
                holder.state = (ImageView) convertView.findViewById(R.id.add_friend_state);
                convertView.setTag(holder);
            }else {
                holder = (ItemHolder) convertView.getTag();
            }

            if(position == 0){
                holder.typeText.setVisibility(View.VISIBLE);
                if(isClass){
                    holder.typeText.setText("班级教师"+"("+mTeacherList.size()+")");
                }else {
                    holder.typeText.setText("课程教师"+"("+mTeacherList.size()+")");
                }
            }else if(position == mTeacherList.size()){
                holder.typeText.setVisibility(View.VISIBLE);
                holder.divider.setVisibility(View.VISIBLE);
                if(isClass){
                    holder.typeText.setText("班级好友"+"("+mTeacherList.size()+")");
                }else {
                    holder.typeText.setText("课程好友"+"("+mStudentList.size()+")");
                }
            }else {
                holder.typeText.setVisibility(View.GONE);
                holder.divider.setVisibility(View.GONE);
            }

            holder.friendImage.setImageResource(mFriendsList.get(position).avatarID);
            holder.friendName.setText(mFriendsList.get(position).nickname);
            switch (mFriendsList.get(position).friendship){
                case Const.HAVE_ADD_TRUE:
                    holder.state.setImageResource(R.drawable.have_add_friend_true);
                    break;
                case Const.HAVE_ADD_FALSE:
                    holder.state.setImageResource(R.drawable.add_friend_selector);
                    break;
                case Const.HAVE_ADD_WAIT:
                    holder.state.setImageResource(R.drawable.have_add_friend_wait);
                    break;
            }

            return convertView;
        }

        private class ItemHolder{
            View divider;
            TextView typeText;
            CircleImageView friendImage;
            TextView friendName;
            ImageView state;
        }
    }
}
