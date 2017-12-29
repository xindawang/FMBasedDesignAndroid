package com.fengmap.indoorPosition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fengmap.indoorPosition.map.FMMap2D3D;
import com.fengmap.indoorPosition.map.FMMapBasic;
import com.fengmap.indoorPosition.map.FMMapCoordTransform;
import com.fengmap.indoorPosition.map.FMMapGroups;
import com.fengmap.indoorPosition.map.FMMapInitialize;
import com.fengmap.indoorPosition.map.FMMapLayers;
import com.fengmap.indoorPosition.map.FMMapOperation;
import com.fengmap.indoorPosition.map.FMMapThemeSwitch;
import com.fengmap.indoorPosition.utils.ViewHelper;
import com.fengmap.android.FMMapSDK;


/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 主页面
 * <p>在Android6.0以上版本使用fengmap地图之前，应注意android.permission.WRITE_EXTERNAL_STORAGE、
 * permission:android.permission.READ_EXTERNAL_STORAGE权限申请</p>
 */
public class MapApiDemoMain extends Activity {

    private final DemoInfo[] DEMOS = {
            new DemoInfo(R.string.demo_title_main_basemap, FMMapBasic.class),
            new DemoInfo(R.string.demo_title_main_init, FMMapInitialize.class),
            new DemoInfo(R.string.demo_title_main_theme, FMMapThemeSwitch.class),
            new DemoInfo(R.string.demo_title_main_operate, FMMapOperation.class),
            new DemoInfo(R.string.demo_title_main_groups, FMMapGroups.class),
            new DemoInfo(R.string.demo_title_main_layers, FMMapLayers.class),
            new DemoInfo(R.string.demo_title_main_mode, FMMap2D3D.class),
            new DemoInfo(R.string.demo_title_main_transform, FMMapCoordTransform.class)
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView titleView = ViewHelper.getView(MapApiDemoMain.this, R.id.navigation_bar);
        titleView.setText(getString(R.string.demo_title_main, FMMapSDK.getVersion()));

        ListView listView = (ListView) findViewById(R.id.listView);
        // 添加ListItem，设置事件响应
        listView.setAdapter(new DemoListAdapter());
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(position);
            }
        });

    }

    /**
     * 列表点击
     *
     * @param position 列表索引
     */
    void onListItemClick(int position) {
        Intent intent;
        intent = new Intent(MapApiDemoMain.this, DEMOS[position].clazz);
        this.startActivity(intent);
    }

    private class DemoListAdapter extends BaseAdapter {

        public DemoListAdapter() {
            super();
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            convertView = View.inflate(MapApiDemoMain.this,
                    R.layout.demo_info_item, null);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(DEMOS[index].title);
            return convertView;
        }

        @Override
        public int getCount() {
            return DEMOS.length;
        }

        @Override
        public Object getItem(int index) {
            return DEMOS[index];
        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }

    private class DemoInfo {
        private final int title;
        private final Class<? extends Activity> clazz;

        public DemoInfo(int title,
                        Class<? extends Activity> clazz) {
            this.title = title;
            this.clazz = clazz;
        }
    }
}