package com.hdu.libnetwork;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.hdu.libnetwork.cache.CacheManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//reponse实体类型，request子类
public abstract class Request<T,R extends Request> implements Cloneable{
    private static final String TAG = "Request";
    
    protected String mUrl;
    protected HashMap<String,String> headers = new HashMap<>();
    protected HashMap<String,Object> params = new HashMap<>();

    public static final int CACHE_ONLY = 1;//仅仅访问本地缓存，即便缓存不存在也不访问网络
    public static final int CACHE_FIRST = 2;//先访问缓存，同时 发起网络请求，成功后缓存到本地
    public static final int NET_ONLY = 3;//仅仅访问网络，不存任何存储
    public static final int NET_CACHE = 4;//先访问网络，成功后缓存到本地
    private String cacheKey;
    private Type mType;
    private Class mClaz;
    private int mCacheStrategy = NET_ONLY;

    @IntDef({CACHE_ONLY,CACHE_FIRST,NET_ONLY,NET_CACHE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CacheStrategy{

    }
    public Request(String url){
        mUrl = url;
    }

    public R addHeader(String key,String value){
        headers.put(key,value);
        return (R)this;
    }

    public R addParam(String key,Object value){
        if (value == null)
            return (R) this;
        try {
            if (value.getClass()==String.class)
                params.put(key,value);
            else {
                Field field = value.getClass().getField("TYPE");
                Class claz = (Class) field.get(null);
                if (claz.isPrimitive()) {//是基本类型
                    params.put(key, value);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R)this;
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy){

        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    public R cacheKey(String key){
        this.cacheKey = key;
        return (R)this;
    }


    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        Call call = ApiService.httpClient.newCall(request);
        return call;
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    protected  void addHeaders(okhttp3.Request.Builder builder){
        for(Map.Entry<String,String> entry:headers.entrySet()){
            builder.addHeader(entry.getKey(),entry.getValue());
        }
    }

    public R responseType(Type type){
        mType = type;
        return (R)this;
    }

    public R responseType(Class claz){
        mClaz = claz;
        return (R)this;
    }
    
    public ApiResponse<T>  execute(){//同步
        if(mType == null)
            throw new RuntimeException("同步方法，response 返回值 类型必须设置");
        if (mCacheStrategy == CACHE_ONLY){
            return readCache();
        }
        if (mCacheStrategy != CACHE_ONLY){
            ApiResponse<T> result = null;
            try {
                Response response = getCall().execute();
                result = parseResponse(response,null);

            } catch (IOException e) {
                e.printStackTrace();
                if (result==null){
                    result = new ApiResponse<>();
                    result.message = e.getMessage();
                }
            }
            return result;
        }
        return null;
    }

    @SuppressLint("RestrictedApi")
    public void  execute(final JsonCallback<T> callback){//异步
        if(mCacheStrategy!=NET_ONLY){
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ApiResponse<T> response = readCache();
                    if (callback!=null && response.body!=null){
                        callback.onCacheSuccess(response);
                    }
                }
            });
        }
        if (mCacheStrategy!=CACHE_ONLY) {
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> response = new ApiResponse<>();
                    response.message = e.getMessage();
                    callback.onError(response);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    ApiResponse<T> apiResponse = parseResponse(response, callback);
                    if (!apiResponse.success) {
                        callback.onError(apiResponse);
                    } else {
                        callback.onSuccess(apiResponse);
                    }
                }
            });
        }
    }

    private ApiResponse<T> readCache() {

        String key = TextUtils.isEmpty(cacheKey)?generateCacheKey():cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.message = "缓存获取成功";
        result.body = (T)cache;
        result.success = true;
        return result;
    }

    private ApiResponse<T> parseResponse(Response response, JsonCallback<T> callback) {
        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert convert = ApiService.sConvert;
        try {
            String content = response.body().string();
            if (success){
                if (callback!=null){
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();//获得实际泛型T类型
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T) convert.convert(content, argument);
                }else if(mType!=null){
                    result.body = (T) convert.convert(content,mType);
                }else if(mClaz!=null){
                    result.body = (T) convert.convert(content,mClaz);
                }else{
                    Log.e(TAG, "parseResponse: 无法解析" );
                }
            }else {
                message = content;
            }
        }catch (Exception e){
            message = e.getMessage();
            success = false;
            status = 0;
        }
        result.success =success;
        result.status = status;
        result.message = message;

        if (mCacheStrategy!=NET_ONLY&&result.success&&result.body!=null&&result.body instanceof Serializable){
            saveCache(result.body);
        }
        return result;
    }

    private void saveCache(T body) {
        String key = TextUtils.isEmpty(cacheKey)?generateCacheKey():cacheKey;
        CacheManager.save(key,body);
    }

    private String generateCacheKey() {
        cacheKey = UrlCreator.createUrlFromParams(mUrl,params);
        return cacheKey;
    }

    @NonNull
    @Override
    public Request<T, R> clone() throws CloneNotSupportedException {
        return (Request<T,R>)super.clone();
    }
}
