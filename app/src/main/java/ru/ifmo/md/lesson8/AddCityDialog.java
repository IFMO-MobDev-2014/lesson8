package ru.ifmo.md.lesson8;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Евгения on 07.12.2014.
 */
public class AddCityDialog extends DialogFragment implements TextWatcher, AdapterView.OnItemClickListener {

    AutoCompleteTextView etName = null;

    private void startSearch(String s) {
        try {
            new InputGeoCompleter().execute(URLEncoder.encode(s.trim(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    InputGeoCompleter actual = null;

    private void finishSearch() {
        actual = null;
    }

    private void exitSearch() {
        finishSearch();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dlgAddCity_title));
        View view = getActivity().getLayoutInflater().inflate(R.layout.dlg_add_layout, null, false);
        etName = (AutoCompleteTextView) view.findViewById(R.id.etName);
        etName.addTextChangedListener(this);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues cv = new ContentValues();
                cv.put(DBAdapter.KEY_WEATHER_ATMOSPHERE_HUMIDITY, 0);
                cv.put(DBAdapter.KEY_WEATHER_ATMOSPHERE_PRESSURE, 0);
                cv.put(DBAdapter.KEY_WEATHER_CODE, 0);
                cv.put(DBAdapter.KEY_WEATHER_TEMPERATURE, 0);
                cv.put(DBAdapter.KEY_WEATHER_DATE, "");
                cv.put(DBAdapter.KEY_WEATHER_WIND_DIRECTION, 0);
                cv.put(DBAdapter.KEY_WEATHER_WIND_SPEED, 0);
                cv.put(DBAdapter.KEY_WEATHER_CITY, etName.getText().toString());
                getActivity().getContentResolver().insert(WeatherContentProvider.WEATHER_URI, cv);
                dismiss();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        etName.setText((CharSequence) parent.getAdapter().getItem(position));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            startSearch(s.toString());
        } else {
            exitSearch();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    class InputGeoCompleter extends GeoCompleter {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            actual = this;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            if (strings != null && this == actual && etName != null && getActivity() != null)
                etName.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, strings));
            super.onPostExecute(strings);
        }
    }
}
