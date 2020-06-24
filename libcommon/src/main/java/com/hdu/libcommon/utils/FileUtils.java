package com.hdu.libcommon.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import com.hdu.libcommon.extention.LiveDataBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class FileUtils {
    /**
     * 截取视频文件的封面
     */
    public static LiveData<String> generateVideoCover(final String filePath){

        MutableLiveData<String> livedate = new MutableLiveData<>();
        ArchTaskExecutor.getIOThreadExecutor().execute(()->{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            //获取第一个关键帧
            Bitmap frame = retriever.getFrameAtTime();
            FileOutputStream fos=null;
            if (frame!=null){
                byte[] bytes = compressBitmap(frame, 200);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                try {
                    file.createNewFile();
                    fos = new FileOutputStream(file);
                    fos.write(bytes);
                    livedate.postValue(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        fos.flush();
                        fos.close();
                        fos =null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }else{
                livedate.postValue(null);
            }

        });


        return null;
    }

    private static byte[] compressBitmap(Bitmap frame, int limit) {//压缩获取到的Bitmap 到200kb以下
        if (frame!=null && limit>0){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            frame.compress(Bitmap.CompressFormat.JPEG,options,baos);
            while(baos.toByteArray().length>limit*1024){ //长度大于200*1024B就进行压缩
                baos.reset();
                options = -5;
                frame.compress(Bitmap.CompressFormat.JPEG,options,baos);//每次压缩质量减5
            }

            byte[] bytes = baos.toByteArray();
            if (baos!=null){
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                baos=null;
            }
            return bytes;
        }
        return null;
    }
}
