package com.wetoop.ecard.ui.dialog;

import android.support.annotation.ColorRes;

public class Text {

    private String text;
    private
    @ColorRes
    int textCRes = 0;

    public Text(String text, int textCRes) {
        this.text = text;
        this.textCRes = textCRes;
    }

    public Text(String text) {
        this.text = text;
        this.textCRes = 0;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextColor() {
        return textCRes;
    }

    public void setTextColor(int textCRes) {
        this.textCRes = textCRes;
    }
}