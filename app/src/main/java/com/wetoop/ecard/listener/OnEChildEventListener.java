package com.wetoop.ecard.listener;

import android.widget.Toast;

import com.wetoop.ecard.App;
import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;

import cn.edots.nest.log.Logger;

/**
 * @author Parck.
 * @date 2017/10/27.
 * @desc
 */

public abstract class OnEChildEventListener implements ChildEventListener {

    private Logger logger = new Logger(OnEChildEventListener.class.getSimpleName(), App.getInstance().isDebug());

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(SyncError syncError) {
        logger.w("ErrCode : " + syncError.getErrCode());
        logger.w("Message : " + syncError.getMessage());
        logger.w("Details : " + syncError.getDetails());
        Toast.makeText(App.getInstance(), "网络不给力", Toast.LENGTH_SHORT).show();
    }
}
