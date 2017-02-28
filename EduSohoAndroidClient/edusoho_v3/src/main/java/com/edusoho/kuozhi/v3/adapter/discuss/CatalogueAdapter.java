package com.edusoho.kuozhi.v3.adapter.discuss;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by DF on 2017/1/4.
 */

public class CatalogueAdapter extends RecyclerView.Adapter {

    public ArrayList<View> headerViews=new ArrayList<>();
    public ArrayList<View> footViews=new ArrayList<>();
    public RecyclerView.Adapter adapter;

    public CatalogueAdapter(RecyclerView.Adapter adapter, ArrayList footViews){
        this.adapter=adapter;
        this.footViews=footViews;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType== RecyclerView.INVALID_TYPE){
            //头部item
            return new RecyclerView.ViewHolder(headerViews.get(0)){};
        }else if(viewType== (RecyclerView.INVALID_TYPE-1)){
            //尾部item
            return new RecyclerView.ViewHolder(footViews.get(0)){};
        }
        return adapter.onCreateViewHolder(parent,viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position>=0&&position<headerViews.size()){
            return;
        }
        if(adapter!=null){
            int p=position-headerViews.size();
            if(p<adapter.getItemCount()){
                adapter.onBindViewHolder(holder,p);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position>=0 && position<headerViews.size()){
            return RecyclerView.INVALID_TYPE;
        }
        if(adapter!=null){
            int p=position-headerViews.size();
            if(p<adapter.getItemCount()){
                return adapter.getItemViewType(p);
            }
        }
        return RecyclerView.INVALID_TYPE-1;
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public int getCount(){
        int count=headerViews.size()+footViews.size();
        if(adapter!=null){
            count+=adapter.getItemCount();
        }
        return count;
    }
}
