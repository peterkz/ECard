package com.wetoop.ecard.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.ecard.R;
import com.wetoop.ecard.tools.InputMethodTool;

/**
 * Created by User on 2017/12/15.
 */

public class CustomAddContactDialog {
    private Dialog confirmDialog;
    private TextView titleText;
    private EditText messageText;
    private TextView cancelButton;
    private TextView sureText;
    private Activity activity;

    public void show(Activity activity, OnClickListener listener) {
        this.activity = activity;
        show(activity, true, listener);
    }

    public void show(Activity activity, boolean cancelOutOutside, OnClickListener listener) {
        init(activity, cancelOutOutside);
        initView();
        initListener(listener);
        InputMethodTool.requestInput(messageText);
        this.confirmDialog.show();
    }

    private void init(Activity activity, boolean cancelOutOutside) {
        if (this.confirmDialog == null) {
            this.confirmDialog = new Dialog(activity, R.style.ConfirmDialogTheme);
            this.confirmDialog.setContentView(R.layout.dialog_custom_add_contact);
            this.confirmDialog.getWindow().setDimAmount(0.3f);
            this.confirmDialog.setCanceledOnTouchOutside(cancelOutOutside);
        }
    }

    private void initView() {
        this.titleText = (TextView) this.confirmDialog.findViewById(R.id.title_text);
        this.messageText = (EditText) this.confirmDialog.findViewById(R.id.message_text);
        this.cancelButton = (TextView) this.confirmDialog.findViewById(R.id.cancel_button);
        this.sureText = (TextView) this.confirmDialog.findViewById(R.id.sure_button);
    }

    private void initListener(final OnClickListener listener) {
        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onCancel();
                hide();
            }
        });

        this.sureText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    String messageTextStr = messageText.getText().toString().trim();
                    if (!"".equals(messageTextStr)) {
                        if(messageTextStr.length()<11) {
                            Toast.makeText(activity, "请输入正确的号码", Toast.LENGTH_SHORT).show();
                        }else{
                            listener.onSure(CustomAddContactDialog.this, messageTextStr);
                            hide();
                        }
                    } else {
                        Toast.makeText(activity, "不能为空", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    hide();
                }
            }
        });
    }

    public void hide() {
        if (this.confirmDialog != null) {
            this.confirmDialog.dismiss();
            this.confirmDialog = null;
        }
    }

    // =====================================================
    // inner class
    // =====================================================

    public static abstract class OnClickListener {

        public abstract void onSure(CustomAddContactDialog dialog, String editText);

        public void onCancel() {

        }
    }
}