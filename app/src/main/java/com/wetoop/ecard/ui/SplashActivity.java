package com.wetoop.ecard.ui;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.tools.RoundTransform;
import com.wetoop.ecard.ui.adapter.ECardPagerAdapter;

import java.util.Map;

import cn.edots.nest.core.Standardize;
import cn.edots.nest.ui.BaseActivity;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;

import static com.wetoop.ecard.App.loadingStartTime;

/**
 * @author Parck.
 * @date 2017/9/29.
 * @desc
 */
@Slug(layout = R.layout.activity_splash)
public class SplashActivity extends BaseActivity implements Standardize {

    private Handler handler = new Handler();
    private static final long TIMEOUT_TIME = 8000;
    private static final long WAIT_TIME = 800;

    @FindView(R.id.icon_view)
    private ImageView iconView;

    @Override
    protected boolean isFeatureNoTitle() {
        return true;
    }

    @Override
    public void setupData(@Nullable Map<String, Object> map) {
        if (loadingStartTime == null) {
            loadingStartTime = System.currentTimeMillis();
        }
        //启动MainActivity前实例化各个pager对象
        ECardPagerAdapter.initFragmentContainer();
    }

    @Override
    public void initView() {
        iconView.setImageBitmap(new RoundTransform(App.getInstance().dip2px(100)).transform(BitmapFactory.decodeResource(THIS.getResources(), R.mipmap.ecard_ic_launcher)));
    }

    @Override
    public void setListeners() {

    }

    @Override
    public void onCreateLast() {
        checkAccess();
    }

    /**
     * 认证登录
     */
    private void checkAccess() {
        startLoginTimeoutTask();
        // 验证身份
        if (App.getWilddogAuth().getCurrentUser() != null) {
            if(App.getWilddogAuth().getCurrentUser().isPhoneVerified()) {
                loadingStartTime = System.currentTimeMillis() - loadingStartTime;
                logger.i("Application initialize consumption time :" + loadingStartTime + "ms");
                startToMainPagerTask();
            }else{
                startToRegisterValidateTask();
            }
        } else {
            startToLoginPagerTask();
        }
    }

    /**
     * 登录超时任务
     */
    private void startLoginTimeoutTask() {
        loginTimeoutTask = new Runnable() {
            @Override
            public void run() {
                if (toMainPagerTask != null) handler.removeCallbacks(toMainPagerTask);
                if (toLoginPagerTask != null) handler.removeCallbacks(toLoginPagerTask);
                startActivity(LoginActivity.class);
                TOAST("登录超时");
            }
        };
        handler.postDelayed(loginTimeoutTask, TIMEOUT_TIME);
    }

    /**
     * 跳转主页任务
     */
    public void startToMainPagerTask() {
        handler.postDelayed(toMainPagerTask, loadingStartTime > WAIT_TIME ? 0 : WAIT_TIME - loadingStartTime); // 保证 8s >= 界面展示时间 >= 2s
    }

    /**
     * 跳转登录页面任务
     */
    public void startToLoginPagerTask() {
        handler.postDelayed(toLoginPagerTask, loadingStartTime > WAIT_TIME ? 0 : WAIT_TIME - loadingStartTime); // 保证 8s >= 界面展示时间 >= 2s
    }

    public void startToRegisterValidateTask() {
        handler.postDelayed(toRegisterValidateTask, loadingStartTime > WAIT_TIME ? 0 : WAIT_TIME - loadingStartTime); // 保证 8s >= 界面展示时间 >= 2s
    }

    private Runnable loginTimeoutTask;
    private Runnable toMainPagerTask = new Runnable() {
        @Override
        public void run() {
            if (loginTimeoutTask != null) handler.removeCallbacks(loginTimeoutTask);
            startActivity(MainActivity.class);
            finish();
        }
    };

    private Runnable toLoginPagerTask = new Runnable() {
        @Override
        public void run() {
            if (loginTimeoutTask != null) handler.removeCallbacks(loginTimeoutTask);
            startActivity(LoginActivity.class);
            finish();
        }
    };

    private Runnable toRegisterValidateTask = new Runnable() {
        @Override
        public void run() {
            if (loginTimeoutTask != null) handler.removeCallbacks(loginTimeoutTask);
            startActivity(RegisterValidateActivity.class);
            finish();
        }
    };

}
