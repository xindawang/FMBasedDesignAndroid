package com.fengmap.indoorPosition;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fengmap.indoorPosition.utils.UserInfo;

/**
 * Created by ACER on 2018/1/13.
 */

public class UserManageActivity extends AppCompatActivity {

    private Button switch_account;
    private Button log_off;
    private TextView user_manage_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage
        );

        user_manage_username = (TextView) findViewById(R.id.user_manage_username);
        user_manage_username.setText(UserInfo.getUserEntity().getUsername());

        switch_account = (Button) findViewById(R.id.switch_account);
        switch_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo.getUserEntity().setUsername("");
                Intent intent = new Intent(UserManageActivity.this,NavActivity.class);
                startActivity(intent);
            }
        });

        log_off = (Button) findViewById(R.id.log_off);
        log_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo.getUserEntity().setUsername("");
                Intent intent = new Intent(UserManageActivity.this,NavActivity.class);
                startActivity(intent);
            }
        });
    }
}
