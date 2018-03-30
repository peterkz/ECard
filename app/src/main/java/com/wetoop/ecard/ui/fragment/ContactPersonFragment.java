package com.wetoop.ecard.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.ContactAPI;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.ContactBean;
import com.wetoop.ecard.bean.MainMenuItem;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.CnToSpell;
import com.wetoop.ecard.tools.InputMethodTool;
import com.wetoop.ecard.tools.PermissionUtil;
import com.wetoop.ecard.tools.PinyinComparator;
import com.wetoop.ecard.ui.CreateCardActivity;
import com.wetoop.ecard.ui.ECardDetailActivity;
import com.wetoop.ecard.ui.NewContactsActivity;
import com.wetoop.ecard.ui.ScanQRActivity;
import com.wetoop.ecard.ui.SearchECardActivity;
import com.wetoop.ecard.ui.adapter.ContactPersonAdapter;
import com.wetoop.ecard.ui.adapter.MainMenuSpinnerAdapter;
import com.wetoop.ecard.ui.adapter.SpinnerPopWindow;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wetoop.ecard.ui.dialog.SynchronizationDialog;
import com.wetoop.ecard.ui.dialog.SynchronizationLoadingDialog;
import com.wetoop.ecard.ui.widget.listview.LetterListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.fragment.TitleBarFragment;
import cn.edots.nest.ui.widget.SwipeRefreshLayout;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;
import cn.edots.slug.listener.OnTextWatcher;


/**
 * Created by User on 2017/9/1.
 */
@Slug(layout = R.layout.fragment_contact)
public class ContactPersonFragment extends TitleBarFragment implements Standardize, ContactPersonAdapter.OnGetAlphaIndexerAndSectionsListener {

    @FindView(R.id.search_bar)
    private RelativeLayout searchLayout;
    @FindView(R.id.search_button)
    private TextView searchButton;
    @FindView(R.id.search)
    private RelativeLayout search;
    @BindView(R.id.search_text)
    private EditSlugger searchSlugger;
    @FindView(R.id.clear_button)
    private ImageView clearButton;
    @FindView(R.id.letterView)
    private LetterListView letterListView;
    @FindView(R.id.listView)
    private ListView listView;
    @FindView(R.id.noData)
    private TextView noData;
    @FindView(R.id.swipe_refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    private TextView overLayout;
    private RelativeLayout newContact, synchronization;

    private Map<String, Integer> alphaIndexer = new HashMap<>();
    private List<String> sections;// 存放存在的汉语拼音首字母
    private Handler handler;
    private OverlayThread overlayThread;
    private ArrayList<ContactBean> contactBeanArrayList;
    private boolean writeToList = false;
    private ContactPersonAdapter myAdapter;
    // 根据拼音来排列ListView里面的数据类
    private PinyinComparator pinyinComparator;
    private WindowManager windowManager;
    private SynchronizationDialog synchronizationDialog;
    private SpinnerPopWindow moreMenuWindow;
    private ArrayList<MainMenuItem> menuItems = new ArrayList<>();
    public TextView unreadTagView;

    @Override
    protected boolean isHideBackButton() {
        return true;
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        handler = new Handler();
        overlayThread = new OverlayThread();
        contactBeanArrayList = new ArrayList<>();
        contactBeanArrayList.clear();
        moreMenuWindow = new SpinnerPopWindow(this.getContext());
        setupMenuData();
    }

    @Override
    public void initView() {
        setCenterTitleContent("联系人");
        moreMenuWindow.setWidth(App.getInstance().dip2px(160));
        setRightButtonImageResource(R.mipmap.add_more_icon);
        View convertView = getActivity().getLayoutInflater().inflate(R.layout.fragment_contact_listview_header, null);
        newContact = (RelativeLayout) convertView.findViewById(R.id.newContact);
        //synchronization = (RelativeLayout) convertView.findViewById(R.id.synchronization);
        unreadTagView = (TextView) convertView.findViewById(R.id.unread_tag);
        listView.addHeaderView(convertView);
        myAdapter = new ContactPersonAdapter(getActivity(), contactBeanArrayList);
        listView.setAdapter(myAdapter);
    }

    @Override
    public void setListeners() {
        setOnRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreMenuWindow.showAsDropDown(rightButton);
            }
        });
        relativeLayoutClick();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CardBean cardBean = contactBeanArrayList.get(position - 1).getCard();
                ECardDetailActivity.OpenParameter parameter = new ECardDetailActivity.OpenParameter();
                parameter.setCardID(cardBean.getInformation().getCard_id());
                parameter.setOpenType(ECardDetailActivity.OpenType.我的联系人);
                ECardDetailActivity.startActivity(getActivity(), parameter);
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFriendData();
            }
        });

        myAdapter = new ContactPersonAdapter(getActivity(), contactBeanArrayList);
        myAdapter.setOnGetAlphaIndeserAndSectionListener(this);
        listView.setAdapter(myAdapter);
        moreMenuWindow.setAdapter(new MainMenuSpinnerAdapter(THIS.getContext(), menuItems));
        moreMenuWindow.setItemListener(new MainMenuSpinnerAdapter.IOnItemSelectListener() {
            @Override
            public void onItemClick(int pos) {
                switch (pos) {
                    case 0:
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ContactPersonFragment.this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                        } else {
                            startActivity(ScanQRActivity.class);
                        }
                        break;
                    case 1:
                        CreateCardActivity.OpenParameter parameter = new CreateCardActivity.OpenParameter();
                        parameter.setOpenType(CreateCardActivity.OpenType.新建联系人);
                        CreateCardActivity.startActivity(THIS.getActivity(), parameter);
                        break;
                    case 2:
                        getActivity().startActivity(new Intent(getActivity(), SearchECardActivity.class));
                        break;
                }
            }
        });
    }

    @Override
    public void onCreateLast() {
        initOverlay();
        getFriendData();
    }

    private void setupMenuData() {
        MainMenuItem item = new MainMenuItem();
        item.setIconRes(R.mipmap.main_qrcode);
        item.setName("扫一扫");
        menuItems.add(item);
        item = new MainMenuItem();
        item.setIconRes(R.mipmap.hand_add_icon);
        item.setName("手动添加  ");
        menuItems.add(item);
        item = new MainMenuItem();
        item.setIconRes(R.mipmap.main_search);
        item.setName("搜索联系人");
        menuItems.add(item);
    }

    private void search() {
        ArrayList<ContactBean> list = new ArrayList<>();
        for (ContactBean bean : contactBeanArrayList) {
            if (!TextUtils.isEmpty(bean.getCard().getInformation().getName())) {
                if (bean.getCard().getInformation().getName().contains(searchSlugger.getView().getText().toString())) {
                    list.add(bean);
                }
            }
        }
        contactBeanArrayList.clear();
        contactBeanArrayList.addAll(list);
        refreshLayout.stopRefresh();
        myAdapter.notifyDataSetChanged();
    }

    private void getFriendData() {
        APIProvider.get(ContactAPI.class).list(new OnESubscriber<List<Card>>() {
            @Override
            protected void onComplete(boolean success, List<Card> cards, Throwable e) {
                LoadingDialog.hide();
                refreshLayout.stopRefresh();
                if (success) {
                    contactBeanArrayList.clear();
                    App.getInstance().getContactsCardIdList().clear();
                    if (cards != null) {
                        for (final Card card : cards) {
                            final ContactBean bean = new ContactBean();
                            CardBean cardBean = CardBean.fromModel(card);
                            bean.setCard(cardBean);
                            if (cardBean.getInformation() != null) {
                                if (cardBean.getInformation().getName() != null) {
                                    CnToSpell cnToSpell = new CnToSpell();
                                    String spellName = cnToSpell.getPinYinHeadChar(cardBean.getInformation().getName());
                                    String spellFirst = cnToSpell.getPinYinFirstLetter(cardBean.getInformation().getName()).toUpperCase();
                                    if (!spellFirst.matches("[A-Z]")) {
                                        spellFirst = "#";
                                    }
                                    bean.setSpellName(spellName);
                                    bean.setSpellFirst(spellFirst);
                                } else {
                                    bean.setSpellName(" ");
                                    bean.setSpellFirst("#");
                                }
                            }

                            contactBeanArrayList.add(bean);
                            App.getInstance().getContactsCardIdList().add(bean.getCard().getInformation().getCard_id());
                        }
                    }
                    if (contactBeanArrayList.size() > 0) {
                        noData.setVisibility(View.GONE);
                    } else {
                        noData.setVisibility(View.VISIBLE);
                    }
                    addAppsData();
                }
            }
        });
    }

    private void addAppsData() {
        pinyinComparator = new PinyinComparator();
        Collections.sort(contactBeanArrayList, pinyinComparator);
        myAdapter.addDataAlphaIndexer(contactBeanArrayList);
        myAdapter.notifyDataSetChanged();
    }

    private void relativeLayoutClick() {
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayout.setVisibility(View.VISIBLE);
                search.setVisibility(View.INVISIBLE);
                searchSlugger.getView().requestFocus();
                InputMethodTool.requestInput(searchSlugger.getView());
                refreshLayout.setOnRefreshListener(new android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        search();
                    }
                });
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(searchSlugger.getView().getText())) {
                    searchSlugger.clearFocus(THIS.getActivity());
                    InputMethodTool.cancelInput(searchSlugger.getView());
                    searchLayout.setVisibility(View.GONE);
                    search.setVisibility(View.VISIBLE);
                    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            getFriendData();
                        }
                    });
                } else search();
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
                    LoadingDialog.show(THIS.getActivity());
                    getFriendData();
                }
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSlugger.getView().setText("");
            }
        });
        newContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewContactsActivity.class);
                startActivity(intent);
            }
        });
        /*synchronization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogShow();
            }
        });*/
        letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
    }

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ContactPersonFragment.this.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 2);
        } else {
            //进入同步读条
            SynchronizationLoadingDialog synchronizationLoadingDialog = new SynchronizationLoadingDialog(getActivity(), contactBeanArrayList);
            synchronizationLoadingDialog.setCanceledOnTouchOutside(false);
            synchronizationLoadingDialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                startActivity(ScanQRActivity.class);
            }
        } else if (requestCode == 2) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                //进入同步读条
                SynchronizationLoadingDialog synchronizationLoadingDialog = new SynchronizationLoadingDialog(getActivity(), contactBeanArrayList);
                synchronizationLoadingDialog.setCanceledOnTouchOutside(false);
                synchronizationLoadingDialog.show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void dialogShow() {
        synchronizationDialog = new SynchronizationDialog(getActivity(), new SynchronizationDialog.OnCustomDialogListener() {
            @Override
            public void back(int type) {
                synchronizationDialog.dismiss();
                if (type == 1) {
                    showContacts();//检测是否有权限读取联系人
                }
            }
        });
        synchronizationDialog.show();
    }

    @Override
    public void getAlphaIndexerAndSectionsListener(Map<String, Integer> alphaIndexer, List<String> sections) {
        this.alphaIndexer = alphaIndexer;
        this.sections = sections;
    }

    /**
     * 字母列表点击滑动监听器事件
     */
    private class LetterListViewListener implements LetterListView.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            if (alphaIndexer.get(s) != null) {//判断当前选中的字母是否存在集合中
                int position = alphaIndexer.get(s);//如果存在集合中则取出集合中该字母对应所在的位置,再利用对应的setSelection，就可以实现点击选中相应字母，然后联系人就会定位到相应的位置
                listView.setSelection(position + 1);
                overLayout.setText(s);
                overLayout.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);
                // 延迟一秒后执行，让overlay为不可见
                handler.postDelayed(overlayThread, 1500);
            }
        }
    }

    /**
     * 初始化汉语拼音首字母弹出提示框
     */
    private void initOverlay() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        overLayout = (TextView) inflater.inflate(R.layout.overlay, null);
        overLayout.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overLayout, lp);
    }

    @Override
    public void onDestroy() {
        windowManager.removeView(overLayout);
        windowManager.removeViewImmediate(overLayout);
        App.loadingStartTime = null;
        super.onDestroy();
    }

    /**
     * 设置overlay不可见
     */
    private class OverlayThread implements Runnable {

        @Override
        public void run() {
            overLayout.setVisibility(View.GONE);
        }
    }
}
