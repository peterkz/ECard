package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.listener.OnECompleteListener;
import com.wetoop.ecard.tools.CountDownTool;
import com.wetoop.ecard.tools.InputMethodTool;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.model.WilddogUser;

import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;
import cn.edots.slug.listener.OnTextWatcher;

/**
 * @author Parck.
 * @date 2017/10/13.
 * @desc 账号信息转移第一步
 */
@Slug(layout = R.layout.activity_account_validate)
public class AccountValidateActivity extends TitleBarActivity implements Standardize {

    @BindView(R.id.cellphone_text)
    private EditSlugger cellphoneSlugger;
    @BindView(R.id.validate_code_text)
    private EditSlugger validateCodeSlugger;
    @FindView(R.id.validate_code_button)
    private TextView validateCodeButton;
    @FindView(R.id.next_button)
    private Button nextButton;

    private WilddogUser wilddogUser;
    private CountDownTool countDownTool;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        countDownTool = new CountDownTool(validateCodeButton);
        wilddogUser = App.getWilddogAuth().getCurrentUser();
        cellphoneSlugger.setText(wilddogUser.getPhone());
    }

    @Override
    public void initView() {
        setCenterTitleContent("账号信息转移 (第1步)");
        nextButton.setEnabled(false);
        nextButton.setClickable(false);
    }

    @Override
    public void setListeners() {
        countDownTool.setOnCountDownListener(new CountDownTool.OnCountDownListener() {
            @Override
            public boolean onStart() {
                if (cellphoneSlugger.isEmpty()) {
                    TOAST("请输入手机号码");
                    countDownTool.stop();
                    return false;
                }
                wilddogUser.sendPhoneVerification().addOnCompleteListener(new OnECompleteListener<Void>(THIS) {
                    @Override
                    public void success(Task<Void> task) {
                        TOAST("获取验证码成功！");
                    }
                });
                return true;
            }
        });

        validateCodeSlugger.addTextChangedListener(new OnTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    nextButton.setEnabled(true);
                    nextButton.setClickable(true);
                } else {
                    nextButton.setEnabled(false);
                    nextButton.setClickable(false);
                }
            }
        });

        cellphoneSlugger.getView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) InputMethodTool.cancelInput(v);
                else InputMethodTool.requestInput(v);
            }
        });

        validateCodeSlugger.getView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) InputMethodTool.cancelInput(v);
                else InputMethodTool.requestInput(v);
            }
        });
    }

    @Override
    public void onCreateLast() {

    }

    private void onValidate() {
        String validateCode = validateCodeSlugger.getText();
        if (TextUtils.isEmpty(validateCode)) {
            TOAST("请输入验证码");
            return;
        }
        wilddogUser.verifiyPhone(validateCode).addOnCompleteListener(THIS, new OnECompleteListener<Void>(THIS) {
            @Override
            public void success(Task<Void> task) {
                startActivity(AccountInformationActivity.class);
                finish();
            }
        });
    }

    @ClickView({R.id.next_button})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.next_button:
                onValidate();
                break;
        }
    }
}
