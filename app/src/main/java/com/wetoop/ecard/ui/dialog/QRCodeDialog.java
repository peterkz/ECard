package com.wetoop.ecard.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.wetoop.ecard.App;
import com.wetoop.ecard.R;
import com.wetoop.ecard.bean.CardBean;
import com.wetoop.ecard.tools.EncodingUtils;
import com.wetoop.ecard.tools.RoundTransformation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by User on 2017/10/25.
 */

public class QRCodeDialog {
    private Context context;
    private String qrStr;
    private Dialog qrCodeDialog;
    private ImageView qrImage, eCardShareImage, eCardPhotoImage;
    private TextView saveQR, eCardShareText;
    private String phoneNum, phoneCompany;
    private String userName, position, company;
    private String cardId, userId;
    private String email;
    private String address;
    private Bitmap bitmapQR;

    public QRCodeDialog(Context context, CardBean cardBean, String qrStr) {
        this.qrStr = qrStr;
        init(context);
        initView(cardBean);
        initListener(cardBean);
        this.qrCodeDialog.show();
    }

    private void init(Context context) {
        if (this.qrCodeDialog == null) {
            this.qrCodeDialog = new Dialog(context, R.style.QRCodeDialogTheme);
            this.qrCodeDialog.setContentView(R.layout.dialog_qrcode);
            this.qrCodeDialog.setCanceledOnTouchOutside(true);
        }
        this.context = context;
    }

    private void initView(CardBean cardBean) {
        this.qrImage = (ImageView) this.qrCodeDialog.findViewById(R.id.qrImage);
        this.eCardShareImage = (ImageView) this.qrCodeDialog.findViewById(R.id.eCardShare);
        this.saveQR = (TextView) this.qrCodeDialog.findViewById(R.id.saveQR);
        this.eCardShareText = (TextView) this.qrCodeDialog.findViewById(R.id.eCardTextShare);
        this.eCardPhotoImage = (ImageView) this.qrCodeDialog.findViewById(R.id.eCardPhoto);

        TextView name = (TextView) this.qrCodeDialog.findViewById(R.id.name_text);
        TextView titleText = (TextView) this.qrCodeDialog.findViewById(R.id.title_text);
        if (cardBean.getInformation().getName() == null) {
            userName = "";
        } else {
            userName = cardBean.getInformation().getName();
            name.setText(userName);
        }
        if (cardBean.getInformation().getPosition() == null) {
            position = "";
        } else {
            position = cardBean.getInformation().getPosition();
            titleText.setText(position);
        }
        Glide.with(context)
                .load(App.oss().getURL(cardBean.getInformation().getCard_id()))
                .placeholder(R.mipmap.default_avatar_icon)
                .transform(new RoundTransformation(context))
                .signature(new StringSignature(String.valueOf(cardBean.getInformation().getDateUpdated() == null ? System.currentTimeMillis() : cardBean.getInformation().getDateUpdated().getTime())))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(eCardPhotoImage);
        createQRCard();

    }

    private void initListener(final CardBean cardBean) {
        this.saveQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardBean.getInformation().getCard_id() != null) {
                    String fileName = cardBean.getInformation().getCard_id() + ".png";
                    File f = new File(getSDPath() + "/eCard", fileName);
                    saveBitmap(f, bitmapQR);
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();

                    // 把文件插入到系统图库
                    try {
                        MediaStore.Images.Media.insertImage(context.getContentResolver(),
                                f.getAbsolutePath(), fileName, null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    // 通知图库更新
                    Uri uri = Uri.fromFile(f);
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                }
            }
        });
        this.eCardShareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage(cardBean);
            }
        });
        this.eCardShareText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage(cardBean);
            }
        });
    }

    private void shareImage(CardBean cardBean) {
        String fileName = cardBean.getInformation().getCard_id() + ".png";
        File f = new File(getSDPath() + "/eCard", fileName);
        saveBitmap(f, bitmapQR);
        Uri uri = Uri.fromFile(f);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("image/*");
        context.startActivity(Intent.createChooser(sendIntent, "分享到"));
    }

    private void createQRCard() {
        bitmapQR = EncodingUtils.createQRCode(qrStr,
                600, 600, null);
        this.qrImage.setImageBitmap(bitmapQR);
    }

    public void hide() {
        if (this.qrCodeDialog != null) {
            this.qrCodeDialog.dismiss();
            this.qrCodeDialog = null;
        }
    }

    public void saveBitmap(File f, Bitmap bm) {

        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取sd卡路径
    public String getSDPath() {
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        File destDir = new File(sdDir + "/eCard");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return sdDir.toString();
    }
}
