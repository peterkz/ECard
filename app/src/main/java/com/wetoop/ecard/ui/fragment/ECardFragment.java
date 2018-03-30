package com.wetoop.ecard.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.MineCardAPI;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.Holdable;
import com.wetoop.ecard.bean.MainMenuItem;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.InputMethodTool;
import com.wetoop.ecard.tools.PermissionUtil;
import com.wetoop.ecard.tools.RegularToQR;
import com.wetoop.ecard.ui.ECardDetailActivity;
import com.wetoop.ecard.ui.ScanQRActivity;
import com.wetoop.ecard.ui.SettingActivity;
import com.wetoop.ecard.ui.adapter.MainMenuSpinnerAdapter;
import com.wetoop.ecard.ui.adapter.SpinnerPopWindow;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wetoop.ecard.ui.dialog.QRCodeDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.fragment.BaseFragment;
import cn.edots.nest.ui.widget.SwipeRefreshLayout;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;
import cn.edots.slug.listener.OnTextWatcher;


/**
 * @author Parck.
 * @date 2017/11/24.
 * @desc
 */
@Slug(layout = R.layout.fragment_ecard)
public class ECardFragment extends BaseFragment implements Standardize, SwipeRefreshLayout.OnRefreshListener {

    @FindView(R.id.title_bar)
    private LinearLayout titleLayout;
    @FindView(R.id.search_bar)
    private RelativeLayout searchLayout;
    @BindView(R.id.search_text)
    private EditSlugger searchSlugger;
    @FindView(R.id.search_button)
    private TextView searchButton;
    @FindView(R.id.more_button)
    private ImageView moreButton;
    @FindView(R.id.refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.empty_layout)
    private RelativeLayout emptyLayout;
    @FindView(R.id.clear_button)
    private ImageView clearButton;

    private SpinnerPopWindow moreMenuWindow;
    private RecyclerViewAdapter adapter;
    private List<Item> items = new ArrayList<>();
    private List<MainMenuItem> menuItems = new ArrayList<>();
    private ArrayList<CardBean> myCardBeanList = new ArrayList<>();

    @Override
    @ClickView({R.id.to_search_button, R.id.search_button, R.id.more_button, R.id.clear_button})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.to_search_button:
                titleLayout.setVisibility(View.GONE);
                searchLayout.setVisibility(View.VISIBLE);
                searchSlugger.getView().requestFocus();
                InputMethodTool.requestInput(searchSlugger.getView());
                refreshLayout.setOnRefreshListener(new android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        search();
                    }
                });
                break;
            case R.id.search_button:
                if (TextUtils.isEmpty(searchSlugger.getView().getText())) {
                    searchSlugger.clearFocus(THIS.getActivity());
                    InputMethodTool.cancelInput(searchSlugger.getView());
                    titleLayout.setVisibility(View.VISIBLE);
                    searchLayout.setVisibility(View.GONE);
                    refreshLayout.setOnRefreshListener(this);
                } else search();
                break;
            case R.id.more_button:
                moreMenuWindow.showAsDropDown(moreButton);
                break;
            case R.id.clear_button:
                searchSlugger.getView().setText("");
                break;
        }
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        LoadingDialog.show(THIS.getActivity());
        flushData();
        moreMenuWindow = new SpinnerPopWindow(THIS.getActivity());
        adapter = new RecyclerViewAdapter<Item>(THIS.getActivity(), R.layout.item_ecard, items) {
            @Override
            protected void binding(ViewHolder holder, Item o, int i) {
                o.holding(holder);
            }
        };
        setupMenuData();
    }

    @Override
    public void initView() {
        moreMenuWindow.setWidth(App.getInstance().dip2px(140));
    }

    @Override
    public void setListeners() {
        refreshLayout.setOnRefreshListener(this);
        recyclerView.setAdapter(adapter);
        moreMenuWindow.setAdapter(new MainMenuSpinnerAdapter(THIS.getActivity(), menuItems));
        moreMenuWindow.setItemListener(new MainMenuSpinnerAdapter.IOnItemSelectListener() {
            @Override
            public void onItemClick(int pos) {
                switch (pos) {
                    case 0:
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ECardFragment.this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                        } else {
                            startActivity(ScanQRActivity.class);
                        }
                        break;
                    case 1:
                        startActivity(SettingActivity.class);
                        break;
                }
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
                    flushData();
                }
            }
        });

        searchSlugger.getView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) InputMethodTool.cancelInput(v);
            }
        });
    }

    @Override
    public void onCreateLast() {

    }

    @Override
    public void onRefresh() {
        flushData();
    }

    private void flushData() {
        APIProvider.get(MineCardAPI.class).list(new OnESubscriber<List<Card>>() {
            @Override
            protected void onComplete(boolean success, List<Card> o, Throwable e) {
                refreshLayout.stopRefresh();
                LoadingDialog.hide();
                myCardBeanList.clear();
                if (success) {
                    items.clear();
                    App.getInstance().getMineCards().clear();
                    if (o != null) {
                        for (Card card : o) {
                            Item item = new Item();
                            item.setBean(CardBean.fromModel(card));
                            items.add(item);
                            //APIProvider.get(ContactAPI.class).create(card,null);
                            myCardBeanList.add(item.getBean());//用于保存我自己e卡的信息
                            App.getInstance().getMineCards().add(item.getBean());
                            App.getInstance().getMineCardIds().add(item.getBean().getInformation().getCard_id());
                        }
                    }
                    if (emptyLayout != null)
                        if (items.size() == 0) {
                            emptyLayout.setVisibility(View.VISIBLE);
                        } else {
                            emptyLayout.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                startActivity(ScanQRActivity.class);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void search() {
        List<Item> list = new ArrayList<>();
        for (Item item : items) {
            if (!TextUtils.isEmpty(item.getBean().getInformation().getName())) {
                if (item.getBean().getInformation().getName().contains(searchSlugger.getView().getText().toString())) {
                    list.add(item);
                }
            }
            if (!TextUtils.isEmpty(item.getBean().getInformation().getCard_label())) {
                if (item.getBean().getInformation().getCard_label().contains(searchSlugger.getView().getText().toString())) {
                    list.add(item);
                }
            }
        }
        items.clear();
        items.addAll(list);
        refreshLayout.stopRefresh();
        adapter.notifyDataSetChanged();
    }

    private void setupMenuData() {
        MainMenuItem item = new MainMenuItem();
        item.setIconRes(R.mipmap.main_qrcode);
        item.setName("扫一扫");
        menuItems.add(item);
        item = new MainMenuItem();
        item.setIconRes(R.mipmap.main_setting);
        item.setName("设置");
        menuItems.add(item);
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public class Item implements Holdable {

        private static final long serialVersionUID = 7435232046926382879L;

        private CardBean bean;

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            holder.setText(R.id.label_text, TextUtils.isEmpty(bean.getInformation().getCard_label()) ? "(未填写)" : bean.getInformation().getCard_label());
            holder.setText(R.id.name_text, TextUtils.isEmpty(bean.getInformation().getName()) ? getEmptyText("姓名") : bean.getInformation().getName());
            holder.setText(R.id.position_text, TextUtils.isEmpty(bean.getInformation().getPosition()) ? getEmptyText("职位") : bean.getInformation().getPosition());
            holder.setText(R.id.company_text, TextUtils.isEmpty(bean.getInformation().getCompany()) ? getEmptyText("公司") : bean.getInformation().getCompany());
            holder.setText(R.id.phone_text, bean.getPhones() != null && bean.getPhones().size() > 0 ? bean.getPhones().get(0).getPhone() : getEmptyText("电话"));
            holder.setText(R.id.address_text, bean.getAddresses() != null && bean.getAddresses().size() > 0 ? bean.getAddresses().get(0).toString() : getEmptyText("地址"));
            holder.setText(R.id.email_text, bean.getEmails() != null && bean.getEmails().size() > 0 ? bean.getEmails().get(0).getEmail() : getEmptyText("邮箱"));
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ECardDetailActivity.OpenParameter parameter = new ECardDetailActivity.OpenParameter();
                    parameter.setCardID(bean.getInformation().getCard_id());
                    parameter.setOpenType(ECardDetailActivity.OpenType.我的e卡);
                    ECardDetailActivity.startActivity(THIS.getActivity(), parameter);
                    InputMethodTool.cancelInput(v);
                }
            });
            holder.findViewById(R.id.qrcode_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new QRCodeDialog(THIS.getActivity(), bean, RegularToQR.createQRCardStr(bean));
                }
            });
        }

        private Spanned getEmptyText(String defaultText) {
            return Html.fromHtml("<font color='#529EEB'>(未填写" + defaultText + ")</font>");
        }

        @Override
        public void onClicked(View v) {
        }

        public CardBean getBean() {
            return bean;
        }

        public void setBean(CardBean bean) {
            this.bean = bean;
        }
    }

}