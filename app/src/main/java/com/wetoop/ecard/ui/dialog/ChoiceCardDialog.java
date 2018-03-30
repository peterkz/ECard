package com.wetoop.ecard.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.CardBean;

import cn.edots.nest.ui.adapter.RecyclerViewAdapter;

/**
 * Created by User on 2017/11/1.
 */

public class ChoiceCardDialog {

    private Dialog dialog;

    private Activity context;
    private CardBean target;
    private RecyclerViewAdapter adapter;
    private RecyclerView recyclerView;

    public ChoiceCardDialog(final Activity context, final OnChoiceListener listener) {
        if (this.dialog == null) {
            this.dialog = new Dialog(context, R.style.ConfirmDialogTheme);
            this.dialog.setContentView(R.layout.dialog_choice_card);
            Window window = this.dialog.getWindow();
            if (window != null) window.setDimAmount(0.3f);
            this.dialog.setCanceledOnTouchOutside(true);
        }
        init(context, listener);
    }

    private void init(final Activity context, final OnChoiceListener listener) {
        final TextView titleView = (TextView) this.dialog.findViewById(R.id.title_view);
        recyclerView = (RecyclerView) this.dialog.findViewById(R.id.recycle_view);
        TextView cancelView = (TextView) this.dialog.findViewById(R.id.cancel);
        TextView submitView = (TextView) this.dialog.findViewById(R.id.submit);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter<CardBean>(context, R.layout.item_card_id_text, App.getInstance().getMineCards()) {
            @Override
            protected void binding(ViewHolder holder, final CardBean o, int i) {
                TextView nameView = holder.findViewById(R.id.name_view);
                nameView.setText(TextUtils.isEmpty(o.getInformation().getCard_label())
                        ? TextUtils.isEmpty(o.getInformation().getName())
                        ? "未填写" : o.getInformation().getName()
                        : o.getInformation().getCard_label());
                if (o.isSelect()) {
                    nameView.setBackgroundResource(R.drawable.shape_blue_solid_r4);
                    nameView.setTextColor(context.getResources().getColor(R.color.white));
                } else {
                    nameView.setBackgroundResource(R.drawable.shape_blue_line_r4);
                    nameView.setTextColor(context.getResources().getColor(R.color.blueColor));
                }
                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (CardBean bean : App.getInstance().getMineCards()) {
                            bean.setSelect(false);
                        }
                        o.setSelect(true);
                        target = o;
                        notifyDataSetChanged();
                    }
                });
            }
        };
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (target != null)
                    target.setSelect(false);
            }
        });

        submitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (target == null) {
                    Toast.makeText(context, "请选择名片", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (listener != null) {
                    listener.onChoice(target);
                    if (target != null) target.setSelect(false);
                    dialog.dismiss();
                }
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (target != null) target.setSelect(false);
            }
        });
    }

    public void show() {
        if (this.dialog != null && !this.dialog.isShowing()) {
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            this.dialog.show();

        }
    }

    public void hide() {
        if (this.dialog != null) {
            this.dialog.dismiss();
            this.dialog = null;
        }
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public interface OnChoiceListener {
        void onChoice(CardBean target);
    }

}
