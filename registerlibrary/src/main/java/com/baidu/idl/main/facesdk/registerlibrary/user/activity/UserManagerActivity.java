package com.baidu.idl.main.facesdk.registerlibrary.user.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.registerlibrary.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.adapter.FaceUserAdapter;
import com.baidu.idl.main.facesdk.registerlibrary.user.listener.OnItemClickListener;
import com.baidu.idl.main.facesdk.registerlibrary.user.listener.OnRemoveListener;
import com.baidu.idl.main.facesdk.registerlibrary.user.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.ShareManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewDepthActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewNIRActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.register.FaceRegisterNewRgbNirDepthActivity;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.DensityUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.KeyboardsUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.ToastUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.view.TipDialog;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.listener.DBLoadListener;
import com.example.datalibrary.model.User;

import java.util.List;

/**
 * ??????????????????
 * Created by liujialu on 2020/02/10.
 */

public class UserManagerActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, OnItemClickListener,
        TipDialog.OnTipDialogClickListener, OnRemoveListener {
    private static final String TAG = UserManagerActivity.class.getName();

    // view
    private RecyclerView mRecyclerUserManager;
    private ImageView mImageIconSearch;        // title??????????????????
    private RelativeLayout mRelativeStandard; // title??????????????????
    private LinearLayout mLinearSearch;       // title??????????????????
    private ImageView mImageMenu;             // title????????????
    private boolean isCheck = false;            // title??????????????????
    private TextView mTextCancel;             // title??????????????????
    private RelativeLayout mRelativeEmpty;   // ????????????
    private TextView mTextEmpty;
    private EditText mEditTitleSearch;

    private TextView mTv_title;

    private PopupWindow mPopupMenu;
    private RelativeLayout mRelativeDelete;   // ????????????
    private CheckBox mCheckAll;               // ??????
    private TextView mTextDelete;             // ??????
    private TipDialog mTipDialog;

    // pop
    private RelativeLayout mPopRelativeImport;

    private Context mContext;
    private UserListListener mUserListListener;
    private FaceUserAdapter mFaceUserAdapter;
    private List<User> mUserInfoList;
    private int mSelectCount;                // ???????????????
    private boolean mIsLongClick;           // ???????????????
    private int mLiveType;

    private ProgressBar progressBar;
    private TextView progressText;
    private View progressGroup;
    private boolean isDBLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        FaceSDKManager.getInstance().initDataBases(this);
        setContentView(R.layout.activity_user_manager);
        mContext = this;
        initView();
        initData();
    }

    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(this, new SdkInitListener() {
                @Override
                public void initStart() {
                }

                @Override
                public void initLicenseSuccess() {
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                }

                @Override
                public void initModelSuccess() {
                    FaceSDKManager.initModelSuccess = true;
                    ToastUtils.toast(UserManagerActivity.this, "?????????????????????????????????");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(UserManagerActivity.this, "??????????????????????????????????????????");
                    }
                }
            });
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // ????????????????????????????????????
        UserInfoManager.getInstance().getUserListInfo(null, mUserListListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ???????????????????????????
//        FaceSDKManager.getInstance().initDatabases();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initView() {
        mRecyclerUserManager = findViewById(R.id.recycler_user_manager);
        mRecyclerUserManager.setOnClickListener(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerUserManager.setLayoutManager(layoutManager);
        // title???????????????
        mRelativeStandard = findViewById(R.id.relative_standard);
        mLinearSearch = findViewById(R.id.linear_title_search);
        mImageIconSearch = findViewById(R.id.image_icon_research);
        mImageIconSearch.setOnClickListener(this);
        mImageMenu = findViewById(R.id.image_menu);
        mImageMenu.setOnClickListener(this);
        mTextCancel = findViewById(R.id.text_cancel);
        mTextCancel.setOnClickListener(this);

        mTv_title = findViewById(R.id.tv_title);
        int i = FaceApi.getInstance().getmUserNum();
        if (i > 0) {
            mTv_title.setText(String.format("???????????????????????? ??? %s ????????????", i));
        }

        mRelativeEmpty = findViewById(R.id.relative_empty);
        mTextEmpty = findViewById(R.id.text_empty);
        // ?????????????????????
        mRelativeDelete = findViewById(R.id.relative_botton_delete);
        mRelativeDelete.setOnClickListener(this);
        mCheckAll = findViewById(R.id.check_all);
        mCheckAll.setOnCheckedChangeListener(this);
        mTextDelete = findViewById(R.id.text_delete);
        mTextDelete.setOnClickListener(this);
        // title???????????????????????????
        Button btnTitleCancel = findViewById(R.id.btn_title_cancel);
        btnTitleCancel.setOnClickListener(this);
        ImageView imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(this);
        ImageView imageInputDelete = findViewById(R.id.image_input_delete);
        imageInputDelete.setOnClickListener(this);
        // ?????????PopupWindow
        initPopupWindow();
        mTipDialog = new TipDialog(mContext);
        mTipDialog.setOnTipDialogClickListener(this);

        mFaceUserAdapter = new FaceUserAdapter();
        mRecyclerUserManager.setAdapter(mFaceUserAdapter);
        mFaceUserAdapter.setItemClickListener(this);
        mFaceUserAdapter.setOnRemoveListener(this);

        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        progressGroup = findViewById(R.id.progress_group);

        // title???????????????
        mEditTitleSearch = findViewById(R.id.edit_title_search);
        // ???????????????????????????
        mEditTitleSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mUserListListener != null && s != null) {
                    // ????????????????????????????????????
                    UserInfoManager.getInstance().getUserListInfo(s.toString(), mUserListListener);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * ?????????PopupWindow
     */
    private void initPopupWindow() {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.userpopup_menu, null);
        mPopupMenu = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupMenu.setFocusable(true);
        mPopupMenu.setOutsideTouchable(true);
        mPopupMenu.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_round));

        RelativeLayout relativeRegister = contentView.findViewById(R.id.relative_register);
        mPopRelativeImport = contentView.findViewById(R.id.relative_import);
        RelativeLayout relativeDelete = contentView.findViewById(R.id.relative_delete);

        relativeRegister.setOnClickListener(this);
        mPopRelativeImport.setOnClickListener(this);
        relativeDelete.setOnClickListener(this);
        mPopupMenu.setContentView(contentView);
    }

    private void initData() {
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        mUserListListener = new UserListListener();
        // ????????????????????????????????????
        UserInfoManager.getInstance().getUserListInfo(null, mUserListListener);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_title_cancel) {  // ????????????
            mEditTitleSearch.setText("");
            mLinearSearch.setVisibility(View.GONE);
            mRelativeStandard.setVisibility(View.VISIBLE);
            UserInfoManager.getInstance().getUserListInfo(null, mUserListListener);
        } else if (id == R.id.image_icon_research) {  // ????????????
            if (!isDBLoad){
                return;
            }
            mLinearSearch.setVisibility(View.VISIBLE);
            mRelativeStandard.setVisibility(View.GONE);
        } else if (id == R.id.image_back) {
            if (FaceApi.getInstance().isDelete){
                mTipDialog = new TipDialog(mContext);
                mTipDialog.setOnTipDialogClickListener(new TipDialog.OnTipDialogClickListener() {
                    @Override
                    public void onCancel() {
                        mTipDialog.dismiss();
                    }

                    @Override
                    public void onConfirm(String tipType) {
                        mTipDialog.dismiss();

                        UserInfoManager.getInstance().release();
                        // ??????
                        finish();
                    }
                });
                mTipDialog.show();
                mTipDialog.setTextMessage("????????????????????????????????????????????????????????????????????????");
                mTipDialog.setTextConfirm("??????");
                mTipDialog.setTextCancel("??????");
                mTipDialog.setTextTitle("??????");
            } else {

                UserInfoManager.getInstance().release();
                // ??????
                finish();
            }
        } else if (id == R.id.image_menu) {
            if (!isDBLoad){
                return;
            }
            if (!isCheck) {
                isCheck = true;
                mImageMenu.setImageResource(R.mipmap.icon_titlebar_menu_hl);
                showPopupWindow(mImageMenu);
            }
        } else if (id == R.id.relative_register) {      // ??????????????????
            if (!isDBLoad){
                return;
            }
            dismissPopupWindow();
            judgeLiveType(mLiveType, FaceRegisterNewActivity.class, FaceRegisterNewNIRActivity.class,
                    FaceRegisterNewDepthActivity.class, FaceRegisterNewRgbNirDepthActivity.class);
        } else if (id == R.id.relative_import) {        // ????????????????????????
            if (!isDBLoad){
                return;
            }
            dismissPopupWindow();
            Intent intent2 = new Intent(mContext, BatchImportActivity.class);
            startActivity(intent2);
        } else if (id == R.id.relative_delete) {       // ????????????UI
            if (!isDBLoad){
                return;
            }
            dismissPopupWindow();
            updateDeleteUI(true);
        } else if (id == R.id.text_cancel) {           // ??????????????????
            if (!isDBLoad){
                return;
            }
            updateDeleteUI(false);
        } else if (id == R.id.text_delete) {           // ????????????
            if (!isDBLoad){
                return;
            }
            if (mSelectCount == 0) {
                ToastUtils.toast(getApplicationContext(), "???????????????????????????");
                return;
            }
            mTipDialog.show();
            mTipDialog.setTextTitle("????????????");
            mTipDialog.setTextMessage("???????????????????????????????????????????????????");
            mTipDialog.setTextConfirm("??????(" + mSelectCount + ")");
            mTipDialog.setCancelable(false);
        } else if (id == R.id.image_input_delete) {
            mEditTitleSearch.setText("");
        } else if (id == R.id.recycler_user_manager) {
            KeyboardsUtils.hintKeyBoards(v);
        } else if (id == R.id.relative_botton_delete) {
            Log.e(TAG, "relative_botton_delete");
        }
    }

    /**
     * ??????????????????????????????
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isDBLoad){
            mCheckAll.setChecked(true);
            return;
        }
        if (isChecked) {      // ??????
            mSelectCount = mUserInfoList.size();
            for (int i = 0; i < mUserInfoList.size(); i++) {
                mUserInfoList.get(i).setChecked(true);
                mTextDelete.setText("??????(" + mSelectCount + ")");
                mTextDelete.setTextColor(Color.parseColor("#F34B56"));
            }
        } else {             // ????????????
            mSelectCount = 0;
            for (int i = 0; i < mUserInfoList.size(); i++) {
                mUserInfoList.get(i).setChecked(false);
                mTextDelete.setText("??????(" + mSelectCount + ")");
                mTextDelete.setTextColor(Color.parseColor("#666666"));
            }
        }
        mFaceUserAdapter.notifyDataSetChanged();
    }

    // ??????adapter???item????????????
    @Override
    public void onItemClick(View view, int position) {
        if (!isDBLoad){
            return;
        }
        if (mRelativeDelete.getVisibility() != View.VISIBLE) {
            return;
        }
        // ????????????item?????????????????????
        if (!mUserInfoList.get(position).isChecked()) {
            mUserInfoList.get(position).setChecked(true);
            mSelectCount++;
            mTextDelete.setText("??????(" + mSelectCount + ")");
            mTextDelete.setTextColor(Color.parseColor("#F34B56"));
        } else {
            // ????????????item??????????????????????????????
            mUserInfoList.get(position).setChecked(false);
            mSelectCount--;
            mTextDelete.setText("??????(" + mSelectCount + ")");
            if (mSelectCount == 0) {
                mTextDelete.setTextColor(Color.parseColor("#666666"));
            }
        }
        mFaceUserAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRemove(int position) {
        mUserInfoList.get(position).setChecked(true);
        mSelectCount = 1;
        mIsLongClick = true;
        mTipDialog.show();
        mTipDialog.setTextTitle("????????????");
        mTipDialog.setTextMessage("???????????????????????????????????????????????????");
        mTipDialog.setTextConfirm("??????");
        mTipDialog.setCancelable(false);
    }

    // ???????????????????????????
    @Override
    public void onCancel() {
        if (mIsLongClick) {
            resetDeleteData();
            mIsLongClick = false;
        }
        mFaceUserAdapter.notifyDataSetChanged();
        if (mTipDialog != null) {
            mTipDialog.dismiss();
        }
    }
    private DBLoadListener loadListener = new DBLoadListener() {
        @Override
        public void onStart(final int successCount) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressGroup.setVisibility(View.VISIBLE);
                    /*    if (successCount < 50 && successCount !=0){
                            loadProgress(10);
                        }*/
                    }
                });
        }

        @Override
        public void onLoad(final int finishCount, final int successCount, final float progress) {
//            if (successCount > 50 || successCount == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress((int) (progress * 100));
                        progressText.setText(finishCount + "/" + successCount);
                    }
                });
//            }
        }

        @Override
        public void onComplete(List<User> features, final int successCount) {
        }

        @Override
        public void onFail(int finishCount, int successCount, List<User> features) {

        }
    };

    // ???????????????????????????
    @Override
    public void onConfirm(String tipType) {
        if (mTipDialog != null) {
            mTipDialog.dismiss();
        }
        if (mSelectCount != 0) {
            isDBLoad = false;
            UserInfoManager.getInstance().deleteUserListInfo(mUserInfoList,
                    mEditTitleSearch.getText().toString() , mUserListListener,
                    loadListener);
        } else {
            updateDeleteUI(false);
        }
        if (mSelectCount == mUserInfoList.size()) {
            // ?????????????????????
            ShareManager.getInstance(mContext).setDBState(false);
        }
    }

    // ?????????????????????UI
    private void updateDeleteUI(boolean isShowDeleteUI) {
        if (isShowDeleteUI) {
            mRelativeDelete.setVisibility(View.VISIBLE);
            mImageMenu.setVisibility(View.GONE);
            mImageIconSearch.setVisibility(View.GONE);
            mTextCancel.setVisibility(View.VISIBLE);
            // ?????????????????????
            mFaceUserAdapter.setShowCheckBox(true);
            mFaceUserAdapter.notifyDataSetChanged();
        } else {
            mRelativeDelete.setVisibility(View.GONE);
            mImageMenu.setVisibility(View.VISIBLE);
            mImageIconSearch.setVisibility(View.VISIBLE);
            mTextCancel.setVisibility(View.GONE);
            // ?????????????????????
            mFaceUserAdapter.setShowCheckBox(false);
            mFaceUserAdapter.notifyDataSetChanged();
            if (mUserInfoList != null) {
                for (int i = 0; i < mUserInfoList.size(); i++) {
                    mUserInfoList.get(i).setChecked(false);
                }
            }
            mCheckAll.setChecked(false);
            mSelectCount = 0;
            mTextDelete.setText("??????");
        }
    }

    private void showPopupWindow(ImageView imageView) {
        if (mPopupMenu != null && imageView != null) {
//            if (mUserInfoList == null || mUserInfoList.size() == 0) {
//                mPopRelativeImport.setBackgroundColor(Color.parseColor("#777777"));
//            } else {
//                mPopRelativeImport.setBackgroundColor(Color.parseColor("#666666"));
//                mPopRelativeImport.setBackground(this.getResources()
//                        .getDrawable(R.drawable.button_selector_homemenu_item2));
//            }
            int marginRight = DensityUtils.dip2px(mContext, 20);
            int marginTop = DensityUtils.dip2px(mContext, 56);

            mPopupMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    isCheck = false;
                    mImageMenu.setImageResource(R.mipmap.icon_titlebar_menu);
                }
            });

            mPopupMenu.showAtLocation(imageView, Gravity.END | Gravity.TOP,
                    marginRight, marginTop);
        }
    }

    private void dismissPopupWindow() {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
    }

    private void resetDeleteData() {
        mSelectCount = 0;
        for (int i = 0; i < mUserInfoList.size(); i++) {
            mUserInfoList.get(i).setChecked(false);
        }
        mTextDelete.setText("??????");
    }

    // ?????????????????????????????????
    private class UserListListener extends UserInfoManager.UserInfoListener {
        // ????????????????????????
        @Override
        public void userListQuerySuccess(final String userName, final List<User> listUserInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressGroup.setVisibility(View.GONE);
                    isDBLoad = true;
                    mUserInfoList = listUserInfo;
                    if (listUserInfo == null || listUserInfo.size() == 0) {
                        mFaceUserAdapter.setDataList(listUserInfo);
                        mFaceUserAdapter.notifyDataSetChanged();
                        mRelativeEmpty.setVisibility(View.VISIBLE);
                        mRecyclerUserManager.setVisibility(View.GONE);
                        // ?????????????????????
                        if (userName == null) {
                            mTextEmpty.setText("????????????");
                            updateDeleteUI(false);
                        } else {
                            mTextEmpty.setText("??????????????????");
                            mRelativeDelete.setVisibility(View.GONE);
                        }
                        return;
                    }

                    // ??????????????????
                    resetDeleteData();
                    mRelativeEmpty.setVisibility(View.GONE);
                    mRecyclerUserManager.setVisibility(View.VISIBLE);
                    if (userName == null || userName.length() == 0) {
                        updateDeleteUI(false);
                    } else {
                        updateDeleteUI(true);
                    }
                    mFaceUserAdapter.setDataList(listUserInfo);
                    mFaceUserAdapter.notifyDataSetChanged();


                }
            });
        }

        // ????????????????????????
        @Override
        public void userListQueryFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mContext == null) {
                        return;
                    }
                    ToastUtils.toast(mContext, message);
                }
            });
        }

        // ????????????????????????
        @Override
        public void userListDeleteSuccess() {
            UserInfoManager.getInstance().getUserListInfo(null, mUserListListener);
        }

        // ????????????????????????
        @Override
        public void userListDeleteFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mContext == null) {
                        return;
                    }
                    ToastUtils.toast(mContext, message);
                }
            });
        }
    }

    /**
     * ?????????????????????????????????
     * ??????????????????
     */
    @CallSuper
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (KeyboardsUtils.isShouldHideKeyBord(view, ev)) {
                KeyboardsUtils.hintKeyBoards(view);
            }
        }
        return super.dispatchTouchEvent(ev);
    }




    private void judgeLiveType(int type, Class<?> rgbCls, Class<?> nirCls, Class<?> depthCls, Class<?> rndCls) {
        switch (type) {
            case 1: { // RGB??????
                startActivity(new Intent(UserManagerActivity.this, rgbCls));
                break;
            }

            case 2: { // NIR??????
                startActivity(new Intent(UserManagerActivity.this, nirCls));
                break;
            }

            case 3: { // ????????????
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, depthCls);
                break;
            }

            case 4: { // rgb+nir+depth??????
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, rndCls);
            }
        }
    }

    private void judgeCameraType(int cameraType, Class<?> depthCls) {
        switch (cameraType) {
            case 1: { // pro
                startActivity(new Intent(mContext, depthCls));
                break;
            }

            case 2: { // atlas
                startActivity(new Intent(mContext, depthCls));
                break;
            }

            default:
                startActivity(new Intent(mContext, depthCls));
                break;
        }
    }
}
