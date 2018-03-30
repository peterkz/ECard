package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.view.View;

import com.wetoop.ecard.R;

import java.io.Serializable;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.slug.annotation.Slug;

/**
 * @author Parck.
 * @date 2017/10/16.
 * @desc
 */

@Slug(layout = R.layout.activity_error)
public class ErrorActivity extends TitleBarActivity implements Standardize {

    public static String ERROR_MODEL_KEY = "ERROR_MODEL_KEY";
    private ErrorModel model;

    private onRefreshListener onRefreshListener;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        if (map != null) {
            model = (ErrorModel) map.get(ERROR_MODEL_KEY);
        } else {
            TOAST("信息缺失，请重试！");
            finish();
        }
    }

    @Override
    public void initView() {
        setCenterTitleContent(model.getTitle());
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onCreateLast() {

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.refresh_button:
                if (onRefreshListener != null) {
                    onRefreshListener.onRefresh(v);
                }
                break;
        }
    }

    public void setOnRefreshListener(onRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    // =============================================================================================
    // inner class
    // =============================================================================================

    public interface onRefreshListener extends Serializable {
        void onRefresh(View v);
    }

    public static class ErrorModel implements Serializable {
        private static final long serialVersionUID = 85491414024794672L;

        private String title;
        private String url;

        public ErrorModel() {

        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
