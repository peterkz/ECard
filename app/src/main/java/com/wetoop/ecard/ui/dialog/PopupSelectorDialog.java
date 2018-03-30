package com.wetoop.ecard.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;

/**
 * @author Parck.
 * @date 2017/10/23.
 * @desc
 */
public class PopupSelectorDialog {

    private Activity activity;
    private Dialog genderSelectorDialog;
    private TextView cancelText;
    private OnClickListener onClickListener;
    private Object data;

    private String[] items;

    public PopupSelectorDialog(Activity activity, String[] items, OnClickListener onClickListener) {
        init(activity, items, onClickListener);
        initView();
        initListener();
    }

    private void init(Activity activity, String[] items, OnClickListener onClickListener) {
        if (this.genderSelectorDialog == null) {
            this.genderSelectorDialog = new Dialog(activity, R.style.GenderSelectorDialogTheme);
            this.genderSelectorDialog.setContentView(R.layout.dialog_popup_selector);
            this.genderSelectorDialog.setCanceledOnTouchOutside(true);
            Window window = this.genderSelectorDialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
                window.setDimAmount(0.3f);
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lp);
            }
        }
        this.activity = activity;
        this.onClickListener = onClickListener;
        this.items = items;
    }

    private void initView() {
        LinearLayout itemsContainer = (LinearLayout) this.genderSelectorDialog.findViewById(R.id.items_container);
        this.cancelText = (TextView) this.genderSelectorDialog.findViewById(R.id.cancel_text);

        for (int i = 0; i < items.length; i++) {
            TextView textChild = new TextView(activity);
            textChild.setText(items[i]);
            textChild.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textChild.setTextColor(activity.getResources().getColor(R.color.light_blue));
            textChild.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, App.getInstance().dip2px(46)));
            textChild.setGravity(Gravity.CENTER);
            itemsContainer.addView(textChild);
            if (i < items.length - 1) {
                View lineChild = new View(activity);
                lineChild.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                lineChild.setBackgroundResource(R.color.line_gray);
                itemsContainer.addView(lineChild);
            }
            final int position = i;
            textChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null)
                        onClickListener.onSure(position, ((TextView) v).getText());
                    hide();
                }
            });
        }
    }

    private void initListener() {
        this.cancelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        this.genderSelectorDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (onClickListener != null) onClickListener.onCancel();
            }
        });
    }

    public void show() {
        if (this.genderSelectorDialog != null) {
            this.genderSelectorDialog.show();
        }
    }

    public void hide() {
        if (this.genderSelectorDialog != null) {
            this.genderSelectorDialog.dismiss();
        }
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    // =====================================================
    // inner class
    // =====================================================

    public static class Builder {
        private Item item;

    }

    private static class Item {
        private Button button;

    }

    public static abstract class OnClickListener {

        public abstract void onSure(int position, CharSequence text);

        public void onCancel() {

        }
    }
}
