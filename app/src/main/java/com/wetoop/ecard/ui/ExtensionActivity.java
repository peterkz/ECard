package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.UrlBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.listener.OnEValueEventListener;
import com.wetoop.ecard.tools.ConvertHelper;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.Query;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;
import cn.edots.nest.ui.widget.SwipeRefreshLayout;
import cn.edots.nest.ui.widget.VerticalRecyclerView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

import static com.wetoop.ecard.Constants.URI_RECOMMENDATIONS;

/**
 * @author Parck.
 * @date 2017/10/26.
 * @desc
 */
@Slug(layout = R.layout.activity_extension)
public class ExtensionActivity extends TitleBarActivity implements Standardize, SwipeRefreshLayout.OnRefreshListener {

    @FindView(R.id.refresh_layout)
    private SwipeRefreshLayout refreshLayout;
    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.empty_layout)
    private LinearLayout emptyLayout;
    private List<UrlBean> list = new ArrayList<>();//推广app列表
    private RecyclerViewAdapter adapter;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        list.clear();
        loadData();
        adapter = new RecyclerViewAdapter<UrlBean>(THIS, R.layout.add_apps_tab_item, list) {
            @Override
            protected void binding(ViewHolder holder, final UrlBean data, final int position) {
                data.holding(holder);
            }
        };
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

    private void loadData() {
        SyncReference ref = WilddogSync.getInstance().getReference(URI_RECOMMENDATIONS);
        Query query = ref.orderByChild("img");
        query.addListenerForSingleValueEvent(new OnEValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    emptyLayout.setVisibility(View.GONE);
                    ConvertHelper.rxConvert(dataSnapshot, new TypeReference<Collection<UrlBean>>() {
                    }).subscribe(new OnESubscriber<Collection<UrlBean>>() {

                        @Override
                        protected void onComplete(boolean success, Collection<UrlBean> beans, Throwable e) {
                            if (success) {
                                if (beans == null) return;
                                list.clear();
                                list.addAll(beans);
                                if (list.size() > 0) {
                                    adapter.notifyDataSetChanged();
                                    refreshLayout.stopRefresh();
                                } else {
                                    TOAST("没有更多数据");
                                }
                            }
                        }
                    });
                }else{
                    emptyLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        list.clear();
        loadData();
    }
}
