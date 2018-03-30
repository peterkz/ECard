package com.wetoop.ecard.ui;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.UrlBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.BitmapTool;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.widget.SwipeRefreshLayout;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * Created by User on 2017/9/6.
 */
@Slug(layout = R.layout.activity_add_apps_main)
public class AddAppsActivity extends TitleBarActivity implements Standardize, SwipeRefreshLayout.OnRefreshListener {

    @FindView(R.id.refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.empty_layout)
    private LinearLayout emptyLayout;

    private List<UrlBean> items = new ArrayList<>();
    private List<String> urls = new ArrayList<>();
    private RecyclerViewAdapter adapter;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        LoadingDialog.show(THIS);
        ArrayList<UrlBean> list = App.getInstance().getMyAddList();
        for (UrlBean bean : list) {
            urls.add(bean.getUrl());
        }
        adapter = new RecyclerViewAdapter<UrlBean>(THIS, R.layout.item_more_collect, items) {
            @Override
            protected void binding(ViewHolder holder, final UrlBean o, int i) {
                holder.setText(R.id.item_label, o.getName());
                ImageView itemImage = holder.findViewById(R.id.item_image);
                Button itemButton = holder.findViewById(R.id.item_button);
                if (o.getIcon() != null) {
                    Bitmap bitmap = BitmapTool.base642Bitmap(o.getIcon());
                    if (bitmap != null) itemImage.setImageBitmap(bitmap);
                }
                if (urls.contains(o.getUrl())) {
                    itemButton.setText("已添加");
                    itemButton.setTextColor(getResources().getColor(R.color.grey));
                    itemButton.setBackgroundColor(getResources().getColor(R.color.white));
                    itemButton.setClickable(false);
                    itemButton.setEnabled(false);
                } else {
                    itemButton.setText("添加");
                    itemButton.setTextColor(getResources().getColor(R.color.white));
                    itemButton.setBackground(getResources().getDrawable(R.drawable.shape_blue_solid_r2_bg));
                    itemButton.setClickable(true);
                    itemButton.setEnabled(true);
                }

                itemButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        HashMap<String, Object> value = new HashMap<>();
                        value.put("icon", o.getIcon());
                        value.put("name", o.getName());
                        value.put("url", o.getUrl());
                        if (o.getRate() != null) {
                            value.put("rate", o.getRate());
                        }
                        WilddogSync.getInstance().getReference("/apps")
                                .child(App.getWilddogAuth().getCurrentUser().getUid())
                                .push().setValue(value, new SyncReference.CompletionListener() {
                            @Override
                            public void onComplete(SyncError error, SyncReference ref) {
                                if (error != null) {
                                    TOAST("添加失败");
                                    ((Button) v).setText("添加");
                                    ((Button) v).setTextColor(getResources().getColor(R.color.white));
                                    v.setBackground(getResources().getDrawable(R.drawable.shape_blue_solid_r2_bg));
                                    v.setClickable(true);
                                    v.setEnabled(true);
                                } else {
                                    TOAST("添加成功");
                                    ((Button) v).setText("已添加");
                                    ((Button) v).setTextColor(getResources().getColor(R.color.grey));
                                    v.setBackgroundColor(getResources().getColor(R.color.white));
                                    v.setClickable(false);
                                    v.setEnabled(false);
                                }
                            }
                        });
                    }
                });
            }
        };
        flushData();
    }

    @Override
    public void initView() {
        setCenterTitleContent("推荐");
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    private void flushData() {
        App.getReference("/public_apps").orderByValue().addValueEventListener(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LoadingDialog.hide();
                if (dataSnapshot.getValue() != null) {
                    emptyLayout.setVisibility(View.GONE);
                    ConvertHelper.rxConvert(dataSnapshot, new TypeReference<List<UrlBean>>() {
                    }).subscribe(new OnESubscriber<List<UrlBean>>() {
                        @Override
                        protected void onComplete(boolean success, List<UrlBean> o, Throwable e) {
                            refreshLayout.stopRefresh();
                            if (success) {
                                if (o == null) return;
                                items.clear();
                                items.addAll(o);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        flushData();
    }
}
