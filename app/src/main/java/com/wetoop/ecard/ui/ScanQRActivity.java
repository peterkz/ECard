package com.wetoop.ecard.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.listener.OnESubscriber;
import com.wetoop.ecard.tools.BitmapTool;
import com.wetoop.ecard.tools.GetImagePath;
import com.wetoop.ecard.tools.RegularToQR;
import com.wetoop.ecard.ui.dialog.LoadingDialog;
import com.wetoop.ecard.zxing.BeepManager;
import com.wetoop.ecard.zxing.CameraManager;
import com.wetoop.ecard.zxing.InactivityTimer;
import com.wetoop.ecard.zxing.ScanQRActivityHandler;
import com.wetoop.ecard.zxing.decode.DecodeThread;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;

import cn.edots.nest.ui.TitleBarActivity;
import cn.edots.slug.annotation.ClickView;
import cn.edots.slug.annotation.FindView;
import cn.edots.slug.annotation.Slug;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by User on 2017/10/11.
 */
@Slug(layout = R.layout.activity_scan_qr)
public class ScanQRActivity extends TitleBarActivity implements SurfaceHolder.Callback {

    @FindView(R.id.capture_preview)
    private SurfaceView scanPreview;
    @FindView(R.id.capture_container)
    private RelativeLayout scanContainer;
    @FindView(R.id.capture_crop_view)
    private RelativeLayout scanCropView;
    @FindView(R.id.capture_scan_line)
    private ImageView scanLine;
    @FindView(R.id.light_button)
    private ImageView lightButton;

    private CameraManager cameraManager;
    private ScanQRActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private Camera camera;

    private Rect mCropRect = null;
    private boolean isHasSurface = false;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @ClickView({R.id.light_button})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.light_button:
                if (cameraManager.flashControlHandler()) {
                    lightButton.setImageResource(R.mipmap.light_open_icon);
                } else {
                    lightButton.setImageResource(R.mipmap.light_close_icon);
                }
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);
        initViews();
    }

    private void initViews() {
        setCenterTitleContent("扫描二维码");
        setRightTextContent("相册", R.color.blueColor, _16SP);
        setOnRightTextListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocalImage();
            }
        });
    }

    @Override
    protected boolean isHideBottomLine() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraManager = new CameraManager(getApplication());
        handler = null;
        if (isHasSurface) {
            initCamera(scanPreview.getHolder());
        } else {
            scanPreview.getHolder().addCallback(this);
        }
        inactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            logger.e("*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of onSuccess and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param bundle    The extras
     */
    public void handleDecode(Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        logger.e("rawResult=" + rawResult.getText());
        if (rawResult.getText().indexOf("card_id") > 0) {
            App app = App.getInstance();
            boolean myCardIdEqual = false;
            boolean contactsCardIdEqual = false;
            String cardId = RegularToQR.patternCardId(rawResult.getText());
            logger.e("cardId=" + cardId);
            if (cardId != null) {
                for (int i = 0; i < app.getMineCards().size(); i++) {
                    if (cardId.equals(app.getMineCards().get(i).getInformation().getCard_id())) {
                        myCardIdEqual = true;
                    }
                }
                for (int i = 0; i < app.getContactsCardIdList().size(); i++) {
                    if (cardId.equals(app.getContactsCardIdList().get(i))) {
                        contactsCardIdEqual = true;
                    }
                }
            }
            final boolean finalMyCardIdEqual = myCardIdEqual;
            final boolean finalContactsCardIdEqual = contactsCardIdEqual;
            ECardDetailActivity.OpenParameter parameter = new ECardDetailActivity.OpenParameter();
            parameter.setCardID(cardId);
            if (!finalMyCardIdEqual && !finalContactsCardIdEqual) {
                parameter.setOpenType(ECardDetailActivity.OpenType.扫描新的联系人);
                ECardDetailActivity.startActivity(THIS, parameter);
            } else {
                if (finalMyCardIdEqual) {
                    parameter.setOpenType(ECardDetailActivity.OpenType.扫描我的e卡);
                    ECardDetailActivity.startActivity(THIS, parameter);
                } else {
                    parameter.setOpenType(ECardDetailActivity.OpenType.扫描我的联系人);
                    ECardDetailActivity.startActivity(THIS, parameter);
                }
            }
            finish();
        } else {
            TOAST("不是e卡二维码");
            restartCamera();
        }

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            logger.w("initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new ScanQRActivityHandler(this, cameraManager, DecodeThread.ALL_MODE);
            }
            initCrop();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    //重启扫描
    private void restartCamera() {
        closeCamera();
        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        initCamera(scanPreview.getHolder());
        //恢复活动监控器
        inactivityTimer.onResume();
    }

    //关闭摄像头
    private void closeCamera() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        cameraManager.closeDriver();
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Camera error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        //修改之后
        /*int widthC = cameraWidth * 7 / 10;
        int heightC = cameraHeight * 7 / 10;

        int leftOffset = (cameraWidth - widthC) / 2;
        int topOffset = (cameraHeight - heightC) / 3;*/

        /** 生成最终的截取的矩形 */
        //mCropRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================================

    private Bitmap scanBitmap;

    private String photo_path;

    private void initEvent() {
    }

    /**
     * 打开本地图片
     */
    private void openLocalImage() {
        // 打开手机中的相册
        BitmapTool.choose_v2(THIS, 0x01);
    }

    /**
     * 扫描二维码图片的方法
     *
     * @param path
     * @return
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); // 设置二维码内容的编码
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0) sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        int width = scanBitmap.getWidth();
        int height = scanBitmap.getHeight();
        int[] pixels = new int[width * height];
        scanBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        /**
         * 第三个参数是图片的像素
         */
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            logger.e(e.getMessage());
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0x01:
                    // 获取选中图片的路径
                    photo_path = GetImagePath.get(THIS, data.getData());
                    LoadingDialog.show(THIS);
                    Observable.create(new Observable.OnSubscribe<Result>() {
                        @Override
                        public void call(Subscriber<? super Result> subscriber) {
                            if (TextUtils.isEmpty(photo_path)) {
                                TOAST("无法识别二维码");
                                return;
                            }
                            Result result = scanningImage(photo_path);
                            subscriber.onNext(result);
                        }
                    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new OnESubscriber<Result>() {
                        @Override
                        protected void onComplete(boolean success, Result o, Throwable e) {
                            if (o != null) {
                                handleDecode(o, new Bundle());
                            } else {
                                TOAST("无法识别二维码");
                            }
                        }
                    });
                    break;

            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return false;
    }


}
