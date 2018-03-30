package com.wetoop.ecard.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.wetoop.ecard.R;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.ValueEventListener;
import com.wilddog.client.WilddogSync;

import cn.edots.nest.ui.BaseActivity;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

import static com.wetoop.ecard.Constants.URI_PUBLIC_TERMS;

/**
 * Created by User on 2017/9/1.
 */
@Slug(layout = R.layout.activity_agreement)
public class AgreementActivity extends BaseActivity {

    @FindView(R.id.add_agree_text)
    private TextView addAgreeText;
    @FindView(R.id.back_button)
    private TextView backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 获取 SyncReference 实例
        SyncReference ref = WilddogSync.getInstance().getReference(URI_PUBLIC_TERMS);
        // 设置监听
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    addAgreeText.setText(dataSnapshot.getValue().toString());
                    System.out.println("onDataChange=" + dataSnapshot.toString());
                }
            }

            @Override
            public void onCancelled(SyncError syncError) {
                if (syncError != null) {
                    System.out.println("onCancelled=" + syncError.toString());
                }
            }
        });
    }

    private void onData() {
        SyncReference ref = WilddogSync.getInstance().getReference(URI_PUBLIC_TERMS);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fullName = (String) dataSnapshot.child("full_name").getValue();
                String gender = (String) dataSnapshot.child("gender").getValue();
                System.out.println(fullName + " 性别为" + gender);
                // ...
            }

            @Override
            public void onCancelled(SyncError syncError) {
                // 获取数据失败，打印错误信息。
                System.out.println("loadPost:onCancelled=" + syncError.toString());
                // ...
            }
        };
        SyncReference postReference = ref.child("/web/saving-data/wildblog/users/Jobs");
        postReference.addValueEventListener(postListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return false;
    }
}
