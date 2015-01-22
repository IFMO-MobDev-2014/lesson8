package freemahn.com.lesson8;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Created by Freemahn on 22.01.2015.
 */
public class ChangeCityActivity extends Activity {
    RadioGroup radiogroup;
    Context context;
    int currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_cities);
        context = this;
        currentCity = getIntent().getIntExtra("city", 0);
        radiogroup = (RadioGroup) findViewById(R.id.cities_view);
        for (int i = 0; i < CurrentWeatherActivity.cities.size(); i++) {

            RadioButton rbtn = new RadioButton(this);
            rbtn.setText(CurrentWeatherActivity.cities.get(i));
            if (i == currentCity)
                rbtn.setActivated(true);
            radiogroup.addView(rbtn);
        }

        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                currentCity = checkedId;

            }
        });
        Button add = (Button) findViewById(R.id.add_btn);
        Button delete = (Button) findViewById(R.id.delete_btn);
        Button save = (Button) findViewById(R.id.save_btn);
        final EditText editText = (EditText) findViewById(R.id.city_name_view);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editText.getText().toString();
                for (int i = 0; i < CurrentWeatherActivity.cities.size(); i++) {
                    if (CurrentWeatherActivity.cities.get(i).equals(newName)) {
                        Toast.makeText(context, "This city is already in list", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                CurrentWeatherActivity.cities.add(newName);
                editText.setText("");
                RadioButton newRadioButton = new RadioButton(context);
                newRadioButton.setText(newName);
                newRadioButton.setActivated(true);
                radiogroup.addView(newRadioButton);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radiogroup.removeViewAt(currentCity);
                CurrentWeatherActivity.cities.remove(currentCity);
                currentCity = 0;


            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentWeatherActivity.currentCityId = currentCity;
                Intent intent = new Intent(context, CurrentWeatherActivity.class);
                startActivity(intent);

            }
        });
    }
}
