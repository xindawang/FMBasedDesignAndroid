package com.fengmap.indoorPosition;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fengmap.indoorPosition.entity.APEntity;
import com.fengmap.indoorPosition.entity.RPEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WifiListActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private List<ScanResult> list;
    private List<ScanResult> selectedList;

    private Button store_RSSI_info;
    private Button end_RSSI_info;
    private FloatingActionButton refresh_RSSI_info;

    private String basicPath = "fengmap/RSSIRecord/";


    int rpCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_list);
        init();

        store_RSSI_info = (Button) findViewById(R.id.store_RSSI_info);
        store_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonStoreClick();
                init();
            }
        });

        end_RSSI_info = (Button) findViewById(R.id.end_RSSI_info);
        end_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameFile();
            }
        });

        refresh_RSSI_info = (FloatingActionButton) findViewById(R.id.refresh_RSSI_info);
        refresh_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
            }
        });
    }

    public void buttonStoreClick() {
        RPEntity rpEntity = new RPEntity();
        Set<APEntity> apEntities = new HashSet<>();
        for (ScanResult scanResult : selectedList) {
            APEntity apEntity = new APEntity();
            apEntity.setApName(scanResult.SSID);
            apEntity.setApStrength(scanResult.level);
            apEntity.setTimestamp(scanResult.timestamp);
            apEntities.add(apEntity);
        }
        rpEntity.setApEntities(apEntities);
        printResult(rpEntity);
    }

    public void printResult(RPEntity rpEntity) {
        try {
            File dir = new File(Environment.getExternalStorageDirectory(),
                    "fengmap/RSSIRecord");
            if (!dir.exists()) {
                //通过file的mkdirs()方法创建<span style="color:#FF0000;">目录中包含却不存在</span>的文件夹
                dir.mkdir();
            }

            File file = new File(Environment.getExternalStorageDirectory(),
                    basicPath +"tmp"+".txt");

            //第二个参数意义是说是否以append方式添加内容
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

            Date date = null;
            bw.write("\r\n");
            for (APEntity apEntity : rpEntity.getApEntities()) {
                bw.write(apEntity.getApName() + " " + apEntity.getApStrength() + ";");
                date = apEntity.getTimestamp();
            }
            bw.write("\r\n" + rpCount++ + "\t" + date + "\r\n");
            bw.flush();
            Toast.makeText(getApplicationContext(), "保存成功！",
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //点击结束按钮时，重命名临时文件
    private void renameFile(){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
        String recordDate = sDateFormat.format(new java.util.Date());
        File file = new File(Environment.getExternalStorageDirectory(),
                basicPath +"tmp"+".txt");
        if(file.renameTo(new File(Environment.getExternalStorageDirectory(),basicPath + recordDate + ".txt"))){
            Toast.makeText(getApplicationContext(), "记录完毕！",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "记录失败，缺少采集文件！",Toast.LENGTH_SHORT).show();
        }
    }

        @Override
    public void onBackPressed() {
        finish();
    }

    private void init() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        openWifi();
        wifiManager.startScan();
        list = wifiManager.getScanResults();
        ListView listView = (ListView) findViewById(R.id.listView);
        if (list == null) {
            Toast.makeText(this, "wifi未打开！", Toast.LENGTH_LONG).show();
        } else {
            listView.setAdapter(new MyAdapter(this, list));
        }

    }

    /**
     * 打开WIFI
     */
    private void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

    }

    public class MyAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<ScanResult> list;

        public MyAdapter(Context context, List<ScanResult> list) {
            // TODO Auto-generated constructor stub
            this.inflater = LayoutInflater.from(context);
            selectedList = new ArrayList<>();
            for (ScanResult scanResult : list) {
                if (scanResult.SSID.contains("abc"))
                    selectedList.add(scanResult);
            }
            this.list = selectedList;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View view = null;
            view = inflater.inflate(R.layout.wifi_list_item, null);
            ScanResult scanResult = list.get(position);

            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(scanResult.SSID);
            TextView signalStrenth = (TextView) view.findViewById(R.id.signal_strenth);
            signalStrenth.setText(String.valueOf(Math.abs(scanResult.level)));

            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            setWifiImage(scanResult.level, imageView);
            return view;
        }
    }

    //根据wifi强度选择相关图片
    private void setWifiImage(int strength, ImageView imageView) {
        if (strength <= 0 && strength >= -40) {
            imageView.setImageResource(R.drawable.wifi3);
        } else if (strength < -40 && strength >= -60) {
            imageView.setImageResource(R.drawable.wifi2);
        } else if (strength < -60 && strength >= -80) {
            imageView.setImageResource(R.drawable.wifi1);
        } else {
            imageView.setImageResource(R.drawable.wifi0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wifi_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}