package com.wetoop.ecard.api;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.Address;
import com.wetoop.ecard.api.model.AddressSearch;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Information;
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
import rx.schedulers.Schedulers;


/**
 * @author Parck.
 * @date 2017/12/14.
 * @desc
 */

public class SearchAddressAPI implements Constants {
    private final SyncReference rootRef;

    public SearchAddressAPI() {
        rootRef = App.getReference(URI_SEARCH_ADDRESS);
    }

    public void sync(Card card) {
        if (card.getAddress() == null || card.getAddress().size() == 0) return;
        Information information = card.getInformation().get(0);
        Address address = card.getAddress().get(0).searchAdr();
        if (TextUtils.isEmpty(address.getProvince())) return;
        rootRef.child(MD5.encoding(address.getProvince()))
                .child(MD5.encoding(address.getCity()))
                .child(MD5.encoding(address.getCounty()))
                .child(information.getCard_id())
                .setValue(new AddressSearch(information.getCard_id(), information.getUser_id()), new SyncReference.CompletionListener() {
                    @Override
                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                        if (syncError != null)
                            Log.e(SearchAddressAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
                    }
                });
    }

    public void search(Address address, final OnESubscriber<List<Card>> subscriber) {
        if (TextUtils.isEmpty(address.getProvince())) return;
        SyncReference childRef = rootRef.child(MD5.encoding(address.getProvince()));
        if (!TextUtils.isEmpty(address.getCity()))
            childRef = childRef.child(MD5.encoding(address.getCity()));
        if (!TextUtils.isEmpty(address.getCounty()))
            childRef = childRef.child(MD5.encoding(address.getCounty()));
        childRef.addListenerForSingleValueEvent(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<AddressSearch> searches = ConvertHelper.convert(dataSnapshot, new TypeReference<List<AddressSearch>>() {
                });
                Observable.create(new Observable.OnSubscribe<List<Card>>() {
                    @Override
                    public void call(final Subscriber<? super List<Card>> subscriber) {
                        if (searches == null) {
                            subscriber.onNext(null);
                            return;
                        }
                        final Iterator<AddressSearch> iterator = searches.iterator();
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
                                        if (success && o != null) cards.add(o);
                                        if (i[0] == searches.size()) subscriber.onNext(cards);
                                    }
                                });
                            } else return;
                        }
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
            }
        });
    }
}
