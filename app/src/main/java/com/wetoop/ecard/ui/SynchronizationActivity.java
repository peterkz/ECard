package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.widget.TextView;

import com.wetoop.ecard.R;

import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

/**
 * Created by User on 2017/12/21.
 */
@Slug(layout = R.layout.activity_synchronization)
public class SynchronizationActivity extends TitleBarActivity implements Standardize{
    @FindView(R.id.textViewHeader)
    private TextView textViewHeader;
    @FindView(R.id.textViewFooter)
    private TextView textViewFooter;
    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        textViewHeader.setText(R.string.syHeader);
        textViewFooter.setText(R.string.syFooter);
    }

    @Override
    public void initView() {
        setCenterTitleContent("同步须知");
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onCreateLast() {

    }
}
