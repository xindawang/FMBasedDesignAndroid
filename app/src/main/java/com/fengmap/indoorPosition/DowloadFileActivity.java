package com.fengmap.indoorPosition;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.fengmap.indoorPosition.widget.DownloadFileAdapter;
import com.fengmap.indoorPosition.widget.UploadInfoAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ACER on 2018/1/15.
 */

public class DowloadFileActivity extends AppCompatActivity {

    private ListView map_listView;
    private ListView theme_listView;
    private String mapBasicPath = "fengmap/map/";
    private String themeBasicPath = "fengmap/theme/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file_list);

        map_listView = (ListView) findViewById(R.id.map_listView);
        theme_listView = (ListView) findViewById(R.id.theme_listView);

        SimpleAdapter mapAdapter = new DownloadFileAdapter(this,getDirName(mapBasicPath),R.layout.download_file_list_item,
                new String[]{mapBasicPath},
                new int[]{R.id.download_file_name});
        map_listView.setAdapter(mapAdapter);

        SimpleAdapter themeAdapter = new DownloadFileAdapter(this,getDirName(themeBasicPath),R.layout.download_file_list_item,
                new String[]{themeBasicPath},
                new int[]{R.id.download_file_name});
        theme_listView.setAdapter(themeAdapter);
    }

    private List<Map<String,Object>> getDirName(String name) {
        File file = new File(Environment.getExternalStorageDirectory(),name);
        if (!file.exists()){
            Toast.makeText(getApplicationContext(), "未找到文件！",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        File [] files = file.listFiles();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;

        for (File f : files){
                map = new HashMap<>();
                map.put(name,f.getName());
                list.add(map);
        }
        return list;
    }
}
