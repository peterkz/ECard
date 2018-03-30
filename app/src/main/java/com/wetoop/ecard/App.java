package com.wetoop.ecard;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.util.Base64;
import android.view.View;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.bugly.crashreport.CrashReport;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.bean.UrlBean;
import com.wetoop.ecard.api.TencentOSS;
import com.wetoop.ecard.ui.MainActivity;
import com.wetoop.ecard.ui.fragment.ContactPersonFragment;
import com.wetoop.ecard.ui.fragment.DiscoverFragment;
import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.model.WilddogUser;
import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edots.nest.core.SlugResourceProvider;
import cn.edots.nest.core.cache.FragmentPool;

import static com.wetoop.ecard.Constants.URI_ROOT_SYNC;

/**
 * Created by User on 2017/8/29.
 */

public class App extends Application implements SlugResourceProvider {
    public static Long loadingStartTime;
    private static App sInstance;
    public static final String PREFS_NAME = "App";
    private ArrayList<UrlBean> myAddAppList = new ArrayList<>();
    private ArrayList<String> contactsCardIdList = new ArrayList<>();
    private List<CardBean> mineCards = new ArrayList<>();
    private List<String> mineCardIds = new ArrayList<>();
    private CardBean cardBean;
    private static ObjectMapper mapper;
    private MainActivity mainActivity;
    private int unreadExchangeMessageCount = 0;
    private int unreadUpdateMessageCount = 0;
    private final String UNREAD_EXCHANGE_MESSAGE_COUNT_KEY = "UNREAD_EXCHANGE_MESSAGE_COUNT_KEY";
    private final String UNREAD_UPDATE_MESSAGE_COUNT_KEY = "UNREAD_UPDATE_MESSAGE_COUNT_KEY";
    private static TencentOSS oss;

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public Activity activity;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public int getUnreadExchangeMessageCount() {
        return unreadExchangeMessageCount;
    }

    public void setUnreadExchangeMessageCount(int unreadExchangeMessageCount) {
        this.unreadExchangeMessageCount = unreadExchangeMessageCount;
        put(UNREAD_EXCHANGE_MESSAGE_COUNT_KEY, this.unreadExchangeMessageCount);
        ContactPersonFragment fragment = FragmentPool.getFragment(ContactPersonFragment.class);
        if (fragment == null) return;
        if (mainActivity == null) return;
        if (unreadExchangeMessageCount != 0) {
            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[]{100, 300, 100, 200}, -1);
            if (mainActivity.contactUnreadTagView == null) return;
            mainActivity.contactUnreadTagView.setVisibility(View.VISIBLE);
            if (fragment.unreadTagView == null) return;
            fragment.unreadTagView.setVisibility(View.VISIBLE);
            fragment.unreadTagView.setText(String.valueOf(this.unreadExchangeMessageCount));
        } else {
            if (mainActivity.contactUnreadTagView == null) return;
            mainActivity.contactUnreadTagView.setVisibility(View.GONE);
            if (fragment.unreadTagView == null) return;
            fragment.unreadTagView.setVisibility(View.GONE);
        }
    }

    public int getUnreadUpdateMessageCount() {
        return unreadUpdateMessageCount;
    }

    public void setUnreadUpdateMessageCount(int unreadUpdateMessageCount) {
        this.unreadUpdateMessageCount = unreadUpdateMessageCount;
        put(UNREAD_UPDATE_MESSAGE_COUNT_KEY, this.unreadUpdateMessageCount);
        DiscoverFragment fragment = FragmentPool.getFragment(DiscoverFragment.class);
        if (fragment == null) return;
        if (mainActivity == null) return;
        if (unreadUpdateMessageCount != 0) {
            if (mainActivity.discoverUnreadTagView == null) return;
            mainActivity.discoverUnreadTagView.setVisibility(View.VISIBLE);
            if (fragment.contactUpdateItem == null || fragment.adapter == null) return;
            fragment.contactUpdateItem.setUnread(String.valueOf(this.unreadUpdateMessageCount));
            fragment.adapter.notifyDataSetChanged();
        } else {
            if (mainActivity.discoverUnreadTagView == null) return;
            mainActivity.discoverUnreadTagView.setVisibility(View.GONE);
            if (fragment.contactUpdateItem == null || fragment.adapter == null) return;
            fragment.contactUpdateItem.setUnread(null);
            fragment.adapter.notifyDataSetChanged();
        }
    }

    public static TencentOSS oss() {
        return oss;
    }

    @Override
    public void onCreate() {
        loadingStartTime = System.currentTimeMillis();
        sInstance = this;
        super.onCreate();
        mapper = new ObjectMapper();
        init();
    }

    public void init() {
        CrashReport.initCrashReport(getApplicationContext(), "296c6a6a2c", false);
        WilddogOptions options = new WilddogOptions.Builder().setSyncUrl(URI_ROOT_SYNC).build();
        WilddogApp.initializeApp(this, options);
        WilddogSync.getInstance().setPersistenceEnabled(true);//开启数据持久化
        oss = new TencentOSS();
        oss.init(this);
    }

    public void initUnreadMessage() {
        this.unreadExchangeMessageCount = get(UNREAD_EXCHANGE_MESSAGE_COUNT_KEY, Integer.class);
        this.unreadUpdateMessageCount = get(UNREAD_UPDATE_MESSAGE_COUNT_KEY, Integer.class);
        setUnreadExchangeMessageCount(this.unreadExchangeMessageCount);
        setUnreadUpdateMessageCount(this.unreadUpdateMessageCount);
    }

    public <T extends Serializable> T get(String key, Class<T> clazz) {
        SharedPreferences sp = getSharedPreferences("ecard_share_data", Context.MODE_PRIVATE);
        if (clazz.equals(Integer.class)) {
            return (T) Integer.valueOf(sp.getInt(key, 0));
        } else if (clazz.equals(String.class)) {
            return (T) sp.getString(key, null);
        }
        return null;
    }

    public void put(String key, Object value) {
        SharedPreferences sp = getSharedPreferences("ecard_share_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (value instanceof Integer)
            editor.putInt(key, (Integer) value);
        else if (value instanceof String)
            editor.putString(key, (String) value);
        else return;
        editor.commit();
    }

    public static App getInstance() {
        return sInstance;
    }

    public static WilddogAuth getWilddogAuth() {
        return WilddogAuth.getInstance();
    }

    public static WilddogUser getCurrentUser() {
        return getWilddogAuth().getCurrentUser();
    }

    public static SyncReference getReference(String uri) {
        return WilddogSync.getInstance().getReference(uri);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public int getFirstUsed() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("firstUsed", 0);
    }

    public void setFirstUsed(int firstUsed) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putInt("firstUsed", firstUsed).commit();
    }

    public CardBean getCardBean() {
        return cardBean;
    }

    public void setCardBean(CardBean cardBean) {
        this.cardBean = cardBean;
    }

    public ArrayList<UrlBean> getMyAddList() {
        return myAddAppList;
    }

    public void setMyAddAppList(ArrayList<UrlBean> list) {
        myAddAppList = list;
    }

    public static String getErrorMessage(int error) {
        String message = "";
        switch (error) {
            case 22001:
                message = "服务异常，操作失败";
                break;
            case 22002:
                message = "登录过期";
                break;
            case 22005:
                message = "用户创建失败，请重试";
                break;
            case 22008:
                message = "身份认证提供商调用错误，请联系野狗 support@wilddog.com";
                break;
            case 22009:
                message = "该邮箱地址无效";
                break;
            case 22010:
                message = "该密码不正确";
                break;
            case 22011:
                message = "该用户不存在";
                break;
            case 22012:
                message = "身份认证过程中，发生了安全错误";
                break;
            case 22013:
                message = "该邮箱地址已经使用";
                break;
            case 22014:
                message = "该身份认证凭证无效";
                break;
            case 22015:
                message = "该身份认证参数无效";
                break;
            case 22018:
                message = "本次重置密码请求无效的";
                break;
            case 22203:
                message = "邮箱地址已经被其他账户使用";
                break;
            case 22204:
                message = "该身份已经与其他账户绑定";
                break;
            case 22205:
                message = "该账户没有绑定邮箱";
                break;
            case 22206:
                message = "没有对应用户记录，该用户可能已经被删除";
                break;
            case 22209:
                message = "该用户尝试安全敏感操作，但登录时间过长，需重新登录";
                break;
            case 22210:
                message = "该用户没有E卡的登录方式";
                break;
            case 22211:
                message = "密码的长度必须在 6 到 32 位";
                break;
            case 22212:
                message = "昵称长度必须小于 20 位";
                break;
            case 22219:
                message = "该手机号码不正确";
                break;
            case 22220:
                message = "该邮箱不存在";
                break;
            case 22221:
                message = "该手机号不存在";
                break;
            case 22222:
                message = "该手机未发送过验证码，请检查";
                break;
            case 22223:
                message = "发送验证码发生错误，请重试";
                break;
            case 22224:
                message = "该手机号已被其他账户使用";
                break;
            case 22225:
                message = "照片地址或昵称包含非法字符";
                break;
            case 22226:
                message = "短信验证码错误，请重新发送验证码";
                break;
            case 22227:
                message = "短信服务错误，请重试";
                break;
            case 22235:
                message = "短信发送过于频繁";
                break;
            case 29999:
                message = "发生未知错误";
                break;
        }
        return message;
    }

    public static Bitmap string2Bitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 是否Debug模式
     *
     * @return
     */
    @Override
    public boolean isDebug() {
        return true;
    }

    /**
     * 设置返回按钮图片
     *
     * @return 返回按钮图片
     */
    @Override
    public int getBackButtonImageResource() {
        return R.mipmap.back_icon;
    }

    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public ArrayList<String> getContactsCardIdList() {
        return contactsCardIdList;
    }

    public void setContactsCardIdList(ArrayList<String> contactsCardIdList) {
        this.contactsCardIdList = contactsCardIdList;
    }

    public List<CardBean> getMineCards() {
        return mineCards;
    }

    public void setMineCards(List<CardBean> mineCards) {
        this.mineCards = mineCards;
    }

    public List<String> getMineCardIds() {
        return mineCardIds;
    }

    public void setMineCardIds(List<String> mineCardIds) {
        this.mineCardIds = mineCardIds;
    }
}
