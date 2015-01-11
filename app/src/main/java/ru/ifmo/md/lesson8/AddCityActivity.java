package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class AddCityActivity extends Activity {
    private static final int MAX_ITEMS = 10;

    private ListView lv;
    private EditText text;
    ArrayAdapter<String> adapter;
    ArrayList<String> cities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        lv = (ListView)findViewById(R.id.listView);
        text = (EditText)findViewById(R.id.editText);
        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE);
                return view;
            }
        };

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startService(new Intent(AddCityActivity.this, CityLoadService.class).putExtra("city", adapter.getItem(i)).putExtra("action", "insert"));
                finish();
            }
        });
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                Log.i("", "text changed!");
                String s = charSequence.toString();
                Comparator<String> comparator = new Comparator<String>() {
                    // I don't know, why, but Integer.compare requires API level 19. I don't like that
                    int compare(int a, int b) {
                        if (a < b) {
                            return -1;
                        }
                        if (a > b) {
                            return 1;
                        }
                        return 0;
                    }

                    int compare(char a, char b) {
                        if (a < b) {
                            return -1;
                        }
                        if (a > b) {
                            return 1;
                        }
                        return 0;
                    }
                    @Override
                    public int compare(String s, String s2) {
                        for (int i = 0; i < Math.min(s.length(), s2.length()); i++) {
                            if (Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(s2.charAt(i))) {
                                return compare(Character.toLowerCase(s.charAt(i)), Character.toLowerCase(s2.charAt(i)));
                            }
                        }
                        return compare(s.length(), s2.length());
                    }
                };
                int l = Collections.binarySearch(cities, s, comparator);
                if (l < 0) {
                    l = (l + 1) * -1;
                }
                int r = Collections.binarySearch(cities, s + "zzzzz", comparator); // Character.MAX_VALUE doesn't work :(
                if (r < 0) {
                    r = (r + 1) * -1;
                }
                r = Math.min(r, l + MAX_ITEMS);
                Log.i("", String.format("l = %d, r = %d", l, r));
                adapter.clear();
                for (int j = l; j < r; j++) {
                    adapter.add(cities.get(j));
                }
                adapter.setNotifyOnChange(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("cities")));
            cities = new ArrayList<String>();
            while (true) {
                String s = bufferedReader.readLine();
                if (s == null) {
                    break;
                }
                cities.add(s);
            }
            Log.i("", cities.size() + " cities found");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("", "file didn't opened");
        }
    }
}
