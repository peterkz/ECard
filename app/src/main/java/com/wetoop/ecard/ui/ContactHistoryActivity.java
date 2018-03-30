package com.wetoop.ecard.ui;

import android.content.Context;
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
import com.wetoop.ecard.api.ContactUpdateItemAPI;
import com.wetoop.ecard.api.model.ContactUpdateItem;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.Holdable;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.RoundTransformation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.widget.SwipeRefreshLayout;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * @author Parck.
 * @date 2017/10/24.
 * @desc 联系人历史更新记录
 */
@Slug(layout = R.layout.activity_contact_history)
public class ContactHistoryActivity extends TitleBarActivity implements Standardize, SwipeRefreshLayout.OnRefreshListener {

    @FindView(R.id.swipe_refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    private ArrayList<ItemBean> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private CardBean card;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        if (map != null)
            card = (CardBean) map.get("card");
        else finish();
        flushData();
        adapter = new RecyclerViewAdapter<ItemBean>(THIS, new int[]{R.layout.item_history_header, R.layout.item_update_history}, items) {

            @Override
            public int getItemViewType(int position) {
                return position == 0 ? 0 : 1;
            }

            @Override
            protected void binding(ViewHolder holder, ItemBean data, int position) {
                switch (getItemViewType(position)) {
                    case 0:
                        holder.setText(R.id.name_text, card.getInformation().getName());
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
                        data.holding(THIS, holder);
                        break;
                }
            }
        };
    }

    private void flushData() {
        APIProvider.get(ContactUpdateItemAPI.class).list(card.getInformation().getUser_id(), card.getInformation().getCard_id(), new OnESubscriber<Map<String, List<ContactUpdateItem>>>() {
            @Override
            protected void onComplete(boolean success, Map<String, List<ContactUpdateItem>> o, Throwable e) {
                if (success && o != null) {
                    ItemBean.initItems(items, o);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void initView() {
        setCenterTitleContent("历史记录");
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    @Override
    public void onRefresh() {
        refreshLayout.stopRefresh();
    }

    // =============================================================================================
    // inner class
    // =============================================================================================

    public static class ItemBean implements Holdable {

        private static final long serialVersionUID = 4902070025952860565L;

        public Context context;
        public String month;
        public List<ItemDetail> detailItems;

        public static void initItems(List<ItemBean> items, Map<String, List<ContactUpdateItem>> records) {
            if (records != null) {
                ItemBean bean = new ItemBean();
                items.add(bean);
                for (String key : records.keySet()) {
                    bean = new ItemBean();
                    bean.month = key;
                    List<ItemDetail> detailItems = new ArrayList<>();
                    for (ContactUpdateItem item : records.get(key)) {
                        ItemDetail detail = new ItemDetail();
                        detail.action = item.getAction();
                        detail.old = item.getOld();
                        detail.date = new SimpleDateFormat("MM-dd").format(item.getDate_created());
                        detail.content = item.getContent();
                        detailItems.add(detail);
                    }
                    bean.detailItems = detailItems;
                    items.add(bean);
                }
            }
        }

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            TextView monthText = holder.findViewById(R.id.month_text);
            if (month != null) {
                monthText.setText(month);
            } else {
                monthText.setText("");
            }

            VerticalRecyclerView recyclerView = holder.findViewById(R.id.recycle_view);
            if (detailItems != null && detailItems.size() > 0) {
                recyclerView.setAdapter(new RecyclerViewAdapter<ItemDetail>(context, R.layout.item_history_detail, detailItems) {
                    @Override
                    protected void binding(ViewHolder holder, ItemDetail data, int position) {
                        data.holding(this, holder);
                    }
                });
            }
        }

        public void holding(Context context, RecyclerViewAdapter.ViewHolder holder) {
            this.context = context;
            holding(holder);
        }

        @Override
        public void onClicked(View v) {

        }

        public static class ItemDetail {

            private static final long serialVersionUID = 5444930090408219676L;
            public String action;
            public String old;
            public String content;
            public String date;
            public boolean hideLine;

            public void holding(RecyclerViewAdapter adapter, RecyclerViewAdapter.ViewHolder holder) {
                ImageView itemIcon = holder.findViewById(R.id.item_icon);
                if (adapter.getItemCount() == 1) {
                    itemIcon.setPadding(0, App.getInstance().dip2px(15), 0, App.getInstance().dip2px(15));
                } else {
                    if (holder.getPosition() == 0) {
                        itemIcon.setPadding(0, App.getInstance().dip2px(15), 0, 0);
                    }
                    if (holder.getPosition() == adapter.getItemCount() - 1) {
                        itemIcon.setPadding(0, 0, 0, App.getInstance().dip2px(15));
                    }
                }
                TextView oldText = holder.findViewById(R.id.old_text);
                if (old != null) {
                    oldText.setText(old);
                } else {
                    oldText.setVisibility(View.GONE);
                }

                TextView typeText = holder.findViewById(R.id.type_text);
                typeText.setText(action);

                TextView detailText = holder.findViewById(R.id.detail_text);
                if (content != null) {
                    detailText.setText(content);
                } else {
                    detailText.setText("");
                }

                TextView dateText = holder.findViewById(R.id.date_text);
                if (date != null) {
                    dateText.setText(date);
                } else {
                    dateText.setText("");
                }

                View line = holder.findViewById(R.id.bottom_line);
                if (hideLine) {
                    line.setVisibility(View.GONE);
                } else {
                    line.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
