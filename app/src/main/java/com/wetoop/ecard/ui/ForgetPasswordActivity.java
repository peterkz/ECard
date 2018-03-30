package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.listener.OnECompleteListener;
import com.wetoop.ecard.tools.CountDownTool;
import com.wilddog.wilddogauth.core.Task;

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
 * @date 2017/11/6.
 * @desc
 */
@Slug(layout = R.layout.activity_forget_password)
public class ForgetPasswordActivity extends TitleBarActivity implements Standardize {

    @BindView(R.id.register_cellphone_text)
    private EditSlugger cellphoneSlugger;
    @BindView(R.id.validate_code_text)
    private EditSlugger validateCodeSlugger;
    @BindView(R.id.new_password_text)
    private EditSlugger newPasswordSlugger;
    @FindView(R.id.viewable_button)
    private ImageView viewableButton;
    @FindView(R.id.validate_code_button)
    private Button validateCodeButton;

    private boolean viewable = false;
    private CountDownTool countTool;

    @Override
    protected boolean isHideBackButton() {
        return true;
    }

    @Override
    protected boolean isHideBottomLine() {
        return true;
    }

    @ClickView({R.id.viewable_button, R.id.complete_button})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.viewable_button:
                viewable();
                break;
            case R.id.complete_button:
                if (!validateCodeSlugger.isEmpty()) reset();
                else TOAST("请输入验证码");
                break;
        }
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        countTool = new CountDownTool(validateCodeButton);
    }

    @Override
    public void initView() {
        setCenterTitleContent("忘记密码");
        setRightButtonImageResource(R.mipmap.close_icon);
        int px = App.getInstance().dip2px(14);
        rightButton.setPadding(px, px, px, px);
    }

    @Override
    public void setListeners() {
        setOnRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        countTool.setOnCountDownListener(new CountDownTool.OnCountDownListener() {
            @Override
            public boolean onStart() {
                if (cellphoneSlugger.isEmpty()) {
                    TOAST("请输入手机号码");
                    return false;
                }
                App.getWilddogAuth().sendPasswordResetSms(cellphoneSlugger.getText()).addOnCompleteListener(new OnECompleteListener<Void>(THIS) {
                    @Override
                    public void success(Task<Void> task) {
                        TOAST("发送成功");
                    }
                });
                return true;
            }
        });
    }

    @Override
    public void onCreateLast() {

    }

    private void reset() {
        App.getWilddogAuth()
                .confirmPasswordResetSms(cellphoneSlugger.getText(), validateCodeSlugger.getText(), newPasswordSlugger.getText())
                .addOnCompleteListener(new OnECompleteListener<Void>(THIS) {
                    @Override
                    public void success(Task<Void> task) {
                        TOAST("修改成功");
                        finish();
                    }
                });
    }

    public void viewable() {
        if (viewable) {
            newPasswordSlugger.getView().setTransformationMethod(PasswordTransformationMethod.getInstance());
            viewableButton.setImageResource(R.mipmap.disviewable_icon);
        } else {
            newPasswordSlugger.getView().setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            viewableButton.setImageResource(R.mipmap.viewable_icon);
        }
        newPasswordSlugger.getView().requestLayout();
        viewable = !viewable;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countTool.finish();
    }
}
