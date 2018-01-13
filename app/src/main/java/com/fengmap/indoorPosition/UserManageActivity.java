package com.fengmap.indoorPosition;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by ACER on 2018/1/13.
 */

public class UserManageActivity extends AppCompatActivity {

    private Button switch_account;
    private Button log_off;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_manage);

        switch_account = (Button) findViewById(R.id.switch_account);
        switch_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        log_off = (Button) findViewById(R.id.log_off);
        log_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
