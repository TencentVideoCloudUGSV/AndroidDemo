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
import com.tencent.ugc.TXVideoJoiner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends Activity implements TXVideoJoiner.TXVideoPreviewListener {
    private TXVideoJoiner mTXVideoJoiner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

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

        // 初始化预览界面
        FrameLayout videoview = (FrameLayout) findViewById(R.id.video_view);
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = videoview;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoJoiner.initWithPreview(param);

        // 设置预览回调进度
        mTXVideoJoiner.setTXVideoPreviewListener(this);
        // 开始预览
        mTXVideoJoiner.startPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTXVideoJoiner != null) {
            // 停止预览
            mTXVideoJoiner.stopPlay();
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
