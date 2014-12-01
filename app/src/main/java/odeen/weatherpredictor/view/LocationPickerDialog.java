package odeen.weatherpredictor.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import odeen.weatherpredictor.R;

/**
 * Created by Женя on 27.11.2014.
 */

public class LocationPickerDialog extends DialogFragment {
    public static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME";
    private String mName;


    public static LocationPickerDialog newInstance() {
        LocationPickerDialog fragment = new LocationPickerDialog();
        return fragment;
    }

    public void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;
        Intent i = new Intent();
        i.putExtra(EXTRA_CITY_NAME, mName);
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
                }).create();
    }
}
