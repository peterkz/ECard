package com.wetoop.ecard.api;

import android.util.Log;

import com.wetoop.ecard.App;
import com.wetoop.ecard.Constants;
import com.wetoop.ecard.api.model.Address;
import com.wetoop.ecard.api.model.Avatar;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Company;
import com.wetoop.ecard.api.model.ContactUpdate;
import com.wetoop.ecard.api.model.ContactUpdateItem;
import com.wetoop.ecard.api.model.Custom;
import com.wetoop.ecard.api.model.Day;
import com.wetoop.ecard.api.model.Email;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.api.model.Name;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.api.model.Position;
import com.wetoop.ecard.api.model.Status;
import com.wetoop.ecard.api.model.Url;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Parck.
 * @date 2017/12/19.
 * @desc
 */

public class ContactUpdateAPI implements Constants {
    private final SyncReference rootRef;

    public ContactUpdateAPI() {
        rootRef = App.getReference(URI_CONTACT_UPDATE);
    }

    public void create(Card card) {
        final Information information = card.getInformation().get(0);
        ContactUpdate update = new ContactUpdate();
        update.setUuid(UUID.randomUUID().toString());
        update.setAvatar(new Avatar(information.getAvatar(), true));
        update.setName(new Name(information.getName(), true));
        update.setPosition(new Position(information.getPosition(), true));
        update.setCompany(new Company(information.getCompany(), true));
        List<Phone> phones = new ArrayList<>();
        for (Phone phone : card.getPhone()) {
            phone.setUpdate(true);
            phones.add(phone);
        }
        update.setPhones(phones);
        List<Address> addresses = new ArrayList<>();
        for (Address address : card.getAddress()) {
            address.setUpdate(true);
            addresses.add(address);
        }
        update.setAddresses(addresses);
        List<Email> emails = new ArrayList<>();
        for (Email email : card.getEmail()) {
            email.setUpdate(true);
            emails.add(email);
        }
        update.setEmails(emails);
        List<Url> urls = new ArrayList<>();
        for (Url url : card.getUrl()) {
            url.setUpdate(true);
            urls.add(url);
        }
        update.setUrls(urls);
        List<Day> days = new ArrayList<>();
        for (Day day : card.getDays()) {
            day.setUpdate(true);
            days.add(day);
        }
        update.setDays(days);
        List<Custom> customs = new ArrayList<>();
        for (Custom custom : card.getCustom()) {
            custom.setUpdate(true);
            customs.add(custom);
        }
        update.setCustoms(customs);
        rootRef.child(App.getCurrentUser().getUid()).child(information.getCard_id()).setValue(update, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError syncError, SyncReference syncReference) {
                if (syncError != null)
                    Log.e(ContactUpdateAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
                ContactUpdateItem item = new ContactUpdateItem();
                item.setCard_id(information.getCard_id());
                item.setAction("创建名片");
                item.setContent("时间：" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                APIProvider.get(ContactUpdateItemAPI.class).record(item);
            }
        });
    }

    public void update(Card oldCard, Card newCard) {
        Information newInformation = newCard.getInformation().get(0);
        Information oldInformation = oldCard.getInformation().get(0);
        ContactUpdate update = new ContactUpdate();
        update.setUser_id(oldInformation.getUser_id());
        update.setCard_id(oldInformation.getCard_id());
        update.setUuid(UUID.randomUUID().toString());
        update.setAvatar(new Avatar(newInformation.getAvatar(), false));

        Boolean nu = oldInformation.getName() == null ? null : !oldInformation.getName().equals(newInformation.getName());
        update.setName(new Name(newInformation.getName(), nu));
        if (nu != null && nu) {
            ContactUpdateItem ni = new ContactUpdateItem();
            ni.setCard_id(oldInformation.getCard_id());
            ni.setOld("姓名：" + oldInformation.getName());
            ni.setAction("更改为");
            ni.setContent("姓名：" + newInformation.getName());
            APIProvider.get(ContactUpdateItemAPI.class).record(ni);
        }

        Boolean pu = oldInformation.getPosition() == null ? null : !oldInformation.getPosition().equals(newInformation.getPosition());
        update.setPosition(new Position(newInformation.getPosition(), pu));
        if (pu != null && pu) {
            ContactUpdateItem pi = new ContactUpdateItem();
            pi.setCard_id(oldInformation.getCard_id());
            pi.setOld("职位：" + oldInformation.getPosition());
            pi.setAction("更改为");
            pi.setContent("职位：" + newInformation.getPosition());
            APIProvider.get(ContactUpdateItemAPI.class).record(pi);
        }

        Boolean cu = oldInformation.getCompany() == null ? null : !oldInformation.getCompany().equals(newInformation.getCompany());
        update.setCompany(new Company(newInformation.getCompany(), cu));
        if (cu != null && cu) {
            ContactUpdateItem ci = new ContactUpdateItem();
            ci.setCard_id(oldInformation.getCard_id());
            ci.setOld("公司：" + oldInformation.getCompany());
            ci.setAction("更改为");
            ci.setContent("公司：" + newInformation.getCompany());
            APIProvider.get(ContactUpdateItemAPI.class).record(ci);
        }

        List<Phone> newPhones = newCard.getPhone();
        List<Phone> oldPhones = oldCard.getPhone();
        for (Phone phone : newPhones) phone.setStatus(Status.CREATE);
        for (Phone phone : oldPhones) phone.setStatus(Status.REMOVE);
        for (int n = 0; n < newPhones.size(); n++) {
            for (int o = 0; o < oldPhones.size(); o++) {
                Phone newPhone = newPhones.get(n);
                Phone oldPhone = oldPhones.get(o);
                if (newPhone.getType().equals(oldPhone.getType()))
                    if (!newPhone.getPhone().equals(oldPhone.getPhone())) {
                        newPhone.setStatus(Status.UPDATE);
                        oldPhone.setStatus(Status.UPDATE);
                        newPhone.setOld(oldPhone.getPhone());
                    } else {
                        newPhone.setStatus(Status.DEFAULT);
                        oldPhone.setStatus(Status.DEFAULT);
                    }
            }
        }

        List<Phone> phones = new ArrayList<>();
        for (Phone phone : newPhones) {
            switch (phone.getStatus()) {
                case CREATE:
                    Phone c = new Phone();
                    c.setUpdate(true);
                    c.setPhone(phone.getPhone());
                    c.setType(phone.getType());
                    phones.add(c);

                    ContactUpdateItem ci = new ContactUpdateItem();
                    ci.setCard_id(oldInformation.getCard_id());
                    ci.setAction("添加");
                    ci.setContent(phone.getType() + "：" + phone.getPhone());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ci);
                    break;
                case UPDATE:
                    Phone u = new Phone();
                    u.setUpdate(true);
                    u.setPhone(phone.getPhone());
                    u.setType(phone.getType());
                    phones.add(u);

                    ContactUpdateItem ui = new ContactUpdateItem();
                    ui.setCard_id(oldInformation.getCard_id());
                    ui.setOld(phone.getType() + "：" + phone.getOld());
                    ui.setAction("更换为");
                    ui.setContent(phone.getType() + "：" + phone.getPhone());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ui);
                    break;
                case DEFAULT:
                    Phone d = new Phone();
                    d.setUpdate(false);
                    d.setPhone(phone.getPhone());
                    d.setType(phone.getType());
                    phones.add(d);
                    break;
            }
        }
        for (Phone phone : oldPhones) {
            if (phone.getStatus().equals(Status.REMOVE)) {
                ContactUpdateItem ri = new ContactUpdateItem();
                ri.setCard_id(oldInformation.getCard_id());
                ri.setAction("删除");
                ri.setContent(phone.getType() + "：" + phone.getPhone());
                APIProvider.get(ContactUpdateItemAPI.class).record(ri);
            }
        }
        update.setPhones(phones);

        List<Email> newEmails = newCard.getEmail();
        List<Email> oldEmails = oldCard.getEmail();
        for (Email email : newEmails) email.setStatus(Status.CREATE);
        for (Email email : oldEmails) email.setStatus(Status.REMOVE);
        for (int n = 0; n < newEmails.size(); n++) {
            for (int o = 0; o < oldEmails.size(); o++) {
                Email newEmail = newEmails.get(n);
                Email oldEmail = oldEmails.get(o);
                if (newEmail.getType().equals(oldEmail.getType()))
                    if (!newEmail.getEmail().equals(oldEmail.getEmail())) {
                        newEmail.setStatus(Status.UPDATE);
                        oldEmail.setStatus(Status.UPDATE);
                        newEmail.setOld(oldEmail.getEmail());
                    } else {
                        newEmail.setStatus(Status.DEFAULT);
                        oldEmail.setStatus(Status.DEFAULT);
                    }
            }
        }

        List<Email> emails = new ArrayList<>();
        for (Email email : newEmails) {
            switch (email.getStatus()) {
                case CREATE:
                    Email c = new Email();
                    c.setUpdate(true);
                    c.setEmail(email.getEmail());
                    c.setType(email.getType());
                    emails.add(c);

                    ContactUpdateItem ci = new ContactUpdateItem();
                    ci.setCard_id(oldInformation.getCard_id());
                    ci.setAction("添加");
                    ci.setContent(email.getType() + "：" + email.getEmail());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ci);
                    break;
                case UPDATE:
                    Email u = new Email();
                    u.setUpdate(true);
                    u.setEmail(email.getEmail());
                    u.setType(email.getType());
                    emails.add(u);

                    ContactUpdateItem ui = new ContactUpdateItem();
                    ui.setCard_id(oldInformation.getCard_id());
                    ui.setOld(email.getType() + "：" + email.getOld());
                    ui.setAction("更换为");
                    ui.setContent(email.getType() + "：" + email.getEmail());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ui);
                    break;
                case DEFAULT:
                    Email d = new Email();
                    d.setUpdate(false);
                    d.setEmail(email.getEmail());
                    d.setType(email.getType());
                    emails.add(d);
                    break;
            }
        }
        for (Email email : oldEmails) {
            if (email.getStatus().equals(Status.REMOVE)) {
                ContactUpdateItem ri = new ContactUpdateItem();
                ri.setCard_id(oldInformation.getCard_id());
                ri.setAction("删除");
                ri.setContent(email.getType() + "：" + email.getEmail());
                APIProvider.get(ContactUpdateItemAPI.class).record(ri);
            }
        }
        update.setEmails(emails);

        List<Address> newAddresses = newCard.getAddress();
        List<Address> oldAddresses = oldCard.getAddress();
        for (Address address : newAddresses) address.setStatus(Status.CREATE);
        for (Address address : oldAddresses) address.setStatus(Status.REMOVE);
        for (int n = 0; n < newAddresses.size(); n++) {
            for (int o = 0; o < oldAddresses.size(); o++) {
                Address newAddress = newAddresses.get(n);
                Address oldAddress = oldAddresses.get(o);
                if (newAddress.getType().equals(oldAddress.getType()))
                    if (!newAddress.toString().equals(oldAddress.toString())) {
                        newAddress.setStatus(Status.UPDATE);
                        oldAddress.setStatus(Status.UPDATE);
                        newAddress.setOld(oldAddress.toString());
                    } else {
                        newAddress.setStatus(Status.DEFAULT);
                        oldAddress.setStatus(Status.DEFAULT);
                    }
            }
        }

        List<Address> addresses = new ArrayList<>();
        for (Address address : newAddresses) {
            switch (address.getStatus()) {
                case CREATE:
                    Address c = new Address();
                    c.setUpdate(true);
                    c.setProvince(address.getProvince());
                    c.setCity(address.getCity());
                    c.setCounty(address.getCounty());
                    c.setAddress(address.getAddress());
                    c.setType(address.getType());
                    addresses.add(c);

                    ContactUpdateItem ci = new ContactUpdateItem();
                    ci.setCard_id(oldInformation.getCard_id());
                    ci.setAction("添加");
                    ci.setContent(address.getType() + "：" + address.toString());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ci);
                    break;
                case UPDATE:
                    Address u = new Address();
                    u.setUpdate(true);
                    u.setProvince(address.getProvince());
                    u.setCity(address.getCity());
                    u.setCounty(address.getCounty());
                    u.setAddress(address.getAddress());
                    u.setType(address.getType());
                    addresses.add(u);

                    ContactUpdateItem ui = new ContactUpdateItem();
                    ui.setCard_id(oldInformation.getCard_id());
                    ui.setOld(address.getType() + "：" + address.getOld());
                    ui.setAction("更换为");
                    ui.setContent(address.getType() + "：" + address.toString());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ui);
                    break;
                case DEFAULT:
                    Address d = new Address();
                    d.setUpdate(false);
                    d.setProvince(address.getProvince());
                    d.setCity(address.getCity());
                    d.setCounty(address.getCounty());
                    d.setAddress(address.getAddress());
                    d.setType(address.getType());
                    addresses.add(d);
                    break;
            }
        }
        for (Address address : oldAddresses) {
            if (address.getStatus().equals(Status.REMOVE)) {
                ContactUpdateItem ri = new ContactUpdateItem();
                ri.setCard_id(oldInformation.getCard_id());
                ri.setAction("删除");
                ri.setContent(address.getType() + "：" + address.toString());
                APIProvider.get(ContactUpdateItemAPI.class).record(ri);
            }
        }
        update.setAddresses(addresses);

        List<Url> newUrls = newCard.getUrl();
        List<Url> oldUrls = oldCard.getUrl();
        for (Url url : newUrls) url.setStatus(Status.CREATE);
        for (Url url : oldUrls) url.setStatus(Status.REMOVE);
        for (int n = 0; n < newUrls.size(); n++) {
            for (int o = 0; o < newUrls.size(); o++) {
                Url newUrl = newUrls.get(n);
                Url oldUrl = oldUrls.get(o);
                if (newUrl.getType().equals(oldUrl.getType()))
                    if (!newUrl.getUrl().equals(oldUrl.getUrl())) {
                        newUrl.setStatus(Status.UPDATE);
                        oldUrl.setStatus(Status.UPDATE);
                        newUrl.setOld(oldUrl.getUrl());
                    } else {
                        newUrl.setStatus(Status.DEFAULT);
                        oldUrl.setStatus(Status.DEFAULT);
                    }
            }
        }

        List<Url> urls = new ArrayList<>();
        for (Url url : newUrls) {
            switch (url.getStatus()) {
                case CREATE:
                    Url c = new Url();
                    c.setUpdate(true);
                    c.setUrl(url.getUrl());
                    c.setType(url.getType());
                    urls.add(c);

                    ContactUpdateItem ci = new ContactUpdateItem();
                    ci.setCard_id(oldInformation.getCard_id());
                    ci.setAction("添加");
                    ci.setContent(url.getType() + "：" + url.toString());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ci);
                    break;
                case UPDATE:
                    Url u = new Url();
                    u.setUpdate(true);
                    u.setUrl(url.getUrl());
                    u.setType(url.getType());
                    urls.add(u);

                    ContactUpdateItem ui = new ContactUpdateItem();
                    ui.setCard_id(oldInformation.getCard_id());
                    ui.setOld(url.getType() + "：" + url.getOld());
                    ui.setAction("更换为");
                    ui.setContent(url.getType() + "：" + url.toString());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ui);
                    break;
                case DEFAULT:
                    Url d = new Url();
                    d.setUpdate(false);
                    d.setUrl(url.getUrl());
                    d.setType(url.getType());
                    urls.add(d);
                    break;
            }
        }
        for (Url url : oldUrls) {
            if (url.getStatus().equals(Status.REMOVE)) {
                ContactUpdateItem ri = new ContactUpdateItem();
                ri.setCard_id(oldInformation.getCard_id());
                ri.setAction("删除");
                ri.setContent(url.getType() + "：" + url.getUrl());
                APIProvider.get(ContactUpdateItemAPI.class).record(ri);
            }
        }
        update.setUrls(urls);

        List<Day> newDays = newCard.getDays();
        List<Day> oldDays = oldCard.getDays();
        for (Day day : newDays) day.setStatus(Status.CREATE);
        for (Day day : oldDays) day.setStatus(Status.REMOVE);
        for (int n = 0; n < newDays.size(); n++) {
            for (int o = 0; o < oldDays.size(); o++) {
                Day newDay = newDays.get(n);
                Day oldDay = oldDays.get(o);
                if (newDay.getType().equals(oldDay.getType()))
                    if (!newDay.getDate().equals(oldDay.getDate())) {
                        newDay.setStatus(Status.UPDATE);
                        oldDay.setStatus(Status.UPDATE);
                        newDay.setOld(oldDay.getDate());
                    } else {
                        newDay.setStatus(Status.DEFAULT);
                        oldDay.setStatus(Status.DEFAULT);
                    }
            }
        }

        List<Day> days = new ArrayList<>();
        for (Day day : newDays) {
            switch (day.getStatus()) {
                case CREATE:
                    Day c = new Day();
                    c.setUpdate(true);
                    c.setDate(day.getDate());
                    c.setType(day.getType());
                    days.add(c);

                    ContactUpdateItem ci = new ContactUpdateItem();
                    ci.setCard_id(oldInformation.getCard_id());
                    ci.setAction("添加");
                    ci.setContent(day.getType() + "：" + new SimpleDateFormat("yyyy-MM-dd").format(day.getDate()));
                    APIProvider.get(ContactUpdateItemAPI.class).record(ci);
                    break;
                case UPDATE:
                    Day u = new Day();
                    u.setUpdate(true);
                    u.setDate(day.getDate());
                    u.setType(day.getType());
                    days.add(u);

                    ContactUpdateItem ui = new ContactUpdateItem();
                    ui.setCard_id(oldInformation.getCard_id());
                    ui.setOld(day.getType() + "：" + new SimpleDateFormat("yyyy-MM-dd").format(day.getOld()));
                    ui.setAction("更换为");
                    ui.setContent(day.getType() + "：" + new SimpleDateFormat("yyyy-MM-dd").format(day.getDate()));
                    APIProvider.get(ContactUpdateItemAPI.class).record(ui);
                    break;
                case DEFAULT:
                    Day d = new Day();
                    d.setUpdate(false);
                    d.setDate(day.getDate());
                    d.setType(day.getType());
                    days.add(d);
                    break;
            }
        }
        for (Day day : oldDays) {
            if (day.getStatus().equals(Status.REMOVE)) {
                ContactUpdateItem ri = new ContactUpdateItem();
                ri.setCard_id(oldInformation.getCard_id());
                ri.setAction("删除");
                ri.setContent(day.getType() + "：" + new SimpleDateFormat("yyyy-MM-dd").format(day.getDate()));
                APIProvider.get(ContactUpdateItemAPI.class).record(ri);
            }
        }
        update.setDays(days);

        List<Custom> newCustoms = newCard.getCustom();
        List<Custom> oldCustoms = oldCard.getCustom();
        for (Custom custom : newCustoms) custom.setStatus(Status.CREATE);
        for (Custom custom : oldCustoms) custom.setStatus(Status.REMOVE);
        for (int n = 0; n < newCustoms.size(); n++) {
            for (int o = 0; o < oldCustoms.size(); o++) {
                Custom newCustom = newCustoms.get(n);
                Custom oldCustom = oldCustoms.get(o);
                if (newCustom.getType().equals(oldCustom.getType()))
                    if (!newCustom.getCustom().equals(oldCustom.getCustom())) {
                        newCustom.setStatus(Status.UPDATE);
                        oldCustom.setStatus(Status.UPDATE);
                        newCustom.setOld(oldCustom.getCustom());
                    } else {
                        newCustom.setStatus(Status.DEFAULT);
                        oldCustom.setStatus(Status.DEFAULT);
                    }
            }
        }

        List<Custom> customs = new ArrayList<>();
        for (Custom custom : newCustoms) {
            switch (custom.getStatus()) {
                case CREATE:
                    Custom c = new Custom();
                    c.setUpdate(true);
                    c.setCustom(custom.getCustom());
                    c.setType(custom.getType());
                    customs.add(c);

                    ContactUpdateItem ci = new ContactUpdateItem();
                    ci.setCard_id(oldInformation.getCard_id());
                    ci.setAction("添加");
                    ci.setContent(custom.getType() + "：" + custom.toString());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ci);
                    break;
                case UPDATE:
                    Custom u = new Custom();
                    u.setUpdate(true);
                    u.setCustom(custom.getCustom());
                    u.setType(custom.getType());
                    customs.add(u);

                    ContactUpdateItem ui = new ContactUpdateItem();
                    ui.setCard_id(oldInformation.getCard_id());
                    ui.setOld(custom.getType() + "：" + custom.getOld());
                    ui.setAction("更换为");
                    ui.setContent(custom.getType() + "：" + custom.toString());
                    APIProvider.get(ContactUpdateItemAPI.class).record(ui);
                    break;
                case DEFAULT:
                    Custom d = new Custom();
                    d.setUpdate(false);
                    d.setCustom(custom.getCustom());
                    d.setType(custom.getType());
                    customs.add(d);
                    break;
            }
        }
        for (Custom custom : oldCustoms) {
            if (custom.getStatus().equals(Status.REMOVE)) {
                ContactUpdateItem ri = new ContactUpdateItem();
                ri.setCard_id(oldInformation.getCard_id());
                ri.setAction("删除");
                ri.setContent(custom.getType() + "：" + custom.getCustom());
                APIProvider.get(ContactUpdateItemAPI.class).record(ri);
            }
        }
        update.setCustoms(customs);

        rootRef.child(App.getCurrentUser().getUid()).child(newInformation.getCard_id()).setValue(update, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError syncError, SyncReference syncReference) {
                if (syncError != null)
                    Log.e(ContactUpdateAPI.class.getSimpleName(), "syncError : " + syncError.getMessage());
            }
        });
    }

    public void get(String userId, String cardId, final OnESubscriber<ContactUpdate> subscriber) {
        rootRef.child(userId).child(cardId).addListenerForSingleValueEvent(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConvertHelper.rxConvert(dataSnapshot, ContactUpdate.class).subscribe(subscriber);
            }
        });
    }
}
