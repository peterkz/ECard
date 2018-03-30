package com.wetoop.ecard.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.wetoop.ecard.R;

/**
 * @author Parck.
 * @date 2017/10/16.
 * @desc
 */

public class ConfirmDialog {

    private Context context;
    private Dialog confirmDialog;
    private Builder builder;

    public void show(Activity context) {
        init(context);
        confirmDialog.show();
    }

    public void show(Fragment context) {
        init(context.getActivity());
        this.confirmDialog.show();
    }

    public void hide() {
        if (confirmDialog != null) this.confirmDialog.dismiss();
    }

    private void init(Activity activity) {
        this.context = activity;
        this.confirmDialog = new Dialog(activity, R.style.ConfirmDialogTheme);
        this.confirmDialog.setContentView(R.layout.dialog_confirm);
        Window window = this.confirmDialog.getWindow();
        if (window != null) window.setDimAmount(0.3f);
        this.confirmDialog.setCanceledOnTouchOutside(builder.isHideOnTouchOutside());
        initView();
    }

    private void initView() {
        TextView titleText = findViewById(R.id.title_text);
        TextView messageText = findViewById(R.id.message_text);
        TextView leftButton = findViewById(R.id.cancel_button);
        TextView rightButton = findViewById(R.id.sure_button);

        titleText.setText(builder.getTitle().getText());
        titleText.setTextColor(context.getResources().getColor(builder.getTitle().getTextColor()));

        messageText.setText(builder.getMessage().getText());
        messageText.setTextColor(context.getResources().getColor(builder.getMessage().getTextColor()));

        leftButton.setText(builder.getLeftButton().getText());
        leftButton.setTextColor(context.getResources().getColor(builder.getLeftButton().getTextColor()));
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder.getLeftButton().getOnClickListener().onClick(v)) hide();
            }
        });

        rightButton.setText(builder.getRightButton().getText());
        rightButton.setTextColor(context.getResources().getColor(builder.getRightButton().getTextColor()));
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder.getRightButton().getOnClickListener().onClick(v)) hide();
            }
        });
    }

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    private <V extends View> V findViewById(@IdRes int id) {
        return (V) this.confirmDialog.findViewById(id);
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public static class Builder {

        private Text title = new Text("提示", R.color.light_black);
        private Text message = new Text("", R.color.light_grey);
        private Button leftButton = new Button("取消", R.color.light_grey, new OnClickListener() {
            @Override
            public boolean onClick(View v) {
                return true;
            }
        });
        private Button rightButton = new Button("确定", R.color.blueColor, new OnClickListener() {
            @Override
            public boolean onClick(View v) {
                return true;
            }
        });
        private boolean hideOnTouchOutside = true;
        private ConfirmDialog v2;

        public Text getTitle() {
            return title;
        }

        public Builder setTitle(String title) {
            this.title.setText(title);
            return this;
        }

        public Builder setTitle(String title, @ColorRes int color) {
            this.title.setText(title);
            this.title.setTextColor(color);
            return this;
        }

        public Builder setTitle(Text title) {
            this.title = title;
            return this;
        }

        public Text getMessage() {
            return message;
        }

        public Builder setMessage(String message) {
            this.message.setText(message);
            return this;
        }

        public Builder setMessage(String message, @ColorRes int color) {
            this.message.setText(message);
            this.message.setTextColor(color);
            return this;
        }

        public Builder setMessage(Text message) {
            this.message = message;
            return this;
        }

        public Button getLeftButton() {
            return leftButton;
        }

        public Builder setLeftButton(String text) {
            this.leftButton.setText(text);
            return this;
        }

        public Builder setLeftButton(String text, @ColorRes int color) {
            this.leftButton.setText(text);
            this.leftButton.setTextColor(color);
            return this;
        }

        public Builder setLeftButton(OnClickListener listener) {
            this.leftButton.setOnClickListener(listener);
            return this;
        }

        public Builder setLeftButton(@ColorRes int color, OnClickListener listener) {
            this.leftButton.setTextColor(color);
            this.leftButton.setOnClickListener(listener);
            return this;
        }

        public Builder setLeftButton(String text, OnClickListener listener) {
            this.leftButton.setText(text);
            this.leftButton.setOnClickListener(listener);
            return this;
        }

        public Builder setLeftButton(String text, @ColorRes int color, OnClickListener listener) {
            this.leftButton.setText(text);
            this.leftButton.setTextColor(color);
            this.leftButton.setOnClickListener(listener);
            return this;
        }

        public Builder setLeftButton(Button leftButton) {
            this.leftButton = leftButton;
            return this;
        }

        public Button getRightButton() {
            return rightButton;
        }

        public Builder setRightButton(String text) {
            this.rightButton.setText(text);
            return this;
        }

        public Builder setRightButton(String text, @ColorRes int color) {
            this.rightButton.setText(text);
            this.rightButton.setTextColor(color);
            return this;
        }

        public Builder setRightButton(@ColorRes int color, OnClickListener listener) {
            this.rightButton.setTextColor(color);
            this.rightButton.setOnClickListener(listener);
            return this;
        }

        public Builder setRightButton(OnClickListener listener) {
            this.rightButton.setOnClickListener(listener);
            return this;
        }

        public Builder setRightButton(String text, OnClickListener listener) {
            this.rightButton.setText(text);
            this.rightButton.setOnClickListener(listener);
            return this;
        }

        public Builder setRightButton(String text, @ColorRes int color, OnClickListener listener) {
            this.rightButton.setText(text);
            this.rightButton.setTextColor(color);
            this.rightButton.setOnClickListener(listener);
            return this;
        }

        public Builder setRightButton(Button rightButton) {
            this.rightButton = rightButton;
            return this;
        }

        public boolean isHideOnTouchOutside() {
            return hideOnTouchOutside;
        }

        public Builder setHideOnTouchOutside(boolean hideOnTouchOutside) {
            this.hideOnTouchOutside = hideOnTouchOutside;
            return this;
        }

        public ConfirmDialog build() {
            v2 = new ConfirmDialog();
            v2.setBuilder(this);
            return v2;
        }
    }

}
