package com.tencent.liteav.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.FrameLayout;

import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PreviewActivity extends Activity implements TXVideoEditer.TXVideoPreviewListener {
    private TXVideoEditer mTXVideoEditer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // 创建编辑对象TXVideoEditer
        mTXVideoEditer = new TXVideoEditer(this);
        // 设置原视频路径
        String dstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Utils.copyFromAssetToSdcard(this, "test.mp4", dstPath);

        String srcPath = dstPath + File.separator + "test.mp4";
        int ret = mTXVideoEditer.setVideoPath(srcPath);
        Log.i("SDK", "ret:" + ret);

        // 初始化预览界面
        FrameLayout videoview = (FrameLayout) findViewById(R.id.video_view);
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = videoview;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);

        // 设置预览回调进度
        mTXVideoEditer.setTXVideoPreviewListener(this);
        // 开始预览
        mTXVideoEditer.startPlayFromTime(0, 10000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTXVideoEditer != null) {
            // 停止预览
            mTXVideoEditer.stopPlay();
            // 释放资源
            mTXVideoEditer.release();
        }
    }


    @Override
    public void onPreviewProgress(int time) {

    }

    @Override
    public void onPreviewFinished() {
        Log.i("SDK", "onPreviewFinished");
    }
}
