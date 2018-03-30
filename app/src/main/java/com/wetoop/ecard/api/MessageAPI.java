package com.wetoop.ecard.api;

import android.util.Log;

import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Income;
import com.wetoop.ecard.api.model.Notification;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.util.Set;

import cn.edots.nest.core.cache.Session;

/**
 * @author Parck.
 * @date 2017/11/22.
 * @desc
 */

public class MessageAPI implements Constants {

    private final SyncReference rootRef;

    public MessageAPI() {
        rootRef = App.getReference(URI_MESSAGE + App.getWilddogAuth().getCurrentUser().getUid());
    }

    /**
     * 批量发送消息
     *
     * @param notification
     * @param listener
     */
    public void send(Notification notification, SyncReference.CompletionListener listener) {
        if (notification.getReceiverUIDs() != null)
            for (String uid : notification.getReceiverUIDs()) {
                SyncReference reference = App.getReference("/message/" + uid).push();
                Notification notif = new Notification();
                notif.setReceiver_id(uid);
                notif.setMessage_id(reference.getKey());
                notif.setSender_id(notification.getSender_id());
                notif.setDate_created(notification.getDate_created());
                notif.setMessage(notification.getMessage());
                if (listener != null) reference.setValue(notif, listener);
                else reference.setValue(notif);
            }
        else {
            SyncReference reference = App.getReference("/message/" + notification.getReceiver_id()).push();
            Notification notif = new Notification();
            notif.setReceiver_id(notification.getReceiver_id());
            notif.setMessage_id(reference.getKey());
            notif.setSender_id(notification.getSender_id());
            notif.setDate_created(notification.getDate_created());
            notif.setMessage(notification.getMessage());
            if (listener != null) reference.setValue(notif, listener);
            else reference.setValue(notif);
        }
    }

    /**
     * 订阅消息
     *
     * @param subscriber
     */
    public void subscribe(final OnESubscriber<Notification> subscriber) {
        rootRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ConvertHelper.rxConvert(dataSnapshot, Notification.class).subscribe(subscriber);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                ConvertHelper.rxConvert(dataSnapshot, Notification.class).subscribe(subscriber);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, Notification.class).subscribe(subscriber);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                ConvertHelper.rxConvert(dataSnapshot, Notification.class).subscribe(subscriber);
            }

            @Override
            public void onCancelled(SyncError syncError) {

            }
        });
    }

    /**
     * 删除消息
     *
     * @param key
     * @param listener
     * @！！！ 我们规定，消息属于一个通知，所以可以把已经处理过的消息删除掉
     */
    public void delete(String key, SyncReference.CompletionListener listener) {
        if (listener != null) rootRef.child(key).removeValue(listener);
        else rootRef.child(key).removeValue();
    }

    /**
     * 更新消息
     *
     * @param notification
     * @param listener
     */
    public void update(Notification notification, SyncReference.CompletionListener listener) {
        if (listener != null)
            rootRef.child(notification.getMessage_id()).setValue(notification, listener);
        else rootRef.child(notification.getMessage_id()).setValue(notification);
    }

    /**
     * 发送名片交换消息
     *
     * @param bean
     * @param receiverUid
     * @param listener
     */
    public void sendExchangeMessage(CardBean bean, String mineId, String receiverUid, SyncReference.CompletionListener listener) {
        Notification notification = new Notification();
        notification.setSender_id(App.getCurrentUser().getUid());
        notification.setReceiver_id(receiverUid);
        Notification.Message message = new Notification.Message();
        message.setType(2);//发送名片交换请求
        message.setState(0);
        message.setCard_id(bean.getInformation().getCard_id());
        message.setContent(bean.getInformation().getName() + "向你发送名片");
        message.setName(bean.getInformation().getName());
        message.setMine_id(mineId);
        notification.setMessage(message);
        send(notification, listener);

        Income income = new Income();
        income.setName(notification.getMessage().getName());
        income.setCard_id(notification.getMessage().getCard_id());
        income.setContent("已向对方发送名片");
        income.setUser_id(notification.getSender_id());
        income.setState(0);
        APIProvider.get(OutGoingAPI.class).create(income, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError syncError, SyncReference syncReference) {
                if (syncError != null)
                    Log.e(MessageAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
            }
        });
    }

    /**
     * 发送名片交换消息
     *
     * @param bean
     * @param receiverUIDs
     * @param listener
     */
    public void sendUpdateMessage(CardBean bean, Set<String> receiverUIDs, SyncReference.CompletionListener listener) {
        Notification notification = new Notification();
        notification.setSender_id(App.getCurrentUser().getUid());
        notification.setReceiverUIDs(receiverUIDs);
        Notification.Message message = new Notification.Message();
        message.setType(1);//e卡更新通知
        message.setState(0);
        message.setCard_id(bean.getInformation().getCard_id());
        message.setContent(bean.getInformation().getName() + "更新了名片");
        message.setName(bean.getInformation().getName());
        notification.setMessage(message);
        send(notification, listener);
    }

    public void receiveUpdateMessage(final OnReceiveListener listener) {
        receiveMessage(new OnReceiveListener() {
            @Override
            public boolean onReceive(final Notification notification) {
                String cardId = notification.getMessage().getCard_id();
                if (App.getInstance().getContactsCardIdList().contains(cardId)) {
                    APIProvider.get(CardAllAPI.class).singleGet(cardId, new OnESubscriber<Card>() {
                        @Override
                        protected void onComplete(boolean success, Card o, Throwable e) {
                            if (success && o != null) {
                                APIProvider.get(ContactAPI.class).save(o, new SyncReference.CompletionListener() {
                                    @Override
                                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                                        if (syncError != null)
                                            Log.e(MessageAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
                                        else listener.onReceive(notification);
                                    }
                                });
                            }
                        }
                    });
                }
                return true;
            }
        }, 1);
    }

    public void receiveExchangeMessage(final OnReceiveListener listener) {
        receiveMessage(new OnReceiveListener() {
            @Override
            public boolean onReceive(final Notification notification) {
                Income income = new Income();
                income.setId(notification.getMessage_id());
                income.setCard_id(notification.getMessage().getCard_id());
                income.setContent(notification.getMessage().getContent());
                income.setName(notification.getMessage().getName());
                income.setUser_id(notification.getSender_id());
                income.setMine_id(notification.getMessage().getMine_id());
                income.setState(0);
                APIProvider.get(InComingAPI.class).create(income, new SyncReference.CompletionListener() {
                    @Override
                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                        if (syncError != null)
                            Log.e(MessageAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
                        else if (listener != null) listener.onReceive(notification);
                    }
                });
                return true;
            }
        }, 2);
    }

    private void receiveMessage(final OnReceiveListener listener, final int type) {
        subscribe(new OnESubscriber<Notification>() {
            @Override
            protected void onComplete(boolean success, final Notification notification, Throwable e) {
                if (success && notification != null) {
                    if (notification.getMessage().getType() == type) {
                        if (notification.getMessage().getState() == 0) {
                            notification.getMessage().setState(1);
                            update(notification, new SyncReference.CompletionListener() {
                                @Override
                                public void onComplete(SyncError syncError, SyncReference syncReference) {
                                    if (syncError != null)
                                        Log.e(MessageAPI.class.getSimpleName(), "syncError:" + syncError.getMessage());
                                    else if (listener != null && Session.getAttribute(notification.getMessage_id()) == null) {
                                        Session.setAttribute(notification.getMessage_id(), 1);
                                        if (listener.onReceive(notification)) {
                                            delete(notification.getMessage_id(), new SyncReference.CompletionListener() {
                                                @Override
                                                public void onComplete(SyncError syncError, SyncReference syncReference) {
                                                    if (syncError != null)
                                                        Log.e(MessageAPI.class.getSimpleName(), "syncError:" + syncError.getMessage());
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public interface OnReceiveListener {
        boolean onReceive(Notification notification);
    }
}
