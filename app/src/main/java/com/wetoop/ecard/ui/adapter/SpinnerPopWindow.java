package com.wetoop.ecard.ui.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.wetoop.ecard.R;

/**
 * Created by User on 2017/11/1.
 */

public class SpinnerPopWindow extends PopupWindow implements AdapterView.OnItemClickListener {

    private Context mContext;
    private ListView mListView;
    private MainMenuSpinnerAdapter mAdapter;
    private MainMenuSpinnerAdapter.IOnItemSelectListener mItemSelectListener;

    public SpinnerPopWindow(Context context) {
        super(context);
        mContext = context;
        init();
    }


    public void setItemListener(MainMenuSpinnerAdapter.IOnItemSelectListener listener) {
        mItemSelectListener = listener;
    }

    public void setAdapter(MainMenuSpinnerAdapter adapter) {
        mAdapter = adapter;
        mListView.setAdapter(mAdapter);
    }


    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.menu_spiner_window_layout, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00);
        setBackgroundDrawable(dw);

        mListView = (ListView) view.findViewById(R.id.menu_spinner_listView);
        mListView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
        dismiss();
        if (mItemSelectListener != null) {
            mItemSelectListener.onItemClick(pos);
        }
    }

}
