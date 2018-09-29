package com.tencent.liteav.demo;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCRecord;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements TXRecordCommon.ITXVideoRecordListener {

    private Button start;
    private Button stop;
    private TXCloudVideoView mTXCloudVideoView;
    // SDK API
    private TXUGCRecord mTXCameraRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasPermission()) {
            startCameraPreview();
        }
    }

    /**
     * 动态申请权限
     *
     * @return
     */
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int ret : grantResults) {
                    if (ret != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                startCameraPreview();
                break;
            default:
                break;
        }
    }

    /**
     * 开启相机预览
     */
    private void startCameraPreview() {
        mTXCameraRecord = TXUGCRecord.getInstance(this.getApplicationContext());
        mTXCameraRecord.setVideoRecordListener(this);
        TXRecordCommon.TXUGCSimpleConfig simpleConfig = new TXRecordCommon.TXUGCSimpleConfig();
        simpleConfig.videoQuality = TXRecordCommon.VIDEO_QUALITY_HIGH;
        simpleConfig.minDuration = 2000; //录制最小时长
        simpleConfig.maxDuration = 60000;//录制最大时长
        simpleConfig.isFront = true;
        simpleConfig.touchFocus = false;

        mTXCameraRecord.startCameraSimplePreview(simpleConfig, mTXCloudVideoView);
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        int result = mTXCameraRecord.startRecord();
        if (result != TXRecordCommon.START_RECORD_OK) {
            if (result == TXRecordCommon.START_RECORD_ERR_NOT_INIT) {
                Toast.makeText(this, "别着急，画面还没出来", Toast.LENGTH_SHORT).show();
            }
            // 增加了TXUgcSDK.licence校验的返回错误码
            else if (result == TXRecordCommon.START_RECORD_ERR_LICENCE_VERIFICATION_FAILED) {
                Toast.makeText(this, "licence校验失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        if (mTXCameraRecord != null) {
            mTXCameraRecord.stopRecord();
        }
    }

    private String getCustomVideoOutputPath() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
        String time = sdf.format(new Date(currentTime));
        String outputDir = Environment.getExternalStorageDirectory() + File.separator + "TXUGC";
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        String tempOutputPath = outputDir + File.separator + "TXUGC_" + time + ".mp4";
        return tempOutputPath;
    }

    @Override
    public void onRecordEvent(int event, Bundle bundle) {
        Log.d("SDK", "onRecordEvent event id = " + event);
    }

    @Override
    public void onRecordProgress(long milliSecond) {

    }

    @Override
    public void onRecordComplete(TXRecordCommon.TXRecordResult result) {
        Toast.makeText(this, "录制失败，原因：" + result.descMsg, Toast.LENGTH_SHORT).show();
    }
}
