package com.wetoop.ecard.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.bean.UrlBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncReference;

import java.util.List;

/**
 * @author Parck.
 * @date 2017/11/23.
 * @desc
 */

public class CollectAPI implements Constants {

    private final SyncReference rootRef;

    public CollectAPI() {
        rootRef = App.getReference("");
    }

    /**
     * 推荐列表
     *
     * @param subscriber
     */
    public void list(final OnESubscriber<List<UrlBean>> subscriber) {
        rootRef.orderByPriority().addValueEventListener(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, new TypeReference<List<UrlBean>>() {
                }).subscribe(subscriber);
            }
        });
    }

}
