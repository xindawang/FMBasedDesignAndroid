package com.fengmap.indoorPosition;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ACER on 2018/1/6.
 */

public class AlgorithmActivity extends AppCompatActivity{

    List<String> list;
    private int algorithm_code;
    ListView listView;
    Button cancel_algorithm;
    Button confirm_algorithm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_algo_list);

        Intent intent = getIntent();
        //从intent取出bundle
        Bundle bundle=intent.getBundleExtra("data");
        //获取数据
        Integer code=intent.getIntExtra("algorithm_code",0);
        if (code != null) algorithm_code = code;

        listView = (ListView) findViewById(R.id.algorithm_listView);
        cancel_algorithm = (Button) findViewById(R.id.cancel_algorithm);
        cancel_algorithm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        confirm_algorithm = (Button) findViewById(R.id.confirm_algorithm);
        confirm_algorithm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("algorithm_code", algorithm_code);
                setResult(1, intent);
                finish();
            }
        });

        initListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == algorithm_code) return;
                algorithm_code = i;
                initListView();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void initListView() {
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
            list.add("卷积神经网络");
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

            if (position == algorithm_code){
                ImageView algorithm_imageView = (ImageView) view.findViewById(R.id.algorithm_imageView);
                algorithm_imageView.setImageResource(R.drawable.select_yes);
            }

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
