package com.hdu.libnetwork.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CacheManager {

    public static <T> void save(String key,T body){
        Cache cache = new Cache();
        cache.key = key;
        cache.data = toByteArray(body);

        CacheDatabase.get().getCache().save(cache);
    }

    private static <T> byte[] toByteArray(T body) {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(body);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (baos!=null)
                    baos.close();
                if (oos!=null)
                    oos.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return new byte[0];
    }

    public static <T> void delete(String key,T body){
        Cache cache = new Cache();
        cache.key = key;
        cache.data =toByteArray(body);
        CacheDatabase.get().getCache().delete(cache);
    }

    public static Object getCache(String key){//读取数据库缓存
        Cache cache = CacheDatabase.get().getCache().getCache(key);
        if (cache!=null&&cache.data!=null){
            return toObject(cache.data);
        }
        return null;
    }

    private static Object toObject(byte[] data) {//二进制数组转object
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            bais = new ByteArrayInputStream(data);
            ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if (bais!=null)
                    bais.close();
                if (ois!=null)
                    ois.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

}
