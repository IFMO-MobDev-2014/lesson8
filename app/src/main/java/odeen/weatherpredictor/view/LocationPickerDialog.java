package odeen.weatherpredictor.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;

import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import odeen.weatherpredictor.AlarmReceiver;
import odeen.weatherpredictor.R;
import odeen.weatherpredictor.WeatherService;

/**
 * Created by Женя on 27.11.2014.
 */

public class LocationPickerDialog extends DialogFragment {
    public static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME";
    public static final String EXTRA_CITY_ID = "ID";
    private String mName;


    public static LocationPickerDialog newInstance(int id) {
        LocationPickerDialog fragment = new LocationPickerDialog();
        Bundle args = new Bundle();
        args.putInt(EXTRA_CITY_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;
        Intent i = new Intent();
        i.putExtra(EXTRA_CITY_NAME, mName);
        i.putExtra(EXTRA_CITY_ID, getArguments().getInt(EXTRA_CITY_ID));
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_channel, null);
        EditText name = (EditText) v.findViewById(R.id.dialog_channel_namePicker);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//                getArguments().putString(EXTRA_CITY_NAME, charSequence.toString());
                mName = charSequence.toString();
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        Button b = (Button) v.findViewById(R.id.get_locButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                String provider = LocationManager.NETWORK_PROVIDER;
                Location loc = lm.getLastKnownLocation(provider);
                if (loc == null) {
                    Toast.makeText(getActivity(), "Location not found", Toast.LENGTH_LONG).show();
                } else {
                    Intent i1 = new Intent(getActivity(), WeatherService.class);
                    i1.putExtra(WeatherService.EXTRA_FROM_CURRENT, true);
                    i1.putExtra(WeatherService.EXTRA_LAT, loc.getLatitude());
                    i1.putExtra(WeatherService.EXTRA_LON, loc.getLongitude());
                    getActivity().startService(i1);
                    dismiss();
                }
            }
        });
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_CANCELED);
                    }
                })
                .create();
    }
}
