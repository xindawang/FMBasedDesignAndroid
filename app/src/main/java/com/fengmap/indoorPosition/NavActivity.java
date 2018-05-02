package com.fengmap.indoorPosition;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.analysis.navi.FMNaviAnalyser;
import com.fengmap.android.analysis.navi.FMNaviResult;
import com.fengmap.android.exception.FMObjectException;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.FMPickMapCoordResult;
import com.fengmap.android.map.FMViewMode;
import com.fengmap.android.map.event.OnFMMapClickListener;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.layer.FMImageLayer;
import com.fengmap.android.map.layer.FMLineLayer;
import com.fengmap.android.map.marker.FMImageMarker;
import com.fengmap.android.map.marker.FMLineMarker;
import com.fengmap.android.map.marker.FMSegment;
import com.fengmap.indoorPosition.entity.AlgoEntity;
import com.fengmap.indoorPosition.entity.ApNameEntity;
import com.fengmap.indoorPosition.httpRequest.HttpUrlConnectionMethod;
import com.fengmap.indoorPosition.httpRequest.RequestManager;
import com.fengmap.indoorPosition.utils.FileUtils;
import com.fengmap.indoorPosition.utils.JsonTool;
import com.fengmap.indoorPosition.utils.UserInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFMMapInitListener, OnFMMapClickListener {

    private FMMapView mMapView;
    private FMMap mFMMap;
    private FMImageLayer mStImageLayer;
    private FMImageLayer mEndImageLayer;
    //    private FMLocationLayer mLocationLayer;
    private FMNaviAnalyser mNaviAnalyser;
    private FMLineLayer mLineLayer;
    private FMMapCoord stCoord;
    private FMMapCoord endCoord;

    private WifiManager wifiManager;
    private boolean show2dMap;

    private String positioningResult;
    private boolean httpIsAvailable;
    private int algorithm_code;

    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        openMapByPath();
        mFMMap.setOnFMMapClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPosition(view);
            }
        });

        final FloatingActionButton select_2d_3d = (FloatingActionButton) findViewById(R.id.select_2d_3d);
        select_2d_3d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_2d_3d(view,select_2d_3d);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {

        if (mFMMap != null) {
            mFMMap.onDestroy();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.select_algorithm) {
            Intent intent = new Intent(NavActivity.this,AlgorithmActivity.class);
            // 把bundle放入intent里
            intent.putExtra("algorithm_code", algorithm_code);
            startActivityForResult(intent,0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent = new Intent(NavActivity.this,PersonalActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(NavActivity.this,VersionInfoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(NavActivity.this,WifiListActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(NavActivity.this,UserManageActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(NavActivity.this,DowloadFileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            Intent intent = new Intent(NavActivity.this,UploadInfoActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 加载地图数据
     */
    private void openMapByPath() {
        mMapView = (FMMapView) findViewById(R.id.mapp_view);
        mFMMap = mMapView.getFMMap();
        mFMMap.setOnFMMapInitListener(this);
        //加载离线数据
        String path = FileUtils.getDefaultMapPath(this);
        mFMMap.openMapByPath(path);

        // openMapById(id,true) 加载在线地图数据，并自动更新地图数据
//        mFMMap.openMapById(FileUtils.DEFAULT_MAP_ID,true);
    }


    /**
     * 地图加载成功回调事件
     *
     * @param path 地图所在sdcard路径
     */
    @Override
    public void onMapInitSuccess(String path) {
        //加载离线主题文件
        mFMMap.loadThemeByPath(FileUtils.getDefaultThemePath(this));

        //加载在线主题文件
        //mFMMap.loadThemeById(FMMap.DEFAULT_THEME_CANDY);

        //添加起点图片图层
        mStImageLayer = mFMMap.getFMLayerProxy().createFMImageLayer(1);
        mFMMap.addLayer(mStImageLayer);

        //添加终点图片图层
        mEndImageLayer = mFMMap.getFMLayerProxy().createFMImageLayer(1);
        mFMMap.addLayer(mEndImageLayer);


        //创建线图层
        mLineLayer = mFMMap.getFMLayerProxy().getFMLineLayer();
        mFMMap.addLayer(mLineLayer);

        try {
            mNaviAnalyser = FMNaviAnalyser.getFMNaviAnalyserByPath(path);
        } catch (FileNotFoundException pE) {
            pE.printStackTrace();
        } catch (FMObjectException pE) {
            pE.printStackTrace();
        }
    }

    //地图加载失败回调事件
    @Override
    public void onMapInitFailure(String path, int errorCode) {
        //TODO 可以提示用户地图加载失败原因，进行地图加载失败处理
        Log.e(String.valueOf(errorCode), FMErrorMsg.getErrorMsg(errorCode));

    }

    @Override
    public boolean onUpgrade(FMMapUpgradeInfo upgradeInfo) {
        //TODO 获取到最新地图更新的信息，可以进行地图的下载操作
        return false;
    }


    @Override
    public void onMapClick(float x, float y) {

        if (positioningResult == null){
//            Looper.prepare();
            Toast.makeText(NavActivity.this, "请先进行定位！", Toast.LENGTH_SHORT).show();
//            Looper.loop();
            return;
        }

        final FMPickMapCoordResult mapCoordResult = mFMMap.pickMapCoord(x, y);
        mLineLayer.removeAll();
        mEndImageLayer.removeAll();

        String[] locInfo = positioningResult.split(",");
        stCoord = new FMMapCoord(Double.parseDouble(locInfo[0]), Double.parseDouble(locInfo[1]));
        endCoord = mapCoordResult.getMapCoord();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.end);
        mEndImageLayer.addMarker(setStartAndEndPic(mapCoordResult.getMapCoord(), bitmap));


        //创建线图层
        mLineLayer = mFMMap.getFMLayerProxy().getFMLineLayer();
        mFMMap.addLayer(mLineLayer);                //添加线图层

        //根据起始点坐标和楼层id等信息进行路径规划
        int type = mNaviAnalyser.analyzeNavi(1, stCoord, 1, endCoord,
                FMNaviAnalyser.FMNaviModule.MODULE_SHORTEST);
        if (type == FMNaviAnalyser.FMRouteCalcuResult.ROUTE_SUCCESS) {
            ArrayList<FMNaviResult> results = mNaviAnalyser.getNaviResults();
            // 构造路径规划线所需数据
            ArrayList<FMSegment> segments = new ArrayList<>();
            for (FMNaviResult r : results) {
                int groupId = r.getGroupId();
                FMSegment s = new FMSegment(groupId, r.getPointList());
                segments.add(s);
            }
            //添加LineMarker
            FMLineMarker lineMarker = new FMLineMarker(segments);
            mLineLayer.addMarker(lineMarker);
        }
    }

    /**
     * 清除起点图层
     */
    protected void clearImageLayer() {
        if (mStImageLayer != null) {
            mStImageLayer.removeAll();
//            mFMMap.removeLayer(mLocationLayer); // 移除图层
//            mLocationLayer = null;
        }
        if (mLineLayer != null) {
            mLineLayer.removeAll();
        }
        if (mEndImageLayer != null) {
            mEndImageLayer.removeAll();
        }
    }

    public void getPosition(View view) {

        clearImageLayer();
        final HashMap<String, String> apEntities = getWifiList();
        if (apEntities == null) return;

        AlgoEntity algoEntity = new AlgoEntity(algorithm_code);
        apEntities.put("algorithm",algoEntity.getName());

        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        final String jsonInfo = gson.toJson(apEntities);
        final String url = "http://211.67.16.39:9090/loc";

        final Thread httpRequest = new Thread() {
            @Override
            public void run() {
//                RequestManager requestManager = RequestManager.getInstance(NavActivity.this);
//                positioningResult = requestManager.requestSyn("loc", 2, apEntities);

                positioningResult = HttpUrlConnectionMethod.doJsonPost(url, jsonInfo);
                if (positioningResult == null) {
                    Looper.prepare();
                    Toast.makeText(NavActivity.this, "请检查服务器连接！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    httpIsAvailable = false;
                } else httpIsAvailable = true;
            }
        };

        httpRequest.start();
        try {
            httpRequest.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!httpIsAvailable) return;

        if (positioningResult.contains("null")) return;
        String[] locInfo = positioningResult.split(",");
        FMMapCoord centerCoord = new FMMapCoord(Double.parseDouble(locInfo[0]), Double.parseDouble(locInfo[1]));
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.start);
        mStImageLayer.addMarker(setStartAndEndPic(centerCoord, bitmap));            //添加图片标志物
    }

    private void select_2d_3d(View view,FloatingActionButton select_2d_3d) {
        if (show2dMap == false) {
            mFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_2D); //设置地图2D显示模式
            select_2d_3d.setImageResource(R.drawable.two_dim);
            show2dMap = true;
        } else {
            mFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_3D); //设置地图3D显示模式
            select_2d_3d.setImageResource(R.drawable.three_dim);
            show2dMap = false;
        }
    }

    //设置起始与终止位置图片样式
    private FMImageMarker setStartAndEndPic(FMMapCoord centerCoord, Bitmap bitmap) {
        FMImageMarker mImageMarker = new FMImageMarker(centerCoord, bitmap);
        //设置图片宽高
        mImageMarker.setMarkerWidth(80);
        mImageMarker.setMarkerHeight(80);
        //设置图片垂直偏离距离
        mImageMarker.setFMImageMarkerOffsetMode(FMImageMarker.FMImageMarkerOffsetMode.FMNODE_CUSTOM_HEIGHT);
        mImageMarker.setCustomOffsetHeight(0);
        return mImageMarker;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Main", "requestCode:"+requestCode+"resultCode:"+resultCode);
        if(resultCode==1){
            if(requestCode==0){
                Integer code=data.getIntExtra("algorithm_code",0);
                algorithm_code = code;
            }
        }
    }

    public void clearTag(View view) {
        clearImageLayer();
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
        for (ScanResult scanResult : list) {
            if (ApNameEntity.getMap().containsKey(scanResult.SSID))
                apEntities.put(ApNameEntity.getMap().get(scanResult.SSID), String.valueOf(scanResult.level));
        }
        return apEntities;
    }

    /**
     * 打开WIFI
     */
    private void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

    }

}
