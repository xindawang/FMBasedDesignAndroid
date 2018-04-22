package com.fengmap.indoorPosition;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fengmap.indoorPosition.entity.APEntity;
import com.fengmap.indoorPosition.entity.AlgoEntity;
import com.fengmap.indoorPosition.entity.RPEntity;
import com.fengmap.indoorPosition.httpRequest.RequestManager;
import com.fengmap.indoorPosition.utils.JsonTool;


import org.java_websocket.WebSocket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import rx.Subscriber;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;


public class WifiListActivity extends AppCompatActivity {

    //广播接收器
    private IntentFilter intentFilter;
    private WifiChangeReceiver wifiChangeReceiver;


    private WifiManager wifiManager;
    private List<ScanResult> list;
    private List<ScanResult> selectedList;

    private Button store_RSSI_info;
    private Button end_RSSI_info;
    private Button send_RSSI_info;
    private FloatingActionButton refresh_RSSI_info;

    private String basicPath = "fengmap/RSSIRecord/";

    private String positioningResult;
    private boolean httpIsAvailable;
    private int algorithm_code;

    int rpCount = 1;

    int count = 0;//数据采集次数

    private StompClient myStompClient = null;//websocket

    Handler handler;
    Runnable runnable;

    private volatile boolean startCollect = false;//是否开始收集数据
    private volatile boolean startSent = false;//是否开始上传实时数据
    private final Timer timer = new Timer();
    private TimerTask WifiTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        init();
        createStompClient();//websoket

        //注册广播
        intentFilter = new IntentFilter();
        intentFilter.addAction(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        wifiChangeReceiver = new WifiChangeReceiver();
        registerReceiver(wifiChangeReceiver, intentFilter);
        WifiSan();


        //采集数据(生成临时文件)
        store_RSSI_info = (Button) findViewById(R.id.store_RSSI_info);
        store_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //一分钟采集20次、3秒一次
                store_RSSI_info.setEnabled(false);
                end_RSSI_info.setEnabled(false);
                send_RSSI_info.setEnabled(false);
                count = 0;//当前点采集计数
                startCollect = true;
            }
        });


        //结束采集数据（生成最终文件）
        end_RSSI_info = (Button) findViewById(R.id.end_RSSI_info);
        end_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameFile();
            }
        });


        //上传采集数据
        send_RSSI_info = (Button) findViewById(R.id.send_RSSI_info);
        send_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInfo();
            }
        });

        //扫描附近的wifi
        refresh_RSSI_info = (FloatingActionButton) findViewById(R.id.refresh_RSSI_info);
        refresh_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
            }
        });
    }

    private void WifiSan() {//间断100ms扫描
        WifiTask = new TimerTask() {
            @Override
            public void run() {
                wifiManager.startScan();
                list = wifiManager.getScanResults();
                showWifiMsg(list);
            }
        };

        timer.schedule(WifiTask, 0, 150);

    }

    public void showWifiMsg(final List<ScanResult> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListView listView = (ListView) findViewById(R.id.listView);
                if (list == null) {
                    Toast.makeText(WifiListActivity.this, "wifi未打开！", Toast.LENGTH_LONG).show();
                } else {
                    listView.setAdapter(new MyAdapter(WifiListActivity.this, list));
                }
            }
        });
    }


    private void sendInfo() {
        if (send_RSSI_info.getText().equals("开始上传")) {
            send_RSSI_info.setText("结束上传");
            startSent = true;
        } else {
            send_RSSI_info.setText("开始上传");
            startSent = false;
        }
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
                    basicPath + "tmp" + ".txt");

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
            Toast.makeText(getApplicationContext(), "当前位置第" + count + "次采集，保存成功！", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //点击结束按钮时，重命名临时文件
    private void renameFile() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
        String recordDate = sDateFormat.format(new java.util.Date());
        File file = new File(Environment.getExternalStorageDirectory(),
                basicPath + "tmp" + ".txt");
        if (file.renameTo(new File(Environment.getExternalStorageDirectory(), basicPath + recordDate + ".txt"))) {
            Toast.makeText(getApplicationContext(), "记录完毕！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "记录失败，缺少采集文件！", Toast.LENGTH_SHORT).show();
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

    //获取wifi列表
    private HashMap<String, String> getWifiList() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        openWifi();
        wifiManager.startScan();
        List<ScanResult> list = wifiManager.getScanResults();
        if (list == null) {
            Toast.makeText(this, "wifi未打开！", Toast.LENGTH_LONG).show();
            return null;
        }
        HashMap<String, String> apEntities = new HashMap<>();
        HashMap<String, String> changeName = new HashMap<>();
        changeName.put("abc3", "ap1");
        changeName.put("abc4", "ap2");
        changeName.put("abc6", "ap3");
        changeName.put("abc7", "ap4");
        changeName.put("abc8", "ap5");
//        changeName.put("Four-Faith-2", "ap1");
//        changeName.put("Four-Faith-3", "ap2");
//        changeName.put("TP-LINK_E7D2", "ap3");
//        changeName.put("TP-LINK_3625", "ap4");
//        changeName.put("TP-LINK_3051", "ap5");
//        changeName.put("TP-LINK_35EB", "ap6");
//        changeName.put("TP-LINK_5958", "ap7");
        for (ScanResult scanResult : list) {
            if (changeName.containsKey(scanResult.SSID))
                apEntities.put(changeName.get(scanResult.SSID), String.valueOf(scanResult.level));
        }
        return apEntities;
    }

    public class MyAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<ScanResult> list;

        public MyAdapter(Context context, List<ScanResult> list) {
            // TODO Auto-generated constructor stub
            this.inflater = LayoutInflater.from(context);
            selectedList = new ArrayList<>();
            HashMap<String, String> changeName = new HashMap<>();
            changeName.put("abc3", "ap1");
            changeName.put("abc4", "ap2");
            changeName.put("abc6", "ap3");
            changeName.put("abc7", "ap4");
            changeName.put("abc8", "ap5");
//            changeName.put("Four-Faith-2", "ap1");
//            changeName.put("Four-Faith-3", "ap2");
//            changeName.put("TP-LINK_E7D2", "ap3");
//            changeName.put("TP-LINK_3625", "ap4");
//            changeName.put("TP-LINK_3051", "ap5");
//            changeName.put("TP-LINK_35EB", "ap6");
//            changeName.put("TP-LINK_5958", "ap7");
            for (ScanResult scanResult : list) {
                if (changeName.containsKey(scanResult.SSID))
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

    //顶部菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wifi_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(WifiListActivity.this, AlgorithmActivity.class);
            // 把bundle放入intent里
            intent.putExtra("algorithm_code", algorithm_code);
            startActivityForResult(intent, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //websocket 建立链接 发送消息 接受消息
    private void createStompClient() {
        myStompClient = Stomp.over(WebSocket.class, "ws://119.29.12.63/endpointWifi/websocket");
        myStompClient.connect();
        myStompClient.lifecycle().subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d("wifiList", "Stomp connection opened123");
                        break;
                    case ERROR:
                        Log.d("wifiList", "Stomp connection error369");
                        break;
                    case CLOSED:
                        Log.d("wifiList", "Stomp connection closed147");
                        break;
                }
            }
        });
    }

    private void sendMessage(String message) {
        myStompClient.send("/app/app_wifiMessage", message)
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Main", "requestCode:" + requestCode + "resultCode:" + resultCode);
        if (resultCode == 1) {
            if (requestCode == 0) {
                Integer code = data.getIntExtra("algorithm_code", 0);
                algorithm_code = code;
            }
        }
    }


    //定义广播接收器
    class WifiChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //判断是否开始搜集数据
            if (startCollect) {
                count++;
                buttonStoreClick();

                if (count >= 100) {
                    startCollect = false;
                    store_RSSI_info.setEnabled(true);
                    end_RSSI_info.setEnabled(true);
                    send_RSSI_info.setEnabled(true);
                }

            }
            //判断是否上传
            if (startSent) {
                HashMap<String, String> map = getWifiList();
                map.put("algorithm", String.valueOf(algorithm_code));
                sendMessage(JsonTool.objectToJson(map));
            }

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //取消动态网络变化广播接收器的注册
        unregisterReceiver(wifiChangeReceiver);
    }


}