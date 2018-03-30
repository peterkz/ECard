package com.wetoop.ecard.bean;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.ui.CollectWebActivity;

import java.io.Serializable;

import cn.edots.nest.ui.adapter.RecyclerViewAdapter;

/**
 * Created by User on 2017/9/4.
 */

public class UrlBean implements Serializable {

    private static final long serialVersionUID = 5766351123511691925L;

    private String id;
    private String description;
    private String ImgUrl;
    private String name;
    private String key;
    private String url;
    private String android;
    private String rate;
    private String icon;


    public void holding(RecyclerViewAdapter.ViewHolder holder) {
        ImageView appsIcon = holder.findViewById(R.id.appsIcon);
        TextView appsName = holder.findViewById(R.id.appsName);
        Button itemButton = holder.findViewById(R.id.item_button);

        Picasso.with(App.getInstance()).load(icon).into(appsIcon);
        appsName.setText(name);

        itemButton.setText("详情");
        itemButton.setBackgroundResource(R.drawable.shape_blue_solid_r2_bg);

        itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, CollectWebActivity.class);
                intent.putExtra("url", android);
                context.startActivity(intent);
            }
        });
    }

    public String getImgUrl() {
        return ImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        ImgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAndroid() {
        return android;
    }

    public void setAndroid(String android) {
        this.android = android;
    }
}
