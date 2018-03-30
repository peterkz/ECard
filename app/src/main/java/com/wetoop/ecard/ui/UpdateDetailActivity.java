package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.ContactUpdateAPI;
import com.wetoop.ecard.api.model.Address;
import com.wetoop.ecard.api.model.ContactUpdate;
import com.wetoop.ecard.api.model.Custom;
import com.wetoop.ecard.api.model.Day;
import com.wetoop.ecard.api.model.Email;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.api.model.Url;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.Holdable;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.RoundTransformation;

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

/**
 * @author Parck.
 * @date 2017/10/26.
 * @desc 信息更新详情
 */
@Slug(layout = R.layout.activity_update_detail)
public class UpdateDetailActivity extends TitleBarActivity implements Standardize {

    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;

    private RecyclerViewAdapter adapter;
    private ArrayList<ItemBean> items = new ArrayList<>();
    private Map<String, Object> intentData;
    private CardBean card;

    @Override
    public void onStart() {
        super.onStart();
        if (card != null)
            App.getInstance().put(card.getInformation().getCard_id() + "_MESSAGE_COUNT", 0);
    }

    @Override
    public void setupData(@Nullable Map<String, Object> intentData) {
        if (intentData != null)
            card = (CardBean) intentData.get("card");
        else finish();
        this.intentData = intentData;
        flushData();
        adapter = new RecyclerViewAdapter<ItemBean>(THIS, new int[]{R.layout.item_detail_layout, R.layout.item_name_value, R.layout.item_occupy_15dp}, items) {

            @Override
            public int getItemViewType(int position) {
                return items.get(position).getType();
            }

            @Override
            protected void binding(ViewHolder holder, ItemBean data, int position) {
                switch (getItemViewType(position)) {
                    case 0:
                        ImageView avatarView = holder.findViewById(R.id.avatar_image);
                        Glide.with(holder.getContext())
                                .load(App.oss().getURL(card.getInformation().getCard_id()))
                                .placeholder(R.mipmap.default_avatar_icon)
                                .transform(new RoundTransformation(THIS))
                                .signature(new StringSignature(String.valueOf(card.getInformation().getDateUpdated() == null ? System.currentTimeMillis() : card.getInformation().getDateUpdated().getTime())))
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .into(avatarView);
                        break;
                    case 1:
                        data.holding(holder);
                        break;
                    case 2:
                        break;
                }
            }
        };
    }

    private void flushData() {
        APIProvider.get(ContactUpdateAPI.class).get(card.getInformation().getUser_id(), card.getInformation().getCard_id(), new OnESubscriber<ContactUpdate>() {
            @Override
            protected void onComplete(boolean success, ContactUpdate o, Throwable e) {
                if (success && o != null) {
                    ItemBean.initItems(items, o, !o.getUuid().equals(App.getInstance().get(card.getInformation().getCard_id(), String.class)));
                    adapter.notifyDataSetChanged();
                    App.getInstance().put(card.getInformation().getCard_id(), o.getUuid());
                }
            }
        });
    }

    @Override
    public void initView() {
        setCenterTitleContent("更新信息");
        setRightTextContent("历史", R.color.blueColor, 16);
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
        setOnRightTextListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ContactHistoryActivity.class, (HashMap<String, Object>) intentData);
            }
        });
    }

    @Override
    public void onCreateLast() {

    }

    // =============================================================================================
    // inner class
    // =============================================================================================

    private static class ItemBean implements Holdable, Gradable {

        private static final long serialVersionUID = 8121350956264485046L;

        public int type;
        public String name;
        public String value;
        public boolean unread;
        public boolean hideLine;

        @Override
        public int getType() {
            return type;
        }

        @Override
        public void setType(int type) {
            this.type = type;
        }

        public static void initItems(List<ItemBean> items, ContactUpdate update, boolean showUnread) {
            ItemBean item = new ItemBean();
            item.setType(0);
            item.value = update.getAvatar().getValue();
            item.unread = update.getAvatar() != null && (update.getAvatar().isUpdate() && showUnread);
            items.add(item);

            item = new ItemBean();
            item.setType(1);
            item.name = "姓名";
            item.value = update.getName().getValue();
            item.unread = update.getName().isUpdate() != null && (update.getName().isUpdate() && showUnread);
            items.add(item);
            item = new ItemBean();
            item.setType(1);
            item.name = "职业";
            item.value = update.getPosition().getValue();
            item.unread = update.getPosition().isUpdate() != null && (update.getPosition().isUpdate() && showUnread);
            items.add(item);
            item = new ItemBean();
            item.setType(1);
            item.name = "公司";
            item.value = update.getCompany().getValue();
            item.unread = update.getCompany().isUpdate() != null && (update.getCompany().isUpdate() && showUnread);
            items.add(item);

            if (update.getPhones() != null && update.getPhones().size() > 0) {
                item = new ItemBean();
                item.setType(2);
                items.add(item);
                boolean hideLine = true;
                for (Phone phone : update.getPhones()) {
                    item = new ItemBean();
                    item.setType(1);
                    item.name = phone.getType();
                    item.value = phone.getPhone();
                    item.unread = phone.isUpdate() != null && (phone.isUpdate() && showUnread);
                    item.hideLine = hideLine;
                    items.add(item);
                    hideLine = false;
                }
            }

            if (update.getEmails() != null && update.getEmails().size() > 0) {
                item = new ItemBean();
                item.setType(2);
                items.add(item);
                boolean hideLine = true;
                for (Email email : update.getEmails()) {
                    item = new ItemBean();
                    item.setType(1);
                    item.name = email.getType();
                    item.value = email.getEmail();
                    item.unread = email.isUpdate() != null && (email.isUpdate() && showUnread);
                    item.hideLine = hideLine;
                    items.add(item);
                    hideLine = false;
                }
            }

            if (update.getUrls() != null && update.getUrls().size() > 0) {
                item = new ItemBean();
                item.setType(2);
                items.add(item);
                boolean hideLine = true;
                for (Url url : update.getUrls()) {
                    item = new ItemBean();
                    item.setType(1);
                    item.name = url.getType();
                    item.value = url.getUrl();
                    item.unread = url.isUpdate() != null && (url.isUpdate() && showUnread);
                    item.hideLine = hideLine;
                    items.add(item);
                    hideLine = false;
                }
            }

            if (update.getAddresses() != null && update.getAddresses().size() > 0) {
                item = new ItemBean();
                item.setType(2);
                items.add(item);
                boolean hideLine = true;
                for (Address address : update.getAddresses()) {
                    item = new ItemBean();
                    item.setType(1);
                    item.name = address.getType();
                    item.value = address.toString();
                    item.unread = address.isUpdate() != null && (address.isUpdate() && showUnread);
                    item.hideLine = hideLine;
                    items.add(item);
                    hideLine = false;
                }
            }

            if (update.getDays() != null && update.getDays().size() > 0) {
                item = new ItemBean();
                item.setType(2);
                items.add(item);
                boolean hideLine = true;
                for (Day day : update.getDays()) {
                    item = new ItemBean();
                    item.setType(1);
                    item.name = day.getType();
                    item.value = new SimpleDateFormat("yyyy-MM-dd").format(day.getDate());
                    item.unread = day.isUpdate() != null && (day.isUpdate() && showUnread);
                    item.hideLine = hideLine;
                    items.add(item);
                    hideLine = false;
                }
            }

            if (update.getCustoms() != null && update.getCustoms().size() > 0) {
                item = new ItemBean();
                item.setType(2);
                items.add(item);
                boolean hideLine = true;
                for (Custom custom : update.getCustoms()) {
                    item = new ItemBean();
                    item.setType(1);
                    item.name = custom.getType();
                    item.value = custom.getCustom();
                    item.unread = custom.isUpdate() != null && (custom.isUpdate() && showUnread);
                    item.hideLine = hideLine;
                    items.add(item);
                    hideLine = false;
                }
            }
        }

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            View line = holder.findViewById(R.id.top_line);
            if (hideLine) {
                line.setVisibility(View.GONE);
            } else {
                line.setVisibility(View.VISIBLE);
            }

            TextView nameText = holder.findViewById(R.id.item_name);
            if (name != null) {
                nameText.setText(name);
            } else {
                nameText.setText("");
            }

            TextView valueText = holder.findViewById(R.id.item_value);
            if (value.length() > 22) valueText.setTextSize(10);
            else if (value.length() > 18) valueText.setTextSize(12);
            else if (value.length() > 14) valueText.setTextSize(14);
            else valueText.setTextSize(16);
            if (value != null) {
                valueText.setText(value);
            } else {
                valueText.setText("");
            }

            View unreadTag = holder.findViewById(R.id.unread_tag);
            if (unread) {
                unreadTag.setVisibility(View.VISIBLE);
            } else {
                unreadTag.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClicked(View v) {

        }
    }
}
