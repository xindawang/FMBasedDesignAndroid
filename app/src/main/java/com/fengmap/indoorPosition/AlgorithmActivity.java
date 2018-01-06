package com.fengmap.indoorPosition;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fengmap.indoorPosition.entity.APEntity;
import com.fengmap.indoorPosition.entity.RPEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ACER on 2018/1/6.
 */

public class AlgorithmActivity extends AppCompatActivity{

    List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.algo_list);
        init();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void init() {
        ListView listView = (ListView) findViewById(R.id.algorithm_listView);
        listView.setAdapter(new AlgorithmActivity.MyAdapter(this, list));
    }

    public class MyAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<String> list;

        public MyAdapter(Context context, List<String> list) {
            // TODO Auto-generated constructor stub
            this.inflater = LayoutInflater.from(context);
            list = new ArrayList<>();
            list.add("k近邻");
            list.add("贝叶斯");
            this.list = list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View view = null;
            view = inflater.inflate(R.layout.algo_list_item, null);

            TextView textView = (TextView) view.findViewById(R.id.algorithm_name);
            textView.setText(list.get(position));

            return view;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wifi_menu, menu);
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
}
