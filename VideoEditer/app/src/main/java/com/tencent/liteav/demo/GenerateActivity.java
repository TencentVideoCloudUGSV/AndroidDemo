package com.tencent.liteav.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.io.File;

public class GenerateActivity extends Activity implements TXVideoEditer.TXVideoGenerateListener {
    private Button start;
    private Button stop;
    private TXVideoEditer mTXVideoEditer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGenerate();
            }
        });

        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopGenerate();
            }
        });
    }

    /**
     * 开始生成
     */
    private void startGenerate() {
        // 创建编辑对象TXVideoEditer
        mTXVideoEditer = new TXVideoEditer(this);
        // 设置原视频路径
        String dstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Utils.copyFromAssetToSdcard(this, "test.mp4", dstPath);

        String srcPath = dstPath + File.separator + "test.mp4";
        int ret = mTXVideoEditer.setVideoPath(srcPath);
        Log.i("SDK", "ret:" + ret);

        // 设置编辑输出文件
        String outputPath = Environment.getExternalStorageDirectory() + File.separator + "edit_output.mp4";
        // 设置编辑回调进度
        mTXVideoEditer.setVideoGenerateListener(this);
        // 开始预览
        mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, outputPath);
    }

    /**
     * 停止生成
     */
    private void stopGenerate() {
        if (mTXVideoEditer != null) {
            // 取消生成
            mTXVideoEditer.cancel();
            // 释放资源
            mTXVideoEditer.release();
        }
    }

    @Override
    public void onGenerateProgress(float progress) {
        Log.i("SDK", "onGenerateProgress:" + progress);
    }

    @Override
    public void onGenerateComplete(TXVideoEditConstants.TXGenerateResult result) {
        Log.i("SDK", "onGenerateComplete:" + result.retCode);
    }
}
