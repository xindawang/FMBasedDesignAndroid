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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fengmap.indoorPosition.R;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.loopj.android.http.AsyncHttpClient.log;

/**
 * Created by ACER on 2018/1/15.
 */

public class UploadInfoAdapter extends SimpleAdapter {

    private String basicPath = "fengmap/RSSIRecord/";
    List<? extends Map<String, ?>> data;
    private Context context;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = super.getView(position, convertView, parent);
        TextView upload_info_name = (TextView) view.findViewById(R.id.upload_info_name);
        ImageView upload_info_view = (ImageView) view.findViewById(R.id.upload_info_view);
        ImageView upload_info_delete = (ImageView) view.findViewById(R.id.upload_info_delete);
        ImageView upload_info_send = (ImageView) view.findViewById(R.id.upload_info_send);

        String filename = upload_info_name.getText().toString();
        final String fullName = Environment.getExternalStorageDirectory()+ "/"+basicPath+filename;
//        File file = new File(Environment.getExternalStorageDirectory(),basicPath+filename);

        //点击查看按钮时打开文件
        upload_info_view.setTag(position);
        upload_info_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getTextFileIntent(fullName,false);
                context.startActivity(intent);
            }
        });

        //点击删除按钮时弹出提示框
        upload_info_delete.setTag(position);
        upload_info_delete.setOnClickListener(new View.OnClickListener() {
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

        upload_info_send.setTag(position);
        upload_info_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.e("a","ss");
            }
        });
        return view;
    }

    public UploadInfoAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
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