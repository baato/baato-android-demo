package com.baato.baatoandroiddemo.models;

public class HomeMenu {
    public static final int SERVICE_TYPE=0;
    public static final int MAP_TYPE=1;
    public static final int TEXT_TYPE=2;

    public String title;
    public int type;
    public int icon;

    public HomeMenu(String title, int type, int icon) {
        this.title = title;
        this.type = type;
        this.icon = icon;
    }
}
