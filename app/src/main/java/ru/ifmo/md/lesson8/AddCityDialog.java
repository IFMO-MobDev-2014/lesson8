package ru.ifmo.md.lesson8;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Created by dimatomp on 01.12.14.
 */
public class AddCityDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText editText = new EditText(getActivity());
        builder.setTitle(getString(R.string.action_add))
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SpinnerAdapter adapter = ((WeatherActivity) getActivity()).adapter;
                        adapter.addCity(editText.getText().toString());
                        getActivity().getActionBar().setSelectedNavigationItem(adapter.getCount() - 1);
                        dismiss();
                    }
                })
                .setView(editText);
        return builder.create();
    }
}
