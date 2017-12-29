package com.fengmap.indoorPosition.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.fengmap.indoorPosition.R;
import com.fengmap.indoorPosition.utils.FileUtils;
import com.fengmap.indoorPosition.utils.ViewHelper;
import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.FMViewMode;
import com.fengmap.android.map.event.OnFMMapInitListener;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 2D 3D显示切换
 * <p>地图为2D模式时候,将会禁用手势倾斜功能</p>
 */
public class FMMap2D3D extends Activity implements OnFMMapInitListener, View.OnClickListener {
    private FMMapView mMapView;
    private FMMap mFMMap;
    private Button[] mButtons = new Button[2];
    private int mPosition = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2d3d);

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

        setViewMode();

        LinearLayout view = ViewHelper.getView(FMMap2D3D.this, R.id.layout_mode);
        for (int i = 0; i < view.getChildCount(); i++) {
            mButtons[i] = (Button) view.getChildAt(i);
            mButtons[i].setTag(i);
            mButtons[i].setEnabled(true);
            mButtons[i].setOnClickListener(this);
        }
        mButtons[mPosition].setEnabled(false);
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
    public void onClick(View v) {
        Button button = (Button) v;
        int position = (int) button.getTag();
        setPosition(position);
        setViewMode();
    }

    /**
     * 切换地图显示模式
     */
    private void setViewMode() {
        if (mPosition == 0) {
            mFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_2D); //设置地图2D显示模式
        } else {
            mFMMap.setFMViewMode(FMViewMode.FMVIEW_MODE_3D); //设置地图3D显示模式
        }
    }

    /**
     * 设置2D、3D选择效果
     *
     * @param position 按钮索引
     */
    private void setPosition(int position) {
        if (mPosition == position) {
            return;
        }
        mButtons[position].setEnabled(false);
        mButtons[mPosition].setEnabled(true);
        mPosition = position;
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
