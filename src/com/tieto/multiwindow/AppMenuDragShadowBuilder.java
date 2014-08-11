package com.tieto.multiwindow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.widget.ImageView;

public class AppMenuDragShadowBuilder extends DragShadowBuilder {
    private ImageView mShadow;
    private int mWidth;
    private int mHeight;

    public AppMenuDragShadowBuilder(View v, int width, int height) {
        super(v);
        mShadow = (ImageView) v;
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        Drawable drawing = mShadow.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, mWidth, mHeight, false);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        shadowSize.set(mWidth, mHeight);
        shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y / 2);
    }
}
