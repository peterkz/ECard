package com.wetoop.ecard.ui.dialog;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.model.Address;
import com.wetoop.ecard.api.model.Email;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.ContactBean;
import com.wetoop.ecard.listener.OnProgressBarListener;
import com.wetoop.ecard.tools.CnToSpell;
import com.wetoop.ecard.tools.ECardToMyContactsTools;
import com.wetoop.ecard.tools.NumberProgressBar;
import com.wetoop.ecard.tools.PermissionUtil;

import java.util.ArrayList;

/**
 * Created by User on 2017/11/22.
 */

public class SynchronizationLoadingDialog extends Dialog implements OnProgressBarListener {
    private Context context;
    private NumberProgressBar bnp;
    private TextView progress_title;
    private ArrayList<ContactBean> contactBeanArrayList = new ArrayList<>();
    private ArrayList<ContactBean> contactsList = new ArrayList<>();
    //用来检测是否有相同手机号，有的话为true
    private Boolean eCardToMyContactsStart = false, myContactsToECardStart = false;
    private int contactBeanArrayListSize = 0;//e卡联系人总数
    private int contactsListSize = 0;//通讯录联系人总数
    private int num = 0;
    public SynchronizationLoadingDialog(Context context,ArrayList<ContactBean> contactBeanArrayList) {
        super(context);
        this.context = context;
        this.contactBeanArrayList = contactBeanArrayList;
        contactsList.clear();
    }
    @Override
    public void onProgressChange(int current, int max) {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_synchronization_loading);
        bnp = (NumberProgressBar)findViewById(R.id.progress);
        progress_title = (TextView)findViewById(R.id.progress_title);
        bnp.setOnProgressBarListener(this);
        synchronizationStart();
    }

    private void synchronizationStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                readAllContacts();
                Message msgBnp = handlerContact.obtainMessage();
                msgBnp.what = 2;
                handlerContact.sendMessage(msgBnp);
                ECardToMyContactsTools eCardToMyContacts = new ECardToMyContactsTools();
                contactBeanArrayListSize = contactBeanArrayList.size();
                contactsListSize = contactsList.size();
                if (contactBeanArrayListSize > 0) {
                    for (int i = 0; i < contactBeanArrayList.size(); i++) {
                        eCardToMyContactsStart = false;
                        String name = contactBeanArrayList.get(i).getCard().getInformation().getName();
                        if (contactsListSize > 0) {
                            //循环遍历该e卡联系人下的所有手机号，并与手机通讯录的手机号对比
                            for(int phoneNumI = 0; phoneNumI<contactBeanArrayList.get(i).getCard().getPhones().size();phoneNumI++) {
                                String phone = contactBeanArrayList.get(i).getCard().getPhones().get(phoneNumI).getPhone();
                                for (int contactsNum = 0; contactsNum < contactsList.size(); contactsNum++) {
                                    String contactsName = contactsList.get(contactsNum).getCard().getInformation().getName();
                                    for(int contactsPhoneNumI=0;contactsPhoneNumI<contactsList.get(contactsNum).getCard().getPhones().size();contactsPhoneNumI++){
                                        String contactsPhone = contactsList.get(contactsNum).getCard().getPhones().get(contactsPhoneNumI).getPhone();
                                        if (contactsPhone.equals(phone)) {
                                            eCardToMyContactsStart = true;
                                        } else {
                                            if (contactsName.equals(name) && !contactsName.contains("(e卡)")) {
                                                name = contactBeanArrayList.get(i).getCard().getInformation().getName() + "(e卡)";
                                                contactBeanArrayList.get(i).getCard().getInformation().setName(name);
                                            }
                                        }
                                    }
                                }
                            }
                            if (!eCardToMyContactsStart) {
                                eCardToMyContacts.addECardToMyContacts(context, contactBeanArrayList.get(i).getCard());
                            }
                        }
                        Message msg = handlerContact.obtainMessage();
                        msg.what = 0;
                        handlerContact.sendMessage(msg);
                    }
                }
                if (contactsListSize > 0) {
                    for (int i = 0; i < contactsList.size(); i++) {
                        myContactsToECardStart = false;
                        for(int phoneNumI=0;phoneNumI<contactsList.get(i).getCard().getPhones().size();phoneNumI++) {
                            String contactsPhone = contactsList.get(i).getCard().getPhones().get(phoneNumI).getPhone();
                            for (int num = 0; num < contactBeanArrayList.size(); num++) {
                                for(int contactsPhoneNum = 0;contactsPhoneNum < contactBeanArrayList.get(num).getCard().getPhones().size();contactsPhoneNum++){
                                    if (contactsPhone.equals(contactBeanArrayList.get(num).getCard().getPhones().get(contactsPhoneNum).getPhone())) {
                                        myContactsToECardStart = true;
                                    }
                                }
                            }
                        }
                        if (!myContactsToECardStart) {
                            eCardToMyContacts.addContactsToECard(App.getWilddogAuth().getCurrentUser(), contactsList.get(i).getCard());
                        }
                        Message msg = handlerContact.obtainMessage();
                        msg.what = 0;
                        handlerContact.sendMessage(msg);
                    }
                }
                Message msg = handlerContact.obtainMessage();
                msg.what = 1;
                handlerContact.sendMessage(msg);
            }
        }).start();
    }

    android.os.Handler handlerContact = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://e卡到通讯录
                    num++;
                    bnp.incrementProgressBy(1);
                    System.out.println("msg.what="+num);
                    break;
                case 1:
                    bnp.incrementProgressBy(contactBeanArrayListSize+contactsListSize);
                    Toast.makeText(context, "同步成功", Toast.LENGTH_SHORT).show();
                    SynchronizationLoadingDialog.this.dismiss();
                    bnp.setProgress(0);
                    break;
                case 2://进度条的值最大为e卡联系人数量加上本地联系人数量
                    contactBeanArrayListSize = contactBeanArrayList.size();
                    contactsListSize = contactsList.size();
                    System.out.println("contactBeanArrayListSize：" + contactBeanArrayListSize);
                    System.out.println("contactsListSize：" + contactsListSize);
                    bnp.setMax(contactBeanArrayListSize+contactsListSize);
                    progress_title.setText("读取通讯录完成，正在同步");
                    break;
            }
        }
    };
    //读取手机的联系人
    public void readAllContacts() {
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                ContactBean contactsBean = new ContactBean();
                StringBuilder buf = new StringBuilder();
                CardBean card = new CardBean();
                Information information = new Information();
                ArrayList<Phone> phoneArrayList = new ArrayList<>();
                phoneArrayList.clear();

                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));//获得ContactID
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));//获得姓名
                //String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                information.setName(name);
                CnToSpell cnToSpell = new CnToSpell();
                String spellName = cnToSpell.getPinYinHeadChar(name);
                contactsBean.setSpellName(spellName);
                String spellFirst = cnToSpell.getPinYinFirstLetter(name);
                if (spellFirst.matches("[A-Z]")) {
                    contactsBean.setSpellFirst(spellFirst);
                } else if (!spellFirst.matches("[A-Z]")) {
                    String spellFirstToUpperCase = cnToSpell.getPinYinFirstLetter(name).toUpperCase();
                    if (!spellFirstToUpperCase.matches("[A-Z]")) {
                        spellFirstToUpperCase = "#";
                    }
                    contactsBean.setSpellFirst(spellFirstToUpperCase);
                }
                // 读取rawContactsId
                String rawContactsId = "";
                Cursor rawContactsIdCur = cr.query(ContactsContract.RawContacts.CONTENT_URI,
                        null,
                        ContactsContract.RawContacts.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                if (rawContactsIdCur.moveToFirst()) {
                    rawContactsId = rawContactsIdCur.getString(rawContactsIdCur.getColumnIndex(ContactsContract.RawContacts._ID));
                }
                rawContactsIdCur.close();
              /*
               Android文档：ContactsContract.CommonDataKinds.Phone：
               CONTENT_URI：The content:// style URI for all data records of the  CONTENT_ITEM_TYPE MIME action, combined with the associated  raw contact and aggregate contact data.
              */
                // 读取号码
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //Uri phoneUri=Uri.parse("content://com.android.contacts/data/phones");
                    // 下面的ContactsContract.CommonDataKinds.Phone.CONTENT_URI可以用phoneUri代替
                    Cursor PhoneCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            //ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + " = ?",
                            new String[]{rawContactsId}, null);
                    while (PhoneCur.moveToNext()) {
                        Phone cardPhone = new Phone();
                        String number = PhoneCur.getString(PhoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String numberType = PhoneCur.getString(PhoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        String type = ContactsContract.CommonDataKinds.Phone.TYPE;
                        cardPhone.setPhone(number);
                        if (Integer.parseInt(numberType) == 1) {
                            cardPhone.setType("住宅号码");
                        } else if (Integer.parseInt(numberType) == 2) {
                            cardPhone.setType("手机");
                        } else if (Integer.parseInt(numberType) == 3) {
                            cardPhone.setType("工作号码");
                        } else if (Integer.parseInt(numberType) == 4) {
                            cardPhone.setType("单位传真");
                        } else if (Integer.parseInt(numberType) == 5) {
                            cardPhone.setType("住宅传真");
                        } else if (Integer.parseInt(numberType) == 6) {
                            cardPhone.setType("寻呼机");
                        } else if (Integer.parseInt(numberType) == 7) {
                            cardPhone.setType("其他");
                        } else if (Integer.parseInt(numberType) == 0) {
                            System.out.println(type);
                            cardPhone.setType(type);
                        } else if (Integer.parseInt(numberType) == 12) {
                            cardPhone.setType("总机");
                        }
                        phoneArrayList.add(cardPhone);
                    }
                    PhoneCur.close();
                }

                // 读取Email
                //Uri emailUri=Uri.parse("content://com.android.contacts/data/emails");
                // 下面的ContactsContract.CommonDataKinds.Email.CONTENT_URI可以用emailUri代替
                ArrayList<Email> emailArrayList = new ArrayList<>();
                emailArrayList.clear();
                Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (emailCur.moveToNext()) {
                    Email emailBean = new Email();
                    String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    String emailType = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                    if (Integer.parseInt(emailType) == 1) {
                        emailBean.setType("个人邮箱");
                        emailBean.setEmail(email);
                    } else if (Integer.parseInt(emailType) == 2) {
                        emailBean.setType("工作邮箱");
                        emailBean.setEmail(email);
                    } else if (Integer.parseInt(emailType) == 0) {
                        emailBean.setType("自定义邮箱");
                        emailBean.setEmail(email);
                    }
                    emailArrayList.add(emailBean);
                }
              /*Email类型：
              1：TYPE_HOME
              2：TYPE_WORK
              3：TYPE_OTHER
              4：TYPE_MOBILE
              */
                emailCur.close();

                // 读取备注
                String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] noteWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
                if (noteCur.moveToFirst()) {
                    String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                }
                noteCur.close();

                // 读取地址
                ArrayList<Address> addressArrayList = new ArrayList<>();
                addressArrayList.clear();
                String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] addrWhereParams = new String[]{id, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
                Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null);
                while (addrCur.moveToNext()) {
                    Address addressBean = new Address();
                    String poBox = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
                    String street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                    String city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                    String state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                    String postalCode = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                    String country = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                    String type = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
                    if (poBox != null) {
                        //"POBOX:";
                    }

                    if (street != null) {
                        //"STREET:";
                    }

                    if (city != null) {
                        addressBean.setCity(city);
                        //"CITY:";
                    }

                    if (state != null) {
                        //"REGION:";
                    }

                    if (postalCode != null) {
                        //"POSTCODE:";
                    }

                    if (country != null) {
                        addressBean.setCounty(country);
                        //"COUNTRY:";
                    }

                    if (type != null) {
                        //"TYPE:";
                    }
                    if (Integer.parseInt(type) == 2) {
                        //addressBean.setAddress();
                        //"单位地址："
                    } else if (Integer.parseInt(type) == 1) {
                        //"住宅地址："
                    } else if (Integer.parseInt(type) == 3) {
                        //"其他地址："
                    } else if (Integer.parseInt(type) == 0) {
                        //"自定义地址："
                    }
                }
                addrCur.close();

                // 读取即时消息
                String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] imWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE};
                Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI, null, imWhere, imWhereParams, null);
                while (imCur.moveToNext()) {
                    String imName = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
                    String imType = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
                    if (Integer.parseInt(imType) == 0) {
                        //"AIM:"
                    } else if (Integer.parseInt(imType) == 1) {
                        //"Window:"
                    } else if (Integer.parseInt(imType) == 2) {
                        //"雅虎:
                    } else if (Integer.parseInt(imType) == 3) {
                        //"Skype:"
                    } else if (Integer.parseInt(imType) == 4) {
                        // "qq:"
                    } else if (Integer.parseInt(imType) == 5) {
                        //"环聊:"
                    } else if (Integer.parseInt(imType) == 6) {
                        //"icq:"
                    } else if (Integer.parseInt(imType) == 7) {
                        //"jabber:"
                    } else if (Integer.parseInt(imType) == -1) {
                        //"自定义:"
                    }
                }
                imCur.close();

                // 读取公司及职位
                String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] orgWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
                Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI, null, orgWhere, orgWhereParams, null);
                while (orgCur.moveToNext()) {
                    String orgName = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
                    String title = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                    information.setCompany(orgName);
                    information.setPosition(title);
                }
                orgCur.close();

                //昵称
                String nicknameWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] nicknameWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE};
                Cursor nicknameCur = cr.query(ContactsContract.Data.CONTENT_URI, null, nicknameWhere, nicknameWhereParams, null);
                if (nicknameCur.moveToFirst()) {
                    String nickname = nicknameCur.getString(nicknameCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                }
                nicknameCur.close();

                card.setInformation(information);
                card.setPhones(phoneArrayList);
                card.setEmails(emailArrayList);
                contactsBean.setCard(card);
                contactsList.add(contactsBean);
            }
        }
    }
}
