package com.wetoop.ecard.ui.fragment;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.OutGoingAPI;
import com.wetoop.ecard.api.model.Income;
import com.wetoop.ecard.bean.ContactBean;
import com.wetoop.ecard.bean.Holdable;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.RoundTransformation;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wilddog.wilddogauth.model.WilddogUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.fragment.BaseFragment;
import cn.edots.nest.ui.widget.SwipeRefreshLayout;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * Created by User on 2017/10/17.
 */
@Slug(layout = R.layout.fragment_records_sends)
public class RecordsSendFragment extends BaseFragment implements Standardize, SwipeRefreshLayout.OnRefreshListener {
    @FindView(R.id.refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.empty_layout)
    private LinearLayout emptyLayout;
    private WilddogUser wilddogUser;
    private RecyclerViewAdapter adapter;
    private List<Item> items = new ArrayList<>();

    private void getSendsData() {
        APIProvider.get(OutGoingAPI.class).list(new OnESubscriber<List<Income>>() {
            @Override
            protected void onComplete(boolean success, List<Income> o, Throwable e) {
                refreshLayout.stopRefresh();
                items.clear();
                if (success) {
                    if (o != null && o.size() > 0) {
                        for (int i = 0; i < o.size(); i++) {
                            Item item = new Item();
                            item.name = o.get(i).getName();
                            item.content = o.get(i).getContent();
                            item.cardId = o.get(i).getCard_id();
                            item.dateUpdated = o.get(i).getDate_created();
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

    private void sendsListData(ArrayList<ContactBean> list) {

    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        adapter = new RecyclerViewAdapter<Item>(THIS.getActivity(), R.layout.item_new_contact, items) {
            @Override
            protected void binding(ViewHolder holder, Item o, int i) {
                o.holding(holder);
            }
        };
        LoadingDialog.show(THIS.getActivity());
        getSendsData();
    }

    @Override
    public void initView() {

    }

    @Override
    public void setListeners() {
        refreshLayout.setOnRefreshListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    @Override
    public void onRefresh() {
        getSendsData();
    }

    public class Item implements Holdable {

        private static final long serialVersionUID = 1884911163234541753L;

        public String cardId;
        public String name;
        public String content;
        public String avatar;
        public int cardType;
        public int cardState;
        public Date dateUpdated;

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            holder.setText(R.id.contactsName, name);
            holder.setText(R.id.contactsContent, content);
            holder.setText(R.id.saveECard, "已发送");
            ImageView avatarView = holder.findViewById(R.id.eCardPhoto);
            Glide.with(holder.getContext())
                    .load(App.oss().getURL(cardId))
                    .placeholder(R.mipmap.default_avatar_icon)
                    .transform(new RoundTransformation(holder.getContext()))
                    .signature(new StringSignature(String.valueOf(dateUpdated.getTime())))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(avatarView);
            holder.findViewById(R.id.saveECard).setBackgroundResource(R.color.white);
        }

        @Override
        public void onClicked(View v) {

        }

    }
}
