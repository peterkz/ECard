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
 * @date 2017/11/28.
 * @desc
 */

public class AppsAPI implements Constants {

    private SyncReference rootRef;

    public AppsAPI() {
        rootRef = App.getReference(URI_APPS + App.getCurrentUser().getUid());
    }

    /**
     * 创建
     *
     * @param value
     * @param listener
     */
    public void create(UrlBean value, SyncReference.CompletionListener listener) {
        SyncReference reference = rootRef.push();
        value.setId(reference.getKey());
        if (listener != null) reference.setValue(value, listener);
        else reference.setValue(value);
    }

    /**
     * 列表
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

    public void deleteAll() {
        rootRef.removeValue();
    }

    /**
     * 设置优先级
     *
     * @param key
     * @param priority
     */
    public void setPriority(String key, int priority) {
        rootRef.child(key).setPriority(priority);
    }

}
