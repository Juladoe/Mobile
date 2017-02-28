package com.edusoho.kuozhi.v3.model.bal.article;

import java.util.List;

/**
 * Created by howzhi on 15/9/9.
 */
public class MenuItem {

    public String id;
    public String title;
    public String type;
    public String action;

    private List<MenuItem> subMenus;

    public static final String MENU = "menu";
    public static final String WEBVIEW = "webview";
    public static final String DATA = "data";

    public static MenuItem createMoreMenu() {
        MenuItem menuItem = new MenuItem();
        menuItem.id = "-1";
        menuItem.title = "更多";
        menuItem.type = MENU;
        menuItem.action = "";

        return menuItem;
    }

    @Override
    public String toString() {
        return title;
    }

    public void setSubMenus(List<MenuItem> menus) {
        this.subMenus = menus;
    }

    public List<MenuItem> getSubMenus() {
        return subMenus;
    }
}
