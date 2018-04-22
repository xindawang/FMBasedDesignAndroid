package com.fengmap.indoorPosition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fengmap.indoorPosition.utils.RoundImageView;
import com.fengmap.indoorPosition.utils.UserInfo;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by ACER on 2018/1/13.
 */

public class UserManageActivity extends AppCompatActivity {

    private RoundImageView personal_portrait;
    private Button switch_account;
    private Button log_off;
    private TextView user_manage_username;

    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private File tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage
        );

        user_manage_username = (TextView) findViewById(R.id.user_manage_username);
        user_manage_username.setText(UserInfo.getUserEntity().getUserName());

        switch_account = (Button) findViewById(R.id.switch_account);
        switch_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo.getUserEntity().setUserName("");
                Intent intent = new Intent(UserManageActivity.this,NavActivity.class);
                startActivity(intent);
            }
        });

        log_off = (Button) findViewById(R.id.log_off);
        log_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo.getUserEntity().setUserName("");
                Intent intent = new Intent(UserManageActivity.this,NavActivity.class);
                startActivity(intent);
            }
        });

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
                startActivityForResult(intent, 2);
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
}
