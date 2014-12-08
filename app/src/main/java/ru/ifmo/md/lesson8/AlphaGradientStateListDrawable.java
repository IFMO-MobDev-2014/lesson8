package ru.ifmo.md.lesson8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

/**
 * Created by dimatomp on 07.12.14.
 */
public class AlphaGradientStateListDrawable extends StateListDrawable {
    private static final Drawable gradient = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xffffffff, 0x00000000});
    private static final Paint paint = new Paint();

    static {
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    private Bitmap gradientBitmap;

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        if (gradientBitmap != null) {
            if (gradientBitmap.getWidth() == right - left && gradientBitmap.getHeight() == bottom - top)
                return;
            gradientBitmap.recycle();
        }
        gradientBitmap = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(gradientBitmap);
        gradient.setBounds(0, 0, gradientBitmap.getWidth(), gradientBitmap.getHeight());
        gradient.draw(canvas);
        gradientBitmap = gradientBitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawBitmap(gradientBitmap, 0, 0, paint);
    }
}
