package me.volhovm.mweather

import java.util.{Random, Date}

import android.content.{ContentValues, ContentResolver, Context}
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

  val CREATE_TABLE = "create table " +
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
    WEATHER_DATE + " integer)"
  val ALL_COLUMNS: Array[String] = Array(BaseColumns._ID, WEATHER_CITY, WEATHER_COUNTRY, WEATHER_HIGH_TEMP, WEATHER_LOW_TEMP,
    WEATHER_STATE_CODE, WEATHER_STATE_DESC, WEATHER_HUMIDITY, WEATHER_PRESSURE, WEATHER_WIND, WEATHER_DATE)
}

class DatabaseHelper(context: Context) extends SQLiteOpenHelper(context, null, null, 1) with BaseColumns {
  private val mContentResolver: ContentResolver = context.getContentResolver
  import me.volhovm.mweather.DatabaseHelper._

  override def onCreate(db: SQLiteDatabase): Unit = {
    db.execSQL(CREATE_TABLE)
//    new Thread(){
//      override def run(): Unit = {
//        Thread.sleep(1000)
//        fakeInit()
//      }
//    }.start()
  }

  override def onUpgrade(p1: SQLiteDatabase, p2: Int, p3: Int): Unit = throw new UnsupportedOperationException("CANNOT UPGRADE DB")

  // DELETE IT
  def fakeInit(): Unit = {
    val rand: Random = new Random()
    var forecast = new Weather("Saint-Petersburg", "Ruske", (20, 18), new WeatherState(803, "cloudy"), 0.68, 756, "2 m/s SW", new Date(System.currentTimeMillis())) :: Nil
    for (i <- 0 to 10) forecast = new Weather(
      "Saint-Petersburg",
      "Russia",
      (rand.nextInt(8) + 15, rand.nextInt(8) + 14),
      new WeatherState(rand.nextInt(8) * 100 + rand.nextInt(24), if (rand.nextBoolean()) "clear" else "lol"),
      rand.nextInt(100).toDouble / 100,
      rand.nextInt(20) + 730,
      "SW 5 m/s",
      new Date(System.currentTimeMillis() + i * 86400000)) :: forecast
    forecast.foreach(addWeather _)
  }
  // DELETE IT

  def addWeather(weather: Weather): Unit = mContentResolver.insert(WeatherProvider.CONTENT_URI, weather.getValues())

  def getCities(): List[String] = {
    var cursor: Cursor =
      mContentResolver.query(WeatherProvider.CONTENT_URI, Array(DatabaseHelper.WEATHER_CITY), null, null, null)
    cursor.moveToFirst()
    compose(cursor, (curs: Cursor) => curs.getString(0)).distinct
  }

  def getWeatherByCity(cityname: String) = {
    val cursor: Cursor =
      mContentResolver.query(WeatherProvider.CONTENT_URI, DatabaseHelper.ALL_COLUMNS, WEATHER_CITY + "='" + cityname + "'", null, null)
    cursor.moveToFirst()
    compose(cursor, cursorToWeather).reverse
  }

  private def compose[A](cursor: Cursor, foo: (Cursor) => A): List[A] =
    if (cursor.isAfterLast) Nil
    else foo(cursor) :: compose({
      cursor.moveToNext(); cursor
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