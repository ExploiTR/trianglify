package com.sdsmdg.kd.trianglify.utilities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;

public class BitmapUtils {

    public static Bitmap createScaledBitmapWithBilinearSampling(View view, int newWidth, int newHeight) {
        int width = view.getWidth();
        int height = view.getHeight();

        // Create a bitmap with the same dimensions as the view
        Bitmap originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Create a canvas with the bitmap
        Canvas canvas = new Canvas(originalBitmap);

        // Draw the view onto the canvas
        view.draw(canvas);

        // Calculate the aspect ratio
        float aspectRatio = (float) width / height;

        // Calculate the new dimensions while maintaining the aspect ratio
        int scaledWidth, scaledHeight;
        if (newWidth / aspectRatio <= newHeight) {
            scaledWidth = newWidth;
            scaledHeight = Math.round(newWidth / aspectRatio);
        } else {
            scaledWidth = Math.round(newHeight * aspectRatio);
            scaledHeight = newHeight;
        }

        // Create a new bitmap with the desired dimensions
        Bitmap scaledBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? Bitmap.Config.RGBA_F16 : Bitmap.Config.ARGB_8888);

        // Create a new canvas for the scaled bitmap
        Canvas scaledCanvas = new Canvas(scaledBitmap);

        // Create and configure the Paint object for bilinear sampling
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        // Draw the original bitmap onto the new canvas, scaling it in the process
        Rect srcRect = new Rect(0, 0, width, height);
        Rect dstRect = new Rect(0, 0, scaledWidth, scaledHeight);
        scaledCanvas.drawBitmap(originalBitmap, srcRect, dstRect, paint);

        return scaledBitmap;
    }
}