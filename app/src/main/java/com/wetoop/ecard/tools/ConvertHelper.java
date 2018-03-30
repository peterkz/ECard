package com.wetoop.ecard.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wetoop.ecard.App;
import com.wilddog.client.DataSnapshot;

import java.io.IOException;
import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Parck.
 * @date 2017/11/9.
 * @desc
 */

public class ConvertHelper {

    public static <T extends Object> T convert(DataSnapshot dataSnapshot, Class<T> clazz) {
        if (dataSnapshot == null || clazz == null) return null;
        try {
            Object value = dataSnapshot.getValue();
            if (value == null) return null;
            String json = App.getMapper().writeValueAsString(value);
            if (json == null) return null;
            return App.getMapper().readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends Object> Observable<T> rxConvert(final DataSnapshot dataSnapshot, final Class<T> clazz) {
        if (dataSnapshot == null || clazz == null) return null;
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                try {
                    Object value = dataSnapshot.getValue();
                    if (value == null) {
                        subscriber.onNext(null);
                        return;
                    }
                    String json = App.getMapper().writeValueAsString(value);
                    if (json == null) {
                        subscriber.onNext(null);
                        return;
                    }
                    subscriber.onNext(App.getMapper().readValue(json, clazz));
                } catch (IOException e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T extends Object> T convert(DataSnapshot dataSnapshot, TypeReference<T> clazz) {
        if (dataSnapshot == null || clazz == null) return null;
        try {
            Object value = dataSnapshot.getValue();
            if (value == null) return null;
            String json = App.getMapper().writeValueAsString(((HashMap<Object, Object>) value).values());
            if (json == null) return null;
            T t = App.getMapper().readValue(json, clazz);
            return t;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends Object> Observable<T> rxConvert(final DataSnapshot dataSnapshot, final TypeReference<T> clazz) {
        if (dataSnapshot == null || clazz == null) return null;
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                String json = null;
                try {
                    Object value = dataSnapshot.getValue();
                    if (value == null) {
                        subscriber.onNext(null);
                        return;
                    }
                    json = App.getMapper().writeValueAsString(((HashMap<Object, Object>) value).values());
                    if (json == null) {
                        subscriber.onNext(null);
                        return;
                    }
                    T t = App.getMapper().readValue(json, clazz);
                    subscriber.onNext(t);
                } catch (IOException e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                } finally {
                    json = null;
                    System.gc();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}