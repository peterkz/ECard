package com.wetoop.ecard.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.UrlBean;
import com.wetoop.ecard.tools.PermissionUtil;
import com.wetoop.ecard.ui.AddAppsActivity;
import com.wetoop.ecard.ui.CollectWebActivity;
import com.wetoop.ecard.ui.adapter.GridViewAdapter;
import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.Query;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.fragment.TitleBarFragment;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * Created by User on 2017/9/1.
 */
@Slug(layout = R.layout.fragment_collect)
public class CollectFragment extends TitleBarFragment implements Standardize, BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    @FindView(R.id.slider)
    private SliderLayout mDemoSlider;
    @FindView(R.id.gridView)
    private GridView gridView;
    @FindView(R.id.cancel_button)
    private Button cancelButton;
    @FindView(R.id.noData)
    private TextView noData;

    private ArrayList<UrlBean> urlBeanArrayList = new ArrayList<>();
    private ArrayList<UrlBean> myAddAppList;//收藏app列表
    private HashMap<String, File> file_maps = new HashMap<>();
    private GridViewAdapter myAdapter;
    private boolean writeToList;

    @Override
    protected boolean isHideBackButton() {
        return true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        App.getInstance().initUnreadMessage();
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        myAddAppList = new ArrayList<>();
        myAddAppList.clear();
        listenerDataChanged();
        showContacts();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mDemoSlider != null) {
            if (isVisibleToUser) {
                mDemoSlider.startAutoCycle(0, 4000, true);
            } else {
                mDemoSlider.stopAutoCycle();
            }
        }
    }

    @Override
    public void initView() {
        setCenterTitleContent("收藏");
    }

    @Override
    public void setListeners() {
        cancelButton.setVisibility(View.GONE);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (myAdapter.isShowDelete()) {
                    return;
                }
                if (myAdapter.getCount() > myAdapter.list.size()) {
                    if (position == parent.getChildCount() - 1) {
                        if (myAdapter.isShowDelete) {
                            myAdapter.setIsShowDelete(false, myAddAppList.size() + 1);
                        } else {
                            App app = App.getInstance();
                            app.setMyAddAppList(myAddAppList);
                            Intent intent = new Intent(getActivity(), AddAppsActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(getActivity(), CollectWebActivity.class);
                        intent.putExtra("url", myAddAppList.get(position).getUrl());
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(getActivity(), CollectWebActivity.class);
                    intent.putExtra("url", myAddAppList.get(position).getUrl());
                    startActivity(intent);
                }
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                myAdapter.setIsShowDelete(true, myAddAppList.size() + 1);
                cancelButton.setVisibility(View.VISIBLE);
                return true;
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAdapter.setIsShowDelete(false, myAddAppList.size() + 1);
                cancelButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCreateLast() {

    }

    private void listenerDataChanged() {
        urlBeanArrayList.clear();
        SyncReference ref = WilddogSync.getInstance().getReference("/public_apps_sliders");
        Query queryRef = ref.orderByChild("img");//加一个loading的功能，等图片加载完了再取消。
        queryRef.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot snapshot, String ref) {
                noData.setVisibility(View.GONE);
                String imgBase64 = snapshot.child("img").getValue().toString();
                File f = new File(getSDPath() + "/eCard/collect", snapshot.getKey() + ".png");
                file_maps.clear();
                if (!f.exists()) {
                    UrlBean urlBean = new UrlBean();
                    urlBean.setImgUrl(snapshot.getKey());
                    urlBeanArrayList.add(urlBean);
                    Bitmap bitmap = stringToBitmap(imgBase64);
                    saveBitmap(f, bitmap, snapshot.getKey());
                    sliderData();
                } else {
                    file_maps.put("image" + snapshot.getKey(), f);
                }
            }

            public void onCancelled(SyncError arg0) {
            }

            public void onChildChanged(DataSnapshot snapshot, String arg1) {
                String imgBase64 = snapshot.child("img").getValue().toString();
                File f = new File(getSDPath() + "/eCard/collect", snapshot.getKey() + ".png");
                file_maps.clear();
                if (!f.exists()) {
                    UrlBean urlBean = new UrlBean();
                    urlBean.setImgUrl(snapshot.getKey());
                    urlBeanArrayList.add(urlBean);
                    Bitmap bitmap = stringToBitmap(imgBase64);
                    saveBitmap(f, bitmap, snapshot.getKey());
                    sliderData();
                } else {
                    file_maps.put("image" + snapshot.getKey(), f);
                }
            }

            public void onChildMoved(DataSnapshot snapshot, String arg1) {
                String imgBase64 = snapshot.child("img").getValue().toString();
                File f = new File(getSDPath() + "/eCard/collect", snapshot.getKey() + ".png");
                file_maps.clear();
                if (!f.exists()) {
                    UrlBean urlBean = new UrlBean();
                    urlBean.setImgUrl(snapshot.getKey());
                    urlBeanArrayList.add(urlBean);
                    Bitmap bitmap = stringToBitmap(imgBase64);
                    saveBitmap(f, bitmap, snapshot.getKey());
                    sliderData();
                } else {
                    file_maps.put("image" + snapshot.getKey(), f);
                }
            }

            public void onChildRemoved(DataSnapshot snapshot) {
                String imgBase64 = snapshot.child("img").getValue().toString();
                File f = new File(getSDPath() + "/eCard/collect", snapshot.getKey() + ".png");
                file_maps.clear();
                if (!f.exists()) {
                    UrlBean urlBean = new UrlBean();
                    urlBean.setImgUrl(snapshot.getKey());
                    urlBeanArrayList.add(urlBean);
                    Bitmap bitmap = stringToBitmap(imgBase64);
                    saveBitmap(f, bitmap, snapshot.getKey());
                    sliderData();
                } else {
                    file_maps.put("image" + snapshot.getKey(), f);
                }
            }
        });
    }

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            CollectFragment.this.requestPermissions( new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            addAppsData(myAddAppList);
            myAddApp();
            File f = new File(getSDPath() + "/eCard/collect");
            if (f.exists()) {
                File[] files = f.listFiles();
                if(files.length > 0) {
                    noData.setVisibility(View.GONE);
                    for (File file : files) {
                        file_maps.clear();
                        if (file.getName().contains(".png")) {
                            String[] s = file.getName().split(".png");
                            File filePut = new File(getSDPath() + "/eCard/collect", file.getName());
                            file_maps.put("image" + s[0], filePut);
                            sliderData();
                        }
                    }
                }else{
                    noData.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                addAppsData(myAddAppList);
                myAddApp();
                File f = new File(getSDPath() + "/eCard/collect");
                if (f.exists()) {
                    File[] files = f.listFiles();
                    if(files.length > 0) {
                        noData.setVisibility(View.GONE);
                        for (File file : files) {
                            file_maps.clear();
                            if (file.getName().contains(".png")) {
                                String[] s = file.getName().split(".png");
                                System.out.println(file.getName() + "=file.getName()");
                                File filePut = new File(getSDPath() + "/eCard/collect", file.getName());
                                file_maps.put("image" + s[0], filePut);
                                sliderData();
                            }
                        }
                    }else{
                        noData.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void myAddApp() {
        String uidStr = App.getWilddogAuth().getCurrentUser().getUid();
        SyncReference ref = WilddogSync.getInstance().getReference("/apps/" + uidStr);

        Query queryRef = ref.orderByValue();//加一个loading的功能，等图片加载完了再取消。
        queryRef.addChildEventListener(new ChildEventListener() {//icon, url ,name,rate
            public void onChildAdded(DataSnapshot snapshot, String ref) {
                writeToList = false;
                String key = snapshot.getKey();
                System.out.println("key=" + key);
                UrlBean urlBean = new UrlBean();
                String url = snapshot.child("url").getValue().toString();
                String icon = snapshot.child("icon").getValue().toString();
                String name = snapshot.child("name").getValue().toString();
                String rate = "";
                urlBean.setName(name);
                urlBean.setUrl(url);
                urlBean.setIcon(icon);
                urlBean.setKey(key);
                if (snapshot.child("rate").getValue() != null) {
                    rate = snapshot.child("rate").getValue().toString();
                    urlBean.setRate(rate);
                }
                if (myAddAppList.size() > 0) {
                    for (int i = 0; i < myAddAppList.size(); i++) {
                        if (myAddAppList.get(i).getUrl().equals(url)) {
                            writeToList = false;
                        } else {
                            writeToList = true;
                        }
                    }
                } else {
                    writeToList = true;
                }
                if (writeToList)
                    myAddAppList.add(urlBean);
                addAppsData(myAddAppList);
            }

            public void onCancelled(SyncError arg0) {
                System.out.println("onCancelled");
            }

            public void onChildChanged(DataSnapshot arg0, String arg1) {
            }

            public void onChildMoved(DataSnapshot arg0, String arg1) {
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int position = -1;
                String key = dataSnapshot.getKey();
                for (int i = 0; i < myAddAppList.size(); i++) {
                    if (myAddAppList.get(i).getKey().equals(key)) {
                        position = i;
                    }
                }
                if (position >= 0) {
                    myAddAppList.remove(position);
                    addAppsData(myAddAppList);
                }
            }
        });
    }

    private void addAppsData(ArrayList<UrlBean> list) {
        String uidStr = App.getWilddogAuth().getCurrentUser().getUid();
        myAdapter = new GridViewAdapter(getContext(), uidStr, list);
        myAdapter.setOnCancelEditListener(new GridViewAdapter.OnCancelEditListener() {
            @Override
            public void onCancel() {
                cancelButton.setVisibility(View.GONE);
            }
        });
        gridView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }

    public Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void saveBitmap(File f, Bitmap bm, String picName) {
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        file_maps.put("image" + picName, f);
    }

    //获取sd卡路径
    public String getSDPath() {
        File sdDir = null;
        sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        File destDir = new File(sdDir + "/eCard/collect");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return sdDir.toString();
    }

    private void sliderData() {
        for (String name : file_maps.keySet()) {
            DefaultSliderView defaultSliderView = new DefaultSliderView(getActivity());
            defaultSliderView.description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
            defaultSliderView.bundle(new Bundle());
            defaultSliderView.getBundle().putString("extra", name);
            mDemoSlider.addSlider(defaultSliderView);//加标题的话就用TextSliderView
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setDuration(4000);
        mDemoSlider.addOnPageChangeListener(this);
        mDemoSlider.stopAutoCycle();
        file_maps.clear();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
