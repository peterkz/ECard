package com.wetoop.ecard.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.MessageAPI;
import com.wetoop.ecard.api.SearchAccountAPI;
import com.wetoop.ecard.api.model.Account;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.ContactBean;
import com.wetoop.ecard.bean.MainMenuItem;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.BitmapTool;
import com.wetoop.ecard.tools.CnToSpell;
import com.wetoop.ecard.tools.InputMethodTool;
import com.wetoop.ecard.tools.MD5;
import com.wetoop.ecard.tools.PermissionUtil;
import com.wetoop.ecard.tools.PinyinComparator;
import com.wetoop.ecard.ui.adapter.MyContactsAdapter;
import com.wetoop.ecard.ui.dialog.CustomAddContactDialog;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wetoop.ecard.ui.widget.listview.LetterListView;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;
import cn.edots.slug.listener.OnTextWatcher;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Slug(layout = R.layout.activity_my_contacts)
public class MyContactsActivity extends TitleBarActivity implements Standardize, MyContactsAdapter.OnGetAlphaIndexerAndSectionsListener {
    public static final String MY_CONTACTS_KEY = "MY_CONTACTS_KEY";

    @FindView(R.id.search_bar)
    private RelativeLayout searchLayout;
    @FindView(R.id.search_button)
    private TextView searchButton;
    @FindView(R.id.search)
    private RelativeLayout search;
    @BindView(R.id.search_text)
    private EditSlugger searchSlugger;
    @FindView(R.id.clear_button)
    private ImageView clearButton;
    @FindView(R.id.listView)
    private ListView listView;
    @FindView(R.id.letterView)
    private LetterListView letterListView;

    private TextView overLayout;
    private Handler handler;
    private OverlayThread overlayThread;
    private List<String> sections;
    private List<ContactBean> contactsList = new ArrayList<>();
    private Map<String, Integer> alphaIndexer = new HashMap<>();
    private WindowManager windowManager;
    private ArrayList<MainMenuItem> menuItems = new ArrayList<>();
    private RelativeLayout newContact;
    private CardBean cardBean;
    private MyContactsAdapter myAdapter;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        if (map != null) {
            cardBean = (CardBean) map.get(MY_CONTACTS_KEY);
            if (cardBean == null) finish();
        } else {
            finish();
        }
    }

    @Override
    public void initView() {
        setCenterTitleContent("通讯录");
        View convertView = getLayoutInflater().inflate(R.layout.activity_contact_listview_header, null);
        newContact = (RelativeLayout) convertView.findViewById(R.id.newContact);
        listView.addHeaderView(convertView);
        handler = new Handler();
        overlayThread = new OverlayThread();
        contactsList.clear();
        initOverlay();
        letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
        showContacts();
    }

    @Override
    public void setListeners() {
        newContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CustomAddContactDialog().show(MyContactsActivity.this, new CustomAddContactDialog.OnClickListener() {

                    @Override
                    public void onSure(CustomAddContactDialog dialog, final String messageText) {
                        LoadingDialog.show(THIS);
                        APIProvider.get(SearchAccountAPI.class).searchContactGET(MD5.encoding(messageText), new OnESubscriber<Account>() {
                            @Override
                            protected void onComplete(boolean success, Account o, Throwable e) {
                                if (success && o != null) {
                                    LoadingDialog.show(THIS);
                                    APIProvider.get(MessageAPI.class).sendExchangeMessage(((ECardDetailActivity) App.getInstance().getActivity()).bean, "", o.getUser_id(), new SyncReference.CompletionListener() {
                                        @Override
                                        public void onComplete(SyncError syncError, SyncReference syncReference) {
                                            if (syncError != null) {
                                                logger.e("syncError: " + syncError.getMessage());
                                                TOAST("操作失败");
                                            } else TOAST("发送成功！");
                                        }
                                    });
                                } else {
                                    Uri uri = Uri.parse("smsto:" + messageText); //要发送短信的电话号码
                                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                                    intent.putExtra("向他发送一张e卡", "这是跳转后发送短信界面的消息编辑框显示内容");
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                });
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayout.setVisibility(View.VISIBLE);
                search.setVisibility(View.INVISIBLE);
                searchSlugger.getView().requestFocus();
                InputMethodTool.requestInput(searchSlugger.getView());
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(searchSlugger.getView().getText())) {
                    searchSlugger.clearFocus(THIS);
                    InputMethodTool.cancelInput(searchSlugger.getView());
                    searchLayout.setVisibility(View.GONE);
                    search.setVisibility(View.VISIBLE);
                } else search();
            }
        });
        searchSlugger.addTextChangedListener(new OnTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    clearButton.setVisibility(View.VISIBLE);
                    searchButton.setText("搜索");
                } else {
                    clearButton.setVisibility(View.GONE);
                    searchButton.setText("取消");
                    showContacts();
                }
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSlugger.getView().setText("");
            }
        });
    }

    private void search() {
        ArrayList<ContactBean> list = new ArrayList<>();
        for (ContactBean bean : contactsList) {
            if (!TextUtils.isEmpty(bean.getCard().getInformation().getName())) {
                if (bean.getCard().getInformation().getName().contains(searchSlugger.getView().getText().toString())) {
                    list.add(bean);
                }
            }
        }
        contactsList.clear();
        contactsList.addAll(list);
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateLast() {

    }

    @Override
    public void getAlphaIndexerAndSectionsListener(Map<String, Integer> alphaIndexer, List<String> sections) {
        this.alphaIndexer = alphaIndexer;
        this.sections = sections;
    }

    /**
     * 字母列表点击滑动监听器事件
     */
    private class LetterListViewListener implements
            LetterListView.OnTouchingLetterChangedListener {
        @Override
        public void onTouchingLetterChanged(final String s) {
            if (alphaIndexer.get(s) != null) {//判断当前选中的字母是否存在集合中
                int position = alphaIndexer.get(s);//如果存在集合中则取出集合中该字母对应所在的位置,再利用对应的setSelection，就可以实现点击选中相应字母，然后联系人就会定位到相应的位置
                System.out.println("position=" + position);
                listView.setSelection(position);
                overLayout.setText(s);
                overLayout.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);
                // 延迟一秒后执行，让overlay为不可见
                handler.postDelayed(overlayThread, 1500);
            }
        }
    }

    /**
     * 初始化汉语拼音首字母弹出提示框
     */
    private void initOverlay() {
        LayoutInflater inflater = LayoutInflater.from(THIS);
        overLayout = (TextView) inflater.inflate(R.layout.overlay, null);
        overLayout.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overLayout, lp);
    }

    /**
     * 设置overlay不可见
     */
    private class OverlayThread implements Runnable {

        @Override
        public void run() {
            overLayout.setVisibility(View.GONE);
        }
    }

    private void getListView(List<ContactBean> cameraList) {
        PinyinComparator pinyinComparator = new PinyinComparator();
        Collections.sort(cameraList, pinyinComparator);
        myAdapter = new MyContactsAdapter(MyContactsActivity.this, cameraList, cardBean, listView);
        myAdapter.setOnGetAlphaIndexerAndSectionListener(this);
        listView.setAdapter(myAdapter);
    }

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(MyContactsActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MyContactsActivity.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MyContactsActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
        } else {
            LoadingDialog.show(THIS);
            Observable.create(new Observable.OnSubscribe<List<ContactBean>>() {
                @Override
                public void call(Subscriber<? super List<ContactBean>> subscriber) {
                    //要放入子线程
                    subscriber.onNext(readAllContacts());
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new OnESubscriber<List<ContactBean>>() {
                @Override
                protected void onComplete(boolean success, List<ContactBean> o, Throwable e) {
                    if (success)
                        getListView(o);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                LoadingDialog.show(THIS);
                Observable.create(new Observable.OnSubscribe<List<ContactBean>>() {
                    @Override
                    public void call(Subscriber<? super List<ContactBean>> subscriber) {
                        //要放入子线程
                        subscriber.onNext(readAllContacts());
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new OnESubscriber<List<ContactBean>>() {
                    @Override
                    protected void onComplete(boolean success, List<ContactBean> o, Throwable e) {
                        if (success)
                            getListView(o);
                    }
                });
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public List<ContactBean> testReadAll() {
        //uri = content://com.android.contacts/contacts
        Uri uri = Uri.parse("content://com.android.contacts/contacts"); //访问raw_contacts表
        ContentResolver resolver = MyContactsActivity.this.getContentResolver();
        //获得_id属性
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.RawContacts.Data._ID}, null, null, null);
        CnToSpell cnToSpell = new CnToSpell();
        while (cursor.moveToNext()) {
            ContactBean contactsBean = new ContactBean();
            CardBean card = new CardBean();
            Information information = new Information();
            ArrayList<Phone> phoneArrayList = new ArrayList<>();
            //获得id并且在data中寻找数据
            int id = cursor.getInt(0);
            uri = Uri.parse("content://com.android.contacts/contacts/" + id + "/data");
            //data1存储各个记录的总数据，mimetype存放记录的类型，如电话、email等
            Cursor cursor2 = resolver.query(uri, new String[]{ContactsContract.Data.DATA1, ContactsContract.Data.MIMETYPE}, null, null, null);
            while (cursor2.moveToNext()) {
                String data = cursor2.getString(cursor2.getColumnIndex("data1"));
                if (cursor2.getString(cursor2.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/name")) {       //如果是名字
                    information.setName(data);
                    //logger.e("名字="+data);
                    String spellFirst = cnToSpell.getPinYinFirstLetter(data);
                    if (spellFirst.matches("[A-Z]")) {
                        contactsBean.setSpellFirst(spellFirst);
                    } else if (!spellFirst.matches("[A-Z]")) {
                        String spellFirstToUpperCase = spellFirst.toUpperCase();
                        if (!spellFirstToUpperCase.matches("[A-Z]")) {
                            spellFirstToUpperCase = "#";
                        }
                        contactsBean.setSpellFirst(spellFirstToUpperCase);
                    }
                } else if (cursor2.getString(cursor2.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/phone_v2")) {  //如果是电话
                    Phone cardPhone = new Phone();
                    cardPhone.setPhone(data);
                    cardPhone.setType("手机号码");
                    phoneArrayList.add(cardPhone);
                } else if (cursor2.getString(cursor2.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/photo")) {
                    byte[] data1 = cursor2.getBlob(cursor2.getColumnIndex("data15"));
                    //cursor2.
                    logger.e("图片=" + Arrays.toString(data1));
                }
            }
            card.setInformation(information);
            card.setPhones(phoneArrayList);
            contactsBean.setCard(card);
            contactsList.add(contactsBean);
        }
        return contactsList;
    }

    //读取手机的联系人
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public List<ContactBean> readAllContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        assert cur != null;
        if (cur.getCount() > 0) {
            CnToSpell cnToSpell = new CnToSpell();

            while (cur.moveToNext()) {
                ContactBean contactsBean = new ContactBean();
                CardBean card = new CardBean();
                Information information = new Information();
                ArrayList<Phone> phoneArrayList = new ArrayList<>();

                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));//获得ContactID
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));//获得姓名
                information.setName(name);
                String spellFirst = cnToSpell.getPinYinFirstLetter(name);
                if (spellFirst.matches("[A-Z]")) {
                    contactsBean.setSpellFirst(spellFirst);
                } else if (!spellFirst.matches("[A-Z]")) {
                    String spellFirstToUpperCase = spellFirst.toUpperCase();
                    if (!spellFirstToUpperCase.matches("[A-Z]")) {
                        spellFirstToUpperCase = "#";
                    }
                    contactsBean.setSpellFirst(spellFirstToUpperCase);
                }
              /*
               Android文档：ContactsContract.CommonDataKinds.Phone：
               CONTENT_URI：The content:// style URI for all data records of the  CONTENT_ITEM_TYPE MIME action, combined with the associated  raw contact and aggregate contact data.
              */
                // 读取号码
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // 下面的ContactsContract.CommonDataKinds.Phone.CONTENT_URI可以用phoneUri代替
                    Cursor PhoneCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null, null);
                    while (PhoneCur.moveToNext()) {
                        String number = PhoneCur.getString(PhoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String numberType = PhoneCur.getString(PhoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        if (Integer.parseInt(numberType) == 2) {
                            Phone cardPhone = new Phone();
                            cardPhone.setPhone(number);
                            cardPhone.setType("手机号码");
                            phoneArrayList.add(cardPhone);
                        }
                    }
                    PhoneCur.close();
                }
                Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                        Long.parseLong(id));
                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
                if (input != null) {
                    Bitmap photo = BitmapFactory.decodeStream(input);
                    information.setAvatar(BitmapTool.bitmap2Base64(photo));
                }
                /*byte[] data = new byte[0];
                Uri u = Uri.parse("content://com.android.contacts/data");
                String where = "raw_contact_id = " + id
                        + " AND mimetype ='vnd.android.cursor.item/photo'";
                Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, null, null, null);
                assert cursor != null;
                if (cursor.moveToFirst()) {
                    data = cursor.getBlob(cursor.getColumnIndex("data15"));
                    logger.e("data= "+ Arrays.toString(data));
                }
                cursor.close();*/
                card.setInformation(information);
                card.setPhones(phoneArrayList);
                contactsBean.setCard(card);
                contactsList.add(contactsBean);
            }
            //getListView( contactsList);
        }
        return contactsList;
    }

    private byte[] getContactPhoto(Context c, String personId,
                                   int defaultIco) {
        byte[] data = new byte[0];
        Uri u = Uri.parse("content://com.android.contacts/data");
        String where = "raw_contact_id = " + personId
                + " AND mimetype ='vnd.android.cursor.item/photo'";
        Cursor cursor = c.getContentResolver()
                .query(u, null, where, null, null);
        if (cursor.moveToFirst()) {
            data = cursor.getBlob(cursor.getColumnIndex("data15"));
        }
        cursor.close();
        return data;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        windowManager.removeView(overLayout);
        windowManager.removeViewImmediate(overLayout);
    }
}
