package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.ecard.R;

import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.BaseActivity;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;
import cn.edots.slug.listener.OnTextWatcher;

/**
 * @author Parck.
 * @date 2017/11/13.
 * @desc
 */

@Slug(layout = R.layout.activity_search_account)
public class SearchAccountActivity extends BaseActivity implements Standardize {

    @BindView(R.id.search_text)
    private EditSlugger searchSlugger;
    @FindView(R.id.clear_button)
    private ImageView clearButton;
    @FindView(R.id.cancel_text)
    private TextView searchButton;

    @Override
    @ClickView({R.id.cancel_text, R.id.clear_button})
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.cancel_text:
                if (searchSlugger.isEmpty()) finish();
                break;
            case R.id.clear_button:
                searchSlugger.getView().setText("");
                break;
        }
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {

    }

    @Override
    public void initView() {

    }

    @Override
    public void setListeners() {
        searchSlugger.addTextChangedListener(new OnTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    searchButton.setText("搜索");
                    clearButton.setVisibility(View.VISIBLE);
                } else {
                    searchButton.setText("取消");
                    clearButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onCreateLast() {

    }
}
