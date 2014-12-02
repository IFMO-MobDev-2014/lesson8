package lesson8.android.weather

import android.content.{UriMatcher, ContentValues, ContentProvider}
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

object WeatherProvider {
  val AUTHORITY: String = "lesson8.android.weather.provider"
  val DB_NAME: String = "weather"
  private val sUriMatcher: UriMatcher = new UriMatcher(0)
  sUriMatcher.addURI(AUTHORITY, "weather", 1)
  sUriMatcher.addURI(AUTHORITY, "weather/#", 2)
}

class WeatherProvider extends ContentProvider {
  import WeatherProvider._
  private val _dbHelper: DatabaseHelper = new DatabaseHelper(getContext)
  private val _dataBase: SQLiteDatabase = null
  override def onCreate(): Boolean = ???
  override def getType(uri: Uri): String = sUriMatcher.`match`(uri) match {
    case 1 => "vnd.android.cursor.dir/vnd.lesson8.android.provider.weather"
    case 2 => "vnd.android.cursor.item/vnd.lesson8.android.provider.weather"
  }
  override def update(uri: Uri, values: ContentValues, p3: String, p4: Array[String]): Int = ???
  override def insert(uri: Uri, values: ContentValues): Uri = ???
  override def delete(uri: Uri, p2: String, p3: Array[String]): Int = ???
  override def query(uri: Uri, p2: Array[String], p3: String, p4: Array[String], p5: String): Cursor = ???
}