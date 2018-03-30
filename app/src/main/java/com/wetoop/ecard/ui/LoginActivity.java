package com.wetoop.ecard.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.SearchAccountAPI;
import com.wetoop.ecard.api.model.Account;
import com.wetoop.ecard.listener.OnECompleteListener;
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
import cn.edots.nest.ui.BaseActivity;
import cn.edots.slug.annotation.BindView;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import cn.edots.slug.core.EditSlugger;

import static com.wetoop.ecard.Constants.CURRENT_USER;

/**
 * @author Parck.
 * @date 2017/8/29.
 * @desc
 */
@Slug(layout = R.layout.activity_login)
public class LoginActivity extends BaseActivity implements Standardize {

    @FindView(R.id.avatar_image)
    private ImageView avatarImage;
    @BindView(R.id.login_cellphone_text)
    private EditSlugger cellphoneSlugger;
    @BindView(R.id.login_password_text)
    private EditSlugger passwordSlugger;
    private Dialog callNotifyDialogP;

    @Override
    protected boolean isTranslucentStatus() {
        return true;
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        if (map != null) {
            cellphoneSlugger.setText((String) map.get("cellphone"));
        }
    }

    @Override
    public void initView() {

    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onCreateLast() {

    }

    @ClickView({R.id.login_button, R.id.register_button, R.id.forget_button})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.login_button:
                doLogin();
                break;
            case R.id.register_button:
                startActivity(RegisterFormActivity.class);
                finish();
                break;
            case R.id.forget_button:
                startActivity(ForgetPasswordActivity.class);
                break;
        }
    }

    private void doLogin() {
        final String username = cellphoneSlugger.getText();
        String password = passwordSlugger.getText();
        if (TextUtils.isEmpty(username)) {
            TOAST("请输入手机号");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            TOAST("请输入密码");
            return;
        }//dea0437e3de8388f134d539eb631
        App.getWilddogAuth().signInWithPhoneAndPassword(username, password).addOnCompleteListener(new OnECompleteListener<AuthResult>(THIS) {
            @Override
            public void success(Task<AuthResult> task) {
                WilddogUser user = task.getResult().getWilddogUser();
                Session.setAttribute(CURRENT_USER, user);
                String uid = task.getResult().getWilddogUser().getUid();
                logger.e("uid="+uid);
                if (user.isPhoneVerified()) {
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
                } else {
                    final AlertDialog.Builder callNotifyDialog = new AlertDialog.Builder(THIS);
                    callNotifyDialog.setTitle("提示");
                    callNotifyDialog.setMessage("您的手机号未验证，请前往验证");
                    callNotifyDialog.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("cellphone", username);
                            startActivity(RegisterValidateActivity.class, data);
                            callNotifyDialogP.dismiss();
                        }
                    });
                    callNotifyDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            callNotifyDialogP.dismiss();
                        }
                    });
                    callNotifyDialogP = callNotifyDialog.create();
                    callNotifyDialogP.show();
                }
            }
        });
    }
}
