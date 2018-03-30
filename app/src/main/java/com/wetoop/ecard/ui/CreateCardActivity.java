package com.wetoop.ecard.ui;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.model.CosXmlResult;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.ContactAPI;
import com.wetoop.ecard.api.ContactUpdateAPI;
import com.wetoop.ecard.api.MineCardAPI;
import com.wetoop.ecard.api.model.Address;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Custom;
import com.wetoop.ecard.api.model.Day;
import com.wetoop.ecard.api.model.Email;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.api.model.Url;
import com.wetoop.ecard.bean.Holdable;
import com.wetoop.ecard.listener.OSSResultListener;
import com.wetoop.ecard.tools.BitmapTool;
import com.wetoop.ecard.tools.CircleTransform;
import com.wetoop.ecard.tools.GetImagePath;
import com.wetoop.ecard.tools.PermissionUtil;
import com.wetoop.ecard.tools.RoundTransformation;
import com.wetoop.ecard.ui.dialog.AreaSelectorDialog;
import com.wetoop.ecard.ui.dialog.ConfirmDialog;
import com.wetoop.ecard.ui.dialog.CustomEditDialog;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wetoop.ecard.ui.dialog.OnClickListener;
import com.wetoop.ecard.ui.dialog.PopupSelectorDialog;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Gradable;
import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static android.support.v4.content.FileProvider.getUriForFile;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.ADDRESS;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.COMPANY;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.CUSTOM;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.DAILY;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.DEFAULT;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.EMAIL;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.INDEX;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.LABEL;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.NAME;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.NOTE;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.PHONE;
import static com.wetoop.ecard.ui.CreateCardActivity.ItemType.POSITION;

/**
 * @author Parck.
 * @date 2017/12/11.
 * @desc
 */

@Slug(layout = R.layout.activity_careate_card)
public class CreateCardActivity extends TitleBarActivity implements Standardize {

    public static final String OPEN_PARAMETER_KEY = "open_parameter_key";
    private File imageFile;

    public static class OpenParameter implements Serializable {
        private static final long serialVersionUID = 8373148559453223819L;

        private OpenType openType;

        public OpenType getOpenType() {
            return openType;
        }

        public void setOpenType(OpenType openType) {
            this.openType = openType;
        }
    }

    public enum OpenType {
        新建我的e卡, 编辑我的e卡, 新建联系人, 编辑联系人
    }

    public static void startActivity(Context context, OpenParameter parameter) {
        Intent intent = new Intent(context, CreateCardActivity.class);
        HashMap<String, Object> data = new HashMap<>();
        data.put(OPEN_PARAMETER_KEY, parameter);
        intent.putExtra("INTENT_DATA", data);
        context.startActivity(intent);
    }

    private final int TAKE_PHOTO = 1000;
    private final int CHOOSE_PHOTO = 1001;
    private final int CROP_PHOTO = 1002;
    private Uri imageUri;

    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.focus_view)
    private EditText focusView;

    private OpenParameter parameter;

    private List<Item> items = new ArrayList<>();
    private RecyclerViewAdapter<Item> adapter;
    private int[] itemLayouts = new int[]{
            R.layout.item_create_header,
            R.layout.item_create_input,
            R.layout.item_create_remove,
            R.layout.item_create_add,
            R.layout.item_grade,
            R.layout.item_delete_button,
            R.layout.item_create_address
    };
    private PopupSelectorDialog[] selectors = new PopupSelectorDialog[5];
    private List<String[]> selectItems = new ArrayList<>();
    private PopupSelectorDialog.OnClickListener[] listeners = new PopupSelectorDialog.OnClickListener[5];

    private Card card;
    private Information information;
    private List<Information> is;
    private List<Day> ds;
    private List<Phone> ps;
    private List<Custom> cs;
    private List<Email> es;
    private List<Url> us;
    private List<Address> as;
    private CustomEditDialog customDialog = new CustomEditDialog();
    private AreaSelectorDialog areaSelectorDialog;
    private PopupSelectorDialog selectorDialog;

    private Card copyCard = new Card();
    private boolean restart = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        initData(map);
        initSelectItemDate();
        initSelectListener();
        initSelectorDialog();
        adapter = new RecyclerViewAdapter<Item>(THIS, itemLayouts, items) {
            @Override
            protected void binding(ViewHolder holder, Item o, int i) {
                holder.setIsRecyclable(false);
                o.holding(holder);
            }

            @Override
            public int getItemViewType(int position) {
                return items.get(position).getType();
            }
        };
        convertData();
    }

    private void initData(@Nullable Map<String, Object> map) {
        if (map != null) {
            ECardDetailActivity activity = (ECardDetailActivity) App.getInstance().getActivity();
            if (activity != null && activity.bean != null) {
                card = activity.bean.toModel();
                List<Information> informations = new ArrayList<>();
                List<Phone> phones = new ArrayList<>();
                List<Email> emails = new ArrayList<>();
                List<Url> urls = new ArrayList<>();
                List<Address> addresses = new ArrayList<>();
                List<Day> days = new ArrayList<>();
                List<Custom> customs = new ArrayList<>();

                Information e = new Information();
                Information information = card.getInformation().get(0);
                e.setCard_id(information.getCard_id());
                e.setMine_id(information.getMine_id());
                e.setCard_label(information.getCard_label());
                e.setUser_id(information.getUser_id());
                e.setAvatar(information.getAvatar());
                e.setPrivacy(information.isPrivacy());
                e.setCompany(information.getCompany());
                e.setDepartment(information.getDepartment());
                e.setName(information.getName());
                e.setNote(information.getNote());
                e.setPosition(information.getPosition());
                e.setType(information.getType());
                informations.add(e);
                copyCard.setInformation(informations);

                for (Phone phone : card.getPhone()) {
                    Phone p = new Phone();
                    p.setOld(phone.getOld());
                    p.setType(phone.getType());
                    p.setUpdate(phone.isUpdate());
                    p.setStatus(phone.getStatus());
                    p.setPhone(phone.getPhone());
                    phones.add(p);
                }
                copyCard.setPhone(phones);

                for (Email email : card.getEmail()) {
                    Email em = new Email();
                    em.setType(email.getType());
                    em.setStatus(email.getStatus());
                    em.setUpdate(email.isUpdate());
                    em.setEmail(email.getEmail());
                    em.setOld(email.getOld());
                    emails.add(em);
                }
                copyCard.setEmail(emails);

                for (Url url : card.getUrl()) {
                    Url u = new Url();
                    u.setOld(url.getOld());
                    u.setType(url.getType());
                    u.setStatus(url.getStatus());
                    u.setUpdate(url.isUpdate());
                    u.setUrl(url.getUrl());
                    urls.add(u);
                }
                copyCard.setUrl(urls);

                for (Address address : card.getAddress()) {
                    Address a = new Address();
                    a.setCity(address.getCity());
                    a.setCounty(address.getCounty());
                    a.setType(address.getType());
                    a.setStatus(address.getStatus());
                    a.setUpdate(address.isUpdate());
                    a.setProvince(address.getProvince());
                    a.setOld(address.getOld());
                    a.setAddress(address.getAddress());
                    addresses.add(a);
                }
                copyCard.setAddress(addresses);

                for (Custom custom : card.getCustom()) {
                    Custom c = new Custom();
                    c.setCustom(custom.getCustom());
                    c.setType(custom.getType());
                    c.setStatus(custom.getStatus());
                    c.setUpdate(custom.isUpdate());
                    c.setOld(custom.getOld());
                    customs.add(c);
                }
                copyCard.setCustom(customs);

                for (Day day : card.getDays()) {
                    Day d = new Day();
                    d.setDate(day.getDate());
                    d.setType(day.getType());
                    d.setStatus(day.getStatus());
                    d.setOld(day.getOld());
                    d.setUpdate(day.isUpdate());
                    days.add(d);
                }
                copyCard.setDays(days);

            }
            parameter = (OpenParameter) map.get(OPEN_PARAMETER_KEY);
            if (parameter == null) finish();
        }
        if (card == null) {
            card = new Card();
            is = new ArrayList<>();
            information = new Information();
            ds = new ArrayList<>();
            ps = new ArrayList<>();
            cs = new ArrayList<>();
            es = new ArrayList<>();
            us = new ArrayList<>();
            as = new ArrayList<>();
            Phone phone = new Phone();
            phone.setType("手机");
            ps.add(phone);
            is.add(information);
            card.setInformation(is);
            card.setDays(ds);
            card.setAddress(as);
            card.setCustom(cs);
            card.setEmail(es);
            card.setPhone(ps);
            card.setUrl(us);
        } else {
            is = card.getInformation();
            information = is.get(0);
            ds = card.getDays();
            ps = card.getPhone();
            cs = card.getCustom();
            es = card.getEmail();
            us = card.getUrl();
            as = card.getAddress();
        }
    }

    @Override
    public void initView() {
        switch (parameter.getOpenType()) {
            case 新建我的e卡:
                setCenterTitleContent("创建e卡");
                break;
            case 新建联系人:
                setCenterTitleContent("新建联系人");
                break;
            case 编辑我的e卡:
            case 编辑联系人:
                setCenterTitleContent("编辑");
                break;
        }
        setRightTextContent("保存", R.color.blueColor, _16SP);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void setListeners() {
        setOnRightTextListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                focusView.requestFocus();
                if (validateForm(card)) {
                    rightText.setFocusable(false);
                    rightText.setEnabled(false);
                    rightText.setClickable(false);
                    rightText.setTextColor(THIS.getResources().getColor(R.color.light_grey));
                    LoadingDialog.show(THIS);
                    submit();
                }
            }
        });
    }

    private void submit() {
        switch (parameter.getOpenType()) {
            case 新建联系人:
                APIProvider.get(ContactAPI.class).create(card, new SyncReference.CompletionListener() {
                    @Override
                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                        if (syncError != null) {
                            logger.e("syncError : " + syncError.getMessage());
                            TOAST("操作失败");
                            rightText.setFocusable(true);
                            rightText.setEnabled(true);
                            rightText.setClickable(true);
                        } else {
                            uploadAvatar();
                            TOAST("添加成功");
                            finish();
                        }
                        LoadingDialog.hide();
                    }
                });
                break;
            case 新建我的e卡:
                APIProvider.get(MineCardAPI.class).create(card, new SyncReference.CompletionListener() {
                    @Override
                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                        if (syncError != null) {
                            logger.e("syncError : " + syncError.getMessage());
                            TOAST("操作失败");
                            rightText.setFocusable(true);
                            rightText.setEnabled(true);
                            rightText.setClickable(true);
                        } else {
                            uploadAvatar();
                            TOAST("添加成功");
                            finish();
                        }
                        LoadingDialog.hide();
                    }
                });
                break;
            case 编辑联系人:
                APIProvider.get(ContactAPI.class).update(card, new SyncReference.CompletionListener() {
                    @Override
                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                        if (syncError != null) {
                            logger.e("syncError : " + syncError.getMessage());
                            TOAST("操作失败");
                            rightText.setFocusable(true);
                            rightText.setEnabled(true);
                            rightText.setClickable(true);
                        } else {
                            uploadAvatar();
                            TOAST("修改成功");
                            finish();
                        }
                        LoadingDialog.hide();
                    }
                });
                break;
            case 编辑我的e卡:
                APIProvider.get(MineCardAPI.class).update(card, new SyncReference.CompletionListener() {
                    @Override
                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                        if (syncError != null) {
                            logger.e("syncError : " + syncError.getMessage());
                            TOAST("操作失败");
                            rightText.setFocusable(true);
                            rightText.setEnabled(true);
                            rightText.setClickable(true);
                        } else {
                            uploadAvatar();
                            TOAST("修改成功");
                            finish();
                        }
                        Observable.create(new Observable.OnSubscribe<String>() {
                            @Override
                            public void call(Subscriber<? super String> subscriber) {
                                APIProvider.get(ContactUpdateAPI.class).update(copyCard, card);
                            }
                        }).subscribeOn(Schedulers.io()).subscribe();
                        LoadingDialog.hide();
                    }
                });
                break;
        }
    }

    private void uploadAvatar() {
        Bitmap image = BitmapTool.adjustImage(THIS, card.getInformation().get(0).getAvatar());
        if (image == null) return;
        App.oss().upload(card.getInformation().get(0).getCard_id(), image, new OSSResultListener() {
            @Override
            public void complete(boolean success, @Nullable CosXmlResult result, @Nullable CosXmlClientException e) {
                if (!success) {
                    TOAST("图片上传失败");
                    logger.e(e);
                    card.getInformation().get(0).setAvatar(null);
                } else card.getInformation().get(0).setAvatar(result.accessUrl);
            }
        });
    }


    private Boolean validateForm(Card cardFrom) {
        Information information = cardFrom.getInformation().get(0);
        switch (parameter.getOpenType()) {
            case 新建我的e卡:
            case 编辑我的e卡:
                if ("".equals(information.getCard_label()) || information.getCard_label() == null) {
                    TOAST("请填写卡片名称");
                    return false;
                }
                break;
        }
        if ("".equals(information.getName()) || information.getName() == null) {
            TOAST("请填写姓名");
            return false;
        }
        if (("".equals(information.getPosition()) || information.getPosition() == null) && !OpenType.编辑联系人.equals(parameter.getOpenType())) {
            TOAST("请填写职位");
            return false;
        }
        if (("".equals(information.getCompany()) || information.getCompany() == null) && !OpenType.编辑联系人.equals(parameter.getOpenType())) {
            TOAST("请填写公司");
            return false;
        }

        for (Phone phone : cardFrom.getPhone()) {
            if (cardFrom.getPhone().size() == 1 && TextUtils.isEmpty(phone.getPhone())) {
                TOAST("请至少填写一个号码");
                return false;
            } else if (TextUtils.isEmpty(phone.getPhone())) cardFrom.getPhone().remove(phone);
        }

        for (Address address : cardFrom.getAddress()) {
            if (TextUtils.isEmpty(address.getProvince()) || TextUtils.isEmpty(address.getAddress()))
                cardFrom.getAddress().remove(address);
        }

        for (Email email : cardFrom.getEmail()) {
            if (TextUtils.isEmpty(email.getEmail())) cardFrom.getEmail().remove(email);
        }

        for (Url url : cardFrom.getUrl()) {
            if (TextUtils.isEmpty(url.getUrl())) cardFrom.getUrl().remove(url);
        }

        for (Day day : cardFrom.getDays()) {
            if (day.getDate() == null) cardFrom.getDays().remove(day);
        }

        for (Custom custom : cardFrom.getCustom()) {
            if (TextUtils.isEmpty(custom.getCustom())) cardFrom.getCustom().remove(custom);
        }
        return true;
    }

    @Override
    public void onCreateLast() {

    }

    private void initSelectItemDate() {
        selectItems.add(new String[]{"手机", "工作号码", "住宅号码", "自定义"});
        selectItems.add(new String[]{"个人邮箱", "工作邮箱", "自定义"});
        selectItems.add(new String[]{"个人网址", "工作网址", "自定义"});
        selectItems.add(new String[]{"个人地址", "工作地址", "自定义"});
        selectItems.add(new String[]{"生日", "纪念日", "自定义"});
    }

    private void initSelectListener() {
        listeners[0] = new PopupSelectorDialog.OnClickListener() {
            @Override
            public void onSure(int position, CharSequence text) {
                if ("自定义".equals(text)) {
                    showCustomDialog(new CustomEditDialog.OnClickListener() {
                        @Override
                        public void onSure(CustomEditDialog dialog, String editText) {
                            Phone phone = new Phone();
                            phone.setType(editText);
                            ps.add(phone);
                            convertData();
                        }
                    });
                    return;
                }
                Phone phone = new Phone();
                phone.setType(text.toString());
                ps.add(phone);
                convertData();
            }
        };
        listeners[1] = new PopupSelectorDialog.OnClickListener() {
            @Override
            public void onSure(int position, CharSequence text) {
                if ("自定义".equals(text)) {
                    showCustomDialog(new CustomEditDialog.OnClickListener() {
                        @Override
                        public void onSure(CustomEditDialog dialog, String editText) {
                            Email email = new Email();
                            email.setType(editText);
                            es.add(email);
                            convertData();
                        }
                    });
                    return;
                }
                Email email = new Email();
                email.setType(text.toString());
                es.add(email);
                convertData();
            }
        };
        listeners[2] = new PopupSelectorDialog.OnClickListener() {
            @Override
            public void onSure(int position, CharSequence text) {
                if ("自定义".equals(text)) {
                    showCustomDialog(new CustomEditDialog.OnClickListener() {
                        @Override
                        public void onSure(CustomEditDialog dialog, String editText) {
                            Url url = new Url();
                            url.setType(editText);
                            us.add(url);
                            convertData();
                        }
                    });
                    return;
                }
                Url url = new Url();
                url.setType(text.toString());
                us.add(url);
                convertData();
            }
        };
        listeners[3] = new PopupSelectorDialog.OnClickListener() {
            @Override
            public void onSure(int position, CharSequence text) {
                if ("自定义".equals(text)) {
                    showCustomDialog(new CustomEditDialog.OnClickListener() {
                        @Override
                        public void onSure(CustomEditDialog dialog, String editText) {
                            Address address = new Address();
                            address.setType(editText);
                            as.add(address);
                            convertData();
                        }
                    });
                    return;
                }
                Address address = new Address();
                address.setType(text.toString());
                as.add(address);
                convertData();
            }
        };
        listeners[4] = new PopupSelectorDialog.OnClickListener() {
            @Override
            public void onSure(int position, CharSequence text) {
                if ("自定义".equals(text)) {
                    showCustomDialog(new CustomEditDialog.OnClickListener() {
                        @Override
                        public void onSure(CustomEditDialog dialog, String editText) {
                            Day day = new Day();
                            day.setType(editText);
                            ds.add(day);
                            convertData();
                        }
                    });
                    return;
                }
                Day day = new Day();
                day.setType(text.toString());
                ds.add(day);
                convertData();
            }
        };
    }

    private void initSelectorDialog() {
        for (int i = 0; i < 5; i++) {
            selectors[i] = new PopupSelectorDialog(THIS, selectItems.get(i), listeners[i]);
        }
    }

    public void showCustomDialog(CustomEditDialog.OnClickListener listener) {
        customDialog.show(THIS, listener);
    }

    public void showAreaSelectDialog(AreaSelectorDialog.OnClickListener listener) {
        areaSelectorDialog = new AreaSelectorDialog(THIS, listener);
        areaSelectorDialog.show();
    }

    public void showSelectPhotoDialog() {
        if (selectorDialog == null)
            selectorDialog = new PopupSelectorDialog(THIS, new String[]{"打开相机", "选择照片"}, new PopupSelectorDialog.OnClickListener() {
                @Override
                public void onSure(int position, CharSequence text) {
                    switch (position) {
                        case 0:
                            if (ActivityCompat.checkSelfPermission(THIS, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(THIS, new String[]{Manifest.permission.CAMERA}, 2);
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    imageFile = BitmapTool.open_v2(THIS, TAKE_PHOTO);
                                } else imageUri = BitmapTool.open(THIS, TAKE_PHOTO);
                            }
                            break;
                        case 1:
                            BitmapTool.choose_v2(THIS, CHOOSE_PHOTO);
                            break;
                    }
                }
            });
        selectorDialog.show();
    }

    public void convertData() {
        items.clear();

        Item li = new HeaderItem();
        li.itemType = LABEL;
        li.object = information;
        items.add(li);

        Item ni = new InputItem();
        ni.itemType = NAME;
        ni.object = information;
        items.add(ni);

        Item oi = new InputItem();
        oi.itemType = POSITION;
        oi.object = information;
        items.add(oi);

        Item ci = new InputItem();
        ci.itemType = COMPANY;
        ci.object = information;
        items.add(ci);

        Item node = new InputItem();
        node.itemType = NOTE;
        node.object = information;
        node.showLong = true;
        items.add(node);

        Item item = new Item();
        item.setType(4);
        item.label = "添加更多信息";
//        items.add(item);

        if (ps.size() > 0) {
            item = new Item();
            item.setType(4);
            item.label = "电话";
            items.add(item);
            for (Phone phone : ps) {
                RemoveItem pi = new RemoveItem(PHONE);
                pi.object = phone;
                items.add(pi);
            }
        }
        items.add(new AddItem(PHONE, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectors[0].show();
            }
        }));

        if (as.size() > 0) {
            item = new Item();
            item.setType(4);
            item.label = "地址";
            items.add(item);
            for (Address address : as) {
                Item ai = new AddressItem();
                ai.object = address;
                items.add(ai);
            }
        }
        items.add(new AddItem(ADDRESS, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectors[3].show();
            }
        }));

        if (es.size() > 0) {
            item = new Item();
            item.setType(4);
            item.label = "邮箱";
            items.add(item);
            for (Email email : es) {
                RemoveItem ei = new RemoveItem(EMAIL);
                ei.object = email;
                items.add(ei);
            }
        }
        items.add(new AddItem(EMAIL, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectors[1].show();
            }
        }));

        if (us.size() > 0) {
            item = new Item();
            item.setType(4);
            item.label = "主页";
            items.add(item);
            for (Url url : us) {
                RemoveItem ui = new RemoveItem(INDEX);
                ui.object = url;
                items.add(ui);
            }
        }
        items.add(new AddItem(INDEX, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectors[2].show();
            }
        }));

        if (ds.size() > 0) {
            item = new Item();
            item.setType(4);
            item.label = "日期";
            items.add(item);
            for (Day day : ds) {
                Item di = new DateItem();
                di.object = day;
                items.add(di);
            }
        }
        items.add(new AddItem(DAILY, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectors[4].show();
            }
        }));


        if (cs.size() > 0) {
            item = new Item();
            item.setType(4);
            item.label = "自定义";
            items.add(item);
            for (Custom custom : cs) {
                ci = new RemoveItem(CUSTOM);
                ci.object = custom;
                items.add(ci);
            }
        }
        AddItem addItem = new AddItem(CUSTOM, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog(new CustomEditDialog.OnClickListener() {
                    @Override
                    public void onSure(CustomEditDialog dialog, String editText) {
                        Custom custom = new Custom();
                        custom.setType(editText);
                        cs.add(custom);
                        convertData();
                    }
                });
            }
        });
        addItem.showLong = true;
        items.add(addItem);

        switch (parameter.getOpenType()) {
            case 新建我的e卡:
                break;
            case 新建联系人:
                break;
            case 编辑我的e卡:
                item = new Item();
                item.setType(5);
                item.listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ConfirmDialog.Builder().setMessage("确定要删除这张卡片吗？").setRightButton(R.color.light_red, new OnClickListener() {
                            @Override
                            public boolean onClick(View v) {
                                if (!TextUtils.isEmpty(information.getCard_id())) {
                                    LoadingDialog.show(THIS);
                                    APIProvider.get(MineCardAPI.class).delete(information.getCard_id(), new SyncReference.CompletionListener() {
                                        @Override
                                        public void onComplete(SyncError syncError, SyncReference syncReference) {
                                            LoadingDialog.hide();
                                            if (syncError != null) {
                                                TOAST("操作失败");
                                                logger.e("syncError : " + syncError.getMessage());
                                            } else {
                                                TOAST("删除成功");
                                                Activity activity = App.getInstance().getActivity();
                                                finish();
                                                if (activity != null) activity.finish();
                                                App.getInstance().setActivity(null);
                                            }
                                        }
                                    });
                                }
                                return true;
                            }
                        }).build().show(THIS);
                    }
                };
                items.add(item);
                break;
            case 编辑联系人:
                item = new Item();
                item.setType(5);
                item.listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ConfirmDialog.Builder().setMessage("确定要删除这张卡片吗？").setRightButton(R.color.light_red, new OnClickListener() {
                            @Override
                            public boolean onClick(View v) {
                                if (!TextUtils.isEmpty(information.getCard_id())) {
                                    LoadingDialog.show(THIS);
                                    APIProvider.get(ContactAPI.class).delete(information.getCard_id(), new SyncReference.CompletionListener() {
                                        @Override
                                        public void onComplete(SyncError syncError, SyncReference syncReference) {
                                            LoadingDialog.hide();
                                            if (syncError != null) {
                                                TOAST("操作失败");
                                                logger.e("syncError : " + syncError.getMessage());
                                            } else {
                                                TOAST("删除成功");
                                                Activity activity = App.getInstance().getActivity();
                                                finish();
                                                if (activity != null) activity.finish();
                                                App.getInstance().setActivity(null);
                                            }
                                        }
                                    });
                                }
                                return true;
                            }
                        }).build().show(THIS);
                    }
                };
                items.add(item);
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        restart = true;
        switch (requestCode) {
            case TAKE_PHOTO:
                // 从拍照界面返回
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        imageUri = getUriForFile(THIS, THIS.getPackageName() + ".file_provider", imageFile);
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.putExtra("noFaceDetection", false);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;
            case CROP_PHOTO:
                // 从裁剪界面返回
                if (resultCode == RESULT_OK) {
                    if (imageUri == null) {
                        TOAST("图片获取失败");
                        return;
                    }
                    information.setAvatar(imageUri.getPath());
                    convertData();
                }

                break;
            case CHOOSE_PHOTO:
                // 选择图片返回
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        imageUri = FileProvider.getUriForFile(THIS, getPackageName() + ".file_provider", new File(GetImagePath.get(THIS, data.getData())));
                    else imageUri = Uri.fromFile(new File(GetImagePath.get(THIS, data.getData())));
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.putExtra("noFaceDetection", false);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;
            default:
                break;
        }
    }

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(CreateCardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreateCardActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            showSelectPhotoDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                showSelectPhotoDialog();
            }
        } else if (requestCode == 2) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    imageFile = BitmapTool.open_v2(THIS, CROP_PHOTO);
                else imageUri = BitmapTool.open(THIS, CROP_PHOTO);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public class Item implements Holdable, Gradable {

        private static final long serialVersionUID = 1599611891542927700L;
        public int type;
        public CharSequence label;
        public boolean showLong;
        public ItemType itemType = DEFAULT;
        public Object object;
        protected View.OnClickListener listener;

        protected EditText inputView;
        protected TextView labelView;

        @Override
        public void setType(int type) {
            this.type = type;
        }

        @Override
        public int getType() {
            return type;
        }

        public void onClicked(View v) {

        }

        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            if (listener != null) holder.setOnItemClickListener(listener);
            if (label != null) {
                labelView = holder.findViewById(R.id.label_text);
                labelView.setText(label);
            }
            if (showLong) {
                holder.findViewById(R.id.long_line).setVisibility(View.VISIBLE);
                holder.findViewById(R.id.short_line).setVisibility(View.GONE);
            }
            if (object != null) {
                inputView = holder.findViewById(R.id.item_value_input_view);
                if (inputView != null)
                    inputView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                Information information = null;
                                String value = inputView.getText().toString();
                                switch (itemType) {
                                    case LABEL:
                                        information = (Information) object;
                                        information.setCard_label(value);
                                        break;
                                    case NAME:
                                        information = (Information) object;
                                        information.setName(value);
                                        break;
                                    case POSITION:
                                        information = (Information) object;
                                        information.setPosition(value);
                                        break;
                                    case COMPANY:
                                        information = (Information) object;
                                        information.setCompany(value);
                                        break;
                                    case PHONE:
                                        Phone phone = (Phone) object;
                                        phone.setPhone(value);
                                        break;
                                    case EMAIL:
                                        Email email = (Email) object;
                                        email.setEmail(value);
                                        break;
                                    case INDEX:
                                        Url url = (Url) object;
                                        url.setUrl(value);
                                        break;
                                    case ADDRESS:
                                        Address address = (Address) object;
                                        address.setAddress(value);
                                        break;
                                    case DAILY:
                                        break;
                                    case CUSTOM:
                                        Custom custom = (Custom) object;
                                        custom.setCustom(value);
                                        break;
                                    case NOTE:
                                        information = (Information) object;
                                        information.setNote(value);
                                        break;
                                }
                            }
                        }
                    });
                labelView = holder.findViewById(R.id.label_text);
                Information information = null;
                switch (itemType) {
                    case LABEL:
                        information = (Information) object;
                        inputView.setText(information.getCard_label());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入e卡名称");
                        break;
                    case NAME:
                        information = (Information) object;
                        inputView.setText(information.getName());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入姓名");
                        labelView.setText("姓名");
                        break;
                    case POSITION:
                        information = (Information) object;
                        inputView.setText(information.getPosition());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入职位");
                        labelView.setText("职位");
                        break;
                    case COMPANY:
                        information = (Information) object;
                        inputView.setText(information.getCompany());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入公司");
                        labelView.setText("公司");
                        break;
                    case PHONE:
                        Phone phone = (Phone) object;
                        labelView.setText(phone.getType());
                        inputView.setInputType(InputType.TYPE_CLASS_PHONE);
                        inputView.setHint("请输入" + phone.getType());
                        inputView.setText(phone.getPhone());
                        break;
                    case EMAIL:
                        Email email = (Email) object;
                        inputView.setText(email.getEmail());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入" + email.getType());
                        labelView.setText(email.getType());
                        break;
                    case INDEX:
                        Url url = (Url) object;
                        labelView.setText(url.getType());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入" + url.getType());
                        inputView.setText(url.getUrl());
                        break;
                    case ADDRESS:
                        Address address = (Address) object;
                        labelView.setText(address.getType());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入详细" + address.getType());
                        inputView.setText(address.getAddress());
                        break;
                    case DAILY:
                        Day day = (Day) object;
                        labelView.setText(day.getType());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入" + day.getType());
                        if (day.getDate() != null)
                            inputView.setText(new SimpleDateFormat("yyyy-MM-dd").format(day.getDate()));
                        break;
                    case CUSTOM:
                        Custom custom = (Custom) object;
                        labelView.setText(custom.getType());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入" + custom.getType());
                        inputView.setText(custom.getCustom());
                        break;
                    case NOTE:
                        information = (Information) object;
                        inputView.setText(information.getNote());
                        inputView.setInputType(InputType.TYPE_CLASS_TEXT);
                        inputView.setHint("请输入你备注");
                        labelView.setText("备注");
                        break;
                }
            }
        }

        public void setListener(View.OnClickListener listener) {
            this.listener = listener;
        }
    }

    public class HeaderItem extends Item {

        private static final long serialVersionUID = -7325673289998786174L;

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            super.holding(holder);
            Information information = (Information) object;
            if (information.getType() != 0) {
                holder.findViewById(R.id.avatar_text).setVisibility(View.VISIBLE);
                holder.findViewById(R.id.input_layout).setVisibility(View.GONE);
            } else {
                holder.findViewById(R.id.avatar_text).setVisibility(View.GONE);
                holder.findViewById(R.id.input_layout).setVisibility(View.VISIBLE);
            }

            switch (parameter.getOpenType()) {
                case 新建我的e卡:
                    holder.findViewById(R.id.avatar_text).setVisibility(View.GONE);
                    holder.findViewById(R.id.input_layout).setVisibility(View.VISIBLE);
                    break;
                case 新建联系人:
                    holder.findViewById(R.id.avatar_text).setVisibility(View.VISIBLE);
                    holder.findViewById(R.id.input_layout).setVisibility(View.GONE);
                    break;
            }
            ImageView avatarImage = holder.findViewById(R.id.avatar_image);
            Bitmap bitmap = BitmapTool.adjustImage(THIS, information.getAvatar());
            if (bitmap != null) {
                Bitmap transform = new CircleTransform().transform(bitmap);
                if (transform != null) avatarImage.setImageBitmap(transform);
                else Glide.with(holder.getContext())
                        .load(App.oss().getURL(information.getCard_id()))
                        .placeholder(R.mipmap.edit_avatar_icon)
                        .signature(new StringSignature(String.valueOf(information.getDateUpdated() == null ? System.currentTimeMillis() : information.getDateUpdated().getTime())))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .transform(new RoundTransformation(THIS))
                        .into(avatarImage);
            } else {
                if (!TextUtils.isEmpty(information.getAvatar()) && restart) TOAST("图片获取失败");
                Glide.with(holder.getContext())
                        .load(App.oss().getURL(information.getCard_id()))
                        .placeholder(R.mipmap.edit_avatar_icon)
                        .signature(new StringSignature(String.valueOf(information.getDateUpdated() == null ? System.currentTimeMillis() : information.getDateUpdated().getTime())))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .transform(new RoundTransformation(THIS))
                        .into(avatarImage);
            }
            holder.findViewById(R.id.edit_avatar_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showContacts();
                }
            });
        }

        @Override
        public int getType() {
            return 0;
        }
    }

    public class InputItem extends Item {

        private static final long serialVersionUID = -2139293971846693222L;

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            super.holding(holder);
        }

        @Override
        public int getType() {
            return 1;
        }
    }

    public class RemoveItem extends InputItem {

        private static final long serialVersionUID = -8413262105311775942L;
        private boolean requestFocus = false;

        public RemoveItem(ItemType itemType) {
            this.itemType = itemType;
        }

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            if (requestFocus) {
                EditText view = holder.findViewById(R.id.item_value_input_view);
                InputMethodManager imm = (InputMethodManager) THIS.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                view.requestFocus();
                view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) requestFocus = false;
                    }
                });
            }
            holder.findViewById(R.id.remove_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (itemType) {
                        case PHONE:
                            if (ps.size() == 1) {
                                TOAST("请至少填写一个号码");
                                return;
                            }
                            ps.remove(object);
                            break;
                        case EMAIL:
                            es.remove(object);
                            break;
                        case INDEX:
                            us.remove(object);
                            break;
                        case ADDRESS:
                            as.remove(object);
                            break;
                        case DAILY:
                            ds.remove(object);
                            break;
                        case CUSTOM:
                            cs.remove(object);
                            break;
                    }
                    convertData();
                    adapter.notifyDataSetChanged();
                }
            });
            super.holding(holder);
        }

        @Override
        public int getType() {
            return 2;
        }
    }

    public class AddressItem extends RemoveItem {


        private static final long serialVersionUID = -4898085866135716840L;

        public AddressItem() {
            super(ADDRESS);
        }

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            super.holding(holder);
            final Address address = (Address) object;
            final TextView addressSelectButton = holder.findViewById(R.id.address_select_button);
            addressSelectButton.setText(TextUtils.isEmpty(address.county()) ? "请选择省 - 市 - 区" : address.county());
            addressSelectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAreaSelectDialog(new AreaSelectorDialog.OnClickListener() {
                        @Override
                        public void onSure(AreaSelectorDialog.AreaDetail area) {
                            address.setProvince(area.getProvince());
                            address.setCity(area.getCity());
                            address.setCounty(area.getArea());
                            addressSelectButton.setText(area.toString());
                        }
                    });
                }
            });
        }

        @Override
        public int getType() {
            return 6;
        }
    }

    public class DateItem extends RemoveItem {

        private static final long serialVersionUID = 4807889565812805620L;

        public DateItem() {
            super(DAILY);
        }

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            super.holding(holder);
            inputView.setFocusable(false);
            final Day day = (Day) object;
            inputView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar now = Calendar.getInstance();
                    new DatePickerDialog(THIS, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            try {
                                StringBuilder sb = new StringBuilder();
                                sb.append(year).append("-").append(month + 1).append("-").append(dayOfMonth);
                                inputView.setText(sb.toString());
                                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(sb.toString());
                                day.setDate(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
        }
    }

    public class AddItem extends Item {

        private static final long serialVersionUID = -9215452506033578352L;

        public AddItem(ItemType itemType, View.OnClickListener listener) {
            this.label = itemType.getLabel();
            this.listener = listener;
        }

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            super.holding(holder);
        }

        @Override
        public int getType() {
            return 3;
        }
    }

    enum ItemType {
        DEFAULT(""), LABEL("卡片名"), NAME("姓名"), POSITION("职位"), COMPANY("公司"), PHONE("电话"), EMAIL("邮箱"), INDEX("主页"), ADDRESS("地址"), DAILY("日期"), CUSTOM("自定义"), NOTE("备注");

        String label;

        ItemType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
