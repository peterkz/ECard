package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.ContactAPI;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.ContactItemBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.ui.dialog.LoadingDialog;

import java.util.ArrayList;
import java.util.HashMap;
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
 * @desc 联系人更新列表
 */
@Slug(layout = R.layout.activity_contact_update)
public class ContactUpdateActivity extends TitleBarActivity implements Standardize, SwipeRefreshLayout.OnRefreshListener {

    @FindView(R.id.swipe_refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.empty_layout)
    private LinearLayout emptyLayout;
    private RecyclerViewAdapter adapter;
    private ArrayList<ItemBean> items = new ArrayList<>();
    private List<CardBean> cards = new ArrayList<>();

    @Override
    public void onStart() {
        super.onStart();
        App.getInstance().setUnreadUpdateMessageCount(0);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        flushData();
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        LoadingDialog.show(THIS);
        flushData();
        adapter = new RecyclerViewAdapter<ItemBean>(THIS, R.layout.item_linkman, items) {
            @Override
            protected void binding(ViewHolder holder, final ItemBean data, final int position) {
                data.holding(holder);
                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, Object> idata = new HashMap<>();
                        idata.put("card", cards.get(position));
                        startActivity(UpdateDetailActivity.class, idata);
                    }
                });
            }
        };
    }

    private void flushData() {
        APIProvider.get(ContactAPI.class).list(new OnESubscriber<List<Card>>() {
            @Override
            protected void onComplete(boolean success, List<Card> cards, Throwable e) {
                refreshLayout.stopRefresh();
                if (success) {
                    if (cards != null) {
                        items.clear();
                        for (Card card : cards) {
                            CardBean bean = CardBean.fromModel(card);
                            if (bean != null && bean.getInformation() != null && bean.getInformation().getType() != 2) {
                                ContactUpdateActivity.this.cards.add(bean);
                                ItemBean item = new ItemBean();
                                item.setName(bean.getInformation().getName());
                                item.setAvatar(bean.getInformation().getAvatar());
                                item.setCardId(bean.getInformation().getCard_id());
                                item.setDateUpdated(bean.getInformation().getDateUpdated());
                                Integer unread = App.getInstance().get(bean.getInformation().getCard_id() + "_MESSAGE_COUNT", Integer.class);
                                if (unread > 0) item.setUnread(String.valueOf(unread));
                                items.add(item);
                            }
                        }

                    }
                    adapter.notifyDataSetChanged();
                }
                if (items.size() > 0) {
                    emptyLayout.setVisibility(View.GONE);
                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void initView() {
        setCenterTitleContent("信息更新");
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
        flushData();
    }

    // =============================================================================================
    // inner class
    // =============================================================================================

    public static class ItemBean extends ContactItemBean {

        private static final long serialVersionUID = -3368908080357125638L;
    }
}
