package com.fengmap.FMDemoBaseMap.map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.fengmap.FMDemoBaseMap.R;
import com.fengmap.FMDemoBaseMap.utils.FileUtils;
import com.fengmap.FMDemoBaseMap.utils.ViewHelper;
import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapCoordZType;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.FMPickMapCoordResult;
import com.fengmap.android.map.FMViewMode;
import com.fengmap.android.map.event.OnFMMapClickListener;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.geometry.FMScreenCoord;
import com.fengmap.android.map.layer.FMImageLayer;
import com.fengmap.android.map.marker.FMImageMarker;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 地图坐标转换
 * <p>地图提供了屏幕坐标转换为地图坐标{@link FMMap#toFMScreenCoord(int, FMMapCoordZType, FMMapCoord)},
 * 地图坐标转换为屏幕坐标{@link FMMap#toFMMapCoord(int, FMScreenCoord)}</p>
 */
public class FMMapCoordTransform extends Activity implements OnFMMapInitListener, OnFMMapClickListener {

    private FMMapView mMapView;
    private FMMap mFMMap;
    private FMImageLayer mImageLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_transform);

        openMapByPath();
    }


    /**
     * 加载地图数据
     */
    private void openMapByPath() {
        mMapView = (FMMapView) findViewById(R.id.map_view);
        mFMMap = mMapView.getFMMap();
        mFMMap.setOnFMMapInitListener(this);
        mFMMap.setOnFMMapClickListener(this);
        //加载离线数据
        String path = FileUtils.getDefaultMapPath(this);
        mFMMap.openMapByPath(path);
    }

    /**
     * 地图加载成功回调事件
     *
     * @param path 地图所在sdcard路径
     */
    @Override
    public void onMapInitSuccess(String path) {
        //加载离线主题
        mFMMap.loadThemeByPath(FileUtils.getDefaultThemePath(this));

        //添加图片图层
        mImageLayer = mFMMap.getFMLayerProxy().createFMImageLayer(mFMMap.getFocusGroupId());
        mFMMap.addLayer(mImageLayer);
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

    /**
     * 地图销毁调用
     */
    @Override
    public void onBackPressed() {
        if (mFMMap != null) {
            mFMMap.onDestroy();
        }
        super.onBackPressed();
    }

    @Override
    public void onMapClick(float x, float y) {
        mImageLayer.removeAll();
        //添加图片标注
        FMPickMapCoordResult mapCoordResult = mFMMap.pickMapCoord(512, 1024);
        if (mapCoordResult != null) {
            FMMapCoord mapCoord = mapCoordResult.getMapCoord();
            FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), mapCoord);
            mImageLayer.addMarker(imageMarker);
        }

        int groupId = mFMMap.getFocusGroupId();
        //屏幕坐标转换为地图坐标
        FMScreenCoord screenCoord = new FMScreenCoord(512, 1024);
        FMMapCoord convertMapCoord = mFMMap.toFMMapCoord(groupId, screenCoord);

        //地图坐标转换为屏幕坐标
        FMScreenCoord convertScreenCoord = mFMMap.toFMScreenCoord(groupId,
                FMMapCoordZType.MAPCOORDZ_MODEL, convertMapCoord);

        //显示转换结果
        TextView mapResult = ViewHelper.getView(FMMapCoordTransform.this, R.id.map_result);
        mapResult.setText(getString(R.string.map_transform_tips, groupId, x, y, convertMapCoord.x, convertMapCoord.y,
                convertScreenCoord.x, convertScreenCoord.y));
    }
}
