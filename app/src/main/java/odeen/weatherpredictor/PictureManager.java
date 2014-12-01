package odeen.weatherpredictor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;

/**
 * Created by Женя on 28.11.2014.
 */
public class PictureManager {
    private static PictureManager sManager;
    private Context mContext;

    private PictureManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static PictureManager get(Context context) {
        if (sManager == null)
            sManager = new PictureManager(context);
        return sManager;
    }

    public Bitmap getIcon(String name) {
        name = name.charAt(name.length() - 1) + name.substring(0, name.length() - 1);
        int id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
        return BitmapFactory.decodeResource(mContext.getResources(), id);
    }

}
