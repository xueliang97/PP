package com.hdu.pp;

import android.os.Bundle;
import android.os.UserManager;
import android.text.TextUtils;
import android.view.MenuItem;
import com.hdu.pp.login.*;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hdu.libnetwork.ApiResponse;
import com.hdu.libnetwork.GetRequest;
import com.hdu.libnetwork.JsonCallback;
import com.hdu.pp.model.Destination;
import com.hdu.pp.model.User;
import com.hdu.pp.utils.AppConfig;
import com.hdu.pp.utils.NavGraphBuilder;
import com.hdu.pp.utils.StatusBar;
import com.hdu.pp.view.AppBottomBar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private NavController navController;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(fragment);
  //      NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    //    NavigationUI.setupWithNavController(navView, navController);
        navView.setOnNavigationItemSelectedListener(this);
        NavGraphBuilder.build(fragment.getChildFragmentManager(),navController,this,fragment.getId());

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        HashMap<String, Destination> destConfig = AppConfig.getsDestConfig();
        Iterator<Map.Entry<String,Destination>> it = destConfig.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, Destination> entry = it.next();
            Destination value = entry.getValue();
            if (value!=null&& !MyUserManager.get().isLogin()&&value.needLogin&&value.id==menuItem.getItemId()){
                MyUserManager.get().login(this).observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        navView.setSelectedItemId(menuItem.getItemId());
                    }
                });
                return false;
            }
        }
        navController.navigate(menuItem.getItemId());
        return !TextUtils.isEmpty(menuItem.getTitle()); //为空就着色
    }
}
