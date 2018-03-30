package com.wetoop.ecard.listener;

import android.support.annotation.Nullable;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;

/**
 * @author Parck.
 * @date 2018/1/10.
 * @desc
 */

public abstract class OSSResultListener implements CosXmlResultListener {

    @Override
    public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
        complete(true, cosXmlResult, null);
    }

    @Override
    public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException e, CosXmlServiceException e1) {
        complete(false, null, null);
    }

    public void progress(long progress, long max) {
    }

    public abstract void complete(boolean success, @Nullable CosXmlResult result, @Nullable CosXmlClientException e);
}
