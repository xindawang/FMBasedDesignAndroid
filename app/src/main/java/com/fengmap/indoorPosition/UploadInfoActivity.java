package com.fengmap.indoorPosition;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.fengmap.indoorPosition.widget.UploadInfoAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ACER on 2018/1/15.
 */

public class UploadInfoActivity extends AppCompatActivity {

    private ListView upload_listView;
    private String basicPath = "fengmap/RSSIRecord/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_info_list);

        upload_listView = (ListView) findViewById(R.id.upload_listView);

        SimpleAdapter adapter = new UploadInfoAdapter(this,getFileName(),R.layout.upload_info_list_item,
                new String[]{"upload_info_name"},
                new int[]{R.id.upload_info_name});
        upload_listView.setAdapter(adapter);

//        upload_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                ImageView upload_info_view = (ImageView) view.findViewById(R.id.upload_info_view);
//
//            }
//        });
    }

    private List<Map<String,Object>> getFileName() {
        File file = new File(Environment.getExternalStorageDirectory(),basicPath);
        if (!file.exists()){
            Toast.makeText(getApplicationContext(), "未找到文件！",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        File [] files = file.listFiles();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;

        for (File f : files){
            if (!f.getName().equals("tmp")){
                map = new HashMap<>();
                map.put("upload_info_name",f.getName());
                list.add(map);
            }
        }
        return list;
    }
}
