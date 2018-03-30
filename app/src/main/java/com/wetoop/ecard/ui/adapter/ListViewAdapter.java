package com.wetoop.ecard.ui.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wetoop.ecard.R;

import java.util.List;

/**
 * @author Parck.
 * @date 2017/10/17.
 * @desc
 */
public abstract class ListViewAdapter<T> extends BaseAdapter {

    private final static int DEFAULT_ITEM = 0;
    private final static int LOAD_MORE_ITEM = 1;

    private
    @LayoutRes
    int layoutId;
    private Context context;
    private List<T> data;

    private boolean isLoadMore;
    private ViewHolder holder;
    private MoreHolder moreHolder;

    public ListViewAdapter(Context context, @LayoutRes int layoutId, List<T> data) {
        this.context = context;
        this.layoutId = layoutId;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size() + (isLoadMore() ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return position != data.size() ? DEFAULT_ITEM : LOAD_MORE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + (isLoadMore() ? 1 : 0);
    }

    @Override
    public T getItem(int position) {
        if (getViewTypeCount() > 1) {
            switch (getItemViewType(position)) {
                case DEFAULT_ITEM:
                    return data.get(position);
                case LOAD_MORE_ITEM:
                    break;
                default:
                    break;
            }
            return null;
        } else {
            return data.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case DEFAULT_ITEM:
                if (convertView == null) {
                    holder = new ViewHolder(context, convertView, parent, layoutId);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                setting(holder, getItem(position), position);
                return holder.getItemView();
            case LOAD_MORE_ITEM:
                if (moreHolder == null) {
                    moreHolder = new MoreHolder(context, convertView, parent, R.layout.item_load_more, new MoreHolder.OnLoadingListener() {

                        @Override
                        public void loading(MoreHolder holder) {
                            onLoad(holder);
                        }
                    });
                } else {
                    moreHolder = (MoreHolder) convertView.getTag();
                }
                return moreHolder.getItemView();
        }
        return null;
    }

    protected abstract void setting(ViewHolder holder, T data, int position);

    public boolean isLoadMore() {
        return isLoadMore;
    }

    protected void onLoad(MoreHolder holder) {

    }

    //=======================================================================
    // inner class
    //=======================================================================

    public static class ViewHolder {
        private View itemView;
        private SparseArray<View> viewContainer;

        public ViewHolder(Context context, View convertView, ViewGroup parent, @LayoutRes int layoutId) {
            convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
            this.viewContainer = new SparseArray<>();
            this.itemView = convertView;
            convertView.setTag(this);
        }

        public <V extends View> V findViewById(@IdRes int id) {
            View view = viewContainer.get(id);
            if (view == null) {
                view = itemView.findViewById(id);
                viewContainer.put(id, view);
            }
            return (V) view;
        }

        public void setText(@IdRes int id, CharSequence text) {
            ((TextView) findViewById(id)).setText(text);
        }

        public void setOnItemClickListener(View.OnClickListener listener) {
            if (listener != null) this.itemView.setOnClickListener(listener);
        }

        public View getItemView() {
            return itemView;
        }
    }

    public static class MoreHolder extends ViewHolder {
        private LinearLayout loadingLayout;
        private LinearLayout successLayout;
        private LinearLayout failLayout;
        private LinearLayout noMoreLayout;

        private OnLoadingListener onLoadingListener;
        private LoadState state;

        public MoreHolder(Context context, View convertView, ViewGroup parent, @LayoutRes int layoutId, OnLoadingListener onLoadingListener) {
            super(context, convertView, parent, layoutId);
            this.onLoadingListener = onLoadingListener;
            init();
        }

        public void setState(LoadState state) {
            this.loadingLayout.setVisibility(state.equals(LoadState.LOADING) ? View.VISIBLE : View.GONE);
            this.successLayout.setVisibility(state.equals(LoadState.SUCCESS) ? View.VISIBLE : View.GONE);
            this.failLayout.setVisibility(state.equals(LoadState.FAIL) ? View.VISIBLE : View.GONE);
            this.noMoreLayout.setVisibility(state.equals(LoadState.NO_MORE) ? View.VISIBLE : View.GONE);
            this.state = state;
            switch (state) {
                case LOADING:
                    loading();
                    break;
                case SUCCESS:
                    success();
                    break;
                case FAIL:
                    fail();
                    break;
                case NO_MORE:
                    noMore();
                    break;
            }
        }

        public void init() {
            this.loadingLayout = this.findViewById(R.id.loading_layout);
            this.successLayout = this.findViewById(R.id.success_layout);
            this.failLayout = this.findViewById(R.id.fail_layout);
            this.noMoreLayout = this.findViewById(R.id.no_more_layout);
            this.startLoading();
        }

        @Override
        public void setOnItemClickListener(View.OnClickListener listener) {
            switch (state) {
                case FAIL:
                    loading();
                    break;
            }
            super.setOnItemClickListener(listener);
        }

        private void loading() {
            if (onLoadingListener != null) onLoadingListener.loading(this);
        }

        private void success() {
        }

        private void fail() {
        }

        private void noMore() {
        }

        public void startLoading() {
            this.getItemView().setVisibility(View.VISIBLE);
            setState(LoadState.LOADING);
        }

        public void stopLoading() {
            this.getItemView().setVisibility(View.GONE);
        }

        public enum LoadState {
            LOADING, SUCCESS, FAIL, NO_MORE
        }

        public interface OnLoadingListener {
            void loading(MoreHolder holder);
        }
    }
}
