package year2013.ifmo.catweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;


public class DeleteCityDialog extends DialogFragment{

    public interface NoticeDialogListener {
        void onDialogPositiveClick(long city, String cityName);
    }

    NoticeDialogListener mListener;
    Context context;
    SimpleCursorAdapter adapter;
    Spinner spinner;
    Cursor o;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        try {
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + context.getString(R.string.listener));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.delete_dialog, null);
        builder.setView(v).setTitle(R.string.delete_title);

        String adapterCols[] = new String[] {Weather.JustWeather.CITY_NAME};
        int[] adapterRowViews = new int[] {android.R.id.text1};
        Cursor cursor = context.getContentResolver().query(Weather.JustWeather.CONTENT_URI, null, null, null, null);
        adapter = new SimpleCursorAdapter(context, android.R.layout.simple_spinner_item, cursor, adapterCols, adapterRowViews, 0);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) v.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        //cursor.close();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                o = (Cursor) adapter.getItem(selectedItemPosition);

            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                mListener.onDialogPositiveClick(o.getLong(Weather.JustWeather.ID_COLUMN), o.getString(Weather.JustWeather.CITY_COLUMN));
            }
        })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DeleteCityDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
