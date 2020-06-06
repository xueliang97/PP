package com.hdu.pp.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hdu.libnetwork.ApiResponse;
import com.hdu.libnetwork.ApiService;
import com.hdu.libnetwork.JsonCallback;
import com.hdu.pp.R;
import com.hdu.pp.model.User;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private View actionClose;
    private View actionLogin;
    Tencent tencent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_login);

        actionClose = findViewById(R.id.action_close);
        actionLogin = findViewById(R.id.action_login);

        actionClose.setOnClickListener(this);

        actionLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.action_close:
                finish();
                break;
            case R.id.action_login:
                login();
                //调用qq快捷登录
                break;

        }
    }

    private void login() {
        if (tencent==null)
            tencent = Tencent.createInstance("101794421",getApplicationContext());
        tencent.login(this, "all", new IUiListener() {
            @Override
            public void onComplete(Object o) {
                JSONObject response = (JSONObject) o;
                try {
                    String open_id = response.getString("openid");
                    String access_token = response.getString("access_token");
                    String expires_in = response.getString("expires_in");
                    long expires_time = response.getLong("expires_time");

                    tencent.setAccessToken(access_token,expires_in);
                    tencent.setOpenId(open_id);
                    QQToken qqToken = tencent.getQQToken();
                    getUserInfo(qqToken,open_id,expires_time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                Toast.makeText(getApplicationContext(),"登陆失败：reason"+uiError.toString(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(),"登陆取消",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserInfo(QQToken qqToken, String open_id, long expires_time) {
        UserInfo userInfo = new UserInfo(getApplicationContext(),qqToken);
        userInfo.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object o) {
                JSONObject response = (JSONObject) o;
                try {//获得数据信息保存到数据库
                    String nickname = response.getString("nickname");
                    String figureurl_2 = response.getString("figureurl_2");
                    save(nickname,figureurl_2,open_id,expires_time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                Toast.makeText(getApplicationContext(),"获取失败：reason"+uiError.toString(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(),"获取取消",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save(String nickname, String avatar, String open_id, long expires_time) {

        ApiService.get("/user/insert")
                .addParam("name",nickname)
                .addParam("avatar",avatar)
                .addParam("qqOpenId",open_id)
                .addParam("expires_time",expires_time)
                .execute(new JsonCallback<User>() {
                    @Override
                    public void onSuccess(ApiResponse<User> response) {
                        super.onSuccess(response);
                        if (response.body!=null)
                            MyUserManager.get().save(response.body);
                        else{//转到主线程
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"登录失败",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }

                    @Override
                    public void onError(ApiResponse<User> response) {
                        super.onError(response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"登录失败：msg"+response.message,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCacheSuccess(ApiResponse<User> response) {
                        super.onCacheSuccess(response);
                    }
                });
    }
}
