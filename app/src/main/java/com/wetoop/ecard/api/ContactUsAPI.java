package com.wetoop.ecard.api;

import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.ContactUs;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncReference;

/**
 * @author Parck.
 * @date 2018/1/11.
 * @desc
 */

public class ContactUsAPI implements Constants {
    private final SyncReference rootRef;
    private OnEValueEventListener listValueListener;

    public ContactUsAPI() {
        rootRef = App.getReference(URI_CONTACT_US);
    }

    public void get(final OnESubscriber<ContactUs> subscriber) {
        rootRef.addListenerForSingleValueEvent(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, ContactUs.class).subscribe(subscriber);
            }
        });
    }
}
