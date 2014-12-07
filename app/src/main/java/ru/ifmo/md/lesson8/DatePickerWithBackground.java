package ru.ifmo.md.lesson8;

import android.content.Context;
import android.util.AttributeSet;

import net.simonvt.numberpicker.NumberPicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

/**
 * Created by dimatomp on 06.12.14.
 */
public class DatePickerWithBackground extends NumberPicker implements DateSelector {
    private static final DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
    TimeOfDay timeOfDay;
    private Calendar calendar = Calendar.getInstance();

    public DatePickerWithBackground(Context context) {
        super(context);
    }

    public DatePickerWithBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    @Override
    public long getDate() {
        return dateByVal(getValue());
    }

    @Override
    public void setMaxDate(long maxDate) {

    }

    @Override
    public void setMinDate(long minDate) {

    }

    private long dateByVal(int val) {
        try {
            return format.parse(getDisplayedValues()[val]).getTime();
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    private void generateDates() {
        String[] dates = new String[11];
        calendar.add(Calendar.DATE, -5);
        for (int i = 0; i < 11; i++) {
            dates[i] = format.format(calendar.getTimeInMillis());
            calendar.add(Calendar.DATE, 1);
        }
        calendar.add(Calendar.DATE, -6);
        setDisplayedValues(dates);
        setMinValue(0);
        setMaxValue(10);
    }

    @Override
    public void setDate(long date, boolean arg1, boolean arg2) {
        calendar.setTimeInMillis(date);
        generateDates();
        setValue(5);
    }

    @Override
    public void setOnTimeChangedListener(final OnTimeChangedListener listener) {
        setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal <= 1 || newVal >= 9) {
                    calendar.add(Calendar.DATE, newVal - 5);
                    generateDates();
                    setOnValueChangedListener(null);
                    setValue(5);
                    setOnValueChangedListener(this);
                }
                if (listener != null)
                    listener.onTimeChanged((DateSelector) picker, dateByVal(newVal));
            }
        });
    }
}
