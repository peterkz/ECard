package com.wetoop.ecard.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wetoop.ecard.R;

/**
 * @author Parck.
 * @date 2017/12/4.
 * @desc
 */

public class SelectorButton extends LinearLayout {

    private ImageView button;
    private boolean selected;
    private OnSelectorListener onSelectorListener;

    public SelectorButton(Context context) {
        super(context);
    }

    public SelectorButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.selector_button_view, this);
        button = (ImageView) view.findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelected(isSelected());
                if (onSelectorListener != null) onSelectorListener.onSelected(selected, v);
            }
        });
    }

    public void setSelected(boolean selector) {
        this.selected = selector;
        if (selector) {
            button.setImageResource(R.mipmap.selector_button_open);
        } else {
            button.setImageResource(R.mipmap.selector_button_close);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setOnSelectorListener(OnSelectorListener onSelectorListener) {
        this.onSelectorListener = onSelectorListener;
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public interface OnSelectorListener {
        void onSelected(boolean selected, View view);
    }
}
