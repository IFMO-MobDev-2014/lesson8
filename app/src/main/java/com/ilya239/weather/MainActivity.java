package com.ilya239.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView listView = (ListView)findViewById(R.id.listView);

        final String[] texts = {"Санкт-Петербург","Москва","Зеленогорск"};
        final int[] index = {2123260,2122265,2124583};

        ArrayList<HashMap<String,String>> items = new ArrayList<HashMap<String, String>>(texts.length);
        HashMap<String, String> map;

        for(int i=0; i<texts.length;i++) {
            map = new HashMap<String, String>();
            map.put("city",texts[i]);
            items.add(map);
        }

        String[] from = {"city"};
        int[] to = {R.id.tvText};

        SimpleAdapter sAdapter = new SimpleAdapter(this,items,R.layout.item,from,to);
        listView.setAdapter(sAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                Intent intent = new Intent(MainActivity.this,ShowWeather.class);
                intent.putExtra("city",texts[position]);
                intent.putExtra("index",index[position]);
                startActivity(intent);
            }
        });
    }
}
