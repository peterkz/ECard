package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;

import com.wetoop.ecard.R;

import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.slug.annotation.Slug;

/**
 * @author Parck.
 * @date 2017/10/24.
 * @desc
 */
@Slug(layout = R.layout.activity_score)
public class ScoreActivity extends TitleBarActivity implements Standardize {

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
    }

    @Override
    public void initView() {
        setCenterTitleContent("我的e积分");
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onCreateLast() {

    }

    // =============================================================================================
    // inner class
    // =============================================================================================

    public class PagerBean {

    }
}
