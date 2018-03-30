package com.wetoop.ecard.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.SelectorItem;
import com.wetoop.ecard.tools.PermissionUtil;
import com.wetoop.ecard.ui.ContactNearbyActivity;
import com.wetoop.ecard.ui.ContactUpdateActivity;
import com.wetoop.ecard.ui.ExtensionActivity;
import com.wetoop.ecard.ui.ScoreActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.fragment.TitleBarFragment;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * @author Parck.
 * @date 2017/9/1.
 * @desc
 */
@Slug(layout = R.layout.fragment_discover)
public class DiscoverFragment extends TitleBarFragment implements Standardize {

    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;

    public RecyclerViewAdapter<ItemBean> adapter;
    private List<ItemBean> items = new ArrayList<>();
    public ItemBean contactUpdateItem;

    @Override
    protected boolean isHideBackButton() {
        return true;
    }

    @Override
    public void setupData(@Nullable Map<String, Object> intentData) {
        initItems(items);
        adapter = new RecyclerViewAdapter<ItemBean>(THIS.getActivity(), new int[]{R.layout.item_occupy, R.layout.item_check_layout}, items) {
            @Override
            public int getItemViewType(int position) {
                return items.get(position).getType();
            }

            @Override
            protected void binding(ViewHolder holder, final ItemBean data, int position) {
                switch (getItemViewType(position)) {
                    case 1:
                        data.holding(holder);
                        break;
                }
            }
        };
    }

    @Override
    public void initView() {
        setCenterTitleContent("发现");
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    public void initItems(List<ItemBean> items) {
        items.clear();
        ItemBean item = new ItemBean();
        item.setType(0);
        items.add(item);

        contactUpdateItem = new ItemBean();
        contactUpdateItem.setLabel("联系人更新");
        contactUpdateItem.setTag(0);
        contactUpdateItem.setLeftImageResId(R.mipmap.message_update_item_icon);
        contactUpdateItem.setShowCount(false);
        contactUpdateItem.setType(1);
        items.add(contactUpdateItem);

        item = new ItemBean();
        item.setType(0);
//            items.add(item);

        item = new ItemBean();
        item.setLabel("我的联系人分布");
        item.setTag(1);
        item.setLeftImageResId(R.mipmap.linkman_distribution_item_icon);
        item.setType(1);
        item.setShowShort(true);
//            items.add(item);

        item = new ItemBean();
        item.setLabel("附近的人");
        item.setTag(2);
        item.setLeftImageResId(R.mipmap.nearby_person_item_icon);
        item.setType(1);
        items.add(item);

        item = new ItemBean();
        item.setType(0);
//            items.add(item);

        item = new ItemBean();
        item.setLabel("我的e积分");
        item.setTag(3);
        item.setLeftImageResId(R.mipmap.score_item_icon);
        item.setType(1);
//            items.add(item);

        item = new ItemBean();
        item.setType(0);
        items.add(item);

        item = new ItemBean();
        item.setLabel("推广");
        item.setTag(4);
        item.setLeftImageResId(R.mipmap.extension_item_icon);
        item.setType(1);
        items.add(item);
    }

    //====================================================================
    // inner class
    //====================================================================

    public class ItemBean extends SelectorItem {

        private static final long serialVersionUID = -1316044179691537137L;

        @Override
        public void onClicked(View v) {
            Context context = v.getContext();
            Intent intent = null;
            switch (getTag()) {
                case 0:
                    intent = new Intent(context, ContactUpdateActivity.class);
                    context.startActivity(intent);
                    break;
                case 1:
//                    intent = new Intent(context, ContactDistributionActivity.class);
                    break;
                case 2:
                    showContacts(context);
                    break;
                case 3:
                    intent = new Intent(context, ScoreActivity.class);
                    context.startActivity(intent);
                    break;
                case 4:
                    intent = new Intent(context, ExtensionActivity.class);
                    context.startActivity(intent);
                    break;
            }
        }
    }

    public void showContacts(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS) != PackageManager.PERMISSION_GRANTED) {
            DiscoverFragment.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            Intent intent = new Intent(context, ContactNearbyActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                Intent intent = new Intent(getActivity(), ContactNearbyActivity.class);
                startActivity(intent);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
