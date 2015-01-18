package ru.ifmo.md.extratask1;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;

public class MyImagesListLoader extends AsyncTaskLoader<ArrayList<MyImage>> {
    Context context;

    public MyImagesListLoader(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public ArrayList<MyImage> loadInBackground() {
        ArrayList<MyImage> list = new ArrayList<MyImage>();

        Cursor c = context.getContentResolver().query(
                ImagesContentProvider.IMAGES_URI,
                null,
                null,
                null,
                null
        );

        if (c != null) {
            c.moveToFirst();
            while (!c.isBeforeFirst() && !c.isAfterLast()) {
                byte[] byteArray = c.getBlob(c.getColumnIndex(DBImages.COLUMN_PICTURE));
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                MyImage img = new MyImage(
                        bmp,
                        c.getString(c.getColumnIndex(DBImages.COLUMN_USERNAME)),
                        c.getString(c.getColumnIndex(DBImages.COLUMN_PICTURE_NAME))
                );
                list.add(img);
                c.moveToNext();
            }
        }
        c.close();

        return list;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
