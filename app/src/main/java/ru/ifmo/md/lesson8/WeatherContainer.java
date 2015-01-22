package ru.ifmo.md.lesson8;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ilya on 22.01.2015.
 */
public class WeatherContainer {
        public byte[] icon;
        public String text, weekday;
        public int year, day;

        public WeatherContainer(byte[] icon, String text, String wday, int year, int day) {
            this.icon = icon;
            this.text = text;
            this.weekday = wday;
            this.year = year;
            this.day = day;
        }

        public String getDayMonth() {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.DAY_OF_YEAR, day);
            SimpleDateFormat format = new SimpleDateFormat("d");
            String date = format.format(new Date());

            if(date.endsWith("1") && !date.endsWith("11"))
                return (new SimpleDateFormat("MMMM d'st'", Locale.US)).format(c.getTime());
            else if(date.endsWith("2") && !date.endsWith("12"))
                return (new SimpleDateFormat("MMMM d'nd'", Locale.US)).format(c.getTime());
            else if(date.endsWith("3") && !date.endsWith("13"))
                return (new SimpleDateFormat("MMMM d'rd'", Locale.US)).format(c.getTime());
            else
                return (new SimpleDateFormat("MMMM d'th'", Locale.US)).format(c.getTime());
        }
}
