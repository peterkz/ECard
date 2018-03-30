package com.wetoop.ecard.listener;

import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wetoop.ecard.App;
import com.wetoop.ecard.ui.dialog.LoadingDialog;

import cn.edots.nest.log.Logger;
import rx.Subscriber;

/**
 * @author Parck.
 * @date 2017/11/17.
 * @desc
 */

public abstract class OnESubscriber<T> extends Subscriber<T> {
    private final Logger logger = new Logger(OnESubscriber.class.getSimpleName(), true);

    @Override
    public void onCompleted() {
    }

    protected abstract void onComplete(boolean success, T o, Throwable e);

    @Override
    public void onError(Throwable e) {
        LoadingDialog.hide();
        onComplete(false, null, e);
        try {
            logger.e("Error message:" + App.getMapper().writeValueAsString(e));
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
        }
        //Toast.makeText(App.getInstance(), "网络不给力", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNext(T o) {
        LoadingDialog.hide();
        onComplete(true, o, null);
    }
}
