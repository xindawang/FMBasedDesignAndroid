package com.fengmap.indoorPosition.map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.fengmap.indoorPosition.R;
import com.fengmap.indoorPosition.utils.FileUtils;
import com.fengmap.indoorPosition.utils.ViewHelper;
import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.geometry.FMMapCoord;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 地图初始化设置
 * <p>在{@link FMMap#onMapInitListener#onMapInitSuccess(String)}中设置地图初始显示状态</p>
 */
public class FMMapInitialize extends Activity implements OnFMMapInitListener {

    private FMMapView mMapView;
    private FMMap mFMMap;
    private FMMapCoord CENTER_COORD = new FMMapCoord(1.296164E7, 4861800.0);
    private int mLevel = 20;
    private float mRotate = 60;
    private float mTilt = 45;
    private int mGroupId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_init);

        initView();
        openMapByPath();
    }

    private void initView() {
        String info = getResources().getString(R.string.map_init_tips, mGroupId, CENTER_COORD.x,
                CENTER_COORD.y, mRotate, mTilt, mLevel);
        TextView textView = ViewHelper.getView(FMMapInitialize.this, R.id.map_result);
        textView.setText(info);
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


    @Override
    public void onMapInitSuccess(String path) {
        //加载离线主题
        mFMMap.loadThemeByPath(FileUtils.getDefaultThemePath(this));
        //2D显示模式
//        mFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_2D);
        //缩放级别
        mFMMap.setZoomLevel(mLevel, false);
        //旋转角度
        mFMMap.setRotateAngle(mRotate);
        //倾角
        mFMMap.setTiltAngle(mTilt);
        //地图中心点
        mFMMap.setMapCenter(CENTER_COORD);
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
}
