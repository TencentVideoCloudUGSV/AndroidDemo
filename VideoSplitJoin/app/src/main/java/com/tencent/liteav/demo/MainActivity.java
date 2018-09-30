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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCRecord;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;
import com.tencent.ugc.TXVideoJoiner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

/**
 * 合唱需要做以下事情：
 * 1、设置一个录制View和一个点播View
 * 2、开始录制时的同时，进行点播播放
 * 3、停止录制后，进行两个mp4文件的画面合成
 */
public class MainActivity extends AppCompatActivity implements TXRecordCommon.ITXVideoRecordListener, TXVideoJoiner.TXVideoJoinerListener {

    private Button start;
    private Button stop;
    private TXCloudVideoView mViewRecord;
    private FrameLayout mViewPlay;
    // SDK API
    private TXUGCRecord mTXCameraRecord;
    private TXVideoEditer mTXVideoEditer;
    private TXVideoJoiner mTXVideoJoiner;
    // 画面合成的两个视频源
    private String mDemoPath;
    private String mRecordPath;
    // 从SDK获取到的两个视频基本信息
    private TXVideoEditConstants.TXVideoInfo demoInfo;
    private TXVideoEditConstants.TXVideoInfo recordInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewRecord = (TXCloudVideoView) findViewById(R.id.video_view_follow_shot_record);
        mViewPlay = (FrameLayout) findViewById(R.id.video_view_follow_shot_play);

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
        initPlayer();
    }

    private void initPlayer() {
        String dstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Utils.copyFromAssetToSdcard(this, "demo.mp4", dstPath);
        mDemoPath = dstPath + File.separator + "demo.mp4";
        // 播放器初始化，这里使用TXVideoEditer，也可以使用TXVodPlayer
        mTXVideoEditer = new TXVideoEditer(this);
        int ret = mTXVideoEditer.setVideoPath(mDemoPath);
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mViewPlay;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasPermission()) {
            startCameraPreview();
        }
    }

    /**
     * 开启相机预览
     */
    private void startCameraPreview() {
        mTXCameraRecord = TXUGCRecord.getInstance(this.getApplicationContext());
        TXRecordCommon.TXUGCSimpleConfig simpleConfig = new TXRecordCommon.TXUGCSimpleConfig();
        simpleConfig.videoQuality = TXRecordCommon.VIDEO_QUALITY_HIGH;
        simpleConfig.minDuration = 2000; //录制最小时长
        simpleConfig.maxDuration = 10000;//录制最大时长
        simpleConfig.isFront = true;
        simpleConfig.touchFocus = false;

        mTXCameraRecord.startCameraSimplePreview(simpleConfig, mViewRecord);
        // 设置渲染模式为自适应模式
        mTXCameraRecord.setVideoRenderMode(TXRecordCommon.VIDEO_RENDER_MODE_ADJUST_RESOLUTION);
        // 静音录制
        mTXCameraRecord.setMute(true);
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
     * 开始录制
     */
    private void startRecord() {
        mTXVideoEditer.startPlayFromTime(0, 10000);

        mTXCameraRecord.setVideoRecordListener(this);
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

    @Override
    public void onRecordEvent(int event, Bundle bundle) {
        Log.d("SDK", "onRecordEvent event id = " + event);
    }

    @Override
    public void onRecordProgress(long milliSecond) {

    }

    @Override
    public void onRecordComplete(TXRecordCommon.TXRecordResult result) {
        Log.d("SDK", "onRecordComplete result.retCode = " + result.retCode);
        if (result.retCode < 0) {
            return;
        }
        mRecordPath = result.videoPath;

        demoInfo = TXVideoInfoReader.getInstance().getVideoFileInfo(mDemoPath);
        recordInfo = TXVideoInfoReader.getInstance().getVideoFileInfo(mRecordPath);

        spliteJoin();
    }

    /**
     * 画面合成
     */
    private void spliteJoin() {
        List<String> sourceList = new ArrayList<>();
        sourceList.add(mRecordPath);
        sourceList.add(mDemoPath);

        // 初始化合拍的接口
        mTXVideoJoiner = new TXVideoJoiner(this);
        mTXVideoJoiner.setVideoJoinerListener(this);
        mTXVideoJoiner.setVideoPathList(sourceList);

        String outputPath = Environment.getExternalStorageDirectory() + File.separator + "splite_output.mp4";
        // 以左边录制的视频宽高为基准，右边视频等比例缩放
        int followVideoWidth;
        int followVideoHeight;
        if ((float) demoInfo.width / demoInfo.height >= (float) recordInfo.width / recordInfo.height) {
            followVideoWidth = recordInfo.width;
            followVideoHeight = (int) ((float) recordInfo.width * demoInfo.height / demoInfo.width);
        } else {
            followVideoWidth = (int) ((float) recordInfo.height * demoInfo.width / demoInfo.height);
            followVideoHeight = recordInfo.height;
        }

        TXVideoEditConstants.TXAbsoluteRect rect1 = new TXVideoEditConstants.TXAbsoluteRect();
        rect1.x = 0;                      //第一个视频的左上角位置
        rect1.y = 0;
        rect1.width = recordInfo.width;   //第一个视频的宽高
        rect1.height = recordInfo.height;

        TXVideoEditConstants.TXAbsoluteRect rect2 = new TXVideoEditConstants.TXAbsoluteRect();
        rect2.x = rect1.x + rect1.width;   //第2个视频的左上角位置
        rect2.y = (recordInfo.height - followVideoHeight) / 2;
        rect2.width = followVideoWidth;    //第2个视频的宽高
        rect2.height = followVideoHeight;

        List<TXVideoEditConstants.TXAbsoluteRect> list = new ArrayList<>();
        list.add(rect1);
        list.add(rect2);
        mTXVideoJoiner.setSplitScreenList(list, recordInfo.width + followVideoWidth, recordInfo.height); //第2，3个param：两个视频合成画布的宽高
        mTXVideoJoiner.splitJoinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, outputPath);
    }

    @Override
    public void onJoinProgress(float progress) {
        Log.d("SDK", "onJoinProgress progress = " + progress);
    }

    @Override
    public void onJoinComplete(TXVideoEditConstants.TXJoinerResult result) {
        Log.d("SDK", "onJoinComplete result = " + result.retCode);
        if (result.retCode == 0) {
            Toast.makeText(this, "合成完成", Toast.LENGTH_SHORT).show();
        }
    }
}