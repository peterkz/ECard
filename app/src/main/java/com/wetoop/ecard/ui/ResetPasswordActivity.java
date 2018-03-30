package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ImageView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.listener.OnECompleteListener;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.result.AuthResult;

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
 * @date 2017/10/16.
 * @desc 更改密码
 */
@Slug(layout = R.layout.activity_reset_password)
public class ResetPasswordActivity extends TitleBarActivity implements Standardize {

    @BindView(R.id.old_password_text)
    private EditSlugger oldPasswordSlugger;
    @BindView(R.id.new_password_text)
    private EditSlugger newPasswordSlugger;
    @BindView(R.id.validate_code_text)
    private EditSlugger validateCodeSlugger;
    @FindView(R.id.old_viewable_button)
    private ImageView oldViewableButton;
    @FindView(R.id.new_viewable_button)
    private ImageView newViewableButton;

    private boolean viewable = false;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {

    }

    @Override
    public void initView() {
        setCenterTitleContent("修改密码");
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onCreateLast() {

    }

    @ClickView({R.id.sure_button, R.id.validate_code_button, R.id.old_viewable_button, R.id.new_viewable_button})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.validate_code_button:
                requestValidateCode();
                break;
            case R.id.sure_button:
                if (oldPasswordSlugger.isEmpty()) {
                    TOAST("请输旧密码");
                    return;
                }
                if (newPasswordSlugger.isEmpty()) {
                    TOAST("请输入新密码");
                    return;
                }
                reset();
                break;
            case R.id.old_viewable_button:
                oldViewable();
                break;
            case R.id.new_viewable_button:
                newViewable();
                break;
        }
    }

    private void requestValidateCode() {
        String phone = App.getWilddogAuth().getCurrentUser().getPhone();
        App.getWilddogAuth().sendPasswordResetSms(phone).addOnCompleteListener(new OnECompleteListener<Void>(THIS) {
            @Override
            public void success(Task<Void> task) {
                TOAST("发送成功");
            }
        });
    }

    private void reset() {
        App.getWilddogAuth()
                .signInWithPhoneAndPassword(App.getWilddogAuth().getCurrentUser().getPhone(), oldPasswordSlugger.getText())
                .addOnCompleteListener(new OnECompleteListener<AuthResult>(THIS) {
                    @Override
                    public void success(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            App.getWilddogAuth().getCurrentUser().updatePassword(newPasswordSlugger.getText());
                            TOAST("修改成功！");
                            finish();
                        } else {
                            oldPasswordSlugger.setText("");
                            newPasswordSlugger.setText("");
                            TOAST("原密码输入错误！");
                        }
                    }
                });
    }

    public void oldViewable() {
        if (viewable) {
            oldPasswordSlugger.getView().setTransformationMethod(PasswordTransformationMethod.getInstance());
            oldViewableButton.setImageResource(R.mipmap.disviewable_icon);
        } else {
            oldPasswordSlugger.getView().setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            oldViewableButton.setImageResource(R.mipmap.viewable_icon);
        }
        newPasswordSlugger.getView().requestLayout();
        viewable = !viewable;
    }

    public void newViewable() {
        if (viewable) {
            newPasswordSlugger.getView().setTransformationMethod(PasswordTransformationMethod.getInstance());
            newViewableButton.setImageResource(R.mipmap.disviewable_icon);
        } else {
            newPasswordSlugger.getView().setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            newViewableButton.setImageResource(R.mipmap.viewable_icon);
        }
        newPasswordSlugger.getView().requestLayout();
        viewable = !viewable;
    }
}