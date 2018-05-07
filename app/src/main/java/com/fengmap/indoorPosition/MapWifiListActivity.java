package com.fengmap.indoorPosition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.FMPickMapCoordResult;
import com.fengmap.android.map.FMViewMode;
import com.fengmap.android.map.event.OnFMMapClickListener;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.layer.FMImageLayer;
import com.fengmap.android.map.marker.FMImageMarker;
import com.fengmap.indoorPosition.entity.APEntity;
import com.fengmap.indoorPosition.entity.ApNameEntity;
import com.fengmap.indoorPosition.entity.RPEntity;
import com.fengmap.indoorPosition.utils.FileUtils;
import com.fengmap.indoorPosition.utils.JsonTool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ACER on 2018/5/4.
 */

public class MapWifiListActivity extends AppCompatActivity implements OnFMMapInitListener, OnFMMapClickListener {

    private FMMapView mwMapView;
    private FMMap mwFMMap;
    private FMImageLayer mwStImageLayer;
    private FMMapCoord wstCoord;

    private WifiManager wifiManager;
    private List<ScanResult> list;
    private List<ScanResult> selectedList;

    private Button store_RSSI_info;
    private Button end_RSSI_info;

    private int count = 0;//数据采集次数
    private String basicPath = "fengmap/RSSIRecord/";
    private volatile boolean startCollect = false;//是否开始收集数据
    private final Timer timer = new Timer();
    private TimerTask WifiTask;
    private MapWifiListActivity.WifiChangeReceiver wifiChangeReceiver;
    private IntentFilter intentFilter;
    int rpCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_wifi);
        init();

        openMapByPath();
        mwFMMap.setOnFMMapClickListener(this);

        //注册广播
        intentFilter = new IntentFilter();
        intentFilter.addAction(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        wifiChangeReceiver = new WifiChangeReceiver();
        registerReceiver(wifiChangeReceiver, intentFilter);
        WifiSan();

        //采集数据(生成临时文件)
        store_RSSI_info = (Button) findViewById(R.id.store_RSSI_info_map);
        store_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //一分钟采集20次、3秒一次
                if (wstCoord == null) return;
                store_RSSI_info.setEnabled(false);
                end_RSSI_info.setEnabled(false);
                count = 0;//当前点采集计数
                startCollect = true;
            }
        });


        //结束采集数据（生成最终文件）
        end_RSSI_info = (Button) findViewById(R.id.end_RSSI_info_map);
        end_RSSI_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameFile();
            }
        });
    }

    /**
     * 加载地图数据
     */
    private void openMapByPath() {
        mwMapView = (FMMapView) findViewById(R.id.map_wifi);
        mwFMMap = mwMapView.getFMMap();
        mwFMMap.setOnFMMapInitListener(this);
        //加载离线数据
        String path = FileUtils.getDefaultMapPath(this);
        mwFMMap.openMapByPath(path);

        // openMapById(id,true) 加载在线地图数据，并自动更新地图数据
//        mFMMap.openMapById(FileUtils.DEFAULT_MAP_ID,true);
    }

    @Override
    public void onMapClick(float x, float y) {
        FMPickMapCoordResult mapCoordResult = mwFMMap.pickMapCoord(x, y);
        mwStImageLayer.removeAll();

        wstCoord = mapCoordResult.getMapCoord();
        if (wstCoord ==null) return;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.start);
        mwStImageLayer.addMarker(setStartAndEndPic(wstCoord, bitmap));
    }

    /**
     * 地图加载成功回调事件
     *
     * @param path 地图所在sdcard路径
     */
    @Override
    public void onMapInitSuccess(String path) {
        //加载离线主题文件
        mwFMMap.loadThemeByPath(FileUtils.getDefaultThemePath(this));

        //添加起点图片图层
        mwStImageLayer = mwFMMap.getFMLayerProxy().createFMImageLayer(1);
        mwFMMap.addLayer(mwStImageLayer);

        mwFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_2D);
        float angle = 270;
        mwFMMap.setRotateAngle(angle);
        int defaultLevel = 21;
        mwFMMap.setZoomLevel(defaultLevel,false);

    }

    //地图加载失败回调事件
    @Override
    public void onMapInitFailure(String path, int errorCode) {
        //TODO 可以提示用户地图加载失败原因，进行地图加载失败处理
        Log.e(String.valueOf(errorCode), FMErrorMsg.getErrorMsg(errorCode));

    }

    @Override
    public boolean onUpgrade(FMMapUpgradeInfo fmMapUpgradeInfo) {
        return false;
    }

    @Override
    public void onBackPressed() {

        if (mwFMMap != null) {
            mwFMMap.onDestroy();
        }

        Intent intent = new Intent(MapWifiListActivity.this,NavActivity.class);
        startActivity(intent);
        finish();
    }

    //点击结束按钮时，重命名临时文件
    private void renameFile() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.switch_method) {
            if (mwFMMap != null)  mwFMMap.onDestroy();
            Intent intent = new Intent(MapWifiListActivity.this, WifiListActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_wifi_menu, menu);
        return true;
    }

    //设置起始与终止位置图片样式
    private FMImageMarker setStartAndEndPic(FMMapCoord centerCoord, Bitmap bitmap) {
        FMImageMarker mImageMarker = new FMImageMarker(centerCoord, bitmap);
        //设置图片宽高
        mImageMarker.setMarkerWidth(80);
        mImageMarker.setMarkerHeight(80);
        //设置图片垂直偏离距离
        mImageMarker.setFMImageMarkerOffsetMode(FMImageMarker.FMImageMarkerOffsetMode.FMNODE_CUSTOM_HEIGHT);
        mImageMarker.setCustomOffsetHeight(0.2f);
        return mImageMarker;
    }

    private void WifiSan() {//间断100ms扫描
        WifiTask = new TimerTask() {
            @Override
            public void run() {
                wifiManager.startScan();
                list = wifiManager.getScanResults();
                selectedList = new ArrayList<>();
                for (ScanResult scanResult : list) {
                    if (ApNameEntity.getMap().containsKey(scanResult.SSID))
                        selectedList.add(scanResult);
                }
            }
        };

        timer.schedule(WifiTask, 0, 150);

    }

    private void init() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        openWifi();
        wifiManager.startScan();
        list = wifiManager.getScanResults();
        selectedList = new ArrayList<>();
        for (ScanResult scanResult : list) {
            if (ApNameEntity.getMap().containsKey(scanResult.SSID))
                selectedList.add(scanResult);
        }
        this.list = selectedList;
    }

    /**
     * 打开WIFI
     */
    private void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

    }

    public void buttonStoreClick() {
        list = wifiManager.getScanResults();
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
            ;
            bw.write("\r\n");
            for (APEntity apEntity : rpEntity.getApEntities()) {
                bw.write(apEntity.getApName() + " " + apEntity.getApStrength() + ";");
                date = apEntity.getTimestamp();
            }
            bw.write("\r\n" + wstCoord.x + "\t"+ wstCoord.y+ "\t" + rpCount++ + "\r\n");
            bw.flush();
            Toast.makeText(getApplicationContext(), "当前位置第" + count + "次采集，保存成功！", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

                if (count >= 20 ) {
                    startCollect = false;
                    store_RSSI_info.setEnabled(true);
                    end_RSSI_info.setEnabled(true);
                }

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
