package com.baidu.idl.face.main.setting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.start.FileUtils;
import com.baidu.idl.face.main.listener.OnItemClickListener;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.ImportFeatureResult;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

/**
 * SD卡文件资源管理器  照片测试
 */
public class SDCardFileExplorerTestActivity extends BaseActivity {


    private Bitmap bitmap = null;


    public static final String TAG = SDCardFileExplorerTestActivity.class.getSimpleName();

    private Context mContext;

    private Button bt_return;
    private RecyclerView recyclerView;
    private SDCardFileExplorerAdapter adapter;
    private TextView logView, logFail, tv_mSuccess, tv_mFail, tv_mTotal;
    private int mSuccess = 1, mFail = 1, mTotal;

    // 记录当前的父文件夹
    File currentParent;
    // 记录当前路径下的所有文件夹的文件数组
    File[] currentFiles;

    private ProgressDialog progressDialog;

    /**
     * 字节数组输出流
     */
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_card_file_explorer);
        mContext = this;

        bt_return = findViewById(R.id.bt_return);
        bt_return.setOnClickListener(v -> finish());

        // 获取系统的SDCard的目录
        String[] allSdPaths = FileUtils.getAllSdPaths(this);

        if (allSdPaths.length >= 2) {
            File root = new File(allSdPaths[1]);
            currentFiles = root.listFiles();
            initView();
        } else {
            ToastUtils.toast(this, "请插入USB外部储存卡");
        }


    }

    private void initView() {
        tv_mSuccess = findViewById(R.id.mSuccess);
        tv_mFail = findViewById(R.id.mFail);
        tv_mTotal = findViewById(R.id.mTotal);

        logFail = findViewById(R.id.logFail);
        logView = (TextView) findViewById(R.id.logView);
        //配置TextView的滚动方式
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());
        logFail.setMovementMethod(ScrollingMovementMethod.getInstance());

        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SDCardFileExplorerAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showAlertDialog(position);
            }
        });
    }

    /**
     * @param msg 展示log信息
     */
    void refreshLogView(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logView.append(Html.fromHtml(msg));
                int offset = logView.getLineCount() * logView.getLineHeight();
                if (offset > logView.getHeight()) {
                    //更新文字时，使用View的scrollTo(int x,int y)方法使其自动滚动到最后一行
                    logView.scrollTo(0, offset - logView.getHeight());
                }
            }
        });
    }

    /**
     * @param msg 单独展示不合格的名字
     */
    void refreshLogFail(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logFail.append(Html.fromHtml(msg));
                int offset = logFail.getLineCount() * logFail.getLineHeight();
                if (offset > logFail.getHeight()) {
                    //更新文字时，使用View的scrollTo(int x,int y)方法使其自动滚动到最后一行
                    logFail.scrollTo(0, offset - logFail.getHeight());
                }
            }
        });
    }


    /**
     * 显示dialog
     *
     * @param position
     */
    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("检测" + currentFiles[position].getName() + "目录下照片");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(mContext);
                }
                progressDialog.setTitle("检测照片是否合格");
                progressDialog.setMessage("正在检测中...");
                progressDialog.setMax(FileUtils.getImagePathFromSDLength(mContext, currentFiles[position].getName()));
                mTotal = FileUtils.getImagePathFromSDLength(mContext, currentFiles[position].getName());
                tv_mTotal.setText("总数: " + mTotal);
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();


                if (FileUtils.isFileExist(mContext, currentFiles[position].getName())) {

//                    DBManager.getInstance().deleteGroup("default");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int i = 1;

                            for (HashMap.Entry<String, String> map : FileUtils.getImagePathFromSD(mContext, currentFiles[position].getName()).entrySet()) {

                                //这里获取的为工号
                                String name = map.getKey().substring(0, map.getKey().length() - 4);
                                String imagePath = map.getValue();

                                // 设置参数
                                try {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true; // 只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
                                    BitmapFactory.decodeFile(imagePath, options);
                                    int height = options.outHeight;
                                    int width = options.outWidth;

                                    int inSampleSize = 2; // 默认像素压缩比例，压缩为原图的1/2  1表示不缩放
//                                    // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可


                                    int minLen = Math.min(height, width); // 原图的最小边长
                                    if (minLen > 200) { // 如果原始图像的最小边长大于100dp（此处单位我认为是dp，而非px）
                                        float ratio = (float) minLen / 200.0f; // 计算像素压缩比例
                                        inSampleSize = (int) ratio;
                                    }
                                    options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
                                    options.inSampleSize = inSampleSize; // 设置为刚才计算的压缩比例
                                    bitmap = BitmapFactory.decodeFile(imagePath, options); // 解码文件
//                                    Log.w("TAG", "卡号：" + name + " size: " + bitmap.getByteCount() + " width: " + bitmap.getWidth() + " heigth:" + bitmap.getHeight()); // 输出图像数据
                                } catch (OutOfMemoryError e) {
                                    ToastUtils.toast(SDCardFileExplorerTestActivity.this, "" + e.toString());
                                }


//                                Bitmap bitmap = BitmapFactory.decodeFile(map.getValue());

                                //Bitmap转换成byte[]
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                                byte[] imageData = Utils.addBytes(imageHead, byteArrayOutputStream.toByteArray(), imageEnd);
//                                Log.w("TAG", "----卡号：" + name + " size: " + (compressedBm.getByteCount()>>10) + " width: " + compressedBm.getWidth() + " heigth:" + compressedBm.getHeight()); // 输出图像数据

                                byteArrayOutputStream.reset();

                                //特征值获取
                                byte[] bytes = new byte[512];
//                                float ret = FaceApi.getInstance().getFeature(bitmap, bytes, BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);

//
//                                FaceApi.getInstance().registerUserIntoDBmanager("default", name, "image" + map.getKey(), name, bytes);
//
//                                FaceApi.getInstance().initDatabases(true);

                                //发送数据
                                if (bitmap != null) {

                                    ImportFeatureResult result;
                                    // 判断是否提取成功：128为成功，-1为参数为空，-2表示未检测到人脸
                                    result = getFeature(bitmap, bytes,
                                            BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);

                                    Log.i(TAG, "live_photo = " + result.getResult());


                                    if (result.getResult() == 128) {
//                                        Log.e("TAG", "initSuccess: " + name + "__" + "图片合格 " + Utils.addZero(name));
                                        refreshLogView("<font color='#16CC77'> " + name + "： </font>" + "<font color='#16CC77'> 图片合格 </font> <br/>");

                                        tv_mSuccess.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv_mSuccess.setText(Html.fromHtml("<font color='#16CC77'> 合格： </font>" + "<font color='#16CC77'> " + mSuccess++ + " </font>"));
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, name + " 错误码：" + result.getResult());
                                        refreshLogView("<font color='#ff0000'> " + name + "： </font>" + "<font color='#ff0000'> 图片不合格 </font> <br/>");
                                        refreshLogFail("<font color='#ff0000'> 工号 ： </font>" + "<font color='#ff0000'> " + name + " </font> <br/>");
                                        tv_mFail.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv_mFail.setText(Html.fromHtml("<font color='#ff0000'> 不合格： </font>" + "<font color='#ff0000'> " + mFail++ + " </font>"));
                                            }
                                        });
                                    }

                                    // 图片回收
                                    if (!bitmap.isRecycled()) {
                                        bitmap.recycle();
                                    }

                                } else {
                                    Log.e("TAG", "initSuccess: " + name + "__" + "该图片转成Bitmap失败");
                                    ToastUtils.toast(SDCardFileExplorerTestActivity.this, name + "__" + "该图片无法转换");
                                }

                                progressDialog.setProgress(i++);

                            }
                            progressDialog.dismiss();
                        }
                    }).start();

                }
            }
        });

        builder.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 先判断是否已经回收
        if (bitmap != null && !bitmap.isRecycled()) {
            // 回收并且置为null
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
    }


    /**
     * 提取特征值
     */
    public ImportFeatureResult getFeature(Bitmap bitmap, byte[] feature, BDFaceSDKCommon.FeatureType featureType) {
        if (bitmap == null) {
            return new ImportFeatureResult(2, null);
        }

        BDFaceImageInstance imageInstance = new BDFaceImageInstance(bitmap);
        // 最大检测人脸，获取人脸信息
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
        if (faceInfos == null || faceInfos.length == 0) {
            imageInstance.destory();
            // 图片外扩
            Bitmap broadBitmap = BitmapUtils.broadImage(bitmap);
            imageInstance = new BDFaceImageInstance(broadBitmap);
            // 最大检测人脸，获取人脸信息
            faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                    .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
            // 若外扩后还未检测到人脸，则旋转图片检测
            if (faceInfos == null || faceInfos.length == 0) {
                return new ImportFeatureResult(/*rotationDetection(broadBitmap , 90)*/8, null);
            }
        }
        // 判断多人脸
        if (faceInfos.length > 1) {
            imageInstance.destory();
            return new ImportFeatureResult(9, null);
        }
        FaceInfo faceInfo = faceInfos[0];
        // 判断质量
        int quality = onQualityCheck(faceInfo);
        if (quality != 0) {
            return new ImportFeatureResult(quality, null);
        }
        // 人脸识别，提取人脸特征值
        float ret = FaceSDKManager.getInstance().getFaceFeature().feature(
                featureType, imageInstance,
                faceInfo.landmarks, feature);
        // 人脸抠图
        /*BDFaceImageInstance cropInstance = FaceSDKManager.getInstance().getFaceCrop()
                .cropFaceByLandmark(imageInstance, faceInfo.landmarks,
                        2.0f, true, new AtomicInteger());
        if (cropInstance == null) {
            imageInstance.destory();
            return new ImportFeatureResult(10, null);
        }

        Bitmap cropBmp = BitmapUtils.getInstaceBmp(cropInstance);
        cropInstance.destory();*/
        imageInstance.destory();
        return new ImportFeatureResult(ret, null);
    }


    /**
     * 质量检测结果过滤，如果需要质量检测，
     * 需要调用 SingleBaseConfig.getBaseConfig().setQualityControl(true);设置为true，
     * 再调用  FaceSDKManager.getInstance().initConfig() 加载到底层配置项中
     *
     * @return
     */
    public int onQualityCheck(FaceInfo faceInfo) {

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return 0;
        }

        if (faceInfo != null) {

            // 角度过滤
            if (Math.abs(faceInfo.yaw) > SingleBaseConfig.getBaseConfig().getYaw()) {
                return 4;
            } else if (Math.abs(faceInfo.roll) > SingleBaseConfig.getBaseConfig().getRoll()) {
                return 4;
            } else if (Math.abs(faceInfo.pitch) > SingleBaseConfig.getBaseConfig().getPitch()) {
                return 4;
            }

            // 模糊结果过滤
            float blur = faceInfo.bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                return 5;
            }

            // 光照结果过滤
            float illum = faceInfo.illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                return 7;
            }


            // 遮挡结果过滤
            if (faceInfo.occlusion != null) {
                BDFaceOcclusion occlusion = faceInfo.occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // 左眼遮挡置信度
                    return 6;
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // 右眼遮挡置信度
                    return 6;
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // 鼻子遮挡置信度
                    return 6;
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // 嘴巴遮挡置信度
                    return 6;
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // 左脸遮挡置信度
                    return 6;
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // 右脸遮挡置信度
                    return 6;
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // 下巴遮挡置信度
                    return 6;
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }

    private static class SDCardFileExplorerHolder extends RecyclerView.ViewHolder {
        private View itemView;

        private ImageView imgFile;
        private TextView tvSDCardFileName;

        public SDCardFileExplorerHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            imgFile = itemView.findViewById(R.id.imgFile);
            tvSDCardFileName = itemView.findViewById(R.id.tvSDCardFileName);
        }
    }

    public class SDCardFileExplorerAdapter extends RecyclerView.Adapter<SDCardFileExplorerHolder> implements View.OnClickListener {

        private OnItemClickListener mItemClickListener;

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public SDCardFileExplorerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sd_card_file_list, parent, false);
            SDCardFileExplorerHolder viewHolder = new SDCardFileExplorerHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull SDCardFileExplorerHolder holder, int position) {
            holder.itemView.setTag(position);

            if (currentFiles[position].isDirectory()) {
                holder.imgFile.setImageResource(R.mipmap.icon_file);
            }
            holder.tvSDCardFileName.setText(currentFiles[position].getName());
        }

        @Override
        public int getItemCount() {
            return currentFiles != null ? currentFiles.length : 0;
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(view, (Integer) view.getTag());
            }
        }
    }
}
