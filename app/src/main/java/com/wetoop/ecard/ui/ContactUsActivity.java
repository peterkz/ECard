package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;

import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.ContactUsAPI;
import com.wetoop.ecard.api.model.ContactUs;
import com.wetoop.ecard.bean.SelectorItem;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.ui.dialog.LoadingDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * @author Parck.
 * @date 2017/11/23.
 * @desc
 */
@Slug(layout = R.layout.activity_contact_us)
public class ContactUsActivity extends TitleBarActivity implements Standardize {

    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;

    private List<Item> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        adapter = new RecyclerViewAdapter<Item>(THIS, new int[]{R.layout.item_occupy, R.layout.item_check_layout}, items) {
            @Override
            protected void binding(ViewHolder holder, Item o, int position) {
                if (o.getType() == 1) o.holding(holder);
            }

            @Override
            public int getItemViewType(int position) {
                return items.get(position).getType();
            }
        };

        LoadingDialog.show(THIS);
        APIProvider.get(ContactUsAPI.class).get(new OnESubscriber<ContactUs>() {
            @Override
            protected void onComplete(boolean success, ContactUs o, Throwable e) {
                if (success) {
                    Item.initItems(items, o);
                    adapter.notifyDataSetChanged();
                } else finish();
            }
        });
    }

    @Override
    public void initView() {
        setCenterTitleContent("联系我们");
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public static class Item extends SelectorItem {

        private static final long serialVersionUID = 5618955863854163320L;

        public static void initItems(List<Item> items, ContactUs us) {

            Item item = new Item();
            item.setType(0);
            items.add(item);

            item = new Item();
            item.setTag(0);
            item.setType(1);
            item.setLeftImageResId(R.mipmap.phone_icon);
            item.setLabel("联系电话");
            item.setValue(us.getPhone());
            item.setShowShort(true);
            item.setShowGuide(false);
            items.add(item);

            item = new Item();
            item.setTag(0);
            item.setType(1);
            item.setLeftImageResId(R.mipmap.email_icon);
            item.setLabel("联系邮箱");
            item.setValue(us.getEmail());
            item.setShowGuide(false);
            items.add(item);
        }
    }
}
