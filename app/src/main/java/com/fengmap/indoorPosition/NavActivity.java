package com.fengmap.indoorPosition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.exception.FMObjectException;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.FMPickMapCoordResult;
import com.fengmap.android.map.event.OnFMMapClickListener;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.layer.FMImageLayer;
import com.fengmap.android.map.layer.FMLineLayer;
import com.fengmap.android.map.marker.FMImageMarker;
import com.fengmap.android.map.marker.FMLineMarker;
import com.fengmap.android.map.marker.FMSegment;
import com.fengmap.indoorPosition.httpRequest.RequestManager;
import com.fengmap.indoorPosition.map.FMMapBasic;
import com.fengmap.indoorPosition.utils.FileUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnFMMapInitListener, OnFMMapClickListener {

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

    private String positioningResult;

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        getMenuInflater().inflate(R.menu.home, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

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

    /**
     * 地图加载失败回调事件
     *
     * @param path      地图所在sdcard路径
     * @param errorCode 失败加载错误码，可以通过{@link FMErrorMsg#getErrorMsg(int)}获取加载地图失败详情
     */
    @Override
    public void onMapInitFailure(String path, int errorCode) {
        //TODO 可以提示用户地图加载失败原因，进行地图加载失败处理


        Log.e(String.valueOf(errorCode),FMErrorMsg.getErrorMsg(errorCode));

    }

    /**
     * 当{@link FMMap#openMapById(String, boolean)}设置openMapById(String, false)时地图不自动更新会
     * 回调此事件，可以调用{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}进行
     * 地图下载更新
     *
     * @param upgradeInfo 地图版本更新详情,地图版本号{@link FMMapUpgradeInfo#getVersion()},<br/>
     *                    地图id{@link FMMapUpgradeInfo#getMapId()}
     * @return 如果调用了{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}地图下载更新，
     * 返回值return true,因为{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}
     * 会自动下载更新地图，更新完成后会加载地图;否则return false。
     */
    @Override
    public boolean onUpgrade(FMMapUpgradeInfo upgradeInfo) {
        //TODO 获取到最新地图更新的信息，可以进行地图的下载操作
        return false;
    }


    @Override
    public void onMapClick(float x, float y) {
        final FMPickMapCoordResult mapCoordResult = mFMMap.pickMapCoord(x, y);
        mLineLayer.removeAll();
        mEndImageLayer.removeAll();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.end);

        String [] locInfo = positioningResult.split(",");
        stCoord = new FMMapCoord(Double.parseDouble(locInfo[0]), Double.parseDouble(locInfo[1]));
        endCoord = mapCoordResult.getMapCoord();
        FMImageMarker mImageMarker = new FMImageMarker(mapCoordResult.getMapCoord(), bitmap);
        //设置图片宽高
        mImageMarker.setMarkerWidth(80);
        mImageMarker.setMarkerHeight(80);
        //设置图片垂直偏离距离
        mImageMarker.setFMImageMarkerOffsetMode(FMImageMarker.FMImageMarkerOffsetMode.FMNODE_CUSTOM_HEIGHT);
        mImageMarker.setCustomOffsetHeight(0.4f);

        mEndImageLayer.addMarker(mImageMarker);


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

    public void getPosition(View view){

        clearImageLayer();
        final HashMap<String, String> apEntities = init(this);

        Thread httpRequest = new Thread(){
            @Override
            public void run(){
                RequestManager requestManager = RequestManager.getInstance(NavActivity.this);
                positioningResult = requestManager.requestSyn("loc",2,apEntities);
            }
        };

        httpRequest.start();
        try {
            httpRequest.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String [] locInfo = positioningResult.split(",");
        FMMapCoord centerCoord = new FMMapCoord(Double.parseDouble(locInfo[0]), Double.parseDouble(locInfo[1]));
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.start);
        FMImageMarker mImageMarker = new FMImageMarker(centerCoord, bitmap);
//设置图片宽高
        mImageMarker.setMarkerWidth(80);
        mImageMarker.setMarkerHeight(80);
//设置图片垂直偏离距离
        mImageMarker.setFMImageMarkerOffsetMode(FMImageMarker.FMImageMarkerOffsetMode.FMNODE_CUSTOM_HEIGHT);
        mImageMarker.setCustomOffsetHeight(0);

        mStImageLayer.addMarker(mImageMarker);            //添加图片标志物
    }

    public void chooseAlgorithm(View view){

    }

    public void clearTag(View view){
        clearImageLayer();
    }

    private HashMap<String, String> init(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        openWifi();
        wifiManager.startScan();
        List<ScanResult> list = wifiManager.getScanResults();
        if (list == null) {
            Toast.makeText(this, "wifi未打开！", Toast.LENGTH_LONG).show();
        }
        HashMap<String, String> apEntities = new HashMap<>();
        for (ScanResult scanResult : list){
            if (scanResult.SSID.contains("abc"))
                apEntities.put(scanResult.SSID,String.valueOf(scanResult.level));
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
