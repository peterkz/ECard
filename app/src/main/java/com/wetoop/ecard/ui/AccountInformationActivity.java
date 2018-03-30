package com.wetoop.ecard.ui;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.api.APIProvider;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.ui.dialog.ConfirmDialog;
import com.wetoop.ecard.ui.dialog.OnClickListener;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.model.WilddogUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * @date 2017/10/13.
 * @desc 新账号信息设置页面，第二步
 */
@Slug(layout = R.layout.activity_account_information)
public class AccountInformationActivity extends TitleBarActivity implements Standardize {

    @BindView(R.id.cellphone_text)
    private EditSlugger cellphoneSlugger;
    @BindView(R.id.confirm_text)
    private EditSlugger confirmSlugger;
    @FindView(R.id.sure_button)
    private Button sureButton;

    private List<Card> myCard = new ArrayList<>();
    private List<Card> myContact = new ArrayList<>();
    private int errorCount = 0;

    @Override
    public void setupData(@Nullable Map<String, Object> map) {

    }

    @Override
    public void initView() {
        setCenterTitleContent("账号信息转移 (第2步)");
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onCreateLast() {

    }

    @ClickView({R.id.sure_button})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.sure_button:
                if (cellphoneSlugger.isEmpty()) {
                    TOAST("请输入手机号码");
                    return;
                }
                if (confirmSlugger.isEmpty()) {
                    TOAST("请确认手机号码");
                    return;
                }
                if (!confirmSlugger.getText().equals(cellphoneSlugger.getText())) {
                    TOAST("两次输入号码不一致");
                    return;
                }
                new ConfirmDialog.Builder()
                        .setMessage("请确认是否要执行此操作？")
                        .setRightButton(R.color.light_red, new OnClickListener() {
                            @Override
                            public boolean onClick(View v) {
                                account();
                                return true;
                            }
                        }).build().show(THIS);
                break;
        }
    }

    private void account() {
        final WilddogUser user = App.getCurrentUser();
        user.updatePhone(cellphoneSlugger.getText())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            TOAST("修改成功");
                            App.getWilddogAuth().signOut();
                            APIProvider.clear();
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("cellphone", cellphoneSlugger.getText());
                            startActivity(LoginActivity.class, data);
                            finish();
                            App.getInstance().getMainActivity().finish();
                            App.getInstance().setMainActivity(null);
                            App.getInstance().getActivity().finish();
                            App.getInstance().setActivity(null);
                        } else {
                            logger.d(task.getException().toString());
                            TOAST("该号码已被注册");
                        }
                    }
                });
    }
}
