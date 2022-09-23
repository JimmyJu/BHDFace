package com.baidu.idl.main.facesdk.registerlibrary.user.manager;


import android.util.Log;

import com.baidu.idl.main.facesdk.registerlibrary.user.callback.RemoveStaffCallback;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.LogUtils;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.listener.DBLoadListener;
import com.example.datalibrary.model.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 用户管理
 * Created by v_liujialu01 on 2018/12/14.
 */

public class UserInfoManager {
    private static final String TAG = UserInfoManager.class.getSimpleName();
    private ExecutorService mExecutorService = null;
    private Future future;

    private RemoveStaffCallback removeStaffCallback;

    // 私有构造
    private UserInfoManager() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    private static class HolderClass {
        private static final UserInfoManager instance = new UserInfoManager();
    }

    public static UserInfoManager getInstance() {
        return HolderClass.instance;
    }

    /**
     * 释放
     */
    public void release() {
        FaceApi.getInstance().isDelete = false;
        if (future != null && !future.isDone()) {
            future.cancel(true);
            return;
        }

        LogUtils.i(TAG, "release");
    }

    /**
     * 删除用户列表信息
     */
    public void deleteUserListInfo(final List<User> list, final String userName,
                                   final UserInfoListener listener, final DBLoadListener dbLoadListener) {
        future = mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (list == null) {
                    listener.userListDeleteFailure("参数异常");
                    return;
                }
                FaceApi.getInstance().isDelete = true;
                FaceApi.getInstance().userDeletes(list, userName != null && !"".equals(userName), dbLoadListener);
                FaceApi.getInstance().isDelete = false;
                listener.userListDeleteSuccess();
            }
        });
    }

    /**
     * 获取用户列表信息
     */
    public void getUserListInfo(final String userName, final UserInfoListener listener) {
        future = mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                // 如果关键字为null，则全局查找
                if (userName == null) {
                    listener.userListQuerySuccess(null, FaceApi.getInstance().getAllUserList());
                } else {
                    listener.userListQuerySuccess(userName,
                            FaceApi.getInstance().getUserListByUserNameVag(userName));
                }
            }
        });
    }


    /**
     * 删除单个用户信息
     *
     * @param userInfo 要删除的用户卡号
     */
    public void deleteUserInfo(final String userInfo) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                boolean success = FaceApi.getInstance().userDelete(userInfo);
                Log.e(TAG, "run: success" + success);
                /*if (success) {
                    Log.e(TAG, "removeStaff成功 " + userInfo);
                } else {
                    Log.e(TAG, "removeStaff失败 " + userInfo);
                }*/


                if (success) {
                    if (removeStaffCallback != null) {
                        removeStaffCallback.removeStaffSuccess();
                    }
                } else {
                    if (removeStaffCallback != null) {
                        removeStaffCallback.removeStaffFailure();
                    }
                }
            }
        });
    }

    public static class UserInfoListener {

        public void userListQuerySuccess(String userName, List<User> listUserInfo) {
            // 用户列表查询成功
        }

        public void userListQueryFailure(String message) {
            // 用户列表查询失败
        }

        public void userListDeleteSuccess() {
            // 用户列表删除成功
        }

        public void userListDeleteFailure(String message) {
            // 用户列表删除失败
        }
    }

    public RemoveStaffCallback getRemoveStaffCallback() {
        return removeStaffCallback;
    }

    public void setRemoveStaffCallback(RemoveStaffCallback removeStaffCallback) {
        this.removeStaffCallback = removeStaffCallback;
    }
}
