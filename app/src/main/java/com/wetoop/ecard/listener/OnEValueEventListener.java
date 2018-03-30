package com.wetoop.ecard.listener;

import android.widget.Toast;

import com.wetoop.ecard.App;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wilddog.client.SyncError;
import com.wilddog.client.ValueEventListener;

import cn.edots.nest.log.Logger;


/**
 * @author Parck.
 * @date 2017/10/17.
 * @desc
 */

public abstract class OnEValueEventListener implements ValueEventListener {

    private Logger logger = new Logger(this.getClass().getSimpleName(), true);

    @Override
    public void onCancelled(SyncError syncError) {
        logger.w("ErrCode : " + syncError.getErrCode());
        logger.w("Message : " + syncError.getMessage());
        logger.w("Details : " + syncError.getDetails());
        if (syncError.getErrCode() == 26101)
            Toast.makeText(App.getInstance(), "身份失效请重新登录", Toast.LENGTH_SHORT).show();
        LoadingDialog.hide();
//        Toast.makeText(App.getInstance(), "网络不给力", Toast.LENGTH_SHORT).show();
    }
}
