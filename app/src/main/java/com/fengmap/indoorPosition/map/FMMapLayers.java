package com.fengmap.indoorPosition.map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.fengmap.indoorPosition.R;
import com.fengmap.indoorPosition.utils.FileUtils;
import com.fengmap.indoorPosition.utils.ViewHelper;
import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.layer.FMLayer;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 图层显示控制
 * <p>地图加载后会自动创建{@link com.fengmap.android.map.layer.FMLabelLayer}和
 * {@link com.fengmap.android.map.layer.FMFacilityLayer},可以在{@link FMMap#onMapInitListener#onMapInitSuccess(String)}
 * 地图加载完成后，动态控制图层的显示与隐藏</p>
 */
public class FMMapLayers extends Activity implements OnFMMapInitListener, CompoundButton.OnCheckedChangeListener {

    private FMMapView mMapView;
    private FMMap mFMMap;
    private int[] mLayerIds = {R.id.cb_layers_label, R.id.cb_layers_facility};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_layers);

        openMapByPath();
    }

    /**
     * 加载地图数据
     */
    private void openMapByPath() {
        mMapView = (FMMapView) findViewById(R.id.map_view);
        mFMMap = mMapView.getFMMap();
        mFMMap.setOnFMMapInitListener(this);
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

        int groupId = mFMMap.getFocusGroupId();
        FMLayer[] layers = new FMLayer[2];
        //获取标注图层
        layers[0] = mFMMap.getFMLayerProxy().getFMLabelLayer(groupId);
        //获取设施图层
        layers[1] = mFMMap.getFMLayerProxy().getFMFacilityLayer(groupId);

        for (int i = 0; i < mLayerIds.length; i++) {
            CheckBox checkBox = ViewHelper.getView(FMMapLayers.this, mLayerIds[i]);
            checkBox.setTag(layers[i]);
            checkBox.setOnCheckedChangeListener(this);
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
    public void onBackPressed() {
        if (mFMMap != null) {
            mFMMap.onDestroy();
        }
        super.onBackPressed();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        FMLayer layer = (FMLayer) buttonView.getTag();
        if (layer != null) {
            //设置控件是否显示true、隐藏false
            layer.setVisible(isChecked);
            mFMMap.updateMap();
        }
    }
}
