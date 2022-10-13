package com.baidu.idl.face.main.qrcode;

import android.graphics.Bitmap;

import com.baidu.idl.face.main.callback.AnalyzeQRCodeCallback;
import com.baidu.idl.face.main.utils.LogUtils;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import java.util.List;

/**
 * 二维码检测
 */
public class QRCodeAnalyze {

    public static void analyzeQR(Bitmap mRBmp, AnalyzeQRCodeCallback analyzeQRCodeCallback) {
        try {
            //通过WeChatQRCodeDetector识别图片中的二维码
            List<String> result = WeChatQRCodeDetector.detectAndDecode(mRBmp);
            LogUtils.d("QRCodeAnalyze-->", result.toString());
            if (result != null && result.size() > 0) {
                if (analyzeQRCodeCallback != null) {
                    String qrData = result.get(0);
                    analyzeQRCodeCallback.onAnalyzeSuccess(qrData);
                }
            } else {
                if (analyzeQRCodeCallback != null) {
                    analyzeQRCodeCallback.onAnalyzeFailed();
                }
            }

        } catch (Exception e) {
            LogUtils.w("QRCodeAnalyze-->error", e.toString());
        }

    }
}
