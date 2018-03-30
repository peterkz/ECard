package com.wetoop.ecard.tools;

import com.wetoop.ecard.bean.ContactBean;

import java.util.Comparator;

/**
 * Created by User on 2017/10/9.
 */

public class PinyinComparator implements Comparator<ContactBean> {

    public int compare(ContactBean o1, ContactBean o2) {
        if ("@".equals(o1.getSpellFirst())
                || "#".equals(o2.getSpellFirst())) {
            return -1;
        } else if ("#".equals(o1.getSpellFirst())
                || "@".equals(o2.getSpellFirst())) {
            return 1;
        } else {
            return o1.getSpellFirst().compareTo(o2.getSpellFirst());
        }
    }

}
