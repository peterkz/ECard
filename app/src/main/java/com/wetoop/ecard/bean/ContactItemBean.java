package com.wetoop.ecard.bean;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.tools.RoundTransformation;

import java.util.Date;

import cn.edots.nest.core.Gradable;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;

/**
 * @author Parck.
 * @date 2017/10/23.
 * @desc
 */

public class ContactItemBean extends AbsBean implements Holdable, Gradable {

    private static final long serialVersionUID = 6472742654344583975L;

    private String avatar;
    private String name;
    private String unread;
    private String userId;
    private String cardId;
    private Date dateUpdated;

    private int type;
    private boolean hideLine;

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnread() {
        return unread;
    }

    public void setUnread(String unread) {
        this.unread = unread;
    }

    public boolean isHideLine() {
        return hideLine;
    }

    public void setHideLine(boolean hideLine) {
        this.hideLine = hideLine;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Override
    public void holding(RecyclerViewAdapter.ViewHolder holder) {
        ImageView avatarImage = holder.findViewById(R.id.item_image);
        Glide.with(holder.getContext())
                .load(App.oss().getURL(cardId))
                .placeholder(R.mipmap.default_avatar_icon)
                .transform(new RoundTransformation(holder.getContext()))

                .signature(new StringSignature(String.valueOf(dateUpdated.getTime())))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)

                .into(avatarImage);
        TextView nameText = holder.findViewById(R.id.item_name);
        if (name != null) {
            nameText.setText(name);
        } else {
            nameText.setText("");
        }

        TextView unreadText = holder.findViewById(R.id.unread_tag);
        if (unread != null) {
            unreadText.setVisibility(View.VISIBLE);
            unreadText.setText(unread);
        } else {
            unreadText.setVisibility(View.GONE);
        }

        View line = holder.findViewById(R.id.item_line);
        if (hideLine) {
            line.setVisibility(View.GONE);
        } else {
            line.setVisibility(View.VISIBLE);
        }

        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClicked(v);
            }
        });
    }

    @Override
    public void onClicked(View v) {

    }
}
