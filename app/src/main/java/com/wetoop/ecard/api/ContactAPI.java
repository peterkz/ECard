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

public class ContactAPI implements Constants {

    private final SyncReference rootRef;

    public ContactAPI() {
        rootRef = App.getReference(URI_BUDDIES + App.getCurrentUser().getUid());
    }

    /**
     * 保存联系人名片
     *
     * @param card
     */
    public void save(final Card card, SyncReference.CompletionListener listener) {
        card.getInformation().get(0).setType(1);
        if (listener != null)
            rootRef.child(card.getInformation().get(0).getCard_id()).setValue(card, listener);
        else
            rootRef.child(card.getInformation().get(0).getCard_id()).setValue(card);
    }

    /**
     * 创建联系人名片
     *
     * @param card
     */
    public void create(Card card, SyncReference.CompletionListener listener) {
        SyncReference reference = rootRef.push();
        card.getInformation().get(0).setCard_id(reference.getKey());
        card.getInformation().get(0).setType(2);
        card.getInformation().get(0).setUser_id(null);
        if (listener != null)
            reference.setValue(card, listener);
        else
            reference.setValue(card);
    }

    /**
     * 删除联系人名片
     *
     * @param key
     * @param listener
     */
    public void delete(String key, SyncReference.CompletionListener listener) {
        if (listener != null)
            rootRef.child(key).removeValue(listener);
        else
            rootRef.child(key).removeValue();
    }

    public void deleteAll() {
        rootRef.removeValue();
    }

    /**
     * 修改联系人名片
     *
     * @param card
     */
    public void update(Card card, SyncReference.CompletionListener listener) {
        if (listener != null)
            rootRef.child(card.getInformation().get(0).getCard_id()).setValue(card, listener);
        else
            rootRef.child(card.getInformation().get(0).getCard_id()).setValue(card);
    }

    /**
     * 联系人名片列表
     *
     * @param subscriber
     */
    public void list(final OnESubscriber<List<Card>> subscriber) {
        rootRef.addValueEventListener(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, new TypeReference<List<Card>>() {
                }).subscribe(subscriber);
            }
        });
    }

    /**
     * 联系人名片列表
     *
     * @param subscriber
     */
    public void get(String key, final OnESubscriber<Card> subscriber) {
        rootRef.child(key).addValueEventListener(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, Card.class).subscribe(subscriber);
            }
        });
    }
}
