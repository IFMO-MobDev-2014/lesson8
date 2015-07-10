package year2013.ifmo.catweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;


public class AddByNameDialog extends DialogFragment {

    public interface NoticeDialogListener {
        void onDialogPositiveClick(String city);
    }

    NoticeDialogListener mListener;
    Context context;
    DelayAutoCompleteTextView cityName;

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
        View v = inflater.inflate(R.layout.progress_bar_and_new_actv, null);


        builder.setView(v).setTitle(R.string.add_title);

        cityName = (DelayAutoCompleteTextView) v.findViewById(R.id.city_name);
        cityName.setAdapter(new CityAutoCompleteAdapter(context));
        cityName.setLoadingIndicator((ProgressBar) v.findViewById(R.id.progress_bar));
        cityName.setThreshold(3);
        cityName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String city = (String) adapterView.getItemAtPosition(position);
                cityName.setText(city);
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        mListener.onDialogPositiveClick(cityName.getText().toString());
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddByNameDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }



}
