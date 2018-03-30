package com.wetoop.ecard.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.SearchAccountAPI;
import com.wetoop.ecard.api.model.Account;
import com.wetoop.ecard.listener.OnECompleteListener;
import com.wetoop.ecard.tools.CountDownTool;
import com.wetoop.ecard.tools.MD5;
import com.wetoop.ecard.ui.adapter.ECardPagerAdapter;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.model.WilddogUser;

import java.util.HashMap;
import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.core.cache.Session;
import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;
import cn.edots.slug.core.TextSlugger;

import static com.wetoop.ecard.Constants.CURRENT_USER;

/**
 * @author Parck.
 * @date 2017/10/12.
 * @desc
 */
@Slug(layout = R.layout.activity_register_validate)
public class RegisterValidateActivity extends TitleBarActivity implements Standardize {

    @FindView(R.id.avatar_image)
    private ImageView avatarImage;
    @BindView(R.id.cellphone_text)
    private TextSlugger cellphoneSlugger;
    @BindView(R.id.validate_code_text)
    private EditSlugger validateCodeSlugger;
    @FindView(R.id.validate_code_button)
    private Button validateCodeButton;
    @FindView(R.id.agreement_text)
    private TextView agreementText;
    @FindView(R.id.login_now_text)
    private TextView loginNowText;

    private WilddogUser wilddogUser;
    private CountDownTool countDownTool;
    private String password;

    @Override
    protected boolean isHideBottomLine() {
        return true;
    }

    @ClickView({R.id.agreement_text, R.id.complete_button, R.id.login_now_text})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.agreement_text:
                startActivity(AgreementActivity.class);
                break;
            case R.id.complete_button:
                onValidate();
                break;
            case R.id.login_now_text:
                startActivity(LoginActivity.class);
                finish();
                break;
        }
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        if (map != null) {
            cellphoneSlugger.setText((String) map.get("cellphone"));
            password = (String) map.get("password");
        }else{
            cellphoneSlugger.setText(App.getCurrentUser().getPhone());
            leftButton.setVisibility(View.INVISIBLE);
        }
        wilddogUser = App.getWilddogAuth().getCurrentUser();
        countDownTool = new CountDownTool(validateCodeButton);
    }

    @Override
    public void initView() {
        setCenterTitleContent("验证手机号");
        String agreementHtml = "注册代表您已同意<font color=\"#529EEA\">信使网络用户协议</font>";
        agreementText.setText(Html.fromHtml(agreementHtml));
        Spanned loginHtml = Html.fromHtml("已有账号，立即<font color=\"#529EEA\">登录</font>");
        loginNowText.setText(loginHtml);
    }

    @Override
    public void setListeners() {
        countDownTool.setOnCountDownListener(new CountDownTool.OnCountDownListener() {
            @Override
            public boolean onStart() {
                getValidateCode();
                return true;
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
                if (task.isSuccessful()) {
                    if(!TextUtils.isEmpty(password)){
                        doLogin(password);
                    }else{
                        TOAST("验证通过");
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("cellphone", cellphoneSlugger.getText());
                        startActivity(LoginActivity.class, data);
                        finish();
                    }
                } else {
                    TOAST("验证失败");
                }
            }
        });
    }

    private void getValidateCode() {
        wilddogUser.sendPhoneVerification().addOnCompleteListener(THIS, new OnECompleteListener<Void>(THIS) {
            @Override
            public void success(Task<Void> task) {
                TOAST("验证码获取成功");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTool.finish();
    }

    private void doLogin(String password) {
        final String username = cellphoneSlugger.getText();
        App.getWilddogAuth().signInWithPhoneAndPassword(username, password).addOnCompleteListener(new OnECompleteListener<AuthResult>(THIS) {
            @Override
            public void success(Task<AuthResult> task) {
                WilddogUser user = task.getResult().getWilddogUser();
                Session.setAttribute(CURRENT_USER, user);
                String uid = task.getResult().getWilddogUser().getUid();
                    Account account = new Account();
                    account.setPhone(MD5.encoding(user.getPhone()));
                    account.setUser_id(user.getUid());
                    APIProvider.get(SearchAccountAPI.class).create(account, new SyncReference.CompletionListener() {
                        @Override
                        public void onComplete(SyncError syncError, SyncReference syncReference) {
                            if (syncError != null) {
                                logger.e("syncError : " + syncError.getMessage());
                            }
                        }
                    });
                    ECardPagerAdapter.initFragmentContainer();
                    TOAST("登录成功");
                    startActivity(MainActivity.class);
                    finish();
            }
        });
    }
}
