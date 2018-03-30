package com.wetoop.ecard.ui.dialog;

public class Button extends Text {

    private OnClickListener onClickListener;

    public Button(String text, int textCRes) {
        super(text, textCRes);
    }

    public Button(String text) {
        super(text);
    }

    public Button(String text, OnClickListener listener) {
        super(text);
        this.onClickListener = listener;
    }

    public Button(String text, int textCRes, OnClickListener listener) {
        super(text, textCRes);
        this.onClickListener = listener;
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}