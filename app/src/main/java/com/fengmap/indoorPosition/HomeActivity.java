package com.fengmap.indoorPosition;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fengmap.android.FMMapSDK;
import com.fengmap.indoorPosition.entity.UserEntity;
import com.fengmap.indoorPosition.utils.EditTextClearTools;
import com.fengmap.indoorPosition.utils.FileUtils;
import com.fengmap.indoorPosition.utils.UserInfo;

public class HomeActivity extends AppCompatActivity {

    private ImageView portrait;
    private Button btn_login;
    private EditText userName;
    private EditText password;
    private ImageView unameClear;
    private ImageView pwdClear;
    private CheckBox rememberMe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        init();

        btn_login = (Button) findViewById(R.id.btn_login);
        FileUtils.readFromPre(this, userName, password);
        FileUtils.readFromPre(this, portrait);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo.getUserEntity().setUsername(userName.getText().toString());
                doLogin();
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
        rememberMe = (CheckBox) findViewById(R.id.cb_checkbox);
        portrait = (ImageView) findViewById(R.id.iv_icon);

        EditTextClearTools.addClearListener(userName,unameClear);
        EditTextClearTools.addClearListener(password,pwdClear);
    }

    private void doLogin() {
        //toString()不能少,getText()是一个EditText对象
        String name = userName.getText().toString();
        String pass = password.getText().toString();

        Log.d("tag", "点击了按钮"+name+","+pass);
        //将输入框输入的用户名和密码保存到本地的文件中
        if(rememberMe.isChecked())//如果选中了"记住我"的多选框，就将用户名和密码保存，否则不保存
        {
            //FileUtil.saveToFile(this,name,pass);
            //FileUtil.savtToSDCard(this,name,pass);
            //FileUtil.saveToFile2(this, name, pass);
            FileUtils.saveToPre(this, name, pass);
        }
        //登录完之后做一个提示，.show()不可少
        Toast.makeText(this,"success",Toast.LENGTH_SHORT).show();

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