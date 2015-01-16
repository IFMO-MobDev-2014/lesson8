package ru.eugene.weather.database;
/**
 * Created by eugene on 12/16/14.
 */
public final class CityDataSource {
    public static final String TABLE_NAME = "cities";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_LINK = "link";
    public static final String CREATE_COMMAND = "CREATE TABLE " + TABLE_NAME +
            "( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
            ", " + COLUMN_CITY + " TEXT NOT NULL" +
            ", " + COLUMN_LINK + " TEXT NOT NULL);";

    public static final String[] getProjection() {
        return new String[] {COLUMN_ID, COLUMN_CITY, COLUMN_LINK};
    }

    private CityDataSource(){}
}
