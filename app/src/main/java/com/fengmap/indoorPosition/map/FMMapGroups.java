package com.fengmap.indoorPosition.map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fengmap.indoorPosition.R;
import com.fengmap.indoorPosition.utils.FileUtils;
import com.fengmap.indoorPosition.utils.ViewHelper;
import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.map.FMGroupInfo;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapInfo;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.animator.FMLinearInterpolator;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.event.OnFMSwitchGroupListener;

import java.util.ArrayList;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 楼层控制
 * <p>切换楼层时，可以使用动画效果,{@link FMLinearInterpolator}线性差值动画，
 * {@link com.fengmap.android.map.animator.FMBounceInterpolator}弹簧反弹的插值,
 * {@link com.fengmap.android.map.animator.FMAccelerateDecelerateInterpolator}先加速后减速的插值,
 * {@link com.fengmap.android.map.animator.FMAccelerateInterpolator}加速插值,
 * {@link com.fengmap.android.map.animator.FMDecelerateInterpolator}减速插值</p>
 */
public class FMMapGroups extends Activity implements OnFMMapInitListener, CompoundButton.OnCheckedChangeListener {

    private FMMapView mMapView;
    private FMMap mFMMap;
    private CheckBox mGroupControl;
    private RadioButton[] mRadioButtons;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_groups);

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

        FMMapInfo mapInfo = mFMMap.getFMMapInfo();
        ArrayList<FMGroupInfo> groups = mapInfo.getGroups();
        displayGroupView(groups);
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
     * 展示地图楼层
     *
     * @param groups 地图楼层信息
     */
    private void displayGroupView(ArrayList<FMGroupInfo> groups) {
        RadioGroup radioGroup = ViewHelper.getView(FMMapGroups.this, R.id.rg_groups);
        int count = groups.size();
        mRadioButtons = new RadioButton[count];

        for (int i = 0; i < count; i++) {
            int position = radioGroup.getChildCount() - i - 1;
            mRadioButtons[i] = (RadioButton) radioGroup.getChildAt(position);

            FMGroupInfo groupInfo = groups.get(i);
            mRadioButtons[i].setTag(groupInfo.getGroupId());
            mRadioButtons[i].setText(groupInfo.getGroupName().toUpperCase());
            mRadioButtons[i].setOnCheckedChangeListener(this);
        }
        mRadioButtons[count - 1].setChecked(true);

        //单、多层控制
        mGroupControl = ViewHelper.getView(FMMapGroups.this, R.id.cb_groups);
        mGroupControl.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mGroupControl.setText(R.string.title_groups_multi);
                    multiDisplayFloor();
                } else {
                    mGroupControl.setText(R.string.title_groups_single);
                    singleDisplayFloor();
                }
            }
        });
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


    /**
     * 单层显示
     */
    private void singleDisplayFloor() {
        int groupId = mFMMap.getFocusGroupId();
        singleDisplayFloor(groupId);
    }

    /**
     * 单层显示
     *
     * @param groupId 楼层id
     */
    private void singleDisplayFloor(int groupId) {
        int[] showFloors = new int[1]; // 需要显示的楼层
        showFloors[0] = groupId;
        // 设置单层显示,及焦点层
        mFMMap.setMultiDisplay(showFloors, 0, null);
    }

    /**
     * 多层显示
     *
     * @param groupId 焦点层id
     */
    private void multiDisplayFloor(int groupId) {
        int[] showFloors = mFMMap.getMapGroupIds();
        // 设置多层显示,及焦点层
        int focus = convertToFocus(groupId);
        mFMMap.setMultiDisplay(showFloors, focus, null);
    }

    /**
     * 多层显示
     */
    private void multiDisplayFloor() {
        int focusGroupId = mFMMap.getFocusGroupId();
        multiDisplayFloor(focusGroupId);
    }

    /**
     * 焦点层id转换成焦点层索引
     *
     * @param focusGroupId 焦点层id
     * @return
     */
    private int convertToFocus(int focusGroupId) {
        FMMapInfo mapInfo = mFMMap.getFMMapInfo();
        int size = mapInfo.getGroups().size();
        int focus = 0;
        for (int i = 0; i < size; i++) {
            int groupId = mapInfo.getGroups().get(i).getGroupId();
            if (focusGroupId == groupId) {
                focus = i;
                break;
            }
        }
        return focus;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            int groupId = (int) buttonView.getTag();
            mFMMap.setFocusByGroupIdAnimated(groupId, new FMLinearInterpolator(), new OnFMSwitchGroupListener() {
                @Override
                public void beforeGroupChanged() {
                    setRadioButtonEnable(false);
                }

                @Override
                public void afterGroupChanged() {
                    setRadioButtonEnable(true);
                }
            });
        }
    }

    /**
     * 设置楼层是否可用
     *
     * @param enable true 可以被点击
     *               false 不可被点击
     */
    private void setRadioButtonEnable(final boolean enable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mRadioButtons.length; i++) {
                    mRadioButtons[i].setEnabled(enable);
                }
            }
        });
    }

}
