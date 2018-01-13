package com.fengmap.indoorPosition;
/**
 * Created by ACER on 2018/1/13.
 */
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fengmap.indoorPosition.utils.RoundImageView;

import org.feezu.liuli.timeselector.TimeSelector;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;

/**
 * Created by ACER on 2018/1/12.
 */

public class PersonalActivity extends AppCompatActivity {
    private RoundImageView personal_portrait;
    private TextView personal_graduate_time;
    private Button personal_info_cancel;
    private TextView personal_device_name;
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private File tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_info);

        personal_portrait = (RoundImageView) findViewById(R.id.personal_portrait);
        personal_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent=new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                startActivityForResult(intent,PHOTO_REQUEST_GALLERY);
                // 激活系统图库，选择一张图片
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
            }
        });

        //获取设备mac
        personal_device_name = (TextView) findViewById(R.id.personal_device_name);
        personal_device_name.setText(getMac());

        //设置时间控件
        final TimeSelector timeSelector = new TimeSelector(this, new TimeSelector.ResultHandler() {
            @Override
            public void handle(String time) {
                personal_graduate_time.setText(time);
            }
        },"2018-1-1 00:00", "2030-1-1 00:00");

        personal_graduate_time = (TextView) findViewById(R.id.personal_graduate_time);
        personal_graduate_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeSelector.show();
            }
        });

        personal_info_cancel = (Button) findViewById(R.id.personal_info_cancel);
        personal_info_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /*
            * 剪切图片
            */
    private void crop(Uri uri) {
        //看到有人问裁剪的路径

        // 获取系统时间 然后将裁剪后的图片保存至指定的文件夹

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String address = sDateFormat.format(new java.util.Date());
        String imagePath = address + ".JPEG";
        Uri imageUri = Uri.parse("testPic/" + address
                + ".JPEG");


        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 128);
        intent.putExtra("outputY", 128);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagePath);//输出路径
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, "a.jpg");//输出路径

        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);


        //Cursor cursor = LocationApplication.getContext().getContentResolver().query(uri, null, null, null, null);
        //if (cursor != null && cursor.moveToFirst()) {
        //photopath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        //Log.e("photopath", "photopath:------------" + photopath);
        //}
//filePath=photopath;
//定义一个全局变量，接受photopath

        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                crop(uri);
            }
        } else if (requestCode == PHOTO_REQUEST_CUT) {
            // 从剪切图片返回的数据
            if (data != null) {
                Bitmap bitmap = data.getParcelableExtra("data");
                personal_portrait.setImageBitmap(bitmap);
            }
            try {
                // 将临时文件删除
                tempFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public static String getMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }
}

