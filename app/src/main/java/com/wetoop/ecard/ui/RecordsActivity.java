package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.InComingAPI;
import com.wetoop.ecard.api.OutGoingAPI;
import com.wetoop.ecard.ui.fragment.RecordsReceiveFragment;
import com.wetoop.ecard.ui.fragment.RecordsSendFragment;

import java.util.ArrayList;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * Created by User on 2017/10/17.
 */
@Slug(layout = R.layout.activity_records)
public class RecordsActivity extends TitleBarActivity implements Standardize {

    @FindView(R.id.tabHost)
    private TabHost mTabHost;
    @FindView(R.id.pager)
    private ViewPager mViewPager;
    private ArrayList<Fragment> fragmentList;
    private int pageControl = 0;
    private static final String RECEIVE = "RECEIVE";
    private static final String SEND = "SEND";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        APIProvider.get(InComingAPI.class).removeListValueListener();
        APIProvider.get(OutGoingAPI.class).removeListValueListener();
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {

    }

    @Override
    public void initView() {
        setCenterTitleContent("收发记录");
        mViewPager.setOnPageChangeListener(new MyPagerOnPageChangeListener());
        mTabHost.setup();
        mTabHost.setOnTabChangedListener(listener);
        initializeTabs();
        inVariable();
        //设置点击之后背景颜色变化
        setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), true);
        setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
        pageControl = 1;
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onCreateLast() {

    }

    private void inVariable() {
        fragmentList = new ArrayList<>();
        Fragment recordsReceiveFragment = new RecordsReceiveFragment();
        Fragment recordsSendFragment = new RecordsSendFragment();
        fragmentList.add(recordsReceiveFragment);
        fragmentList.add(recordsSendFragment);
        mViewPager.setAdapter(new myPageAdapter(getSupportFragmentManager()));
    }

    //初始化tabhost
    private void initializeTabs() {
        TabHost.TabSpec spec;
        //收到的e卡
        spec = mTabHost.newTabSpec(RECEIVE);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(getString(R.string.recordsReceive)));
        mTabHost.addTab(spec);
        //发送的e卡
        spec = mTabHost.newTabSpec(SEND);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(getString(R.string.recordsSend)));
        mTabHost.addTab(spec);
    }

    private View createTabView(final String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.tabs_bar_item_records, null);
        TextView textView = (TextView) view.findViewById(R.id.tab_text_records);
        textView.setText(text);
        return view;
    }

    private void setTabBarItemIndicator(View view, boolean active) {
        TextView textView = (TextView) view.findViewById(R.id.tab_text_records);
        TextView view1 = (TextView) view.findViewById(R.id.view_records);
        if (active) {
            textView.setTextColor(getResources().getColor(R.color.action_blue));
            view1.setVisibility(View.VISIBLE);
        } else {
            textView.setTextColor(getResources().getColor(R.color.action_gray));
            view1.setVisibility(View.GONE);
        }
    }

    private class myPageAdapter extends FragmentStatePagerAdapter {
        FragmentManager fm;

        public myPageAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {

            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            if (tabId.equals(RECEIVE)) {
                mViewPager.setCurrentItem(0);
                if (pageControl == 1) {
                    setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                    setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                }
            } else if (tabId.equals(SEND)) {
                mViewPager.setCurrentItem(1);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), true);
            }
        }

    };

    /**
     * ViewPager的PageChangeListener(页面改变的监听器)
     */
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
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
            } else if (position == 1) {
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), true);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
