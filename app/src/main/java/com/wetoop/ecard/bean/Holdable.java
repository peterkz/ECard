package com.wetoop.ecard.bean;

import android.view.View;

import java.io.Serializable;

import cn.edots.nest.ui.adapter.RecyclerViewAdapter;

/**
 * @author Parck.
 * @date 2017/10/26.
 * @desc
 */

public interface Holdable extends Serializable {

    void holding(RecyclerViewAdapter.ViewHolder holder);

    void onClicked(View v);

}
