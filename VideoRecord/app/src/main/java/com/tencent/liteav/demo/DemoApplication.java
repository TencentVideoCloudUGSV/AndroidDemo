package com.tencent.liteav.demo;

import android.app.Application;
import android.util.Log;

import com.tencent.rtmp.TXLiveBase;
import com.tencent.ugc.TXUGCBase;

public class DemoApplication extends Application {
    String ugcLicenceUrl = "http://download-1252463788.cossh.myqcloud.com/xiaoshipin/licence_android/TXUgcSDK.licence";
    String ugcKey = "731ebcab46ecc59ab1571a6a837ddfb6";

    @Override
    public void onCreate() {
        super.onCreate();

        TXUGCBase.getInstance().setLicence(this, ugcLicenceUrl, ugcKey);

        String string = TXUGCBase.getInstance().getLicenceInfo(this);
        Log.i("SDK", "string=" + string);
    }
}
