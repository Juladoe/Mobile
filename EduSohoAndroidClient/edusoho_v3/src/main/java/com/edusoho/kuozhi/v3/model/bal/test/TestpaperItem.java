package com.edusoho.kuozhi.v3.model.bal.test;

/**
 * Created by howzhi on 14-9-23.
 */
public class TestpaperItem {

    public String title;
    public String[] content;
    public boolean isFirstPadding;


    public TestpaperItem(
            String title, String[] content, boolean isFirstPadding){
        this.title = title;
        this.content = content;
        this.isFirstPadding = isFirstPadding;
    }
}
