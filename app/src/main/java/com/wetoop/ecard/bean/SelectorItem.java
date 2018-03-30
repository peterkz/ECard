package com.wetoop.ecard.bean;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;

import cn.edots.nest.core.Gradable;
import cn.edots.nest.ui.adapter.RecyclerViewAdapter;

/**
 * @author Parck.
 * @date 2017/10/26.
 * @desc
 */

public class SelectorItem extends AbsBean implements Holdable, Gradable {

    private static final long serialVersionUID = -5052952943822512750L;

    private int type;
    private
    @DrawableRes
    int leftImageResId;
    private String label;
    private String value;
    private String unread;
    private boolean showCount;
    private
    @DrawableRes
    int rightImageResId;
    private boolean showShort;
    private boolean showGuide = true;
    private int tag;

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnread() {
        return unread;
    }

    public void setUnread(String unread) {
        this.unread = unread;
    }

    public int getLeftImageResId() {
        return leftImageResId;
    }

    public void setLeftImageResId(int leftImageResId) {
        this.leftImageResId = leftImageResId;
    }

    public int getRightImageResId() {
        return rightImageResId;
    }

    public void setRightImageResId(int rightImageResId) {
        this.rightImageResId = rightImageResId;
    }

    public boolean isShowShort() {
        return showShort;
    }

    public void setShowShort(boolean showShort) {
        this.showShort = showShort;
    }

    public boolean isShowGuide() {
        return showGuide;
    }

    public void setShowGuide(boolean showGuide) {
        this.showGuide = showGuide;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public boolean isShowCount() {
        return showCount;
    }

    public void setShowCount(boolean showCount) {
        this.showCount = showCount;
    }

    @Override
    public void holding(RecyclerViewAdapter.ViewHolder holder) {
        TextView labelView = holder.findViewById(R.id.item_label);
        if (label != null) {
            labelView.setText(label);
        }

        holder.setText(R.id.item_value, value == null ? "" : value);

        TextView unreadCountTag = holder.findViewById(R.id.unread_count_tag);
        TextView unreadTag = holder.findViewById(R.id.unread_tag);
        if (!TextUtils.isEmpty(unread)) {
            if (showCount) {
                unreadTag.setVisibility(View.VISIBLE);
                unreadTag.setText(unread);
            } else {
                unreadTag.setVisibility(View.VISIBLE);
            }
        } else {
            unreadTag.setVisibility(View.GONE);
        }

        ImageView leftImage = holder.findViewById(R.id.left_image);
        if (leftImageResId != 0) {
            leftImage.setVisibility(View.VISIBLE);
            leftImage.setImageResource(leftImageResId);
            labelView.setPadding(0, 0, 0, 0);
        } else {
            labelView.setPadding(App.getInstance().dip2px(5), 0, 0, 0);
            leftImage.setVisibility(View.GONE);
        }

        ImageView rightImage = holder.findViewById(R.id.right_image);
        if (rightImageResId != 0) {
            rightImage.setVisibility(View.VISIBLE);
            rightImage.setImageResource(rightImageResId);
        }

        if (showGuide) {
            rightImage.setVisibility(View.VISIBLE);
        } else {
            rightImage.setVisibility(View.GONE);
        }

        View shortLine = holder.findViewById(R.id.short_line);
        View longLine = holder.findViewById(R.id.long_line);
        if (showShort) {
            shortLine.setVisibility(View.VISIBLE);
            longLine.setVisibility(View.GONE);
        } else {
            longLine.setVisibility(View.VISIBLE);
            shortLine.setVisibility(View.GONE);
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
