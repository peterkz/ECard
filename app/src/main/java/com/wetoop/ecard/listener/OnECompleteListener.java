package com.wetoop.ecard.listener;

import android.app.Activity;
import android.widget.Toast;

import com.wetoop.ecard.App;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;

import cn.edots.nest.core.SlugResourceProvider;
import cn.edots.nest.log.Logger;

/**
 * @author Parck.
 * @date 2017/10/12.
 * @desc
 */

public abstract class OnECompleteListener<T> implements OnCompleteListener<T> {

    private final String TAG = this.getClass().getSimpleName();

    private Logger logger;
    private Activity activity;

    public OnECompleteListener(Activity activity) {
        this.activity = activity;
        LoadingDialog.show(activity);
        logger = new Logger(TAG, ((SlugResourceProvider) activity.getApplication()).isDebug());
    }

    @Override
    public void onComplete(Task<T> task) {
        complete(task);
        if (task.isSuccessful()) {
            success(task);
        } else {
            fail(task);
        }
    }

    public void fail(Task<T> task) {
        String error = task.getException().toString();
        String[] errorCode = error.split("errCode:");
        String[] message = errorCode[1].split(" message:");
        String errorMessage = App.getErrorMessage(Integer.parseInt(message[0]));
        Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
        logger.e("errorCode:" + errorCode + "  message:" + message);
    }

    public void complete(Task<T> task) {
        LoadingDialog.hide();
    }

    public abstract void success(Task<T> task);
}
