package com.wetoop.ecard.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;

import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.SelectorItem;

import java.util.ArrayList;
import java.util.HashMap;
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
 * @date 2017/10/16.
 * @desc 关于e卡页面
 */

@Slug(layout = R.layout.activity_about)
public class AboutActivity extends TitleBarActivity implements Standardize {

    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;

    private List<AboutItem> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        AboutItem.initItems(items);
        adapter = new RecyclerViewAdapter<AboutItem>(THIS, new int[]{R.layout.item_history_header, R.layout.item_occupy, R.layout.item_check_layout}, items) {
            @Override
            protected void binding(ViewHolder holder, AboutItem o, int i) {
                o.holding(holder);
            }

            @Override
            public int getItemViewType(int position) {
                return items.get(position).getType();
            }
        };

    }

    @Override
    public void initView() {
        setCenterTitleContent("关于e卡");
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
    public static class AboutItem extends SelectorItem {

        private static final long serialVersionUID = 4496082648130930481L;

        public static void initItems(List<AboutItem> items) {
            AboutItem item = new AboutItem();
            item.setType(0);
            items.add(item);
            item = new AboutItem();
            item.setType(1);
            items.add(item);
            item = new AboutItem();
            item.setType(2);
            item.setLabel("用户反馈");
            item.setTag(0);
            item.setShowShort(true);
            items.add(item);
            item = new AboutItem();
            item.setType(2);
            item.setLabel("投诉与举报");
            item.setTag(1);
            item.setShowShort(true);
            items.add(item);
            item = new AboutItem();
            item.setType(2);
            item.setLabel("联系我们");
            item.setTag(2);
            items.add(item);
        }

        @Override
        public void holding(RecyclerViewAdapter.ViewHolder holder) {
            switch (holder.getItemViewType()) {
                case 0:
                    holder.setText(R.id.name_text, "e卡 2.0");
                    break;
                case 2:
                    super.holding(holder);
                    break;
            }
        }

        @Override
        public void onClicked(View v) {
            super.onClicked(v);
            Intent intent;
            Context context = v.getContext();
            switch (getTag()) {
                case 0:
                case 1:
                    intent = new Intent(context, FeedbackActivity.class);
                    HashMap<String, Object> data = new HashMap<>();
                    data.put(FeedbackActivity.TITLE_TEXT, getLabel());
                    intent.putExtra("INTENT_DATA", data);
                    context.startActivity(intent);
                    break;
                case 2:
                    intent = new Intent(context, ContactUsActivity.class);
                    context.startActivity(intent);
                    break;
            }
        }
    }
}
