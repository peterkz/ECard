package com.wetoop.ecard.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;

import com.wetoop.ecard.R;

/**
 * @author Parck.
 * @date 2017/10/12.
 * @desc
 */

public class LoadingDialog {

    private static Dialog loadingDialog;

    public static void show(Activity activity) {
        show(activity, null);
    }

    public static void show(Activity activity, final DialogInterface.OnCancelListener listener) {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(activity, R.style.LoadingDialogTheme);
            loadingDialog.setContentView(R.layout.dialog_loading);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.getWindow().setDimAmount(0.0f);
        }
        if (loadingDialog.isShowing()) {
            return;
        }
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null)
                    listener.onCancel(dialog);
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
        loadingDialog.show();
    }

    public static void hide() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

}
