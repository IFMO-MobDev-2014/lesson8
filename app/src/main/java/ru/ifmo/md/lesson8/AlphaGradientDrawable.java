package ru.ifmo.md.lesson8;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Created by dimatomp on 07.12.14.
 */
public class AlphaGradientDrawable extends Drawable {
    private static final int MAX_POS = 10;
    private static final int[] colors;

    static {
        colors = new int[MAX_POS + 1];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = ((int) (Math.sqrt(1 - ((double) i) / MAX_POS) * 0xaf + 0x50)) << 24;
            colors[i] |= 0xffffff;
        }
    }

    private LinearGradient shader = new LinearGradient(0, 0, MAX_POS, 0, colors, null, Shader.TileMode.REPEAT);
    private final int parentBound;
    private final Drawable top, bottom;
    private Matrix matrix = new Matrix();
    private Paint paint = new Paint();

    public AlphaGradientDrawable(int parentBound, Drawable top, Drawable bottom) {
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        this.parentBound = parentBound;
        this.top = top.getConstantState().newDrawable();
        this.bottom = bottom.getConstantState().newDrawable();
    }

    @Override
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        this.top.setBounds(left, top, right, parentBound);
        this.bottom.setBounds(left, parentBound, right, bottom);
        matrix.setScale((right - left) / ((float) MAX_POS), 1);
        shader.setLocalMatrix(matrix);
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        top.setState(state);
        bottom.setState(state);
        invalidateSelf();
        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        top.draw(canvas);
        bottom.draw(canvas);
        canvas.drawPaint(paint);
    }
}
