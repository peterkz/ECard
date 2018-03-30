package com.wetoop.ecard.ui;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.SearchAddressAPI;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.bean.ContactItemBean;
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
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * Created by User on 2017/9/15.
 */
@Slug(layout = R.layout.activity_contact_nearby)
public class ContactNearbyActivity extends TitleBarActivity implements Standardize {

    @FindView(R.id.recycle_view)
    private VerticalRecyclerView recyclerView;
    @FindView(R.id.empty_layout)
    private LinearLayout emptyLayout;

    private List<ContactItemBean> items = new ArrayList();
    private RecyclerViewAdapter adapter;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        flushData();
        adapter = new RecyclerViewAdapter<ContactItemBean>(THIS, R.layout.item_linkman, items) {
            @Override
            protected void binding(ViewHolder holder, final ContactItemBean o, int i) {
                o.holding(holder);
                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ECardDetailActivity.OpenParameter parameter = new ECardDetailActivity.OpenParameter();
                        parameter.setCardID(o.getCardId());
                        parameter.setOpenType(ECardDetailActivity.OpenType.新的联系人);
                        ECardDetailActivity.startActivity(THIS, parameter);
                    }
                });
            }
        };
    }

    private void flushData() {
        Observable.create(new Observable.OnSubscribe<Address>() {
            @Override
            public void call(Subscriber<? super Address> subscriber) {
                Address address = getAddress();
                subscriber.onNext(address);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new OnESubscriber<Address>() {
            @Override
            protected void onComplete(boolean success, Address o, Throwable e) {
                if (success && o != null) {
                    com.wetoop.ecard.api.model.Address searchAdr = new com.wetoop.ecard.api.model.Address();
                    searchAdr.setProvince(o.getAdminArea());
                    searchAdr.setCity(o.getLocality());
                    searchAdr.setCounty(o.getSubLocality());
                    APIProvider.get(SearchAddressAPI.class).search(searchAdr.searchAdr(), new OnESubscriber<List<Card>>() {
                        @Override
                        protected void onComplete(boolean success, List<Card> o, Throwable e) {
                            if (success && o != null && o.size() > 0) {
                                emptyLayout.setVisibility(View.GONE);
                                LoadingDialog.show(THIS);
                                items.clear();
                                for (Card card : o) {
                                    ContactItemBean item = new ContactItemBean();
                                    if (card.getInformation() == null || card.getInformation().size() == 0)
                                        return;
                                    Information information = card.getInformation().get(0);
                                    item.setAvatar(information.getAvatar());
                                    item.setName(information.getName());
                                    item.setCardId(information.getCard_id());
                                    item.setDateUpdated(information.getDateUpdated());
                                    item.setUserId(information.getUser_id());
                                    items.add(item);
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                emptyLayout.setVisibility(View.VISIBLE);
                                //TOAST("暂无数据");
                            }
                        }
                    });
                } else {
                    TOAST("定位失败");
                }
            }
        });
    }

    @Override
    public void initView() {
        setCenterTitleContent("附近的人");
    }

    @Override
    public void setListeners() {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateLast() {

    }

    private Location getLocation() {
        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if (status.isProviderEnabled(NETWORK_PROVIDER)) {
            return status.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            TOAST("定位失败");
            return null;
        }
    }

    private Address getAddress() {
        Geocoder geocoder = new Geocoder(THIS);
        List<Address> places = null;
        try {
            Location location = getLocation();
            if (location == null) return null;
            places = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (places != null && places.size() > 0) {
            return places.get(0);
        }
        return null;
    }
}
