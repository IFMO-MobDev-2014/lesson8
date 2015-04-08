package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;


public class AddCity extends FragmentActivity {
    public static final String CITY_ADDED_NAME = "city_added_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new AddCityFragment()).commit();
        }
    }

    public static class AddCityFragment extends Fragment {

        public AddCityFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_add_city, container, false);
            ListView citiesListView = (ListView) rootView.findViewById(R.id.cityNameListView);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            citiesListView.setAdapter(adapter);
            citiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra(AddCity.CITY_ADDED_NAME, adapter.getItem(i)));
                    getActivity().finish();
                }
            });
            EditText inputCityName = (EditText) rootView.findViewById(R.id.cityNameInput);
            final DataManager dataManager = new DataManager(getActivity());
            inputCityName.addTextChangedListener(new TextWatcher() {
                private String lastWord = "";

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    String s = charSequence.toString();
                    newWord(s);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }

                private void newWord(String s) {
                    if (lastWord.equals(s))
                        return;
                    lastWord = s;
                    if (s.equals("")) {
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                    } else {
                        int left = dataManager.prefixLeftBound(s);
                        int right = dataManager.prefixRightBound(s);
                        Log.d("AddCity", "from " + left + " to " + right);
                        adapter.clear();
                        for (int i = left; i <= right; ++i) {
                            adapter.add(dataManager.getCityName(i));
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
            return rootView;
        }
    }
}
