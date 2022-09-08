package com.xeasy.noticefix.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageUtils {
    private int[] mTempBuffer;
    private Bitmap mTempCompactBitmap;
    private Canvas mTempCompactBitmapCanvas;
    private Paint mTempCompactBitmapPaint;
    private final Matrix mTempMatrix = new Matrix();

    public ImageUtils() {
    }

    public boolean isGrayscale(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        if (height > 64 || width > 64) {
            if (this.mTempCompactBitmap == null) {
                this.mTempCompactBitmap = Bitmap.createBitmap(64, 64, Config.ARGB_8888);
                this.mTempCompactBitmapCanvas = new Canvas(this.mTempCompactBitmap);
                this.mTempCompactBitmapPaint = new Paint(1);
                this.mTempCompactBitmapPaint.setFilterBitmap(true);
            }

            this.mTempMatrix.reset();
            this.mTempMatrix.setScale(64.0F / (float)width, 64.0F / (float)height, 0.0F, 0.0F);
            this.mTempCompactBitmapCanvas.drawColor(0, Mode.SRC);
            this.mTempCompactBitmapCanvas.drawBitmap(bitmap, this.mTempMatrix, this.mTempCompactBitmapPaint);
            bitmap = this.mTempCompactBitmap;
            height = 64;
            width = 64;
        }

        int size = height * width;
        this.ensureBufferSize(size);
        bitmap.getPixels(this.mTempBuffer, 0, width, 0, 0, width, height);

        for(int i = 0; i < size; ++i) {
            if (!isGrayscale(this.mTempBuffer[i])) {
                return false;
            }
        }

        return true;
    }

    private void ensureBufferSize(int size) {
        if (this.mTempBuffer == null || this.mTempBuffer.length < size) {
            this.mTempBuffer = new int[size];
        }

    }

    public static boolean isGrayscale(int color) {
        int alpha = 255 & color >> 24;
        if (alpha < 50) {
            return true;
        } else {
            int r = 255 & color >> 16;
            int g = 255 & color >> 8;
            int b = 255 & color;
            boolean b1 = Math.abs(r - g) < 20 && Math.abs(r - b) < 20 && Math.abs(g - b) < 20;
            if ( ! b1) {
                System.out.println(color);
            }
            return b1;
        }
    }

    public static Bitmap buildScaledBitmap(Drawable drawable, int maxWidth, int maxHeight) {
        if (drawable == null) {
            return null;
        } else {
            int originalWidth = drawable.getIntrinsicWidth();
            int originalHeight = drawable.getIntrinsicHeight();
            if (originalWidth <= maxWidth && originalHeight <= maxHeight && drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable)drawable).getBitmap();
            } else if (originalHeight > 0 && originalWidth > 0) {
                float ratio = Math.min((float)maxWidth / (float)originalWidth, (float)maxHeight / (float)originalHeight);
                ratio = Math.min(1.0F, ratio);
                int scaledWidth = (int)(ratio * (float)originalWidth);
                int scaledHeight = (int)(ratio * (float)originalHeight);
                Bitmap result = Bitmap.createBitmap(scaledWidth, scaledHeight, Config.ARGB_8888);
                Canvas canvas = new Canvas(result);
                drawable.setBounds(0, 0, scaledWidth, scaledHeight);
                drawable.draw(canvas);
                return result;
            } else {
                return null;
            }
        }
    }

}
