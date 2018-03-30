package com.wetoop.ecard.api;

import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

/**
 * @author Parck.
 * @date 2017/12/12.
 * @desc
 */

public class CardAllAPI implements Constants {

    private final SyncReference rootRef;

    public CardAllAPI() {
        rootRef = App.getReference(URI_CARD_ALL);
    }

    public void createOrUpdate(Card card, SyncReference.CompletionListener listener) {
        if (listener != null)
            rootRef.child(card.getInformation().get(0).getCard_id()).setValue(card, listener);
        else
            rootRef.child(card.getInformation().get(0).getCard_id()).setValue(card);
    }

    public void delete(String key) {
        rootRef.child(key).removeValue(new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError syncError, SyncReference syncReference) {
                if (syncError != null) new Throwable("更新公共库失败，message：" + syncError.getMessage());
            }
        });
    }

    /**
     * @param subscriber
     */
    public void get(String key, final OnESubscriber<Card> subscriber) {
        rootRef.child(key).addValueEventListener(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, Card.class).subscribe(subscriber);
                rootRef.removeEventListener(this);
            }
        });
    }
    public void singleGet(String key, final OnESubscriber<Card> subscriber) {
        rootRef.child(key).addListenerForSingleValueEvent(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, Card.class).subscribe(subscriber);
            }
        });
    }
}
