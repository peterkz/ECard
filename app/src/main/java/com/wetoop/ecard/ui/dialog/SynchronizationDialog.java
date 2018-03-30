package com.wetoop.ecard.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.ecard.R;
import com.wetoop.ecard.ui.SynchronizationActivity;

/**
 * Created by User on 2017/11/17.
 */

public class SynchronizationDialog extends Dialog{
    private ImageView chance;
    private RelativeLayout cancel,agree;
    private TextView agreeMessage;
    private Context context;
    private Boolean aBoolean = true;
    private OnCustomDialogListener onCustomDialogListener;
    public SynchronizationDialog(@NonNull Context context,OnCustomDialogListener onCustomDialogListener) {
        super(context);
        this.context = context;
        this.onCustomDialogListener = onCustomDialogListener;
    }
    public interface OnCustomDialogListener {
        public void back(int type);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_synchronization);
        chance = (ImageView) findViewById(R.id.chance);
        cancel = (RelativeLayout)findViewById(R.id.cancel);
        agree = (RelativeLayout)findViewById(R.id.agree);
        agreeMessage = (TextView)findViewById(R.id.agreeMessage);
        clickEvent();
    }
    private void clickEvent(){
        chance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aBoolean){
                    chance.setImageResource(R.drawable.chance2);
                    aBoolean = false;
                }else{
                    chance.setImageResource(R.drawable.chance1);
                    aBoolean = true;
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SynchronizationDialog.this.dismiss();
            }
        });
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aBoolean){
                    onCustomDialogListener.back(1);
                }else{
                    Toast.makeText(context,"请先点击同意协议",Toast.LENGTH_SHORT).show();
                }
            }
        });
        agreeMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SynchronizationActivity.class);
                context.startActivity(intent);
            }
        });
    }
}
