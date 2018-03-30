package com.wetoop.ecard.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.MessageAPI;
import com.wetoop.ecard.api.model.Notification;
import com.wetoop.ecard.ui.adapter.ECardPagerAdapter;
import com.wetoop.ecard.ui.widget.MyViewPager;
import com.wilddog.wilddogauth.WilddogAuth;

import cn.edots.nest.core.cache.AppCachePool;

public class MainActivity extends AppCompatActivity {
    public WilddogAuth wilddogAuth;

    private MyViewPager mViewPager;
    private TabHost mTabHost;
    private final static String TAG = "MainActivity";
    private static final String TAB1 = "TAB1";
    private static final String TAB2 = "TAB2";
    private static final String TAB3 = "TAB3";
    private static final String TAB4 = "TAB4";
    private int tab1_test = 0;
    private ECardPagerAdapter adapter;
    private RelativeLayout addCard;
    public View mineCardUnreadTagView;
    public View contactUnreadTagView;
    public View collectUnreadTagView;
    public View discoverUnreadTagView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().setMainActivity(this);
        setContentView(R.layout.activity_main);
        wilddogAuth = WilddogAuth.getInstance();
        mViewPager = (MyViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new MyPagerOnPageChangeListener());

        mTabHost = (TabHost) findViewById(R.id.tabHost);
        mTabHost.setup();
        mTabHost.setOnTabChangedListener(listener);

        initializeTabs();
        inVariable();

        //设置点击之后背景颜色变化
        setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(0), true);
        setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(1), false);
        setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
        setTabBarItemIndicator4(mTabHost.getTabWidget().getChildTabViewAt(3), false);

        syncSomething();
    }

    private void syncSomething() {
        APIProvider.get(MessageAPI.class).receiveUpdateMessage(new MessageAPI.OnReceiveListener() {
            @Override
            public boolean onReceive(Notification notification) {
                App.getInstance().put(notification.getMessage().getCard_id() + "_MESSAGE_COUNT", App.getInstance().get(notification.getMessage().getCard_id() + "_MESSAGE_COUNT", Integer.class) + 1);
                App.getInstance().setUnreadUpdateMessageCount(App.getInstance().getUnreadUpdateMessageCount() + 1);
                return true;
            }
        });
        APIProvider.get(MessageAPI.class).receiveExchangeMessage(new MessageAPI.OnReceiveListener() {
            @Override
            public boolean onReceive(Notification notification) {
                App.getInstance().setUnreadExchangeMessageCount(App.getInstance().getUnreadExchangeMessageCount() + 1);
                return true;
            }
        });
    }

    private void inVariable() {
        mViewPager.setOffscreenPageLimit(3); // 优化页面切换速度，注意要刷新的操作放在onResume里
        adapter = new ECardPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        addCard = (RelativeLayout) findViewById(R.id.addCard);
        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateCardActivity.OpenParameter parameter = new CreateCardActivity.OpenParameter();
                parameter.setOpenType(CreateCardActivity.OpenType.新建我的e卡);
                CreateCardActivity.startActivity(MainActivity.this, parameter);
            }
        });
    }

    //初始化tabhost
    private void initializeTabs() {
        TabHost.TabSpec spec;
        //我的e卡
        spec = mTabHost.newTabSpec(TAB1);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(R.mipmap.tab_my_ecard_norm_icon, getString(R.string.tab1)));
        mTabHost.addTab(spec);
        //联系人
        spec = mTabHost.newTabSpec(TAB2);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(R.mipmap.phone, getString(R.string.tab2)));
        mTabHost.addTab(spec);
        //发现
        spec = mTabHost.newTabSpec(TAB3);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(R.mipmap.offline_fill, getString(R.string.tab3)));
        mTabHost.addTab(spec);
        //收藏
        spec = mTabHost.newTabSpec(TAB4);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(R.mipmap.collect, getString(R.string.tab4)));
        mTabHost.addTab(spec);
    }

    private View createTabView(final int id, final String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.tabs_bar_item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        //imageView.setColorFilter(getResources().getColor(R.color.action_blue));
        imageView.setImageDrawable(getResources().getDrawable(id));//setImageDrawable是最省内存高效的,
        // 如果担心图片过大或者图片过多影响内存和加载效率,可以自己解析图片然后通过调用setImageDrawable方法进行设置
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        textView.setText(text);
        return view;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setTabBarItemIndicator1(View view, boolean active) {
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        mineCardUnreadTagView = view.findViewById(R.id.unread_tag);
        if (active) {
            addCard.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.tab_my_ecard_selected_icon));
            textView.setTextColor(getResources().getColor(R.color.blueColor));
        } else {
            addCard.setVisibility(View.GONE);
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.tab_my_ecard_norm_icon));
            textView.setTextColor(getResources().getColor(R.color.grey));
        }
    }

    private void setTabBarItemIndicator2(View view, boolean active) {
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        contactUnreadTagView = view.findViewById(R.id.unread_tag);
        if (active) {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.phone_clicked));
            textView.setTextColor(getResources().getColor(R.color.blueColor));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.phone));
            textView.setTextColor(getResources().getColor(R.color.grey));
        }
    }

    private void setTabBarItemIndicator3(View view, boolean active) {
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        discoverUnreadTagView = view.findViewById(R.id.unread_tag);
        if (active) {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.offline_fill_clicked));
            textView.setTextColor(getResources().getColor(R.color.blueColor));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.offline_fill));
            textView.setTextColor(getResources().getColor(R.color.grey));
        }
    }

    private void setTabBarItemIndicator4(View view, boolean active) {
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        collectUnreadTagView = view.findViewById(R.id.unread_tag);
        if (active) {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.collect_clicked));
            textView.setTextColor(getResources().getColor(R.color.blueColor));

        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.collect));
            textView.setTextColor(getResources().getColor(R.color.grey));
        }
    }

    private class MyPagerOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        /**
         * 滑动ViewPager的时候,让上方的HorizontalScrollView自动切换
         */
        @Override
        public void onPageSelected(int position) {
            // TODO Auto-generated method stub

            if (position == 0) {
                //title_text.setText("已保存");
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
                setTabBarItemIndicator4(mTabHost.getTabWidget().getChildTabViewAt(3), false);
            } else if (position == 1) {
                //title_text.setText("监控");
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(1), true);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
                setTabBarItemIndicator4(mTabHost.getTabWidget().getChildTabViewAt(3), false);
            } else if (position == 2) {
                //title_text.setText("帐号信息");
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), true);
                setTabBarItemIndicator4(mTabHost.getTabWidget().getChildTabViewAt(3), false);
            } else if (position == 3) {
                //title_text.setText("帐号信息");
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
                setTabBarItemIndicator4(mTabHost.getTabWidget().getChildTabViewAt(3), true);
            }
        }
    }

    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            if (TAB1.equals(tabId)) {
                mViewPager.setCurrentItem(0);
                String shareItem = "true";
                if (tab1_test == 1) {
                    //title_text.setText("已保存");
                    setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                    setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                    setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
                    setTabBarItemIndicator4(mTabHost.getTabWidget().getChildTabViewAt(3), false);
                }
            } else if (TAB2.equals(tabId)) {
                mViewPager.setCurrentItem(1);
                //title_text.setText("监控");
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(1), true);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
                setTabBarItemIndicator4(mTabHost.getTabWidget().getChildTabViewAt(3), false);
            } else if (TAB3.equals(tabId)) {
                mViewPager.setCurrentItem(2);
                //title_text.setText("账号信息");
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), true);
                setTabBarItemIndicator4(mTabHost.getTabWidget().getChildTabViewAt(3), false);
            } else if (TAB4.equals(tabId)) {
                mViewPager.setCurrentItem(3);
                //title_text.setText("账号信息");
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
                setTabBarItemIndicator4(mTabHost.getTabWidget().getChildTabViewAt(3), true);
            }
        }

    };

    private long firstClick = -1;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long l = System.currentTimeMillis() - firstClick;
            if (firstClick == -1) {
                firstClick = System.currentTimeMillis();
                Toast.makeText(this, "再次点击退出应用", Toast.LENGTH_SHORT).show();
            } else if (l < 1000) {
                finish();
            } else {
                firstClick = -1;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppCachePool.getInstance().clear();
        adapter.destroy();
    }
}
