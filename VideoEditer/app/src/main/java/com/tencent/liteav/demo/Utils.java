package com.tencent.liteav.demo;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    public static boolean copyFromAssetToSdcard(Context context, String assetFilename, String dstPath) {
        InputStream source = null;
        OutputStream destination = null;
        try {
            source = context.getAssets().open(new File(assetFilename).getPath());
            File destinationFile = new File(dstPath, assetFilename);

            destinationFile.getParentFile().mkdirs();
            destination = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int nread;

            while ((nread = source.read(buffer)) != -1) {
                if (nread == 0) {
                    nread = source.read();
                    if (nread < 0)
                        break;
                    destination.write(nread);
                    continue;
                }
                destination.write(buffer, 0, nread);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (source != null) {
                try {
                    source.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (destination != null) {
                try {
                    destination.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
