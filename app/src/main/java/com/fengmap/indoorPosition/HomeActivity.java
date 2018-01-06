package com.fengmap.indoorPosition;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.fengmap.android.FMMapSDK;

public class HomeActivity extends AppCompatActivity {

    Button forward_to_wifi_list;
    Button forward_to_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        forward_to_wifi_list = (Button) findViewById(R.id.forward_to_wifi_list) ;
        forward_to_wifi_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,WifiListActivity.class);
                startActivityForResult(intent,0);
            }
        });

        forward_to_map = (Button) findViewById(R.id.forward_to_map) ;
        forward_to_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,NavActivity.class);
                startActivityForResult(intent,0);
            }
        });
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