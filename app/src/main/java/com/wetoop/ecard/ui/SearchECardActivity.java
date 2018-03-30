package com.wetoop.ecard.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.SearchPhoneAPI;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.ContactItemBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.InputMethodTool;
import com.wetoop.ecard.ui.dialog.AreaSelectorDialog;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wetoop.ecard.ui.dialog.PopupSelectorDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.BaseActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.widget.SwipeRefreshLayout;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;
import cn.edots.slug.core.TextSlugger;
import cn.edots.slug.listener.OnTextWatcher;

/**
 * @author Parck.
 * @date 2017/10/17.
 * @desc
 */
@Slug(layout = R.layout.activity_search_ecard)
public class SearchECardActivity extends BaseActivity implements Standardize, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.search_text)
    private EditSlugger searchSlugger;
    @FindView(R.id.clear_button)
    private ImageView clearButton;
    @FindView(R.id.cancel_text)
    private TextView searchButton;
    @BindView(R.id.gender_text)
    private TextSlugger genderText;
    @BindView(R.id.number_text)
    private TextSlugger numberText;
    @BindView(R.id.viewable_text)
    private TextSlugger viewableText;
    @FindView(R.id.swipe_refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    @FindView(R.id.card_recycler_view)
    private VerticalRecyclerView cardRecyclerView;

    private AreaSelectorDialog areaSelectorDialog;
    private PopupSelectorDialog genderSelectorDialog;
    private PopupSelectorDialog viewableSelectorDialog;
    private List<ItemBean> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        areaSelectorDialog = new AreaSelectorDialog(THIS, false, new AreaSelectorDialog.OnClickListener() {
            @Override
            public void onSure(AreaSelectorDialog.AreaDetail detail) {
                String text = detail.toString();
                if (text.length() > 10) {
                    numberText.getView().setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                } else if (text.length() > 12) {
                    numberText.getView().setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                }
                numberText.setText(text);
            }
        });

        genderSelectorDialog = new PopupSelectorDialog(THIS, new String[]{"男", "女", "不限"}, new PopupSelectorDialog.OnClickListener() {
            @Override
            public void onSure(int position, CharSequence text) {
                genderText.setText(text.toString());
            }
        });

        viewableSelectorDialog = new PopupSelectorDialog(THIS, new String[]{"不限", "可查看", "不可查看"}, new PopupSelectorDialog.OnClickListener() {
            @Override
            public void onSure(int position, CharSequence text) {
                viewableText.setText(text.toString());
            }
        });

        adapter = new RecyclerViewAdapter<ItemBean>(THIS, new int[]{R.layout.item_text, R.layout.item_linkman}, items) {

            @Override
            public int getItemViewType(int position) {
                return items.get(position).getType();
            }

            @Override
            protected void binding(ViewHolder holder, ItemBean data, int position) {
                switch (getItemViewType(position)) {
                    case 0:
                        holder.setText(R.id.item_label, data.getName());
                        break;
                    case 1:
                        data.holding(holder);
                        break;
                }
            }
        };
    }

    @Override
    public void initView() {
        refreshLayout.setVisibility(View.GONE);
    }

    @Override
    public void setListeners() {
        cardRecyclerView.setAdapter(adapter);
        searchSlugger.getView().addTextChangedListener(new OnTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    searchButton.setText("搜索");
                    clearButton.setVisibility(View.VISIBLE);
                } else {
                    searchButton.setText("取消");
                    clearButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onCreateLast() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchSlugger.getView(), InputMethodManager.SHOW_FORCED);
    }

    @ClickView({R.id.clear_button,
            R.id.cancel_text,
            R.id.gender_layout_button,
            R.id.munber_layout_button,
            R.id.viewable_layout_button})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.clear_button:
                searchSlugger.getView().setText("");
                items.clear();
                adapter.notifyDataSetChanged();
                refreshLayout.setVisibility(View.GONE);
                break;
            case R.id.cancel_text:
                if ("取消".equals(searchButton.getText())) {
                    finish();
                    InputMethodTool.cancelInput(searchSlugger.getView());
                } else search();
                break;
            case R.id.gender_layout_button:
                genderSelectorDialog.show();
                break;
            case R.id.munber_layout_button:
                areaSelectorDialog.show();
                break;
            case R.id.viewable_layout_button:
                viewableSelectorDialog.show();
                break;
        }
    }

    private void search() {
        LoadingDialog.show(THIS);
        flushData();
        refreshLayout.setVisibility(View.VISIBLE);
        searchSlugger.clearFocus(THIS);
    }

    private void flushData() {
        APIProvider.get(SearchPhoneAPI.class).search(searchSlugger.getText().trim(), new OnESubscriber<List<Card>>() {
            @Override
            protected void onComplete(boolean success, List<Card> o, Throwable e) {
                refreshLayout.stopRefresh();
                if (success) {
                    items.clear();
                    final List<ItemBean> list = getItemsByCards(o);
                    if (list != null && list.size() > 0) {
                        items.addAll(list);
                    }
                    if (items.size() < 1) {
                        TOAST("没有匹配数据");
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter.notifyDataSetChanged();
                        refreshLayout.stopRefresh();
                    }
                }
            }
        });
    }

    private List<ItemBean> getItemsByCards(final List<Card> cards) {
        final List<ItemBean> beans = new ArrayList<>();
        if (cards == null) return null;
        beans.clear();
        for (Card card : cards) {
            CardBean bean = CardBean.fromModel(card);
            if (bean != null && bean.getInformation() != null &&
                    bean.getInformation().getName() != null) {
                ItemBean item = new ItemBean();
                item.setName(bean.getInformation().getName());
                item.setType(1);
                item.setAvatar(bean.getInformation().getAvatar());
                item.setCard(bean);
                item.setCardId(bean.getInformation().getCard_id());
                item.setDateUpdated(bean.getInformation().getDateUpdated());
                beans.add(item);
            }
        }
        return beans;
    }

    @Override
    public void onRefresh() {
        flushData();
    }

    // =============================================================================================
    // inner class
    // =============================================================================================

    public class ItemBean extends ContactItemBean {

        private static final long serialVersionUID = -4832010861240267333L;
        private CardBean bean;

        public CardBean getCard() {
            return bean;
        }

        public void setCard(CardBean bean) {
            this.bean = bean;
        }

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            super.holding(holder);
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ECardDetailActivity.OpenParameter parameter = new ECardDetailActivity.OpenParameter();
                    parameter.setCardID(bean.getInformation().getCard_id());
                    String cardId = bean.getInformation().getCard_id();
                    App app = App.getInstance();
                    boolean myCardIdEqual = false;
                    boolean contactsCardIdEqual = false;
                    if (cardId != null) {
                        for (int i = 0; i < app.getMineCards().size(); i++) {
                            if (cardId.equals(app.getMineCards().get(i).getInformation().getCard_id())) {
                                myCardIdEqual = true;
                            }
                        }
                        for (int i = 0; i < app.getContactsCardIdList().size(); i++) {
                            if (cardId.equals(app.getContactsCardIdList().get(i))) {
                                contactsCardIdEqual = true;
                            }
                        }
                    }
                    final boolean finalMyCardIdEqual = myCardIdEqual;
                    final boolean finalContactsCardIdEqual = contactsCardIdEqual;
                    if (!finalMyCardIdEqual && !finalContactsCardIdEqual) {
                        parameter.setOpenType(ECardDetailActivity.OpenType.新的联系人);
                    } else {
                        if (finalMyCardIdEqual) {
                            parameter.setOpenType(ECardDetailActivity.OpenType.我的e卡);
                        } else {
                            parameter.setOpenType(ECardDetailActivity.OpenType.我的联系人);
                        }
                    }
                    ECardDetailActivity.startActivity(THIS, parameter);
                    InputMethodTool.cancelInput(v);
                }
            });
        }
    }
}