package me.volhovm.mweather

import android.content.{ContentProvider, ContentValues, UriMatcher}
import android.database.Cursor
import android.database.sqlite.{SQLiteQueryBuilder, SQLiteDatabase}
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils

object WeatherProvider {
  val AUTHORITY: String = "me.volhovm.mweather.WeatherProvider"
//  val TABLE_NAME: String = "weather"
  val CONTENT_URI: Uri = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.WEATHER_TABLE_NAME)
  val ITEMS: Int = 1
  val ITEMS_ID: Int = 2
  private val sUriMatcher: UriMatcher = new UriMatcher(0)
  sUriMatcher.addURI(AUTHORITY, "weather", ITEMS)
  sUriMatcher.addURI(AUTHORITY, "weather/#", ITEMS_ID)
}

class WeatherProvider extends ContentProvider {
  import me.volhovm.mweather.WeatherProvider._

  private var mDbHelper: DatabaseHelper = null
  private var mDataBase: SQLiteDatabase = null

  override def onCreate(): Boolean = {
    mDbHelper = new DatabaseHelper(getContext)
    false
  }

  override def getType(uri: Uri): String = sUriMatcher.`match`(uri) match {
    case ITEMS => "vnd.android.cursor.dir/vnd." + AUTHORITY + ".weather"
    case ITEMS_ID => "vnd.android.cursor.item/vnd" + AUTHORITY + ".weather"
  }

  override def update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    val ret = sUriMatcher.`match`(uri) match {
      case ITEMS => mDbHelper.getWritableDatabase.update(DatabaseHelper.WEATHER_TABLE_NAME, values, selection, selectionArgs)
      case ITEMS_ID =>
        if (TextUtils.isEmpty(selection))
          mDbHelper.getWritableDatabase.update(DatabaseHelper.WEATHER_TABLE_NAME, values, BaseColumns._ID + "=" + uri.getLastPathSegment, null)
        else
          mDbHelper.getWritableDatabase.update(DatabaseHelper.WEATHER_TABLE_NAME, values, BaseColumns._ID + "=" + uri.getLastPathSegment + " and " + selection, selectionArgs)
      case _ => throw new IllegalArgumentException("URI IS WRONG: " + uri.toString)
    }
    getContext.getContentResolver.notifyChange(uri, null)
    ret
  }

  override def insert(uri: Uri, values: ContentValues): Uri = {
    val ret = sUriMatcher.`match`(uri) match {
      case ITEMS => Uri.parse(DatabaseHelper.WEATHER_TABLE_NAME + "/" + mDbHelper.getWritableDatabase.insert(DatabaseHelper.WEATHER_TABLE_NAME, null, values))
      case a => throw new IllegalArgumentException("URI IS WRONG: " + uri.toString)
    }
    getContext.getContentResolver.notifyChange(uri, null)
    ret
  }

  override def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
     val ret = sUriMatcher.`match`(uri) match {
      case ITEMS => mDbHelper.getWritableDatabase.delete(DatabaseHelper.WEATHER_TABLE_NAME, selection, selectionArgs)
      case ITEMS_ID =>
        if (TextUtils.isEmpty(selection))
          mDbHelper.getWritableDatabase.delete(DatabaseHelper.WEATHER_TABLE_NAME, BaseColumns._ID + "=" + uri.getLastPathSegment, null)
        else
          mDbHelper.getWritableDatabase.delete(DatabaseHelper.WEATHER_TABLE_NAME, BaseColumns._ID + "=" + uri.getLastPathSegment + " and " + selection, selectionArgs)
      case _ => throw new IllegalArgumentException("URI IS WRONG: " + uri.toString)
    }
    getContext.getContentResolver.notifyChange(uri, null)
    ret
  }

  override def query(uri: Uri, projection: Array[String], selection: String,
                     selectionArgs: Array[String], sortOrder: String): Cursor = {
    val builder = new SQLiteQueryBuilder()
    builder.setTables(DatabaseHelper.WEATHER_TABLE_NAME)
    sUriMatcher.`match`(uri) match {
      case ITEMS_ID => builder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment)
      case ITEMS => ()
      case _ => throw new IllegalArgumentException("WRONG URI: " + uri.toString)
    }
    val cursor = builder.query(mDbHelper.getReadableDatabase, projection, selection, selectionArgs, null, null, sortOrder)
    cursor.setNotificationUri(getContext.getContentResolver, uri)
    cursor
  }

}