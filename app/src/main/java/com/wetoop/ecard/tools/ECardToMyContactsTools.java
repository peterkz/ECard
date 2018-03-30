package com.wetoop.ecard.tools;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;

import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.ContactAPI;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.bean.CardBean;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.wilddogauth.model.WilddogUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 2017/10/19.
 */

public class ECardToMyContactsTools {

    //把e卡的联系人放入手机联系人里
    public void addECardToMyContacts(Context context, CardBean cardBean) {
        //插入raw_contacts表，并获取_id属性
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        long contact_id = ContentUris.parseId(resolver.insert(uri, values));
        //插入data表
        uri = Uri.parse("content://com.android.contacts/data");
        //add Name
        values.put("raw_contact_id", contact_id);
        values.put(ContactsContract.RawContacts.Data.MIMETYPE, "vnd.android.cursor.item/name");
        values.put("data2", cardBean.getInformation().getName());
        values.put("data1", cardBean.getInformation().getName());
        resolver.insert(uri, values);
        values.clear();
        //add Phonevnd.android.cursor.item
        for(Phone phone : cardBean.getPhones() ){
            if ("手机".equals(phone.getType())) {
                values.put("raw_contact_id", contact_id);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE, "vnd.android.cursor.item/phone_v2");
                values.put("data2", "2");   //手机
                values.put("data1", phone.getPhone());
                resolver.insert(uri, values);
                values.clear();
            } else if ("工作号码".equals(phone.getType())) {
                values.put("raw_contact_id", contact_id);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE, "vnd.android.cursor.item/phone_v2");
                values.put("data2", "3");   //单位
                values.put("data1", phone.getPhone());
                resolver.insert(uri, values);
                values.clear();
            }
        }
        //add email
        for (int i = 0; i < cardBean.getEmails().size(); i++) {
            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.RawContacts.Data.MIMETYPE, "vnd.android.cursor.item/email_v2");
            values.put("data2", "2");   //email
            values.put("data1", cardBean.getEmails().get(i).getEmail());
            resolver.insert(uri, values);
        }
        for (int i = 0; i < cardBean.getAddresses().size(); i++) {
            values.put("raw_contact_id", contact_id);
            values.put(ContactsContract.RawContacts.Data.MIMETYPE, "vnd.android.cursor.item/postal-address_v2");
            values.put("data2", "2");   //地址
            values.put("data1", cardBean.getAddresses().get(i).getAddress());
            resolver.insert(uri, values);
        }
        /*values.put("raw_contact_id", contact_id);
        values.put(ContactsContract.RawContacts.Data.MIMETYPE, "vnd.android.cursor.item/note");
        values.put("data2", "2");   //备注
        values.put("data1", cardBean);
        resolver.insert(uri, values);*/

    }

    //把手机联系人放入e卡的联系人里
    public void addContactsToECard(WilddogUser user, CardBean cardBean) {
        Card card = new Card();
        String uidStr = user.getUid();
        List<Information> informationList = new ArrayList<>();
        if (cardBean.getInformation() == null && cardBean.getPhones().size() > 0) {
            cardBean.getInformation().setName(cardBean.getPhones().get(0).getPhone());
        } else if (cardBean.getInformation() == null && cardBean.getPhones().size() <= 0) {
            cardBean.getInformation().setName(" ");
        }
        Information cardInformation = cardBean.getInformation();
        cardInformation.setPrivacy(true);//隐私设置
//        cardInformation.setUser_id(uidStr); // 创建联系人不需要user_id
        informationList.add(cardInformation);
        card.setInformation(informationList);

        if (cardBean.getPhones().size() > 0)
            card.setPhone(cardBean.getPhones());
        APIProvider.get(ContactAPI.class).create(card, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError syncError, SyncReference syncReference) {
                if (syncError != null) {
                    //Toast.makeText(context, "添加失败：" + syncError.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
