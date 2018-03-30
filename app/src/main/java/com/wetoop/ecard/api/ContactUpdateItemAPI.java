package com.wetoop.ecard.api;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.ContactUpdateItem;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Parck.
 * @date 2017/12/19.
 * @desc
 */

public class ContactUpdateItemAPI implements Constants {

    private final SyncReference rootRef;

    public ContactUpdateItemAPI() {
        rootRef = App.getReference(URI_CONTACT_UPDATE_ITEM);
    }

    public void record(ContactUpdateItem item) {
        String format = new SimpleDateFormat("yyyy-MM").format(item.getDate_created());
        String childKey = URLEncoder.encode(format);
        SyncReference reference = rootRef.child(App.getCurrentUser().getUid()).child(item.getCard_id())
                .child(childKey)
                .push();
        item.setId(reference.getKey());
        reference.setValue(item, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError syncError, SyncReference syncReference) {
                if (syncError != null)
                    Log.e(ContactUpdateItemAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
            }
        });
    }

    public void list(String userId, String cardId, final OnESubscriber<Map<String, List<ContactUpdateItem>>> subscriber) {
        rootRef.child(userId).child(cardId).addListenerForSingleValueEvent(new OnEValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Observable.create(new Observable.OnSubscribe<Map<String, List<ContactUpdateItem>>>() {
                    @Override
                    public void call(Subscriber<? super Map<String, List<ContactUpdateItem>>> subscriber) {
                        Map<String, Map<String, Object>> dataSnapshots = (Map<String, Map<String, Object>>) dataSnapshot.getValue();
                        Map<String, List<ContactUpdateItem>> items = new LinkedHashMap<>();
                        if (dataSnapshots == null) return;
                        Set<String> keys = dataSnapshots.keySet();
                        try {
                            for (String key : keys) {
                                String json = App.getMapper().writeValueAsString(dataSnapshots.get(key).values());
                                List<ContactUpdateItem> itemList = App.getMapper().readValue(json, new TypeReference<List<ContactUpdateItem>>() {
                                });
                                items.put(URLDecoder.decode(key), itemList);
                            }
                            subscriber.onNext(items);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(subscriber);
            }
        });
    }

}
