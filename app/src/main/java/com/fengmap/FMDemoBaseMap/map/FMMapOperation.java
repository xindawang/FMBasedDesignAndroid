package com.fengmap.FMDemoBaseMap.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.fengmap.FMDemoBaseMap.R;
import com.fengmap.FMDemoBaseMap.utils.FileUtils;
import com.fengmap.FMDemoBaseMap.utils.ViewHelper;
import com.fengmap.FMDemoBaseMap.widget.OnSingleClickListener;
import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.animator.FMAnimation;
import com.fengmap.android.map.animator.FMAnimationFactory;
import com.fengmap.android.map.animator.FMValueAnimation;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.geometry.FMMapCoord;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 地图操作(程序控制)
 * <p>平移动画、缩放动画、倾斜动画、旋转动画，注:{@link FMAnimationFactory#createFMValueAnimation(String)}
 * 传入动画名称不能相同，否则创建失败</p>
 */
public class FMMapOperation extends Activity implements OnFMMapInitListener {
    private static final String ANIM_ROTATE = "anim_rotate";
    private static final String ANIM_TILT = "anim_rotate";
    private FMMapCoord CENTER_COORD = new FMMapCoord(1.296164E7, 4861800.0);
    private int[] mViewIds = {R.id.btn_move, R.id.btn_zoom, R.id.btn_tilt, R.id.btn_rotate};
    private FMMapView mMapView;
    private FMMap mFMMap;
    private float mTiltAngle = 90;
    private int mLevel = 20;
    private float mRotateAngle = 60;
    private FMAnimationFactory mAnimationFactory;
    /**
     * 点击事件
     */
    private OnSingleClickListener mOnSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View view) {
            switch (view.getId()) {
                case R.id.btn_move:
                    animateMove();
                    break;
                case R.id.btn_zoom:
                    animateZoom();
                    break;
                case R.id.btn_tilt:
                    animateTilt();
                    break;
                case R.id.btn_rotate:
                    animateRotate();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_operate);

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

        mAnimationFactory = mFMMap.getAnimationFactory();
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

        String[] labels = new String[4];
        labels[0] = getString(R.string.operate_btn_move, mFMMap.getFocusGroupId(), CENTER_COORD.x, CENTER_COORD.y);
        labels[1] = getString(R.string.operate_btn_zoom, mLevel);
        labels[2] = getString(R.string.operate_btn_tilt, mTiltAngle);
        labels[3] = getString(R.string.operate_btn_rotate, mRotateAngle);

        for (int i = 0; i < labels.length; i++) {
            Button button = ViewHelper.getView(FMMapOperation.this, mViewIds[i]);
            button.setText(labels[i]);
            button.setOnClickListener(mOnSingleClickListener);
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
     * 动画移动
     */
    private void animateMove() {
        mFMMap.moveToCenter(CENTER_COORD, true);
    }

    /**
     * 动画缩放
     */
    private void animateZoom() {
        mFMMap.setZoomLevel(mLevel, true);
    }

    /**
     * 动画倾斜
     */
    private void animateTilt() {
        FMValueAnimation animRotate = mAnimationFactory.getFMValueAnimation(ANIM_TILT);
        if (animRotate != null) {
            mAnimationFactory.destroyFMValueAnimation(ANIM_TILT);
        }

        float fromAngle = mFMMap.getTiltAngle();
        float toAngle = mTiltAngle;

        //创建动画对象
        animRotate = mAnimationFactory.createFMValueAnimation(ANIM_TILT);
        animRotate.setOnFMAnimationListener(new FMAnimation.OnFMAnimationListener() {
            @Override
            public void beforeAnimation(String s) {

            }

            @Override
            public void updateAnimationFrame(String s, Object last, Object current) {
                //执行倾斜
                Double angel = (Double) current;
                mFMMap.setTiltAngle(angel.floatValue());
            }

            @Override
            public void afterAnimation(String s) {

            }
        });
        animRotate.ofDouble(fromAngle, toAngle);
        animRotate.start();
    }

    /**
     * 动画旋转
     */
    private void animateRotate() {
        FMValueAnimation animRotate = mAnimationFactory.getFMValueAnimation(ANIM_ROTATE);
        if (animRotate != null) {
            animRotate.stop();
            mAnimationFactory.destroyFMValueAnimation(ANIM_ROTATE);
        }

        float fromAngle = getCurrentAngle(mRotateAngle);
        float toAngle = mRotateAngle;

        //创建动画对象
        animRotate = mAnimationFactory.createFMValueAnimation(ANIM_ROTATE);
        animRotate.setOnFMAnimationListener(new FMAnimation.OnFMAnimationListener() {
            @Override
            public void beforeAnimation(String s) {

            }

            @Override
            public void updateAnimationFrame(String s, Object last, Object current) {
                //执行旋转
                Double angel = (Double) current;
                mFMMap.setRotateAngle(angel.floatValue());
            }

            @Override
            public void afterAnimation(String s) {

            }
        });
        animRotate.ofDouble(fromAngle, toAngle);
        animRotate.start();
    }

    /**
     * 获取当前角度
     *
     * @return
     */
    private float getCurrentAngle(float toAngle) {
        float startAngle = mFMMap.getRotateAngle();
        float changeAngle = toAngle - startAngle;
        if (changeAngle > 180) {
            startAngle += 360.0f;
        } else if (changeAngle < -180) {
            startAngle -= 360.0f;
        }
        return startAngle % 360;
    }
}
