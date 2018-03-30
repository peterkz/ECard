package com.wetoop.ecard.api;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.api.model.PhoneSearch;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wetoop.ecard.tools.MD5;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author Parck.
 * @date 2017/12/14.
 * @desc
 */

public class SearchPhoneAPI implements Constants {

    private final SyncReference rootRef;

    public SearchPhoneAPI() {
        rootRef = App.getReference(URI_SEARCH_PHONE);
    }

    /**
     * 同步
     *
     * @param card
     */
    public void sync(Card card) {
        if (card == null || card.getPhone() == null || card.getPhone().size() == 0) return;
        Information information = card.getInformation().get(0);
        for (Phone phone : card.getPhone()) {
            if (information.isPrivacy()) {
                rootRef.child(MD5.encoding(phone.getPhone()))
                        .child(information.getCard_id())
                        .setValue(new PhoneSearch(MD5.encoding(phone.getPhone()), information.getCard_id(), information.getUser_id()), new SyncReference.CompletionListener() {
                            @Override
                            public void onComplete(SyncError syncError, SyncReference syncReference) {
                                if (syncError != null)
                                    Log.e(SearchPhoneAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
                            }
                        });
            } else {
                rootRef.child(MD5.encoding(phone.getPhone())).child(information.getCard_id()).removeValue();
            }
        }
    }

    /**
     * 删除
     *
     * @param search
     */
    public void delete(PhoneSearch search) {
        rootRef.child(search.getPhone()).child(search.getCard_id()).removeValue();
    }

    /**
     * 搜索电话
     *
     * @param phone
     * @param subscriber
     */
    public void search(String phone, final OnESubscriber<List<Card>> subscriber) {
        rootRef.child(MD5.encoding(phone)).addListenerForSingleValueEvent(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<PhoneSearch> searches = ConvertHelper.convert(dataSnapshot, new TypeReference<List<PhoneSearch>>() {
                });
                Observable.create(new Observable.OnSubscribe<List<Card>>() {
                    @Override
                    public void call(final Subscriber<? super List<Card>> subscriber) {
                        if (searches == null) {
                            subscriber.onNext(null);
                            return;
                        }
                        final Iterator<PhoneSearch> iterator = searches.iterator();
                        final List<Card> cards = new ArrayList<>();
                        final int[] i = {0};
                        while (true) {
                            if (iterator.hasNext()) {
                                final String cardId = iterator.next().getCard_id();

                                APIProvider.get(CardAllAPI.class).singleGet(cardId, new OnESubscriber<Card>() {
                                    @Override
                                    protected void onComplete(boolean success, Card o, Throwable e) {
                                        i[0]++;
                                        if (App.getInstance().getMineCardIds().contains(cardId))
                                            return;
                                        if (success && o != null)
                                            cards.add(o);
                                        if (i[0] == searches.size())
                                            subscriber.onNext(cards);
                                    }
                                });
                            } else break;
                        }
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);

                if (searches == null) return;
                Observable.from(searches).forEach(new Action1<PhoneSearch>() {
                    @Override
                    public void call(final PhoneSearch phoneSearch) {
                        APIProvider.get(CardAllAPI.class).singleGet(phoneSearch.getCard_id(), new OnESubscriber<Card>() {
                            @Override
                            protected void onComplete(boolean success, Card o, Throwable e) {
                                if (!success || o == null) {
                                    delete(phoneSearch);
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
