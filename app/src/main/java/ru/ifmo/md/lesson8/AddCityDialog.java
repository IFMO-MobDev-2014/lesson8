package ru.ifmo.md.lesson8;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
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
                        CitiesAdapter adapter = ((WeatherActivity) getActivity()).adapter;
                        adapter.addCity(editText.getText().toString());
                        ((ViewPager) getActivity().findViewById(R.id.city_pager)).setCurrentItem(adapter.getCount() - 1, true);
                        dismiss();
                    }
                })
                .setView(editText);
        return builder.create();
    }
}
