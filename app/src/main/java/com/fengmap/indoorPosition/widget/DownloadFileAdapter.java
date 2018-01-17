package com.fengmap.indoorPosition.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fengmap.indoorPosition.R;
import com.fengmap.indoorPosition.httpRequest.HttpUpload;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by ACER on 2018/1/15.
 */

public class DownloadFileAdapter extends SimpleAdapter {

    private String mapBasicPath = "fengmap/map/";
    private String themeBasicPath = "fengmap/theme/";

    List<? extends Map<String, ?>> data;
    private Context context;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = super.getView(position, convertView, parent);
        TextView download_file_name = (TextView) view.findViewById(R.id.download_file_name);
        ImageView download_file_view = (ImageView) view.findViewById(R.id.download_file_view);
        ImageView download_file_delete = (ImageView) view.findViewById(R.id.download_file_delete);
        ImageView download_file_send = (ImageView) view.findViewById(R.id.download_file_send);

        String filename = download_file_name.getText().toString();

        String basicPath = "";
        if (data.get(0).containsKey("fengmap/map/")) basicPath = mapBasicPath;
        else  basicPath = themeBasicPath;

        final String fullName = Environment.getExternalStorageDirectory()+ "/"+basicPath+filename;
//        File file = new File(Environment.getExternalStorageDirectory(),basicPath+filename);

        //点击查看按钮时打开文件
        download_file_view.setTag(position);
        download_file_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getTextFileIntent(fullName,false);
                context.startActivity(intent);
            }
        });

        //点击删除按钮时弹出提示框
        download_file_delete.setTag(position);
        download_file_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setTitle("提示").setMessage("确认删除文件？")

                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which){
                                dialog.dismiss();
                                deleteFile(fullName,position);

                                //结束当前调用的activity方法
//                                Activity activity = (Activity) context;
//                                activity.finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        //点击上传文件时发送至服务器端
        download_file_send.setTag(position);
        download_file_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) context;
                HttpUpload httpUpload = new HttpUpload();
                httpUpload.uploadFile(activity,"/",fullName);
            }
        });
        return view;
    }

    public DownloadFileAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        this.data = data;
    }

    //android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent( String param, boolean paramBoolean) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (paramBoolean) {
            Uri uri1 = Uri.parse(param );
            intent.setDataAndType(uri1, "text/plain");
        } else {
            Uri uri2 = Uri.fromFile(new File(param ));
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }

    //根据文件名删除文件方法
    public void deleteFile(String filePath, int position) {

        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            if(file.delete()){
                Toast.makeText(context, "删除成功！",
                        Toast.LENGTH_SHORT).show();
                data.remove(position);
                notifyDataSetChanged();
            }else{
                Toast.makeText(context, "删除失败，未找到文件！",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}