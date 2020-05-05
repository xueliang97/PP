package com.hdu.pp.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hdu.libcommon.AppGlobals;
import com.hdu.pp.model.BottomBar;
import com.hdu.pp.model.Destination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class AppConfig {//Json解析成实体类并存入Map

    private static HashMap<String, Destination> sDestConfig;

    private static BottomBar sBottomBar;

    public static BottomBar getBottomBar(){
        if (sBottomBar == null){
            String content = parseFile("main_tabs_config.json");
            sBottomBar = JSON.parseObject(content,BottomBar.class);
        }

        return sBottomBar;
    }

    public static HashMap<String, Destination> getsDestConfig() {
        if(sDestConfig == null){
            String content = parseFile("destination.json");
            sDestConfig = JSON.parseObject(content,new TypeReference<HashMap<String,Destination>>(){}.getType());
        }
        return sDestConfig;
    }

    public static String parseFile(String fileName){
        AssetManager assets = AppGlobals.getApplication().getResources().getAssets();
        InputStream in = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();

        try {
            in = assets.open(fileName);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line = reader.readLine())!=null){
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (in != null)
                    in.close();
                if (reader != null)
                    reader.close();
            }catch (Exception e){

            }
        }
        return builder.toString();
    }

}
