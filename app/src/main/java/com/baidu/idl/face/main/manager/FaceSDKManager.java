package com.baidu.idl.face.main.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.baidu.idl.face.main.callback.AnalyzeQRCodeCallback;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.listener.SdkInitListener;
import com.baidu.idl.face.main.model.GlobalSet;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.qrcode.QRCodeAnalyze;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.FaceDarkEnhance;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceFeature;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.FaceLive;
import com.baidu.idl.main.facesdk.FaceSearch;
import com.baidu.idl.main.facesdk.ImageIllum;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceDetectListConf;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;
import com.baidu.idl.main.facesdk.model.Feature;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.db.DBManager;
import com.example.datalibrary.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.baidu.idl.face.main.model.GlobalSet.FEATURE_SIZE;


public class FaceSDKManager {

    public static final int SDK_MODEL_LOAD_SUCCESS = 0;
    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INIT_FAIL = 5;
    public static final int SDK_INIT_SUCCESS = 6;

    private List<Boolean> mRgbLiveList = new ArrayList<>();
    private List<Boolean> mNirLiveList = new ArrayList<>();
    private float mRgbLiveScore;
    private float mNirLiveScore;
    private int mLastFaceId;

    private float threholdScore;

    public static volatile int initStatus = SDK_UNACTIVATION;
    public static volatile boolean initModelSuccess = false;
    private FaceAuth faceAuth;
    private FaceDetect faceDetect;
    private FaceFeature faceFeature;
    private FaceLive faceLiveness;

    private ExecutorService es = Executors.newSingleThreadExecutor();
    private Future future;
    private ExecutorService es2 = Executors.newSingleThreadExecutor();
    private Future future2;
    private ExecutorService es3 = Executors.newSingleThreadExecutor();
    private Future future3;

    private FaceDetect faceDetectNir;
    private float[] scores;
    private ImageIllum imageIllum;
    private long startInitModelTime;
    private FaceDarkEnhance faceDarkEnhance;

    private static int failNumber = 0;
    private static int faceId = 0;
    private static int lastFaceId = 0;
    private static LivenessModel faceAdoptModel;
    private boolean isFail = false;
    private long trackTime;
    private boolean isPush;
    private FaceSearch faceSearch;

    private Bitmap mRBmp = null;
    /**
     * ???????????????
     */
    private AnalyzeQRCodeCallback mAnalyzeQRCodeCallback;

    public AnalyzeQRCodeCallback getAnalyzeQRCodeCallback() {
        return mAnalyzeQRCodeCallback;
    }

    public void setAnalyzeQRCodeCallback(AnalyzeQRCodeCallback analyzeQRCodeCallback) {
        this.mAnalyzeQRCodeCallback = analyzeQRCodeCallback;
    }

    private FaceSDKManager() {
        faceAuth = new FaceAuth();
        setActiveLog();
        faceAuth.setCoreConfigure(BDFaceSDKCommon.BDFaceCoreRunMode.BDFACE_LITE_POWER_NO_BIND, 2);
    }

    public void setActiveLog() {
        if (faceAuth != null) {
            if (SingleBaseConfig.getBaseConfig().isLog()) {
                faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_ALL, 1);
            } else {
                faceAuth.setActiveLog(BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_TYPE_ALL, 0);
            }
        }
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public FaceDetect getFaceDetect() {
        return faceDetect;
    }

    public FaceFeature getFaceFeature() {
        return faceFeature;
    }

    public FaceLive getFaceLiveness() {
        return faceLiveness;
    }

    public ImageIllum getImageIllum() {
        return imageIllum;
    }

    public FaceSearch getFaceSearch() {
        return faceSearch;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param context
     * @param listener
     */
    public void initModel(final Context context, final SdkInitListener listener) {

        // ????????????
        BDFaceInstance bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.creatInstance();
        faceDetect = new FaceDetect(bdFaceInstance);
        // ????????????
        BDFaceInstance IrBdFaceInstance = new BDFaceInstance();
        IrBdFaceInstance.creatInstance();
        faceDetectNir = new FaceDetect(IrBdFaceInstance);
        // ????????????
        faceFeature = new FaceFeature();

        faceLiveness = new FaceLive();
        // ????????????
        faceDarkEnhance = new FaceDarkEnhance();
        // ??????
        imageIllum = new ImageIllum();
        // ??????
        faceSearch = new FaceSearch();


        initConfig();

        startInitModelTime = System.currentTimeMillis();

        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        mNirLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();

        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.ALIGN_TRACK_MODEL,
                BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.ALIGN_RGB_MODEL, BDFaceSDKCommon.DetectType.DETECT_VIS,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetectNir.initModel(context,
                GlobalSet.DETECT_NIR_MODE,
                GlobalSet.ALIGN_NIR_MODEL, BDFaceSDKCommon.DetectType.DETECT_NIR,
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initQuality(context,
                GlobalSet.BLUR_MODEL,
                GlobalSet.OCCLUSION_MODEL, new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initAttrbute(context, GlobalSet.ATTRIBUTE_MODEL, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });
        faceDetect.initBestImage(context, GlobalSet.BEST_IMAGE, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });
        // ?????????????????????
        faceDarkEnhance.initFaceDarkEnhance(context,
                GlobalSet.DARK_ENHANCE_MODEL, new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceLiveness.initModel(context,
                GlobalSet.LIVE_VIS_MODEL,
//                GlobalSet.LIVE_VIS_2DMASK_MODEL,
//                GlobalSet.LIVE_VIS_HAND_MODEL,
//                GlobalSet.LIVE_VIS_REFLECTION_MODEL,
                "", "", "",
                GlobalSet.LIVE_NIR_MODEL,
                GlobalSet.LIVE_DEPTH_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        // ???????????????????????????
        faceFeature.initModel(context,
                GlobalSet.RECOGNIZE_IDPHOTO_MODEL,
                GlobalSet.RECOGNIZE_VIS_MODEL,
                GlobalSet.RECOGNIZE_NIR_MODEL,
                GlobalSet.RECOGNIZE_RGBD_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        long endInitModelTime = System.currentTimeMillis();
//                        LogUtils.e(TIME_TAG, "init model time = " + (endInitModelTime - startInitModelTime));
                        if (code != 0) {
//                            ToastUtils.toast(context, "??????????????????,??????????????????");
                            if (listener != null) {
                                listener.initModelFail(code, response);
                            }
                        } else {
                            initStatus = SDK_MODEL_LOAD_SUCCESS;
                            // ??????????????????????????????????????????
//                            ToastUtils.toast(context, "?????????????????????????????????");
                            if (listener != null) {
                                listener.initModelSuccess();
                            }
                        }
                    }
                });

    }

    public void initDataBases(Context context) {
        if (FaceApi.getInstance().getmUserNum() != 0) {
//            ToastUtils.toast(context, "??????????????????");
            Log.e("TAG", "initDataBases: ??????????????????");
        }
        isPush = false;
        emptyFrame();
        // ??????????????????
        DBManager.getInstance().init(context);
        // ???????????????????????????
        initPush(context);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????id+feature
     */
    public void initPush(final Context context) {
        if (future3 != null && !future3.isDone()) {
            future3.cancel(true);
        }

        future3 = es3.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (FaceApi.getInstance().getAllUserList1()) {
//                    FaceSDKManager.getInstance().getFaceSearch().pushPersonFeatureList(
//                            FaceApi.getInstance().getAllUserList1());

                    FaceSDKManager.getInstance().getFaceFeature().featurePush(FaceApi.getInstance().getAllUserList1());
                    if (FaceApi.getInstance().getmUserNum() != 0) {
//                        ToastUtils.toast(context, "?????????????????????");
                        Log.e("TAG", "initDataBases: ?????????????????????");
                    }
                    isPush = true;
                }
            }
        });
    }

    /**
     * ????????????
     */
    private ExecutorService es4 = Executors.newSingleThreadExecutor();
    private Future future4;

    public void refreshMemory(Context context) {

        if (future4 != null && !future3.isDone()) {
            future4.cancel(true);
        }
        future4 = es4.submit(new Runnable() {
            @Override
            public void run() {

            }
        });
    }


    /**
     * ???????????????
     *
     * @return
     */
    public boolean initConfig() {
        if (faceDetect != null) {
            BDFaceSDKConfig config = new BDFaceSDKConfig();
            // TODO: ??????????????????????????????????????????1,??????????????????????????????
            config.maxDetectNum = 2;

            // TODO: ?????????80px??????????????????30px????????????????????????????????????????????????????????????????????????????????????
            config.minFaceSize = SingleBaseConfig.getBaseConfig().getMinimumFace();

            // TODO: ?????????0.5??????????????????0.3?????????
            config.notRGBFaceThreshold = SingleBaseConfig.getBaseConfig().getFaceThreshold();
            config.notNIRFaceThreshold = SingleBaseConfig.getBaseConfig().getFaceThreshold();

            // ???????????????????????????????????????
            config.isAttribute = SingleBaseConfig.getBaseConfig().isAttribute();
//
//            // TODO: ?????????????????????????????????????????????????????????????????????????????????????????????????????????
            config.isCheckBlur = config.isOcclusion
                    = config.isIllumination = config.isHeadPose
                    = SingleBaseConfig.getBaseConfig().isQualityControl();

            faceDetect.loadConfig(config);
            return true;
        }
        return false;
    }

    /**
     * ??????-??????-??????-??????????????????
     *
     * @param rgbData            ?????????YUV ?????????
     * @param nirData            ??????YUV ?????????
     * @param depthData          ??????depth ?????????
     * @param srcHeight          ?????????YUV ?????????-??????
     * @param srcWidth           ?????????YUV ?????????-??????
     * @param liveCheckMode      ???????????????????????????????????????0?????????RGB?????????1?????????RGB+NIR?????????2?????????RGB+Depth?????????3?????????RGB+NIR+Depth?????????4???
     * @param faceDetectCallBack
     */
    public void onDetectCheck(final byte[] rgbData,
                              final byte[] nirData,
                              final byte[] depthData,
                              final int srcHeight,
                              final int srcWidth,
                              final int liveCheckMode,
                              final FaceDetectCallBack faceDetectCallBack) {

        // ??????????????????+1???N?????????3???
        onDetectCheck(rgbData, nirData, depthData, srcHeight, srcWidth, liveCheckMode, 3, faceDetectCallBack);
    }


    private void setFail(LivenessModel livenessModel) {
        Log.e("faceId", livenessModel.getFaceInfo().faceID + "");
        if (failNumber >= 2) {
            faceId = livenessModel.getFaceInfo().faceID;
            faceAdoptModel = livenessModel;
            trackTime = System.currentTimeMillis();
            isFail = false;
            faceAdoptModel.setMultiFrame(true);
        } else {
            failNumber += 1;
            faceId = 0;
            faceAdoptModel = null;
            isFail = true;
            livenessModel.setMultiFrame(true);

        }
    }

    public void emptyFrame() {
        failNumber = 0;
        faceId = 0;
        isFail = false;
        trackTime = 0;
        faceAdoptModel = null;
    }


    /**
     * ??????-??????-??????- ?????????
     *
     * @param rgbData            ?????????YUV ?????????
     * @param nirData            ??????YUV ?????????
     * @param depthData          ??????depth ?????????
     * @param srcHeight          ?????????YUV ?????????-??????
     * @param srcWidth           ?????????YUV ?????????-??????
     * @param liveCheckMode      ???????????????????????????????????????0?????????RGB?????????1?????????RGB+NIR?????????2?????????RGB+Depth?????????3?????????RGB+NIR+Depth?????????4???
     * @param featureCheckMode   ???????????????????????????????????????1????????????????????????2?????????????????????+1???N?????????3??????
     * @param faceDetectCallBack
     */
    public void onDetectCheck(final byte[] rgbData,
                              final byte[] nirData,
                              final byte[] depthData,
                              final int srcHeight,
                              final int srcWidth,
                              final int liveCheckMode,
                              final int featureCheckMode,
                              final FaceDetectCallBack faceDetectCallBack) {

        if (future != null && !future.isDone()) {
            return;
        }

        future = es.submit(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                // ??????????????????????????????
                LivenessModel livenessModel = new LivenessModel();
                // ???????????????????????????????????????YUV??????????????????????????????BGR
                // TODO: ??????????????????????????????????????????????????????????????????????????????
                BDFaceImageInstance rgbInstance;
                if (SingleBaseConfig.getBaseConfig().getType() == 3
                        && SingleBaseConfig.getBaseConfig().getCameraType() == 6) {
                    rgbInstance = new BDFaceImageInstance(rgbData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB,
                            SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
                } else if (SingleBaseConfig.getBaseConfig().getType() == 4
                        && SingleBaseConfig.getBaseConfig().getCameraType() == 6) {
                    rgbInstance = new BDFaceImageInstance(rgbData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB,
                            SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
                } else {
                    rgbInstance = new BDFaceImageInstance(rgbData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                            SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
                }
                livenessModel.setTestBDFaceImageInstanceDuration(System.currentTimeMillis() - startTime);
                long darkEnhanceDuration = System.currentTimeMillis();
                BDFaceImageInstance rgbInstanceOne;
                // ??????????????????
                if (SingleBaseConfig.getBaseConfig().isDarkEnhance()) {
                    rgbInstanceOne = faceDarkEnhance.faceDarkEnhance(rgbInstance);
                    rgbInstance.destory();
                } else {
                    rgbInstanceOne = rgbInstance;
                }

                // TODO: getImage() ??????????????????,??????????????????????????????????????????image view ??????????????????
                livenessModel.setBdFaceImageInstance(rgbInstanceOne.getImage());
                livenessModel.setDarkEnhanceDuration(System.currentTimeMillis() - darkEnhanceDuration);


                // ???????????????????????????????????????
                long startDetectTime = System.currentTimeMillis();


                // ??????????????????????????????????????????????????????????????????????????????????????????
                FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                        .track(BDFaceSDKCommon.DetectType.DETECT_VIS,
                                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_FAST, rgbInstanceOne);
                livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startDetectTime);
                //                LogUtils.e(TIME_TAG, "detect vis time = " + livenessModel.getRgbDetectDuration());

                // ??????????????????
                if (faceInfos != null && faceInfos.length > 0) {
                    long multiFrameTime = System.currentTimeMillis();
                    livenessModel.setTrackFaceInfo(faceInfos);
                    livenessModel.setFaceInfo(faceInfos[0]);
                    livenessModel.setTrackLandmarks(faceInfos[0].landmarks);
                    if (lastFaceId != faceInfos[0].faceID) {
                        lastFaceId = faceInfos[0].faceID;
                    }

                    if (System.currentTimeMillis() - trackTime < 3000 && faceId == faceInfos[0].faceID) {
                        faceAdoptModel.setMultiFrame(true);
                        rgbInstanceOne.destory();
                        if (faceDetectCallBack != null && faceAdoptModel != null) {
                            //                            faceAdoptModel.setAllDetectDuration(System.currentTimeMillis() - startTime);
                            faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                            faceDetectCallBack.onFaceDetectCallback(faceAdoptModel);

                        }
                        return;
                    }
                    if (faceAdoptModel != null) {
                        faceAdoptModel.setMultiFrame(false);
                    }
                    faceId = 0;
                    faceAdoptModel = null;
                    if (!isFail /*&& failNumber != 0*/) {
                        failNumber = 0;
                    }

                    livenessModel.setTrackStatus(1);
                    livenessModel.setLandmarks(faceInfos[0].landmarks);
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectDarwCallback(livenessModel);
                    }

                    onLivenessCheck(rgbInstanceOne, nirData, depthData, srcHeight,
                            srcWidth, livenessModel.getLandmarks(),
                            livenessModel, startTime, liveCheckMode, featureCheckMode,
                            faceDetectCallBack, faceInfos);
                    livenessModel.setMultiFrameTime(System.currentTimeMillis() - multiFrameTime);
                } else {
                    emptyFrame();

                    //??????QR
                    try {
                        mRBmp = BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance());
                        QRCodeAnalyze.analyzeQR(mRBmp,getAnalyzeQRCodeCallback());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // ???????????????????????????
                        if (mRBmp != null && !mRBmp.isRecycled()) {
                            // ??????????????????null
                            mRBmp.recycle();
                            mRBmp = null;
                        }
                        System.gc();
                    }

                    // ???????????????????????????????????????????????????????????????????????????
                    rgbInstanceOne.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(null);
                        faceDetectCallBack.onFaceDetectDarwCallback(null);
                        faceDetectCallBack.onTip(0, "??????????????????");
                    }
                }
            }
        });
    }


    /**
     * ??????????????????????????????????????????????????????
     * ???????????? SingleBaseConfig.getBaseConfig().setQualityControl(true);?????????true???
     * ?????????  FaceSDKManager.getInstance().initConfig() ???????????????????????????
     *
     * @param livenessModel
     * @param faceDetectCallBack
     * @return
     */
    public boolean onQualityCheck(final LivenessModel livenessModel,
                                  final FaceDetectCallBack faceDetectCallBack) {

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return true;
        }

        if (livenessModel != null && livenessModel.getFaceInfo() != null) {

            // ????????????
            if (Math.abs(livenessModel.getFaceInfo().yaw) > SingleBaseConfig.getBaseConfig().getGesture()) {
                faceDetectCallBack.onTip(-1, "?????????????????????????????????");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().roll) > SingleBaseConfig.getBaseConfig().getGesture()) {
                faceDetectCallBack.onTip(-1, "???????????????????????????????????????????????????");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().pitch) > SingleBaseConfig.getBaseConfig().getGesture()) {
                faceDetectCallBack.onTip(-1, "?????????????????????????????????");
                return false;
            }

            // ??????????????????
            float blur = livenessModel.getFaceInfo().bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                faceDetectCallBack.onTip(-1, "????????????");
                return false;
            }

            // ??????????????????
            float illum = livenessModel.getFaceInfo().illum;
            Log.e("illum", "illum = " + illum);
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                faceDetectCallBack.onTip(-1, "?????????????????????");
                return false;
            }


            // ??????????????????
            if (livenessModel.getFaceInfo().occlusion != null) {
                BDFaceOcclusion occlusion = livenessModel.getFaceInfo().occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // ?????????????????????
                    faceDetectCallBack.onTip(-1, "????????????");
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ??????-??????-?????????????????????
     *
     * @param rgbInstance        ???????????????????????????
     * @param nirData            ??????YUV ?????????
     * @param depthData          ??????depth ?????????
     * @param srcHeight          ?????????YUV ?????????-??????
     * @param srcWidth           ?????????YUV ?????????-??????
     * @param landmark           ?????????????????????????????????72????????????
     * @param livenessModel      ????????????????????????
     * @param startTime          ??????????????????
     * @param liveCheckMode      ???????????????????????????????????????0?????????RGB?????????1?????????RGB+NIR?????????2?????????RGB+Depth?????????3?????????RGB+NIR+Depth?????????4???
     * @param featureCheckMode   ???????????????????????????????????????1????????????????????????2?????????????????????+1???N?????????3??????
     * @param faceDetectCallBack
     */
    public void onLivenessCheck(final BDFaceImageInstance rgbInstance,
                                final byte[] nirData,
                                final byte[] depthData,
                                final int srcHeight,
                                final int srcWidth,
                                final float[] landmark,
                                final LivenessModel livenessModel,
                                final long startTime,
                                final int liveCheckMode,
                                final int featureCheckMode,
                                final FaceDetectCallBack faceDetectCallBack,
                                final FaceInfo[] fastFaceInfos) {

        if (future2 != null && !future2.isDone()) {
            // ???????????????????????????????????????????????????????????????????????????
            rgbInstance.destory();
            return;
        }

        future2 = es2.submit(new Runnable() {

            @Override
            public void run() {

                long accurateTime = System.currentTimeMillis();
                BDFaceDetectListConf bdFaceDetectListConfig = new BDFaceDetectListConf();
                bdFaceDetectListConfig.usingQuality = bdFaceDetectListConfig.usingHeadPose
                        = SingleBaseConfig.getBaseConfig().isQualityControl();
                bdFaceDetectListConfig.usingBestImage = SingleBaseConfig.getBaseConfig().isBestImage();
                FaceInfo[] faceInfos = FaceSDKManager.getInstance()
                        .getFaceDetect()
                        .detect(BDFaceSDKCommon.DetectType.DETECT_VIS,
                                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE,
                                rgbInstance,
                                fastFaceInfos, bdFaceDetectListConfig);
                livenessModel.setAccurateTime(System.currentTimeMillis() - accurateTime);

                // ??????????????????????????????
                if (faceInfos != null && faceInfos.length > 0) {
                    faceInfos[0].faceID = livenessModel.getFaceInfo().faceID;
                    livenessModel.setFaceInfo(faceInfos[0]);
                    livenessModel.setTrackStatus(2);
                    livenessModel.setLandmarks(faceInfos[0].landmarks);

                    if (mLastFaceId != fastFaceInfos[0].faceID) {
                        mLastFaceId = fastFaceInfos[0].faceID;
                        mRgbLiveList.clear();
                        mNirLiveList.clear();
                    }
                } else {
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    }
                    return;
                }

                // ??????????????????
                if (!onBestImageCheck(livenessModel, faceDetectCallBack)) {
                    livenessModel.setQualityCheck(false);
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    }
                    return;
                }

                // ?????????????????????,??????BDFaceImageInstance???????????????
                if (!onQualityCheck(livenessModel, faceDetectCallBack)) {
                    livenessModel.setQualityCheck(false);
                    rgbInstance.destory();
                    if (faceDetectCallBack != null) {
                        faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    }
                    return;
                }
                livenessModel.setQualityCheck(true);
                // ??????LivenessConfig liveCheckMode ????????????????????????????????????0?????????RGB?????????1?????????RGB+NIR?????????2?????????RGB+Depth?????????3?????????RGB+NIR+Depth?????????4???
                // TODO ????????????
                float rgbScore = -1;
                if (liveCheckMode != 0) {
                    long startRgbTime = System.currentTimeMillis();
                    rgbScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                            BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_RGB,
                            rgbInstance, faceInfos[0].landmarks);
                    mRgbLiveList.add(rgbScore > mRgbLiveScore);
                    livenessModel.setRgbLivenessDuration(System.currentTimeMillis() - startRgbTime);
                    while (mRgbLiveList.size() > 6) {
                        mRgbLiveList.remove(0);
                    }
                    if (mRgbLiveList.size() > 2) {
                        int rgbSum = 0;
                        for (Boolean b : mRgbLiveList) {
                            if (b) {
                                rgbSum++;
                            }
                        }
                        if (1.0 * rgbSum / mRgbLiveList.size() > 0.6) {
                            if (rgbScore < mRgbLiveScore) {
                                rgbScore = mRgbLiveScore + (1 - mRgbLiveScore) * new Random().nextFloat();
                            }
                        } else {
                            if (rgbScore > mRgbLiveScore) {
                                rgbScore = new Random().nextFloat() * mRgbLiveScore;
                            }
                        }
                    }
                    livenessModel.setRgbLivenessScore(rgbScore);
                }

                float nirScore = -1;
                FaceInfo[] faceInfosIr = null;
                BDFaceImageInstance nirInstance = null;
                if (liveCheckMode == 2 || liveCheckMode == 4 && nirData != null) {
                    // ???????????????????????????????????????YUV-IR??????????????????????????????BGR
                    // TODO: ??????????????????????????????????????????????????????????????????????????????
                    long nirInstanceTime = System.currentTimeMillis();
                    nirInstance = new BDFaceImageInstance(nirData, srcHeight,
                            srcWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                            SingleBaseConfig.getBaseConfig().getNirDetectDirection(),
                            SingleBaseConfig.getBaseConfig().getMirrorDetectNIR());
                    livenessModel.setBdNirFaceImageInstance(nirInstance.getImage());
                    livenessModel.setNirInstanceTime(System.currentTimeMillis() - nirInstanceTime);

                    // ??????RGB??????????????????IR???????????????????????????????????????
                    long startIrDetectTime = System.currentTimeMillis();
                    BDFaceDetectListConf bdFaceDetectListConf = new BDFaceDetectListConf();
                    bdFaceDetectListConf.usingDetect = true;
                    faceInfosIr = faceDetectNir.detect(BDFaceSDKCommon.DetectType.DETECT_NIR,
                            BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_NIR_ACCURATE,
                            nirInstance, null, bdFaceDetectListConf);
                    bdFaceDetectListConf.usingDetect = false;
                    livenessModel.setIrLivenessDuration(System.currentTimeMillis() - startIrDetectTime);
//                    LogUtils.e(TIME_TAG, "detect ir time = " + livenessModel.getIrLivenessDuration());

                    if (faceInfosIr != null && faceInfosIr.length > 0) {
                        long startNirTime = System.currentTimeMillis();
                        FaceInfo faceInfoIr = faceInfosIr[0];
                        nirScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_NIR,
                                nirInstance, faceInfoIr.landmarks);

                        livenessModel.setIrSilentLiveDuration(System.currentTimeMillis() - startNirTime);
                        mNirLiveList.add(nirScore > mNirLiveScore);
                        while (mNirLiveList.size() > 6) {
                            mNirLiveList.remove(0);
                        }
                        if (mNirLiveList.size() > 2) {
                            int nirSum = 0;
                            for (Boolean b : mNirLiveList) {
                                if (b) {
                                    nirSum++;
                                }
                            }
                            if (1.0f * nirSum / mNirLiveList.size() > 0.6) {
                                if (nirScore < mNirLiveScore) {
                                    nirScore = mNirLiveScore + new Random().nextFloat() * (1 - mNirLiveScore);
                                }
                            } else {
                                if (nirScore > mNirLiveScore) {
                                    nirScore = new Random().nextFloat() * mNirLiveScore;
                                }
                            }
                        }
                        livenessModel.setIrLivenessScore(nirScore);
//                        LogUtils.e(TIME_TAG, "live ir time = " + livenessModel.getIrLivenessDuration());
                    }
                }

                float depthScore = -1;
                if (liveCheckMode == 3 || liveCheckMode == 4 && depthData != null) {
                    // TODO: ????????????????????????????????????????????????Atlas ????????????????????????400*640????????????????????????????????????,??????72 ????????????x ??????????????????80????????????
                    float[] depthLandmark = new float[faceInfos[0].landmarks.length];
                    BDFaceImageInstance depthInstance;
                    if (SingleBaseConfig.getBaseConfig().getCameraType() == 1) {
                        System.arraycopy(faceInfos[0].landmarks, 0, depthLandmark, 0, faceInfos[0].landmarks.length);
                        if (SingleBaseConfig.getBaseConfig().getCameraType() == 1) {
                            for (int i = 0; i < 144; i = i + 2) {
                                depthLandmark[i] -= 80;
                            }
                        }
                        depthInstance = new BDFaceImageInstance(depthData,
                                SingleBaseConfig.getBaseConfig().getDepthWidth(),
                                SingleBaseConfig.getBaseConfig().getDepthHeight(),
                                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_DEPTH,
                                0, 0);
                    } else {
                        depthInstance = new BDFaceImageInstance(depthData,
                                SingleBaseConfig.getBaseConfig().getDepthHeight(),
                                SingleBaseConfig.getBaseConfig().getDepthWidth(),
                                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_DEPTH,
                                0, 0);
                    }

                    livenessModel.setBdDepthFaceImageInstance(depthInstance.getImage());
                    // ???????????????????????????????????????Depth
                    long startDepthTime = System.currentTimeMillis();
                    if (SingleBaseConfig.getBaseConfig().getCameraType() == 1) {
                        depthScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH,
                                depthInstance, depthLandmark);
                    } else {
                        depthScore = FaceSDKManager.getInstance().getFaceLiveness().silentLive(
                                BDFaceSDKCommon.LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH,
                                depthInstance, faceInfos[0].landmarks);
                    }
                    livenessModel.setDepthLivenessScore(depthScore);
                    livenessModel.setDepthtLivenessDuration(System.currentTimeMillis() - startDepthTime);
//                    LogUtils.e(TIME_TAG, "live depth time = " + livenessModel.getDepthtLivenessDuration());
                    depthInstance.destory();
                }

                // TODO ????????????+????????????
                if (liveCheckMode == 0) {
                    onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr,
                            nirInstance, livenessModel, featureCheckMode,
                            SingleBaseConfig.getBaseConfig().getActiveModel());
                } else {
                    if (liveCheckMode == 1 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr,
                                nirInstance, livenessModel, featureCheckMode,
                                1);
                    } else if (liveCheckMode == 2 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && nirScore > SingleBaseConfig.getBaseConfig().getNirLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr, nirInstance,
                                livenessModel, featureCheckMode,
                                SingleBaseConfig.getBaseConfig().getActiveModel());
                    } else if (liveCheckMode == 3 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && depthScore > SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr, nirInstance,
                                livenessModel, featureCheckMode,
                                1);
                    } else if (liveCheckMode == 4 && rgbScore > SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                            && nirScore > SingleBaseConfig.getBaseConfig().getNirLiveScore()
                            && depthScore > SingleBaseConfig.getBaseConfig().getDepthLiveScore()) {
                        onFeatureCheck(rgbInstance, faceInfos[0].landmarks, faceInfosIr, nirInstance,
                                livenessModel, featureCheckMode,
                                SingleBaseConfig.getBaseConfig().getActiveModel());
                    }
                }
                // ????????????,??????????????????
                livenessModel.setAllDetectDuration(System.currentTimeMillis() - startTime);
//                LogUtils.e(TIME_TAG, "all process time = " + livenessModel.getAllDetectDuration());
                // ???????????????????????????????????????????????????????????????????????????
                rgbInstance.destory();
                if (nirInstance != null) {
                    nirInstance.destory();
                }
                // ????????????????????????
                if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                }

            }
        });
    }

    /**
     * ??????????????????
     *
     * @param livenessModel
     * @param faceDetectCallBack
     * @return
     */
    public boolean onBestImageCheck(LivenessModel livenessModel,
                                    FaceDetectCallBack faceDetectCallBack) {
        if (!SingleBaseConfig.getBaseConfig().isBestImage()) {
            return true;
        }
        if (livenessModel != null && livenessModel.getFaceInfo() != null) {
            float bestImageScore = livenessModel.getFaceInfo().bestImageScore;
            if (bestImageScore < 0.5) {
                faceDetectCallBack.onTip(-1, "?????????????????????");
                return false;
            }
        }
        return true;
    }

    /**
     * ????????????-??????????????????
     *
     * @param rgbInstance      ???????????????????????????
     * @param landmark         ?????????????????????????????????72????????????
     * @param faceInfos        nir????????????
     * @param nirInstance      nir ????????????
     * @param livenessModel    ????????????????????????
     * @param featureCheckMode ???????????????????????????????????????1????????????????????????2?????????????????????+1???N?????????3??????
     * @param featureType      ???????????????????????? ???????????????1?????????????????????2????????????????????????3??????
     */
    private void onFeatureCheck(BDFaceImageInstance rgbInstance,
                                float[] landmark,
                                FaceInfo[] faceInfos,
                                BDFaceImageInstance nirInstance,
                                LivenessModel livenessModel,
                                final int featureCheckMode,
                                final int featureType) {
        if (!isPush) {
            return;
        }
        // ????????????????????????????????????
        if (featureCheckMode == 1) {
            return;
        }
        byte[] feature = new byte[512];
        if (featureType == 3) {
            // todo: ????????????????????????????????????????????????????????????????????????type??????????????????????????????0~1??????
            AtomicInteger atomicInteger = new AtomicInteger();
            FaceSDKManager.getInstance().getImageIllum().imageIllum(rgbInstance, atomicInteger);
            int illumScore = atomicInteger.get();
            if (illumScore < SingleBaseConfig.getBaseConfig().getIllumination()) {
                if (faceInfos != null && nirInstance != null) {
                    long startFeatureTime = System.currentTimeMillis();
                    float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                            BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_NIR, nirInstance,
                            faceInfos[0].landmarks, feature);
                    livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
                    livenessModel.setFeature(feature);
                    // ????????????
                    featureSearch(featureCheckMode, livenessModel, feature, featureSize,
                            BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_NIR);
                }

            } else {
                long startFeatureTime = System.currentTimeMillis();
                float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, landmark, feature);
                livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
                livenessModel.setFeature(feature);
                // ????????????
                featureSearch(featureCheckMode, livenessModel, feature, featureSize,
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
            }

        } else {
            // ???????????????
            long startFeatureTime = System.currentTimeMillis();
            float featureSize = FaceSDKManager.getInstance().getFaceFeature().feature(
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, landmark, feature);
            livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
            livenessModel.setFeature(feature);
            livenessModel.setFeatureDuration(System.currentTimeMillis() - startFeatureTime);
            // ????????????
            featureSearch(featureCheckMode, livenessModel, feature, featureSize,
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
        }

    }


    /**
     * ???????????????
     *
     * @param featureCheckMode ???????????????????????????????????????1????????????????????????2?????????????????????+1???N?????????3??????
     * @param livenessModel    ????????????????????????
     * @param feature          ?????????
     * @param featureSize      ????????????size
     * @param type             ??????????????????
     */
    private void featureSearch(final int featureCheckMode,
                               LivenessModel livenessModel,
                               byte[] feature,
                               float featureSize,
                               BDFaceSDKCommon.FeatureType type) {

        // ???????????????????????????????????????????????????
        if (featureCheckMode == 2) {
            livenessModel.setFeatureCode(featureSize);
            return;
        }
        // ??????????????????+???????????????search ??????
        if (featureSize == FEATURE_SIZE / 4) {

            // ??????????????????
            // TODO ????????????????????????????????????
            long startFeature = System.currentTimeMillis();
            List<? extends Feature> featureResult = FaceSDKManager.getInstance()
                    .getFaceSearch().search(type, 1, feature, true);

            // TODO ??????top num = 1 ?????????????????????????????????????????????????????????????????????????????????num ???????????????
            if (featureResult != null && featureResult.size() > 0) {

                // ?????????????????????
                Feature topFeature = featureResult.get(0);

                if (topFeature != null) {
                    User user = FaceApi.getInstance().getUserListById(topFeature.getId());
                    Log.e("TAG", "------>user: " + user.getUserName().trim() + "????????????__" + topFeature.getScore());
                }

                // ???????????????????????????????????????????????????????????????????????????
                if (SingleBaseConfig.getBaseConfig().getActiveModel() == 1) {
                    threholdScore = SingleBaseConfig.getBaseConfig().getLiveThreshold();
                } else if (SingleBaseConfig.getBaseConfig().getActiveModel() == 2) {
                    threholdScore = SingleBaseConfig.getBaseConfig().getIdThreshold();
                } else if (SingleBaseConfig.getBaseConfig().getActiveModel() == 3) {
                    threholdScore = SingleBaseConfig.getBaseConfig().getRgbAndNirThreshold();
                }
                if (topFeature != null && topFeature.getScore() >
                        threholdScore) {
                    // ??????featureEntity ??????id+feature ??????????????????????????????????????????
                    User user = FaceApi.getInstance().getUserListById(topFeature.getId());
                    if (user != null) {
                        livenessModel.setUser(user);
                        livenessModel.setFeatureScore(topFeature.getScore());
                        /*faceId = livenessModel.getFaceInfo().faceID;
                        trackTime = System.currentTimeMillis();
                        faceAdoptModel = livenessModel;
                        failNumber = 0;
                        isFail = false;*/
                        setFail(livenessModel);
                    } else {
                        setFail(livenessModel);
                    }
                } else {
                    setFail(livenessModel);
                }
            } else {
                setFail(livenessModel);
            }
            livenessModel.setCheckDuration(System.currentTimeMillis() - startFeature);
        }
    }

    /**
     * ????????????
     */
//    public void uninitModel() {
//        if (faceDetect != null) {
//            faceDetect.uninitModel();
//        }
//        if (faceFeature != null) {
//            faceFeature.uninitModel();
//        }
//        if (faceDetectNir != null) {
//            faceDetectNir.uninitModel();
//        }
//        if (faceLiveness != null) {
//            faceLiveness.uninitModel();
//        }
//        if (faceDetect.uninitModel() == 0
//                && faceFeature.uninitModel() == 0
//                && faceDetectNir.uninitModel() == 0
//                && faceLiveness.uninitModel() == 0) {
//            initStatus = SDK_UNACTIVATION;
//            initModelSuccess = false;
//        }
//        Log.e("uninitModel","gate-uninitModel"
//                + faceDetect.uninitModel()
//                + faceFeature.uninitModel()
//                + faceDetectNir.uninitModel()
//                + faceLiveness.uninitModel());
//    }

}