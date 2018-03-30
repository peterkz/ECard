package com.wetoop.ecard.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.wetoop.ecard.ui.fragment.CollectFragment;
import com.wetoop.ecard.ui.fragment.ContactPersonFragment;
import com.wetoop.ecard.ui.fragment.DiscoverFragment;
import com.wetoop.ecard.ui.fragment.ECardFragment;

import java.util.ArrayList;
import java.util.List;

import cn.edots.nest.core.cache.FragmentPool;

/**
 * @author Parck.
 * @date 2017/10/26.
 * @desc
 */

public class ECardPagerAdapter extends FragmentStatePagerAdapter {

    private FragmentManager fragmentManager;
    private static List<Fragment> fragments = new ArrayList<>();
    private static boolean inited;

    public ECardPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public static void initFragmentContainer() {
        if (!inited) {
            fragments.add(FragmentPool.getFragment(ECardFragment.class));
            fragments.add(FragmentPool.getFragment(ContactPersonFragment.class));
            fragments.add(FragmentPool.getFragment(DiscoverFragment.class));
            fragments.add(FragmentPool.getFragment(CollectFragment.class));
            inited = true;
        }
    }

    public void destroy() {
        inited = false;
        fragments.clear();
    }
}
