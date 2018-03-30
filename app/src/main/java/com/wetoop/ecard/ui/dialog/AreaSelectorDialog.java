package com.wetoop.ecard.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.ChinaBean;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.ui.widget.WheelView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Parck.
 * @date 2017/10/19.
 * @desc
 */

public class AreaSelectorDialog {

    private Activity activity;
    private Dialog areaSelectorDialog;
    private WheelView areaWheelView;
    private WheelView cityWheelView;
    private WheelView provinceWheelView;
    private TextView cancelText;
    private TextView sureText;
    private boolean selectArea;
    private boolean showed = false;
    private List<String> provinces = new ArrayList<>();
    private List<String> cities = new ArrayList<>();
    private List<String> areas;
    private ChinaBean.Province province;
    private ChinaBean.Province.City city;
    private AreaDetail area;

    public AreaSelectorDialog(Activity activity, OnClickListener listener) {
        init(activity, true);
        initView();
        initListener(listener);
    }

    public AreaSelectorDialog(Activity activity, boolean selectArea, OnClickListener listener) {
        init(activity, selectArea);
        initView();
        initListener(listener);
    }

    private void init(Activity activity, boolean selectArea) {
        if (this.areaSelectorDialog == null) {
            this.areaSelectorDialog = new Dialog(activity, R.style.AreaSelectorDialogTheme);
            this.areaSelectorDialog.setContentView(R.layout.dialog_area_selector);
            this.areaSelectorDialog.getWindow().setDimAmount(0.2f);
            this.areaSelectorDialog.setCanceledOnTouchOutside(true);
            this.areaSelectorDialog.getWindow().setGravity(Gravity.BOTTOM);
            Window window = this.areaSelectorDialog.getWindow();
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
        this.activity = activity;
        this.selectArea = selectArea;
    }

    private void initView() {
        this.areaWheelView = (WheelView) this.areaSelectorDialog.findViewById(R.id.area_wheel_view);
        this.cityWheelView = (WheelView) this.areaSelectorDialog.findViewById(R.id.city_wheel_view);
        this.provinceWheelView = (WheelView) this.areaSelectorDialog.findViewById(R.id.province_wheel_view);
        this.cancelText = (TextView) this.areaSelectorDialog.findViewById(R.id.cancel_text);
        this.sureText = (TextView) this.areaSelectorDialog.findViewById(R.id.sure_text);
        if (!selectArea) {
            areaWheelView.setVisibility(View.GONE);
        }
    }

    private void initListener(final OnClickListener listener) {
        this.areaSelectorDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) listener.onCancel();
            }
        });

        this.cancelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        this.sureText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onSure(area);
                hide();
            }
        });
    }

    public void show() {
        if (!showed) {
            showed = true;
            LoadingDialog.show(activity);
            ChinaBean.rxLoad(activity).subscribe(new OnESubscriber<ChinaBean>() {
                @Override
                protected void onComplete(boolean success, final ChinaBean china, Throwable e) {
                    if (success) {
                        area = new AreaDetail();
                        provinces.clear();
                        for (ChinaBean.Province province : china.getProvince()) {
                            provinces.add(province.getName());
                        }
                        provinceWheelView.setItemsReFlush(provinces);
                        provinceWheelView.setOnSelectedListener(0, new WheelView.OnSelectedListener() {

                            @Override
                            public void onSelected(final int provincePosition, final String provinceText) {
                                province = china.getProvince().get(provincePosition - 1);
                                area.setProvince(province.getName());
                                cities.clear();
                                for (ChinaBean.Province.City city : province.getCity()) {
                                    cities.add(city.getName());
                                }
                                cityWheelView.setItemsReFlush(cities);
                                cityWheelView.setOnSelectedListener(0, new WheelView.OnSelectedListener() {
                                    @Override
                                    public void onSelected(int cityPosition, final String cityText) {
                                        try {
                                            List<ChinaBean.Province.City> cities = china.getProvince().get(provincePosition - 1).getCity();
                                            AreaSelectorDialog.this.city = cities.get(cityPosition - 1);
                                            area.setCity(AreaSelectorDialog.this.city.getName());
                                            if (selectArea) {
                                                areas = AreaSelectorDialog.this.city.getArea();
                                                areaWheelView.setItemsReFlush(areas);
                                                areaWheelView.setOnSelectedListener(0, new WheelView.OnSelectedListener() {
                                                    @Override
                                                    public void onSelected(int position, String areaText) {
                                                        area.setArea(areas.get(position - 1));
                                                    }
                                                });
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                if (areaSelectorDialog != null) areaSelectorDialog.show();
                            }
                        });
                    }
                }

            });
        } else {
            areaSelectorDialog.show();
        }
    }

    public void hide() {
        if (this.areaSelectorDialog != null) {
            this.areaSelectorDialog.dismiss();
        }
    }

    // =====================================================
    // inner class
    // =====================================================

    public static abstract class OnClickListener {

        public abstract void onSure(AreaDetail area);

        public void onCancel() {

        }
    }

    public static class AreaDetail implements Serializable {

        private static final long serialVersionUID = -8707049943939634030L;

        private String province = "";
        private String city = "";
        private String area = "";
        private String addressEditStr = "";

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public String getAddressEditStr() {
            return addressEditStr;
        }

        public void setAddressEditStr(String addressEditStr) {
            this.addressEditStr = addressEditStr;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(province)) sb.append(this.province).append("-");
            if (!TextUtils.isEmpty(city)) sb.append(this.city).append("-");
            if (!TextUtils.isEmpty(area)) sb.append(this.area);
            return sb.toString();
        }
    }
}
