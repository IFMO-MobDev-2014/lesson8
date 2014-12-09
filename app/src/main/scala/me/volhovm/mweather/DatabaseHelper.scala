package me.volhovm.mweather

import java.util.Date

import android.content.{ContentResolver, ContentValues, Context}
import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.provider.BaseColumns

object DatabaseHelper extends BaseColumns {
  private val DATABASE_VERSION: Int = 1
  val DATABASE_NAME = "weather.db"
  val WEATHER_TABLE_NAME = "weather"
  val WEATHER_CITY = "weather_city"
  val WEATHER_COUNTRY = "weather_country"
  val WEATHER_HIGH_TEMP = "weather_high_temp"
  val WEATHER_LOW_TEMP = "weather_low_temp"
  val WEATHER_STATE_CODE = "weather_state_code"
  val WEATHER_STATE_DESC = "weather_state_descr"
  val WEATHER_HUMIDITY = "weather_humidity"
  val WEATHER_PRESSURE = "weather_pressure"
  val WEATHER_WIND = "weather_wind"
  val WEATHER_DATE = "weather_date"

  val CITIES_TABLE_NAME = "cities"
  val CITY = "city"

  val CREATE_WEATHER_TABLE = "create table " +
    WEATHER_TABLE_NAME + " (" +
    BaseColumns._ID + " integer primary key autoincrement, " +
    WEATHER_CITY + " text not null, " +
    WEATHER_COUNTRY + " text not null, " +
    WEATHER_HIGH_TEMP + " integer, " +
    WEATHER_LOW_TEMP + " integer, " +
    WEATHER_STATE_CODE + " integer, " +
    WEATHER_STATE_DESC + " text not null, " +
    WEATHER_HUMIDITY + " integer, " +
    WEATHER_PRESSURE + " integer, " +
    WEATHER_WIND + " text not null, " +
    WEATHER_DATE + " integer);"

  val CREATE_CITIES_TABLE = "create table " + CITIES_TABLE_NAME + " (" +
    BaseColumns._ID + " integer primary key autoincrement, " +
    CITY + " text not null);"
}

class DatabaseHelper(context: Context) extends SQLiteOpenHelper(context, null, null, 1) with BaseColumns {
  private val mContentResolver: ContentResolver = context.getContentResolver

  import me.volhovm.mweather.DatabaseHelper._

  override def onCreate(db: SQLiteDatabase): Unit = {
    db.execSQL(CREATE_CITIES_TABLE)
    db.execSQL(CREATE_WEATHER_TABLE)
  }

  override def onUpgrade(p1: SQLiteDatabase, p2: Int, p3: Int): Unit = throw new UnsupportedOperationException("CANNOT UPGRADE DB")

  def addWeather(weather: Weather): Unit = mContentResolver.insert(WeatherProvider.MAIN_CONTENT_URI, weather.getValues)

  def getCities(): List[String] = {
    var cursor: Cursor =
      mContentResolver.query(WeatherProvider.CITIES_CONTENT_URI, Array(DatabaseHelper.CITY), null, null, null)
    cursor.moveToFirst()
    compose(cursor, (curs: Cursor) => curs.getString(0))
  }

  def getWeatherByCity(cityname: String) = {
    val cursor: Cursor =
      mContentResolver.query(WeatherProvider.MAIN_CONTENT_URI, null, WEATHER_CITY + "='" + cityname + "'", null, null)
    cursor.moveToFirst()
    compose(cursor, cursorToWeather).reverse
  }

  def addCity(cityname: String) = {
    val values = new ContentValues()
    values.put(DatabaseHelper.CITY, cityname)
    mContentResolver.insert(WeatherProvider.CITIES_CONTENT_URI, values)
  }

  def deleteCity(cityname: String) = mContentResolver.delete(WeatherProvider.CITIES_CONTENT_URI, DatabaseHelper.CITY + "='" + cityname + "'", null)

  def updateCity(oldName: String, newName: String) = {
    val values = new ContentValues()
    values.put(DatabaseHelper.CITY, newName)
    mContentResolver.update(WeatherProvider.CITIES_CONTENT_URI, values, DatabaseHelper.CITY + "='" + oldName + "'", null)
  }

  private def compose[A](cursor: Cursor, foo: (Cursor) => A): List[A] =
    if (cursor.isAfterLast) Nil
    else foo(cursor) :: compose({
      cursor.moveToNext();
      cursor
    }, foo)

  def cursorToWeather(cursor: Cursor): Weather =
    if (cursor.isAfterLast)
      null
    else new Weather(
      cursor.getString(1),
      cursor.getString(2),
      (cursor.getInt(3), cursor.getInt(4)),
      new WeatherState(cursor.getInt(5), cursor.getString(6)),
      cursor.getInt(7).toDouble / 100,
      cursor.getInt(8),
      cursor.getString(9),
      new Date(1000l * cursor.getInt(10).toLong)
    )
}