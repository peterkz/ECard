package com.wetoop.ecard.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.bean.SelectorItem;
import com.wetoop.ecard.ui.dialog.ConfirmDialog;
import com.wetoop.ecard.ui.dialog.OnClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * @author Parck.
 * @date 2017/10/13.
 * @desc 设置页面
 */
@Slug(layout = R.layout.activity_setting)
public class SettingActivity extends TitleBarActivity implements Standardize {

    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private ArrayList<SettingItem> items = new ArrayList<>();

    @ClickView({R.id.logout_button})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.logout_button:
                doLogout();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        App.getInstance().setActivity(THIS);
    }

    @Override
    public void setupData(@Nullable Map<String, Object> intentData) {
        SettingItem.initItems(items);
        adapter = new RecyclerViewAdapter<SettingItem>(THIS, new int[]{R.layout.item_occupy, R.layout.item_check_layout}, items) {

            @Override
            public int getItemViewType(int position) {
                return items.get(position).getType();
            }

            @Override
            protected void binding(ViewHolder holder, SettingItem data, int position) {
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
        setCenterTitleContent("设置");
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    private void doLogout() {
        new ConfirmDialog.Builder()
                .setMessage("确认退出当前账号吗？")
                .setRightButton(R.color.light_red, new OnClickListener() {
                    @Override
                    public boolean onClick(View v) {
                        App.getWilddogAuth().signOut();
                        APIProvider.clear();
                        startActivity(LoginActivity.class);
                        finish();
                        App.getInstance().getMainActivity().finish();
                        App.getInstance().setMainActivity(null);
                        return true;
                    }
                }).build().show(THIS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getInstance().setActivity(null);
    }

    // =================================================================
    // inner class
    // =================================================================

    private static class SettingItem extends SelectorItem {

        private static final long serialVersionUID = -4814812601636681855L;

        public ItemTag tag;

        static void initItems(List<SettingItem> items) {
            items.clear();
            SettingItem item = new SettingItem();
            item.setType(0);
            items.add(item);

            item = new SettingItem();
            item.setType(1);
            item.setLabel("账号信息转移");
            item.tag = ItemTag.TRANSFER;
            item.setShowShort(true);
            items.add(item);

            item = new SettingItem();
            item.setType(1);
            item.setLabel("修改密码");
            item.tag = ItemTag.RESET;
            items.add(item);

            item = new SettingItem();
            item.setType(0);
            items.add(item);

            item = new SettingItem();
            item.setType(1);
            item.setLabel("关于e卡");
            item.tag = ItemTag.ABOUT;
            items.add(item);
        }

        @Override
        public void onClicked(View v) {
            Context context = v.getContext();
            Intent intent = null;
            switch (tag == null ? ItemTag.DEFAULT : tag) {
                case DEFAULT:
                    Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show();
                    break;
                case TRANSFER:
                    intent = new Intent(context, AccountValidateActivity.class);
                    break;
                case RESET:
                    intent = new Intent(context, ResetPasswordActivity.class);
                    break;
                case ABOUT:
                    intent = new Intent(context, AboutActivity.class);
                    break;
            }
            context.startActivity(intent);
        }
    }

    private enum ItemTag {
        DEFAULT, TRANSFER, RESET, ABOUT
    }
}
