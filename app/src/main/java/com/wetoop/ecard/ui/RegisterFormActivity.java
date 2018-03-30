package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.listener.OnECompleteListener;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.result.AuthResult;

import java.util.HashMap;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;

/**
 * @author Parck.
 * @date 2017/10/12.
 * @desc 注册表单界面
 */
@Slug(layout = R.layout.activity_register_form)
public class RegisterFormActivity extends TitleBarActivity implements Standardize {

    @FindView(R.id.avatar_image)
    private ImageView avatarImage;
    @BindView(R.id.register_cellphone_text)
    private EditSlugger cellphoneSlugger;
    @BindView(R.id.register_password_text)
    private EditSlugger passwordSlugger;
    @FindView(R.id.login_now_text)
    private TextView loginNowText;

    @Override
    protected boolean isHideBackButton() {
        return true;
    }

    @Override
    protected boolean isHideBottomLine() {
        return true;
    }

    @ClickView({R.id.next_button, R.id.login_now_text})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.next_button:
                onRegister();
                break;
            case R.id.login_now_text:
                startActivity(LoginActivity.class);
                finish();
                break;
        }
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
    }

    @Override
    public void initView() {
        setCenterTitleContent("注册账号");
        Spanned html = Html.fromHtml("已有账号，立即<font color=\"#529EEA\">登录</font>");
        loginNowText.setText(html);
        setRightButtonImageResource(R.mipmap.close_icon);
        int px = App.getInstance().dip2px(14);
        rightButton.setPadding(px, px, px, px);
    }

    @Override
    public void setListeners() {
        setOnRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LoginActivity.class);
                finish();
            }
        });
    }

    @Override
    public void onCreateLast() {

    }

    private void onRegister() {
        final String cellphone = cellphoneSlugger.getText();
        final String password = passwordSlugger.getText();
        if (TextUtils.isEmpty(cellphone)) {
            TOAST("请输入手机号");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            TOAST("请输入密码");
            return;
        }
        App.getWilddogAuth().createUserWithPhoneAndPassword(cellphone, password).addOnCompleteListener(this, new OnECompleteListener<AuthResult>(THIS) {
            @Override
            public void success(Task<AuthResult> task) {
                TOAST("注册成功");
                HashMap<String, Object> data = new HashMap<>();
                data.put("cellphone", cellphone);//
                data.put("password", password);
                startActivity(RegisterValidateActivity.class, data);
                finish();
            }
        });
    }
}