package com.wetoop.ecard.bean;

import android.support.annotation.DimenRes;

import java.io.Serializable;

/**
 * Created by User on 2017/11/1.
 */

public class MainMenuItem implements Serializable {

    private static final long serialVersionUID = -4578331909627574211L;
    
    private
    @DimenRes
    int iconRes;
    private String name;

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
