package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Collections;

/**
 * Created by pva701 on 07.12.14.
 */
public class AddCityFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private EditText inputCity;
    private ListView cityList;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_addcity, container, false);
        inputCity = (EditText)root.findViewById(R.id.city_name_input);
        cityList = (ListView)root.findViewById(R.id.city_list);
        final ArrayAdapter <String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        cityList.setAdapter(adapter);
        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getActivity().setResult(getActivity().RESULT_OK, new Intent().putExtra(AddCityActivity.ADDED_NAME, adapter.getItem(i)));
                getActivity().finish();
            }
        });

        inputCity.addTextChangedListener(new TextWatcher() {
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
                    DataManager dataManager = DataManager.get(getActivity());
                    int left = getLeftBound(s);
                    int right = getRightBound(s);
                    adapter.clear();
                    for (int i = left; i <= right; ++i)
                        if (!dataManager.contains(dataManager.getCity(i)))
                            adapter.add(dataManager.getCity(i));
                    adapter.notifyDataSetChanged();
                }
            }
        });
        return root;
    }

    private int getLeftBound(String s) {
        DataManager dataManager = DataManager.get(getActivity());
        int l = 0, r = dataManager.countCities(), mid;
        s = s.toLowerCase();
        while (l + 1 < r) {
            mid = (l + r) / 2;
            if (s.compareTo(dataManager.getCity(mid).toLowerCase()) > 0)
                l = mid;
            else
                r = mid;
        }
        return r;
    }

    private int getRightBound(String s) {
        DataManager dataManager = DataManager.get(getActivity());
        int l = 0, r = dataManager.countCities(), mid;
        s = s.toLowerCase();
        while (l + 1 < r) {
            mid = (l + r) / 2;
            String cur = dataManager.getCity(mid).toLowerCase();
            cur = cur.substring(0, Math.min(cur.length(), s.length()));
            if (s.compareTo(cur) >= 0)
                l = mid;
            else
                r = mid;
        }
        return l;
    }
}
