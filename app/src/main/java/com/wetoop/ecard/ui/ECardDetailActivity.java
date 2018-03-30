package com.wetoop.ecard.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.CardAllAPI;
import com.wetoop.ecard.api.ContactAPI;
import com.wetoop.ecard.api.ExchangeAPI;
import com.wetoop.ecard.api.MessageAPI;
import com.wetoop.ecard.api.MineCardAPI;
import com.wetoop.ecard.api.model.Address;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Custom;
import com.wetoop.ecard.api.model.Day;
import com.wetoop.ecard.api.model.Email;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.api.model.Url;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.Holdable;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.RegularToQR;
import com.wetoop.ecard.tools.RoundTransformation;
import com.wetoop.ecard.ui.dialog.ChoiceCardDialog;
import com.wetoop.ecard.ui.dialog.ConfirmDialog;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wetoop.ecard.ui.dialog.OnClickListener;
import com.wetoop.ecard.ui.dialog.PopupSelectorDialog;
import com.wetoop.ecard.ui.dialog.QRCodeDialog;
import com.wetoop.ecard.ui.widget.SelectorButton;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import static com.wetoop.ecard.ui.ECardDetailActivity.OpenType.我的e卡;


/**
 * @author Parck.
 * @date 2017/11/22.
 * @desc
 */

@Slug(layout = R.layout.activity_ecard_detail)
public class ECardDetailActivity extends TitleBarActivity implements Standardize {

    public static String OPEN_PARAMETER_KEY = "OpenParameterKey";

    public static class OpenParameter implements Serializable {

        private static final long serialVersionUID = 8967184244747031307L;
        private OpenType openType;
        private String cardID;
        private String mineID;

        public OpenType getOpenType() {
            return openType;
        }

        public void setOpenType(OpenType openType) {
            this.openType = openType;
        }

        public String getCardID() {
            return cardID;
        }

        public void setCardID(String cardID) {
            this.cardID = cardID;
        }

        public String getMineID() {
            return mineID;
        }

        public void setMineID(String mineID) {
            this.mineID = mineID;
        }
    }

    public enum OpenType {
        扫描我的e卡, 扫描我的联系人, 扫描新的联系人, 我的e卡, 我的联系人, 新的联系人
    }

    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;

    private OpenParameter parameter;
    public CardBean bean;
    private HeaderItem headerItem = new HeaderItem();
    private List<Item> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private PopupSelectorDialog selectorDialog;
    private int[] layoutIds = {
            R.layout.item_ecard_detail_header,
            R.layout.item_ecard_detail,
            R.layout.item_label,
            R.layout.item_public,
            R.layout.item_add_contacts,
            R.layout.item_delete_button,
            R.layout.item_create_address
    };

    /**
     * 开启页面
     *
     * @param context
     * @param parameter
     */
    public static void startActivity(Context context, OpenParameter parameter) {
        Intent intent = new Intent(context, ECardDetailActivity.class);
        HashMap<String, Object> data = new HashMap<>();
        data.put(OPEN_PARAMETER_KEY, parameter);
        intent.putExtra("INTENT_DATA", data);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bean = null;
        App.getInstance().setActivity(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (TextUtils.isEmpty(parameter.getCardID())) finish();
        LoadingDialog.show(THIS);
        switch (parameter.getOpenType()) {
            case 扫描我的e卡:
            case 我的e卡:
                APIProvider.get(MineCardAPI.class).get(parameter.getCardID(), new OnESubscriber<Card>() {
                    @Override
                    protected void onComplete(boolean success, Card o, Throwable e) {
                        LoadingDialog.hide();
                        if (success && o != null) {
                            App.getInstance().setActivity(THIS);
                            bean = CardBean.fromModel(o);
                            convertData();
                        }
                    }
                });
                break;
            case 扫描我的联系人:
            case 我的联系人:
                APIProvider.get(ContactAPI.class).get(parameter.getCardID(), new OnESubscriber<Card>() {
                    @Override
                    protected void onComplete(boolean success, Card o, Throwable e) {
                        LoadingDialog.hide();
                        if (success && o != null) {
                            App.getInstance().setActivity(THIS);
                            bean = CardBean.fromModel(o);
                            convertData();
                        }
                    }
                });
                break;
            case 扫描新的联系人:
            case 新的联系人:
                APIProvider.get(CardAllAPI.class).get(parameter.getCardID(), new OnESubscriber<Card>() {
                    @Override
                    protected void onComplete(boolean success, Card o, Throwable e) {
                        LoadingDialog.hide();
                        if (success && o != null) {
                            App.getInstance().setActivity(THIS);
                            bean = CardBean.fromModel(o);
                            convertData();
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        if (map != null) {
            parameter = (OpenParameter) map.get(OPEN_PARAMETER_KEY);
            if (parameter == null) finish();
        } else finish();
        adapter = new RecyclerViewAdapter<Item>(THIS, layoutIds, items) {
            @Override
            protected void binding(ViewHolder holder, Item o, int position) {
                o.holding(holder);
            }

            @Override
            public int getItemViewType(int position) {
                return items.get(position).getType();
            }
        };
        loadData();
    }

    @Override
    public void initView() {
        selectorDialog = new PopupSelectorDialog(THIS, new String[]{"通讯录", "名片交换"}, new PopupSelectorDialog.OnClickListener() {
            @Override
            public void onSure(int position, CharSequence text) {
                switch (position) {
                    case 0:
                        HashMap<String, Object> data = new HashMap<>();
                        data.put(MyContactsActivity.MY_CONTACTS_KEY, bean);
                        startActivity(MyContactsActivity.class, data);
                        break;
                    case 1:
                        LoadingDialog.show(THIS);
                        APIProvider.get(ExchangeAPI.class).upload(bean.toModel(), new SyncReference.CompletionListener() {
                            @Override
                            public void onComplete(SyncError syncError, SyncReference syncReference) {
                                LoadingDialog.hide();
                                startActivity(ExchangeCardActivity.class);
                            }
                        });
                        break;
                }
            }
        });
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
        setOnRightTextListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> data = new HashMap<>();
                CreateCardActivity.OpenParameter openParameter = new CreateCardActivity.OpenParameter();
                switch (parameter.getOpenType()) {
                    case 我的e卡:
                        openParameter.setOpenType(CreateCardActivity.OpenType.编辑我的e卡);
                        CreateCardActivity.startActivity(THIS, openParameter);
                        break;
                    case 我的联系人:
                        if (TextUtils.isEmpty(bean.getInformation().getUser_id()) || bean.getInformation().getType() == 2) {
                            openParameter.setOpenType(CreateCardActivity.OpenType.编辑联系人);
                            CreateCardActivity.startActivity(THIS, openParameter);
                        } else {
                            if (App.getInstance().getMineCards().size() > 0)
                                new ChoiceCardDialog(THIS, new ChoiceCardDialog.OnChoiceListener() {
                                    @Override
                                    public void onChoice(CardBean target) {
                                        APIProvider.get(MessageAPI.class).sendExchangeMessage(target, bean.getInformation().getCard_id(), bean.getInformation().getUser_id(), new SyncReference.CompletionListener() {
                                            @Override
                                            public void onComplete(SyncError syncError, SyncReference syncReference) {
                                                if (syncError != null)
                                                    logger.e("syncError : " + syncError.getMessage());
                                                else TOAST("发送成功!");
                                            }
                                        });
                                    }
                                }).show();
                            else TOAST("你还未创建任何名片");
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onCreateLast() {

    }

    private void call(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void sendSMS(String phone) {
        Uri smsToUri = Uri.parse("smsto:" + phone);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        startActivity(intent);

    }

    /**
     * @！！！ 把card对象转化成list
     */
    private void convertData() {
        switch (parameter.getOpenType()) {
            case 我的e卡:
                setRightTextContent("编辑", R.color.blueColor, 16);
                break;
            case 我的联系人:
                if (TextUtils.isEmpty(bean.getInformation().getUser_id()) || bean.getInformation().getType() == 2) {
                    setRightTextContent("编辑", R.color.blueColor, 16);
                } else {
                    setRightTextContent("发送", R.color.blueColor, 16);
                }
                break;
        }

        switch (parameter.getOpenType()) {
            case 扫描我的e卡:
            case 我的e卡:
                setCenterTitleContent(TextUtils.isEmpty(bean.getInformation().getCard_label()) ? "e卡" : bean.getInformation().getCard_label() + "详情");
                break;
            case 扫描我的联系人:
            case 我的联系人:
            case 扫描新的联系人:
            case 新的联系人:
                setCenterTitleContent("联系人详情");
                break;
        }
        items.clear();
        Item e = new Item();
        e.setType(0);
        items.add(e);
        headerItem.avatar = bean.getInformation().getAvatar();
        headerItem.name = bean.getInformation().getName();
        headerItem.company = bean.getInformation().getCompany();
        headerItem.position = bean.getInformation().getPosition();
        if (bean.getPhones() != null)
            for (final Phone phone : bean.getPhones()) {
                Item item = new Item();
                item.setType(1);
                item.label = phone.getType();
                item.value = phone.getPhone();
                item.listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new PopupSelectorDialog(THIS, new String[]{"呼叫", "发短信"}, new PopupSelectorDialog.OnClickListener() {
                            @Override
                            public void onSure(int position, CharSequence text) {
                                switch (position) {
                                    case 0:
                                        call(phone.getPhone());
                                        break;
                                    case 1:
                                        sendSMS(phone.getPhone());
                                        break;
                                }
                            }
                        }).show();
                    }
                };
                items.add(item);
            }
        List<Address> addresses = bean.getAddresses();
        if (addresses != null && addresses.size() > 0) {
            Item item = new Item();
            item.setType(2);
            item.label = "地址";
            items.add(item);
            for (Address address : addresses) {
                item = new Item();
                item.setType(6);
                item.address = address;
                item.label = address.getType();
                item.value = address.toString();
                items.add(item);
            }
        }
        List<Email> emails = bean.getEmails();
        if (emails != null && emails.size() > 0) {
            Item item = new Item();
            item.setType(2);
            item.label = "邮箱";
            items.add(item);
            for (Email email : emails) {
                item = new Item();
                item.setType(1);
                item.label = email.getType();
                item.value = email.getEmail();
                items.add(item);
            }
        }
        List<Day> days = bean.getDays();
        if (days != null && days.size() > 0) {
            Item item = new Item();
            item.setType(2);
            item.label = "日期";
            items.add(item);
            for (Day day : days) {
                item = new Item();
                item.setType(1);
                item.label = day.getType();
                item.value = new SimpleDateFormat("yyyy-MM-dd").format(day.getDate());
                items.add(item);
            }
        }
        List<Url> urls = bean.getUrls();
        if (urls != null && urls.size() > 0) {
            Item item = new Item();
            item.setType(2);
            item.label = "主页";
            items.add(item);
            for (Url url : urls) {
                item = new Item();
                item.setType(1);
                item.label = url.getType();
                item.value = url.getUrl();
                items.add(item);
            }
        }
        List<Custom> customs = bean.getCustoms();
        if (customs != null && customs.size() > 0) {
            Item item = new Item();
            item.setType(2);
            item.label = "自定义";
            items.add(item);
            for (Custom custom : customs) {
                item = new Item();
                item.setType(1);
                item.label = custom.getType();
                item.value = custom.getCustom();
                items.add(item);
            }
        }

        if (!TextUtils.isEmpty(bean.getInformation().getNote())) {
            Item item = new Item();
            item.setType(2);
            items.add(item);
            item = new Item();
            item.setType(1);
            item.label = "备注";
            item.value = bean.getInformation().getNote();
            items.add(item);
        }


        // 显示public按钮
        if (bean.getInformation().getType() == 0 && 我的e卡.equals(parameter.getOpenType())) {
            Item item = new Item();
            item.setType(3);
            items.add(item);
        }

        // 显示添加按钮
        if (
                (OpenType.扫描新的联系人.equals(parameter.getOpenType())
                        || OpenType.新的联系人.equals(parameter.getOpenType())
                        || !App.getCurrentUser().getUid().equals(bean.getInformation().getUser_id())
                ) && !App.getInstance().getContactsCardIdList().contains(bean.getInformation().getCard_id())) {
            Item item = new Item();
            item.setType(4);
            items.add(item);
        } else if (bean.getInformation().getType() == 1) { // 显示删除按钮
            Item item = new Item();
            item.setType(5);
            items.add(item);
        }

        adapter.notifyDataSetChanged();
    }

    // =============================================================================================
    // inner class
    // =============================================================================================

    public class HeaderItem implements Holdable {

        private static final long serialVersionUID = 1656304168107221496L;

        public String avatar;
        public String name;
        public String position;
        public String company;
        public String qrCode;

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            ImageView avatarView = holder.findViewById(R.id.item_avatar);
            String url = App.oss().getURL(bean.getInformation().getCard_id());
            Glide.with(holder.getContext())
                    .load(url)
                    .placeholder(R.mipmap.default_avatar_icon)
                    .signature(new StringSignature(String.valueOf(bean.getInformation().getDateUpdated() == null ? System.currentTimeMillis() : bean.getInformation().getDateUpdated().getTime())))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .transform(new RoundTransformation(THIS))
                    .into(avatarView);
            holder.setText(R.id.item_name, name);
            holder.setText(R.id.item_position, position);
            holder.setText(R.id.item_company, company);
            holder.findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectorDialog.show();
                }
            });
            holder.findViewById(R.id.qrcode_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new QRCodeDialog(THIS, bean, RegularToQR.createQRCardStr(bean));
                }
            });
            switch (parameter.getOpenType()) {
                case 我的e卡:
                case 扫描我的e卡:
                    holder.findViewById(R.id.send_button).setVisibility(View.VISIBLE);
                    holder.findViewById(R.id.qrcode_button).setVisibility(View.VISIBLE);
                    break;
                case 我的联系人:
                case 扫描我的联系人:
                    holder.findViewById(R.id.send_button).setVisibility(View.GONE);
                    holder.findViewById(R.id.qrcode_button).setVisibility(View.GONE);
                    break;
                case 新的联系人:
                case 扫描新的联系人:
                    holder.findViewById(R.id.send_button).setVisibility(View.GONE);
                    holder.findViewById(R.id.qrcode_button).setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void onClicked(View v) {

        }
    }

    public class Item implements Holdable, Gradable {

        private static final long serialVersionUID = -9027542147235968616L;

        public String label;
        public String value;
        private int type;
        public Address address;
        private View.OnClickListener listener;

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            switch (type) {
                case 0:
                    headerItem.holding(holder);
                    break;
                case 1:
                    holder.setText(R.id.item_label, label);
                    TextView itemText = holder.findViewById(R.id.item_value);
                    if (value.length() > 22) itemText.setTextSize(10);
                    else if (value.length() > 18) itemText.setTextSize(12);
                    else if (value.length() > 14) itemText.setTextSize(14);
                    else itemText.setTextSize(16);
                    itemText.setText(value);
                    if (listener != null)
                        holder.setOnItemClickListener(listener);
                    break;
                case 2:
                    ImageView labelIcon = holder.findViewById(R.id.label_icon);
                    holder.setText(R.id.label_text, label);
                    break;
                case 3:
                    final SelectorButton publicButton = holder.findViewById(R.id.public_button);
                    publicButton.setSelected(bean.getInformation().isPrivacy());
                    publicButton.setOnSelectorListener(new SelectorButton.OnSelectorListener() {
                        @Override
                        public void onSelected(boolean selected, View view) {
                            LoadingDialog.show(THIS);
                            bean.getInformation().setPrivacy(!bean.getInformation().isPrivacy());
                            publicButton.setSelected(bean.getInformation().isPrivacy());
                            APIProvider.get(MineCardAPI.class).update(bean.toModel(), new SyncReference.CompletionListener() {
                                @Override
                                public void onComplete(SyncError syncError, SyncReference syncReference) {
                                    LoadingDialog.hide();
                                    if (syncError == null) TOAST("设置成功！");
                                    else {
                                        TOAST("操作失败！");
                                        publicButton.setSelected(!bean.getInformation().isPrivacy());
                                    }
                                }
                            });
                        }
                    });
                    break;
                case 5:
                    holder.setOnItemClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ConfirmDialog.Builder().setMessage("确定删除该联系人吗？").setRightButton(R.color.light_red, new OnClickListener() {
                                @Override
                                public boolean onClick(View v) {
                                    LoadingDialog.show(THIS);
                                    APIProvider.get(ContactAPI.class).delete(bean.getInformation().getCard_id(), new SyncReference.CompletionListener() {
                                        @Override
                                        public void onComplete(SyncError syncError, SyncReference syncReference) {
                                            LoadingDialog.hide();
                                            if (syncError != null) {
                                                logger.e("syncError : " + syncError);
                                                TOAST("操作失败");
                                            } else {
                                                TOAST("删除成功");
                                                finish();
                                            }
                                        }
                                    });
                                    return true;
                                }
                            }).build().show(THIS);
                        }
                    });
                    break;
                case 4:
                    holder.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LoadingDialog.show(THIS);
                            bean.getInformation().getMine_id().add(parameter.getMineID());
                            APIProvider.get(ContactAPI.class).save(bean.toModel(), new SyncReference.CompletionListener() {
                                @Override
                                public void onComplete(SyncError syncError, SyncReference syncReference) {
                                    LoadingDialog.hide();
                                    if (syncError != null) {
                                        logger.e("syncError : " + syncError.getMessage());
                                        TOAST("操作失败！");
                                    } else {
                                        convertData();
                                        new ConfirmDialog.Builder().setMessage("给对方会发一张卡片吗？").setRightButton(new OnClickListener() {
                                            @Override
                                            public boolean onClick(View v) {
                                                if (App.getInstance().getMineCards().size() > 0)
                                                    new ChoiceCardDialog(THIS, new ChoiceCardDialog.OnChoiceListener() {
                                                        @Override
                                                        public void onChoice(CardBean target) {
                                                            APIProvider.get(MessageAPI.class).sendExchangeMessage(target, bean.getInformation().getCard_id(), bean.getInformation().getUser_id(), new SyncReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(SyncError syncError, SyncReference syncReference) {
                                                                    if (syncError != null)
                                                                        logger.e("syncError : " + syncError.getMessage());
                                                                    else TOAST("发送成功！");
                                                                }
                                                            });
                                                        }
                                                    }).show();
                                                else TOAST("你还未创建任何名片");
                                                return true;
                                            }
                                        }).build().show(THIS);
                                    }
                                }
                            });
                        }
                    });
                    break;
                case 6:
                    holder.findViewById(R.id.middle_short_line).setVisibility(View.GONE);
                    holder.findViewById(R.id.middle_long_line).setVisibility(View.VISIBLE);
                    holder.findViewById(R.id.short_line).setVisibility(View.GONE);
                    holder.findViewById(R.id.long_line).setVisibility(View.VISIBLE);
                    holder.findViewById(R.id.remove_button).setVisibility(View.GONE);

                    TextView labelText = holder.findViewById(R.id.label_text);
                    labelText.setPadding(App.getInstance().dip2px(20), 0, 0, 0);
                    labelText.setText(label);
                    labelText.setTextColor(THIS.getResources().getColor(R.color._527397));
                    TextView addressSelectText = holder.findViewById(R.id.address_select_button);
                    addressSelectText.setText(address.county());
                    EditText addressDetailText = holder.findViewById(R.id.item_value_input_view);
                    addressDetailText.setPadding(0, 0, 0, 0);
                    addressDetailText.setTextSize(14);
                    addressDetailText.setEnabled(false);
                    addressDetailText.setFocusable(false);
                    addressDetailText.setFocusableInTouchMode(false);
                    addressDetailText.setText(address.getAddress());
                    break;
            }
        }

        @Override
        public void onClicked(View v) {

        }

        @Override
        public void setType(int type) {
            this.type = type;
        }

        @Override
        public int getType() {
            return this.type;
        }
    }
}
