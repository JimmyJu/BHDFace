package com.baidu.idl.face.main.callback;

/**
 * 二维码检测回调
 */
public interface AnalyzeQRCodeCallback {
    public void onAnalyzeSuccess(String result);

    public void onAnalyzeFailed();
}
