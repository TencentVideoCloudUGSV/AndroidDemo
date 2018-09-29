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
import com.tencent.ugc.TXVideoJoiner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GenerateActivity extends Activity implements TXVideoJoiner.TXVideoJoinerListener {
    private Button start;
    private Button stop;
    private TXVideoJoiner mTXVideoJoiner;

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
        String dstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Utils.copyFromAssetToSdcard(this, "test.mp4", dstPath);
        Utils.copyFromAssetToSdcard(this, "test2.mp4", dstPath);

        // 创建视频合成对象TXVideoJoiner
        mTXVideoJoiner = new TXVideoJoiner(this);
        String srcPath1 = dstPath + File.separator + "test.mp4";
        String srcPath2 = dstPath + File.separator + "test2.mp4";
        List list = new ArrayList();
        list.add(srcPath1);
        list.add(srcPath2);

        // 设置原视频路径
        int ret = mTXVideoJoiner.setVideoPathList(list);
        Log.i("SDK", "ret:" + ret);

        // 设置合成输出文件
        String outputPath = Environment.getExternalStorageDirectory() + File.separator + "join_output.mp4";
        // 设置合成回调进度
        mTXVideoJoiner.setVideoJoinerListener(this);
        // 开始合成
        mTXVideoJoiner.joinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, outputPath);
    }

    /**
     * 停止生成
     */
    private void stopGenerate() {
        if (mTXVideoJoiner != null) {
            // 取消生成
            mTXVideoJoiner.cancel();
        }
    }


    @Override
    public void onJoinProgress(float progress) {
        Log.i("SDK", "onJoinProgress:" + progress);
    }

    @Override
    public void onJoinComplete(TXVideoEditConstants.TXJoinerResult result) {
        Log.i("SDK", "onJoinComplete:" + result.retCode);
    }
}
