package com.hdu.pp.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.hdu.libnetwork.cache.CacheManager;
import com.hdu.pp.model.User;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MyUserManager {
    private static MyUserManager userManager = new MyUserManager();

    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    public static final String KEY_CACHE_USER = "cache_user";
    private User mUser;


    public static MyUserManager get(){
        return userManager;
    }

    private MyUserManager(){
        User cache = (User) CacheManager.getCache(KEY_CACHE_USER);
        if (cache!=null && cache.expires_time>System.currentTimeMillis() )
            mUser = cache;

    }

    public void save(User user){
        mUser = user;
        CacheManager.save(KEY_CACHE_USER,user);
        if (userLiveData.hasObservers()){
            userLiveData.postValue(user);
        }
    }

    public LiveData<User> login(Context context){
        Intent intent = new Intent(context,LoginActivity.class);
        if (!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return getUserLiveData();
    }

    public boolean isLogin(){
        return mUser==null ? false: mUser.expires_time>System.currentTimeMillis();
    }

    public User getUser(){
        return isLogin()?mUser:null;
    }

    public long getUserId(){
        return isLogin() ? mUser.id : 0;
    }

    private MutableLiveData<User> getUserLiveData() {
        if (userLiveData==null)
            userLiveData = new MutableLiveData<>();
        return userLiveData;
    }


}
