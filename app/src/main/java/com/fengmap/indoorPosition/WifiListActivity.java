package com.fengmap.indoorPosition;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
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

import com.fengmap.indoorPosition.R;
import com.fengmap.indoorPosition.entity.APEntity;
import com.fengmap.indoorPosition.entity.RPEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WifiListActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    List<ScanResult> list;
    List<ScanResult> selectedList;

    Button store_RSSI_info;
    Button refresh_RSSI_info;

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

        refresh_RSSI_info = (Button) findViewById(R.id.refresh_RSSI_info);
        refresh_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
            }
        });
    }

    public void buttonStoreClick(){
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

    public void printResult(RPEntity rpEntity){
        try {
            File file = new File(Environment.getExternalStorageDirectory(),
                    "data.txt");
            //第二个参数意义是说是否以append方式添加内容
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

            Date date = null;
            bw.write("\r\n");
            for (APEntity apEntity : rpEntity.getApEntities()) {
                bw.write(apEntity.getApName() + " " +apEntity.getApStrength()+";");
                date = apEntity.getTimestamp();
            }
            bw.write("\r\n" + rpCount++ +"\t"+ date+ "\r\n");
            bw.flush();
            Toast.makeText(getApplicationContext(), "保存成功！",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
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
            return view;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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