package com.wetoop.ecard.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.UrlBean;
import com.wetoop.ecard.ui.dialog.ConfirmDialog;
import com.wetoop.ecard.ui.dialog.OnClickListener;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;

import java.util.ArrayList;

/**
 * Created by User on 2017/9/6.
 */

public class GridViewAdapter extends BaseAdapter {

    private Context context;
    private String uid;
    private int size;
    private OnCancelEditListener onCancelEditListener;

    public ArrayList<UrlBean> list = new ArrayList<>();
    public boolean isShowDelete;
    public TextView name;

    public GridViewAdapter(Context context, String uid, ArrayList<UrlBean> list) {
        this.context = context;
        this.list = list;
        this.uid = uid;
        setSize(list.size() + 1);
    }

    public void setIsShowDelete(boolean isShowDelete, int size) {
        this.isShowDelete = isShowDelete;
        setSize(size);
        notifyDataSetChanged();
    }

    public boolean isShowDelete() {
        return isShowDelete;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int getCount() {
        return size;//注意此处
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        ImageView iv = (ImageView) convertView.findViewById(R.id.itemImage);
        name = (TextView) convertView.findViewById(R.id.itemText);
        RelativeLayout closeR = (RelativeLayout) convertView.findViewById(R.id.closeR);
        if (position < list.size()) {
            Bitmap bitmap = App.string2Bitmap(list.get(position).getIcon());
            iv.setImageBitmap(bitmap);
            name.setText(list.get(position).getName());
            closeR.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);
        } else {
            closeR.setVisibility(View.GONE);
            iv.setBackgroundResource(R.mipmap.collect_more_icon);//最后一个显示加号图片
            name.setText("更多");
        }

        closeR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConfirmDialog.Builder()
                        .setMessage("删除应用，同时删除该应用下的数据及信息吗？")
                        .setRightButton(R.color.light_red, new OnClickListener() {
                            @Override
                            public boolean onClick(View v) {
                                WilddogSync.getInstance().getReference("/apps/" + uid + "/" + list.get(position).getKey())
                                        .removeValue(new SyncReference.CompletionListener() {
                                            @Override
                                            public void onComplete(SyncError syncError, SyncReference syncReference) {
                                                if (syncError != null) {
                                                    Toast.makeText(context, syncError.getMessage(), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                                                }
                                                if (onCancelEditListener != null) {
                                                    onCancelEditListener.onCancel();
                                                }
                                            }
                                        });
                                return true;
                            }
                        }).build().show((Activity) context);
            }
        });
        return convertView;
    }

    public void setOnCancelEditListener(OnCancelEditListener onCancelEditListener) {
        this.onCancelEditListener = onCancelEditListener;
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public interface OnCancelEditListener {
        void onCancel();
    }

}

