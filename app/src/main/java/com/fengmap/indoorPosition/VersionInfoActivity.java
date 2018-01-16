package com.fengmap.indoorPosition;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by ACER on 2018/1/16.
 */

public class VersionInfoActivity extends AppCompatActivity {

    private Button update_version;
    private Button version_info_cancel;
    private TextView version_code;
    private TextView version_name;
    private TextView version_latest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_info);

        version_code = (TextView) findViewById(R.id.version_code);
        version_code.setText(String.valueOf(getVersionCode(this)));

        version_name = (TextView) findViewById(R.id.version_name);
        version_name.setText(getVersionName(this));

        version_latest = (TextView) findViewById(R.id.version_latest);
        version_latest.setText("是");

        update_version = (Button) findViewById(R.id.update_version);
        update_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        version_info_cancel = (Button) findViewById(R.id.version_info_cancel);
        version_info_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * 获取当前本地apk的版本
     *
     * @param mContext
     * @return
     */
    public int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return
     */
    public String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }
}
