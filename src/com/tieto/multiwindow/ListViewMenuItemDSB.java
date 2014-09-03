/**
 * AndroidDesktop
 * Copyright (C) 2014 Tieto Poland Sp. z o.o.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.tieto.multiwindow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.widget.ImageView;

public class ListViewMenuItemDSB extends DragShadowBuilder {
    private ImageView mShadow;
    private int mWidth;
    private int mHeight;

    public ListViewMenuItemDSB(View v, int width, int height) {
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
