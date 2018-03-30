package com.wetoop.ecard.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.Income;
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

public class InComingAPI implements Constants {

    private final SyncReference rootRef;
    private OnEValueEventListener listValueListener;

    public InComingAPI() {
        rootRef = App.getReference(URI_INCOMING_LOGS + App.getWilddogAuth().getCurrentUser().getUid());
    }


    /**
     * 创建记录
     *
     * @param income
     * @param listener
     */
    public void create(final Income income, final SyncReference.CompletionListener listener) {
        SyncReference reference = rootRef.child(income.getCard_id());
        income.setId(income.getCard_id());
        if (listener != null) reference.setValue(income, listener);
        else reference.setValue(income);
    }

    /**
     * 更新记录
     *
     * @param income
     * @param listener
     */
    public void update(final Income income, final SyncReference.CompletionListener listener) {
        if (listener != null)
            rootRef.child(income.getId()).setValue(income, listener);
        else rootRef.child(income.getId()).setValue(income);
    }

    /**
     * 记录列表
     *
     * @param subscriber
     */
    public void list(final OnESubscriber<List<Income>> subscriber) {
        listValueListener = new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, new TypeReference<List<Income>>() {
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
