package com.wetoop.ecard.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncReference;

import java.util.List;

/**
 * @author Parck.
 * @date 2017/11/21.
 * @desc
 */

public class ExchangeAPI implements Constants {

    private final SyncReference rootRef;

    public ExchangeAPI() {
        rootRef = App.getReference(URI_EXCHANGE);
    }

    public OnEValueEventListener listValueListener;

    /**
     * 创建
     *
     * @param card
     * @param listener
     */
    public void upload(Card card, SyncReference.CompletionListener listener) {
        if (listener != null)
            rootRef.child(card.getInformation().get(0).getCard_id()).setValue(card, listener);
        else
            rootRef.child(card.getInformation().get(0).getCard_id()).setValue(card);

    }

    /**
     * 名片交换
     *
     * @param key
     * @param listener
     */
    public void exchange(String key, final SyncReference.CompletionListener listener) {
        rootRef.child(key).addValueEventListener(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, Card.class).subscribe(new OnESubscriber<Card>() {
                    @Override
                    protected void onComplete(boolean success, Card o, Throwable e) {
                        APIProvider.get(ContactAPI.class).save(o, listener);
                    }
                });
            }
        });
    }

    /**
     * 清除位置信息
     *
     * @param key
     */
    public void clear(String key, SyncReference.CompletionListener listener) {
        if (listener != null)
            rootRef.child(key).removeValue(listener);
        else
            rootRef.child(key).removeValue();
    }

    /**
     * 交换页面数据列表
     *
     * @param subscriber
     */
    public void list(final OnESubscriber<List<Card>> subscriber) {
        listValueListener = new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, new TypeReference<List<Card>>() {
                }).subscribe(subscriber);
            }
        };
        rootRef.addValueEventListener(listValueListener);
    }

    public void removeListValueListener() {
        if (listValueListener != null)
            rootRef.removeEventListener(listValueListener);
    }

}
