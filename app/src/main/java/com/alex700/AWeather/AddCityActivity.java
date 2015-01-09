package com.alex700.AWeather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


public class AddCityActivity extends Activity implements TextWatcher {

    public static final String CITY_NAME = "city_name";

    private ListView listView;
    private Button button;
    private EditText editText;
    private ArrayAdapter adapter;
    private CityManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        Log.d("ADD", "START");
        listView = (ListView) findViewById(R.id.add_city_list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        manager = new CityManager(this);

        button = (Button) findViewById(R.id.add_city_button);

        editText = (EditText) findViewById(R.id.add_city_edit_text);
        editText.addTextChangedListener(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(CITY_NAME, manager.getCityName(manager.leftBinarySearch(editText.getText().toString())));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editText.setText(parent.getItemAtPosition(position).toString());
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() >= 1) {
            int left = manager.leftBinarySearch(s.toString());
            int right = manager.rightBinarySearch(s.toString());
            adapter.clear();
            Log.d("BS", "" + left + " " + right);
            for (int i = left; i < right; i++) {
                adapter.add(manager.getCityName(i));
            }
            adapter.notifyDataSetChanged();
            button.setEnabled(manager.getCount() != left && manager.getCityName(left).equalsIgnoreCase(s.toString()));
        } else {
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void afterTextChanged(Editable s) {
    }
}
