package com.wetoop.ecard.api;

import java.util.HashSet;
import java.util.Set;

import cn.edots.nest.core.cache.Session;

/**
 * @author Parck.
 * @date 2017/11/23.
 * @desc
 */

public class APIProvider {

    private static Set<String> keys = new HashSet<>();

    public static <T extends Object> T get(Class<T> clazz) {
        if (Session.getAttribute(clazz.getSimpleName()) == null)
            try {
                String key = clazz.getSimpleName();
                keys.add(key);
                Session.setAttribute(key, clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        return (T) Session.getAttribute(clazz.getSimpleName());
    }

    public static void clear() {
        for (String key : keys)
            Session.remove(key);
    }
}
