package com.ibamb.udm.component;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.ibamb.udm.activity.DeviceProfileActivity;
import com.ibamb.udm.activity.LoginActivity;
import com.ibamb.udm.log.UdmLog;
import com.ibamb.udm.module.core.TryUser;
import com.ibamb.udm.task.UserLoginAsyncTask;

public class LoginComponet {
    private String mac;
    private String ip;
    private boolean isToProfile;//是否直接跳入设备profile页面
    private boolean isSyncMenuEnabled;//是否显示同步菜单。
    private Activity activity;

    public boolean isToProfile() {
        return isToProfile;
    }

    public void setToProfile(boolean toProfile) {
        isToProfile = toProfile;
    }

    public boolean isSyncMenuEnabled() {
        return isSyncMenuEnabled;
    }

    public void setSyncMenuEnabled(boolean syncMenuEnabled) {
        isSyncMenuEnabled = syncMenuEnabled;
    }

    public LoginComponet(Activity activity, String mac, String ip) {
        this.activity = activity;
        this.mac = mac;
        this.ip = ip;
    }


    public void login() {
        try {
            boolean trySuccess = false;
            for (int i = 0; i < TryUser.getUserCount(); i++) {
                UserLoginAsyncTask loginAsyncTask = new UserLoginAsyncTask();
                String[] tryUser = TryUser.getUser(i + 1);
                if (tryUser == null) {
                    continue;
                }
                String[] loginInfo = {tryUser[0], tryUser[1], mac, ip};
                loginAsyncTask.execute(loginInfo);
                trySuccess = loginAsyncTask.get();
                if (trySuccess) {
                    break;
                }
            }
            if (trySuccess) {
                if(isToProfile){
                    Intent intent = new Intent(activity, DeviceProfileActivity.class);
                    Bundle params = new Bundle();
                    params.putString("HOST_ADDRESS", ip);
                    params.putString("HOST_MAC", mac);
                    params.putBoolean("SYNC_ENABLED", isSyncMenuEnabled);
                    intent.putExtras(params);
                    activity.startActivity(intent);
                }else {
                    activity.recreate();//如果不需要跳转到设备profile页面，则认为是重建当前页面。
                }
            } else {
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.putExtra("HOST_ADDRESS",ip);
                intent.putExtra("HOST_MAC",mac);
                activity.startActivityForResult(intent,1);
            }
        } catch (Exception e) {
            UdmLog.e(this.getClass().getName(),e.getMessage());
        }
    }
}