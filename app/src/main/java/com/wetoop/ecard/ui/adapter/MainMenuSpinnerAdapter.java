package com.wetoop.ecard.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.MainMenuItem;

import java.util.List;

/**
 * Created by User on 2017/11/1.
 */

public class MainMenuSpinnerAdapter extends BaseAdapter {
    private List<MainMenuItem> mainMenuItemArrayList;
    private Context context;

    public MainMenuSpinnerAdapter(Context context, List<MainMenuItem> mainMenuItemArrayList) {
        this.mainMenuItemArrayList = mainMenuItemArrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mainMenuItemArrayList.size();
    }

    @Override
    public Object getItem(int pos) {
        return mainMenuItemArrayList.get(pos).toString();
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_main_menu, parent, false);
        }
        TextView menu_text = (TextView) convertView.findViewById(R.id.menu_text);
        ImageView menu_image = (ImageView) convertView.findViewById(R.id.menu_image);
        MainMenuItem data = mainMenuItemArrayList.get(position);
        menu_text.setText(TextUtils.isEmpty(data.getName()) ? "" : data.getName());
        if (data.getIconRes() != 0) menu_image.setImageResource(data.getIconRes());
        else menu_image.setVisibility(View.GONE);
        return convertView;
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public interface IOnItemSelectListener {
        void onItemClick(int pos);
    }
}
