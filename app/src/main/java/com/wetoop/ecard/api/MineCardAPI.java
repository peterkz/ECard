package com.wetoop.ecard.api;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * @author Parck.
 * @date 2017/11/20.
 * @desc
 */

public class MineCardAPI implements Constants {

    private final SyncReference rootRef;
    private final CardAllAPI cardAllAPI;
    private final SearchPhoneAPI searchPhoneAPI;
    private final SearchAddressAPI searchAddressAPI;

    public MineCardAPI() {
        rootRef = App.getReference(URI_CARDS + App.getCurrentUser().getUid());
        cardAllAPI = APIProvider.get(CardAllAPI.class);
        searchPhoneAPI = APIProvider.get(SearchPhoneAPI.class);
        searchAddressAPI = APIProvider.get(SearchAddressAPI.class);
    }

    /**
     * 创建我的名片
     *
     * @param card
     */
    public void create(final Card card, final SyncReference.CompletionListener listener) {
        SyncReference reference = rootRef.push();
        card.getInformation().get(0).setCard_id(reference.getKey());
        card.getInformation().get(0).setType(0);
        card.getInformation().get(0).setUser_id(App.getCurrentUser().getUid());
        card.getInformation().get(0).setDateUpdated(new Date());
        reference.setValue(card, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError syncError, SyncReference syncReference) {
                if (listener != null) listener.onComplete(syncError, syncReference);
                if (syncError != null)
                    Log.e(ContactUpdateAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        searchPhoneAPI.sync(card);
                        searchAddressAPI.sync(card);
                        cardAllAPI.createOrUpdate(card, null);
                        APIProvider.get(ContactUpdateAPI.class).create(card);
                    }
                }).subscribeOn(Schedulers.io()).subscribe();
            }
        });
    }

    /**
     * 删除我的名片
     *
     * @param key
     * @param listener
     */
    public void delete(String key, SyncReference.CompletionListener listener) {
        if (listener != null)
            rootRef.child(key).removeValue(listener);
        else
            rootRef.child(key).removeValue();
        cardAllAPI.delete(key);
    }

    /**
     * 更新我的名片
     *
     * @param card
     */
    public void update(final Card card, final SyncReference.CompletionListener listener) {
        card.getInformation().get(0).setDateUpdated(new Date());
        rootRef.child(card.getInformation().get(0).getCard_id()).setValue(card, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError syncError, SyncReference syncReference) {
                if (listener != null) listener.onComplete(syncError, syncReference);
                if (syncError != null)
                    Log.e(ContactUpdateAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
                else
                    Observable.create(new Observable.OnSubscribe<String>() {
                        @Override
                        public void call(Subscriber<? super String> subscriber) {
                            APIProvider.get(ContactAPI.class).list(new OnESubscriber<List<Card>>() {
                                @Override
                                protected void onComplete(boolean success, List<Card> o, Throwable e) {
                                    if (success && o != null) {
                                        Set<String> userIds = new HashSet<>();
                                        for (Card c : o) {
                                            if (c.getInformation().get(0).getMine_id().contains(card.getInformation().get(0).getCard_id()))
                                                userIds.add(c.getInformation().get(0).getUser_id());
                                        }
                                        if (userIds.size() > 0) {
                                            APIProvider.get(MessageAPI.class).sendUpdateMessage(CardBean.fromModel(card), userIds, new SyncReference.CompletionListener() {
                                                @Override
                                                public void onComplete(SyncError syncError, SyncReference syncReference) {
                                                    if (syncError != null)
                                                        Log.e(MineCardAPI.class.getSimpleName(), "syncError:" + syncError.getMessage());
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                            cardAllAPI.createOrUpdate(card, null);
                            searchPhoneAPI.sync(card);
                            searchAddressAPI.sync(card);
                        }
                    }).subscribeOn(Schedulers.io()).subscribe();
            }
        });
    }

    /**
     * 我的名片列表
     *
     * @return
     */
    public void list(final OnESubscriber<List<Card>> subscriber) {
        rootRef.addValueEventListener(new OnEValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, new TypeReference<List<Card>>() {
                }).subscribe(subscriber);
            }
        });
    }

    /**
     * 我的名片详情
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
