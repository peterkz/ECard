package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.CardAllAPI;
import com.wetoop.ecard.api.ContactAPI;
import com.wetoop.ecard.api.InComingAPI;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Income;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.Holdable;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.RoundTransformation;
import com.wetoop.ecard.ui.dialog.ConfirmDialog;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wetoop.ecard.ui.dialog.OnClickListener;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.wilddogauth.model.WilddogUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Gradable;
import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.widget.SwipeRefreshLayout;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * Created by User on 2017/10/17.
 */
@Slug(layout = R.layout.activity_new_contacts)
public class NewContactsActivity extends TitleBarActivity implements Standardize, SwipeRefreshLayout.OnRefreshListener {
    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    @FindView(R.id.empty_layout)
    private LinearLayout emptyLayout;
    private HeaderItem headerItem = new HeaderItem();
    private Item item = new Item();
    private List<Item> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private WilddogUser wilddogUser;
    private int[] layoutIds = {
            R.layout.item_new_contact_header,
            R.layout.item_new_contact,
            R.layout.item_label
    };

    @Override
    public void onStart() {
        super.onStart();
        App.getInstance().setUnreadExchangeMessageCount(0);
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        LoadingDialog.show(THIS);
        getNewContactsData();
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
    }

    @Override
    public void initView() {
        wilddogUser = App.getWilddogAuth().getCurrentUser();
        setCenterTitleContent("新的联系人");
    }

    @Override
    public void setListeners() {
        refreshLayout.setOnRefreshListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    private void getNewContactsData() {
        items.clear();
        Item e0 = new Item();
        e0.setType(2);
        items.add(e0);
        Item e1 = new Item();
        e1.setType(0);
        items.add(e1);
        Item e2 = new Item();
        e2.setType(2);
        items.add(e2);
        APIProvider.get(InComingAPI.class).list(new OnESubscriber<List<Income>>() {
            @Override
            protected void onComplete(boolean success, List<Income> o, Throwable e) {
                refreshLayout.stopRefresh();
                if (success) {
                    if (o != null && o.size() > 0) {
                        for (int i = 0; i < o.size(); i++) {
                            Item item = new Item();
                            item.setType(1);
                            item.name = o.get(i).getName();
                            item.content = o.get(i).getContent();
                            item.cardId = o.get(i).getCard_id();
                            item.mineId = o.get(i).getMine_id();
                            item.dateUpdated = o.get(i).getDate_created() == null ? new Date() : o.get(i).getDate_created();
                            if (App.getInstance().getContactsCardIdList().contains(o.get(i).getCard_id())) {
                                item.cardType = "已添加";
                            } else {
                                item.cardType = "保存";
                            }
                            items.add(item);
                        }
                        adapter.notifyDataSetChanged();
                        emptyLayout.setVisibility(View.GONE);
                    } else {
                        emptyLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void newContactsListData(ArrayList<CardBean> list) {

    }

    @Override
    public void onRefresh() {
        getNewContactsData();
    }

    public class HeaderItem implements Holdable {

        private static final long serialVersionUID = -8433423143789025188L;

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            holder.findViewById(R.id.rsRecords).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(RecordsActivity.class);
                }
            });
        }

        @Override
        public void onClicked(View v) {

        }
    }

    public class Item implements Holdable, Gradable {

        private static final long serialVersionUID = -8433423143789025188L;

        public String cardId;
        public String mineId;
        public String name;
        public String content;
        public String avatar;
        private int type;
        public String cardType;
        public int cardState;
        public Date dateUpdated;

        @Override
        public void holding(final RecyclerViewAdapter.ViewHolder holder) {
            switch (type) {
                case 0:
                    headerItem.holding(holder);
                    break;
                case 1:
                    holder.findViewById(R.id.saveECard).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ConfirmDialog.Builder().setMessage("确定保存该联系人吗？").setRightButton(new OnClickListener() {
                                @Override
                                public boolean onClick(View v) {
                                    LoadingDialog.show(THIS);
                                    APIProvider.get(CardAllAPI.class).singleGet(cardId, new OnESubscriber<Card>() {
                                        @Override
                                        protected void onComplete(boolean success, Card o, Throwable e) {
                                            if (success && o != null) {
                                                o.getInformation().get(0).getMine_id().add(mineId);
                                                APIProvider.get(ContactAPI.class).save(o, new SyncReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(SyncError syncError, SyncReference syncReference) {
                                                        if (syncError != null) {
                                                            Log.e("NewContactsActivity", "syncError : " + syncError.getMessage());
                                                        } else {
                                                            TOAST("保存成功");
                                                            holder.findViewById(R.id.saveECard).setBackgroundResource(R.color.white);
                                                            holder.setText(R.id.saveECard, "已添加");
                                                            holder.findViewById(R.id.saveECard).setOnClickListener(null);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    return true;
                                }
                            }).build().show(THIS);
                        }
                    });
                    ImageView avatarView = holder.findViewById(R.id.eCardPhoto);
                    holder.setText(R.id.contactsName, name);
                    holder.setText(R.id.contactsContent, content);
                    Glide.with(holder.getContext())
                            .load(App.oss().getURL(cardId))
                            .placeholder(R.mipmap.default_avatar_icon)
                            .signature(new StringSignature(String.valueOf(dateUpdated.getTime())))
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .transform(new RoundTransformation(THIS))
                            .into(avatarView);
                    if ("已添加".equals(cardType)) {
                        holder.findViewById(R.id.saveECard).setBackgroundResource(R.color.white);
                        holder.setText(R.id.saveECard, cardType);
                        holder.findViewById(R.id.saveECard).setOnClickListener(null);
                    } else {
                        holder.findViewById(R.id.saveECard).setBackgroundResource(R.drawable.save_card_background);
                        holder.setText(R.id.saveECard, cardType);
                    }
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
            return type;
        }
    }
}
