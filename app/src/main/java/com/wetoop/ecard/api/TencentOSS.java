package com.wetoop.ecard.api;

import android.content.Context;
import android.graphics.Bitmap;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.qcloud.core.network.QCloudProgressListener;
import com.tencent.qcloud.core.network.auth.LocalCredentialProvider;
import com.wetoop.ecard.listener.OSSResultListener;
import com.wetoop.ecard.tools.BitmapTool;

import java.io.File;

/**
 * @author Parck.
 * @date 2018/1/10.
 * @desc
 */

public class TencentOSS {

    private final String appid = "1255868239";
    private final String region = "ap-guangzhou";
    private final String secretId = "AKID9OhssgQh7Zr03v59oS8dw4ziSdeTRYpw";
    private final String secretKey = "9DIPZkE3kFZo12qS9g1gAc3JPK5e3INH";
    private CosXmlService oss;


    private final String bucket = "ecard-1255868239"; // cos v5 的 bucket格式为：xxx-appid, 如 test-1253960454
    private final String directory = "/avatar/"; //格式如 directory = "/test.txt";
    private final String host = "https://ecard-1255868239.cos.ap-guangzhou.myqcloud.com";
    private long signDuration = 600; //签名的有效期，单位为秒

    public void init(Context context) {
        long keyDuration = 600; //SecretKey 的有效时间，单位秒
        //创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .setConnectionTimeout(45000)
                .setSocketTimeout(30000)
                .build();
        //创建获取签名类
        LocalCredentialProvider localCredentialProvider = new LocalCredentialProvider(secretId, secretKey, keyDuration);
        //创建 CosXmlService 对象，实现对象存储服务各项操作.
        oss = new CosXmlService(context, serviceConfig, localCredentialProvider);
    }

    public void upload(String id, String path, final OSSResultListener listener) {
        File file = new File(path);
        if (!file.exists()) return;
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, directory + id + ".jpg", file.getPath());
        putObjectRequest.setSign(signDuration, null, null);

        putObjectRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                listener.progress(progress, max);
            }
        });

        oss.putObjectAsync(putObjectRequest, listener);
    }

    public void upload(String id, Bitmap image, final OSSResultListener listener) {
        File file = BitmapTool.saveImage(image, id);
        if (file == null || !file.exists()) return;
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, directory + id + ".jpg", file.getPath());
        putObjectRequest.setSign(signDuration, null, null);

        putObjectRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                listener.progress(progress, max);
            }
        });

        oss.putObjectAsync(putObjectRequest, listener);
    }

    public String getURL(String id) {
        return host + directory + id + ".jpg";
    }
}
