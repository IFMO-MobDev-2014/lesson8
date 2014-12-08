package ru.ifmo.md.lesson8;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by kna on 08.12.2014.
 */
public class MergeAdapter extends BaseAdapter {
    final BaseAdapter adapterFirst, adapterSecond;

    MergeAdapter(BaseAdapter adapterFirst, BaseAdapter adapterSecond) {
        super(); // maybe?
        this.adapterFirst = adapterFirst;
        this.adapterSecond = adapterSecond;
    }

    @Override
    public boolean hasStableIds() {
            return adapterFirst.hasStableIds() && adapterSecond.hasStableIds();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(i >= adapterFirst.getCount())
            return adapterSecond.getView(i - adapterFirst.getCount(), view, viewGroup);

        return adapterFirst.getView(i, view, viewGroup);
    }

    public void registerDataSetObserver(android.database.DataSetObserver observer) {
        adapterFirst.registerDataSetObserver(observer);
        adapterSecond.registerDataSetObserver(observer);
    }

    public void unregisterDataSetObserver(android.database.DataSetObserver observer) {
        adapterFirst.unregisterDataSetObserver(observer);
        adapterSecond.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        return adapterFirst.getCount() + adapterSecond.getCount();
    }

    @Override
    public Object getItem(int i) {
        if(i >= adapterFirst.getCount())
            return adapterSecond.getItem(i - adapterFirst.getCount());

        return adapterFirst.getItem(i);
    }

    @Override
    public long getItemId(int i) {
        if(i >= adapterFirst.getCount())
            return adapterSecond.getItemId(i - adapterFirst.getCount());

        return adapterFirst.getItemId(i);
    }

    public void notifyDataSetChanged() {
        adapterFirst.notifyDataSetChanged();
        adapterSecond.notifyDataSetChanged();
    }

    public void notifyDataSetInvalidated() {
        adapterFirst.notifyDataSetInvalidated();
        adapterSecond.notifyDataSetInvalidated();
    }

    public boolean areAllItemsEnabled() {
        return adapterFirst.areAllItemsEnabled() && adapterSecond.areAllItemsEnabled();
    }

    public boolean isEnabled(int i) {
        if(i >= adapterFirst.getCount())
            return adapterSecond.isEnabled(i - adapterFirst.getCount());

        return adapterFirst.isEnabled(i);
    }

    public android.view.View getDropDownView(int i, android.view.View convertView, android.view.ViewGroup parent) {
        if(i >= adapterFirst.getCount())
            return adapterSecond.getDropDownView(i - adapterFirst.getCount(), convertView, parent);

        return adapterFirst.getDropDownView(i, convertView, parent);
    }

    public int getItemViewType(int i) {
        if(i >= adapterFirst.getCount())
            return adapterFirst.getViewTypeCount() + adapterSecond.getItemViewType(i - adapterFirst.getCount());

        return adapterFirst.getItemViewType(i);
    }

    public int getViewTypeCount() {
        return adapterFirst.getViewTypeCount() + adapterSecond.getViewTypeCount();
    }

    public boolean isEmpty() {
        return adapterFirst.isEmpty() && adapterSecond.isEmpty();
    }
}
