package lesson8.android.weather

import android.content.Context
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.provider.BaseColumns

object DatabaseHelper {
  private val DATABASE_VERSION: Int = 1
  val DATABASE_NAME = "weather.db"
  val DATABASE_TABLE = "weather"
  val WEATHER_CITY = "weather_city"
  val WEATHER_COUNTRY = "weather_country"
  val WEATHER_TEMP = "weather_temp"
  val WEATHER_TEMP_UNIT = "weather_temp_unit"
  val WEATHER_CLOUDNESS = "weather_cloudness"
  val WEATHER_DATE = "weather_date"

  val CREATE_TABLE = "create table " +
    DATABASE_TABLE + " (" +
    BaseColumns._ID + " integer primary key autoincrement, " +
    WEATHER_CITY + " text not null, " +
    WEATHER_COUNTRY + " text not null, " +
    WEATHER_TEMP + " text not null, " +
    WEATHER_TEMP_UNIT + " text not null, " +
    WEATHER_CLOUDNESS + " text not null, " +
    WEATHER_DATE + " text)"
}

class DatabaseHelper(context: Context) extends SQLiteOpenHelper(context, null, null, 1) with BaseColumns {
  import DatabaseHelper._
  override def onCreate(db: SQLiteDatabase): Unit = db.execSQL(CREATE_TABLE)
  override def onUpgrade(p1: SQLiteDatabase, p2: Int, p3: Int): Unit = ???
}