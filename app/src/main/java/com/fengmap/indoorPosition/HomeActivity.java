package com.fengmap.indoorPosition;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.fengmap.android.FMMapSDK;
import com.fengmap.indoorPosition.entity.UserEntity;
import com.fengmap.indoorPosition.utils.EditTextClearTools;
import com.fengmap.indoorPosition.utils.UserInfo;

public class HomeActivity extends AppCompatActivity {

    private Button btn_login;
    private EditText userName;
    private EditText password;
    private ImageView unameClear;
    private ImageView pwdClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        init();

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo.getUserEntity().setUsername(userName.getText().toString());
                Intent intent = new Intent(HomeActivity.this,NavActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init(){
        userName = (EditText) findViewById(R.id.et_userName);
        password = (EditText) findViewById(R.id.et_password);
        unameClear = (ImageView) findViewById(R.id.iv_unameClear);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);

        EditTextClearTools.addClearListener(userName,unameClear);
        EditTextClearTools.addClearListener(password,pwdClear);
    }


    /**
     * @Email hezutao@fengmap.com
     * @Version 2.0.0
     * @Description 应用层初始化
     */
    public static class DemoApplication extends Application {

        @Override
        public void onCreate() {
            super.onCreate();
            // 在使用 SDK 各组间之前初始化 context 信息，传入 Application
            FMMapSDK.init(this);
        }

    }
}