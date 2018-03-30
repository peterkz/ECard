package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.ContactAPI;
import com.wetoop.ecard.api.ExchangeAPI;
import com.wetoop.ecard.api.MessageAPI;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.RoundTransformation;
import com.wetoop.ecard.ui.dialog.ChoiceCardDialog;
import com.wetoop.ecard.ui.dialog.ConfirmDialog;
import com.wetoop.ecard.ui.dialog.OnClickListener;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;

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
 * @date 2017/11/22.
 * @desc
 */
@Slug(layout = R.layout.activity_exchange)
public class ExchangeCardActivity extends TitleBarActivity implements Standardize {

    public static final String EXCHANGE_CARD_KEY = "EXCHANGE_CARD_KEY";
    @FindView(R.id.refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.empty_layout)
    private LinearLayout emptyLayout;

    private List<CardBean> beans = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private CardBean bean;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        APIProvider.get(ExchangeAPI.class).removeListValueListener();
        APIProvider.get(ExchangeAPI.class).clear(bean.getInformation().getCard_id(), new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError syncError, SyncReference syncReference) {
                if (syncError != null) logger.e("syncError: " + syncError.getMessage());
            }
        });
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        bean = ((ECardDetailActivity) App.getInstance().getActivity()).bean;
        if (bean == null) finish();
        adapter = new RecyclerViewAdapter<CardBean>(THIS, R.layout.item_exchange_linkman, beans) {
            @Override
            protected void binding(ViewHolder holder, final CardBean o, int i) {
                ImageView avatarView = holder.findViewById(R.id.item_image);
                String avatar = o.getInformation().getAvatar();
                Glide.with(holder.getContext())
                        .load(App.oss().getURL(o.getInformation().getCard_id()))
                        .placeholder(R.mipmap.default_avatar_icon)
                        .transform(new RoundTransformation(THIS))
                        .signature(new StringSignature(String.valueOf(o.getInformation().getDateUpdated() == null ? System.currentTimeMillis() : o.getInformation().getDateUpdated().getTime())))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(avatarView);
                TextView itemText = holder.findViewById(R.id.item_name);
                itemText.setText(o.getInformation().getName());
                TextView exchangeButton = holder.findViewById(R.id.exchange_button);
                if (App.getInstance().getContactsCardIdList().contains(o.getInformation().getCard_id())) {
                    exchangeButton.setText("已添加");
                    exchangeButton.setOnClickListener(null);
                    exchangeButton.setBackground(null);
                    exchangeButton.setTextColor(THIS.getResources().getColor(R.color.grey));
                } else {
                    exchangeButton.setText("添加");
                    exchangeButton.setBackgroundResource(R.drawable.shape_blue_solid_r2_bg);
                    exchangeButton.setTextColor(THIS.getResources().getColor(R.color.white));
                    exchangeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            v.setClickable(false);
                            v.setFocusable(false);
                            v.setEnabled(false);
                            v.setFocusableInTouchMode(false);
                            o.getInformation().getMine_id().add(bean.getInformation().getCard_id());
                            APIProvider.get(ContactAPI.class).save(o.toModel(), new SyncReference.CompletionListener() {
                                @Override
                                public void onComplete(SyncError syncError, SyncReference syncReference) {
                                    if (syncError != null) {
                                        logger.e("syncError : " + syncError.getMessage());
                                        v.setClickable(true);
                                        v.setFocusable(true);
                                        v.setEnabled(true);
                                        v.setFocusableInTouchMode(true);
                                    } else {
                                        flushData();
                                        new ConfirmDialog.Builder().setMessage("添加成功，确定给对方回发一张名片吗？").setRightButton(new OnClickListener() {
                                            @Override
                                            public boolean onClick(View v) {
                                                new ChoiceCardDialog(THIS, new ChoiceCardDialog.OnChoiceListener() {
                                                    @Override
                                                    public void onChoice(CardBean target) {
                                                        APIProvider.get(MessageAPI.class).sendExchangeMessage(bean, bean.getInformation().getCard_id(), o.getInformation().getUser_id(), new SyncReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(SyncError syncError, SyncReference syncReference) {
                                                                if (syncError != null) {
                                                                    logger.e("syncError: " + syncError.getMessage());
                                                                    TOAST("操作失败");
                                                                } else TOAST("发送成功！");
                                                            }
                                                        });
                                                    }
                                                }).show();
                                                return true;
                                            }
                                        }).build().show(THIS);
                                    }
                                }
                            });
                        }
                    });
                }
                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ECardDetailActivity.OpenParameter parameter = new ECardDetailActivity.OpenParameter();
                        parameter.setCardID(o.getInformation().getCard_id());
                        parameter.setMineID(bean.getInformation().getCard_id());
                        parameter.setOpenType(ECardDetailActivity.OpenType.新的联系人);
                        ECardDetailActivity.startActivity(THIS, parameter);
                    }
                });
            }
        };

        flushData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void initView() {
        setCenterTitleContent("名片交换");
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    private void flushData() {
        APIProvider.get(ExchangeAPI.class).list(new OnESubscriber<List<Card>>() {
            @Override
            protected void onComplete(boolean success, List<Card> o, Throwable e) {
                if (success) {
                    beans.clear();
                    if (o != null) {
                        for (Card card : o) {
                            Information information = card.getInformation().get(0);
                            if (information != null && !App.getCurrentUser().getUid().equals(information.getUser_id()))
                                beans.add(CardBean.fromModel(card));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                if (beans.size() == 0) {
                    emptyLayout.setVisibility(View.VISIBLE);
                } else {
                    emptyLayout.setVisibility(View.GONE);
                }
            }
        });
    }
}
