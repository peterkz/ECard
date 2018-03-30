package com.wetoop.ecard.api;

import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.Account;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncReference;

/**
 * Created by User on 2017/12/15.
 */

public class SearchAccountAPI implements Constants {
    private final SyncReference rootRef;

    public SearchAccountAPI() {
        rootRef = App.getReference(URI_ACCOUNT);
    }

    public void create(Account account, SyncReference.CompletionListener listener){
        rootRef.child(account.getPhone()).setValue(account,listener);
    }

    public void searchContactGET(String phone, final OnESubscriber<Account> subscriber){
        rootRef.child(phone).addListenerForSingleValueEvent(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, Account.class).subscribe(subscriber);
            }
        });
    }
}
