package com.wetoop.ecard.bean;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.wetoop.ecard.ui.dialog.LoadingDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Parck.
 * @date 2017/10/19.
 * @desc
 */

public class ChinaBean implements Serializable {

    private static final long serialVersionUID = -6303353694866811679L;

    private List<Province> province;

    public List<Province> getProvince() {
        return province;
    }

    public void setProvince(List<Province> province) {
        this.province = province;
    }

    public static class Province {

        private String name;
        private List<City> city;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<City> getCity() {
            return city;
        }

        public void setCity(List<City> city) {
            this.city = city;
        }

        public static class City {

            private String name;
            private List<String> area;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<String> getArea() {
                return area;
            }

            public void setArea(List<String> area) {
                this.area = area;
            }
        }
    }


    public static void loadChina(final Context context, final OnLoadListener listener) {
        LoadingDialog.show((Activity) context);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LoadingDialog.hide();
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        listener.onSuccess((ChinaBean) msg.obj);
                        break;
                    case 2:
                        listener.onFail((IOException) msg.obj);
                        break;
                }
            }
        };
        new Thread() { // 应该用线程池管理线程
            @Override
            public void run() {
                Message message = Message.obtain();
                try {
                    InputStream is = context.getAssets().open("china.json");
                    int available = is.available();
                    byte[] buffer = new byte[available];
                    is.read(buffer);
                    String json = new String(buffer);
                    Gson mapper = new Gson();
                    message.what = 1;
                    message.obj = mapper.fromJson(json, ChinaBean.class);
                    handler.sendMessage(message);
                } catch (IOException e) {
                    message.what = 2;
                    message.obj = e;
                    handler.sendMessage(message);
                }
            }
        }.start();
    }

    public static Observable<ChinaBean> rxLoad(final Context context) {
        return Observable.create(new Observable.OnSubscribe<ChinaBean>() {
            @Override
            public void call(Subscriber<? super ChinaBean> subscriber) {
                try {
                    InputStream is = context.getAssets().open("china.json");
                    int available = is.available();
                    byte[] buffer = new byte[available];
                    is.read(buffer);
                    String json = new String(buffer);
                    ChinaBean china = new Gson().fromJson(json, ChinaBean.class);
                    subscriber.onNext(china);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //=========================================================================
    // inner class
    //=========================================================================
    public interface OnLoadListener {

        void onSuccess(ChinaBean china);

        void onFail(IOException e);
    }
}
