/**
 * Desktop for Android
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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.tieto.extension.multiwindow.MultiwindowManager;
import com.tieto.extension.multiwindow.Window;

public class ButtonContextMenu extends Dialog {
    private final int MENU_WIDTH = 150;
    private final int MENU_HEIGHT = 200;
    private final int WINDOW_SPAN_RESIZE = 25;
    private WindowManager.LayoutParams mLayoutParams;
    private AppInfo mAppInfo;
    private DisplayMetrics mScreenSizes;
    private MultiwindowManager mMultiWindow;
    private int mStatusBarSize;

    public ButtonContextMenu(Context context, AppInfo appInfo) {
        super(context, R.style.popupMenu);
        setContentView(R.layout.menu_popup_layout);
        mLayoutParams = getWindow().getAttributes();
        mMultiWindow = new MultiwindowManager(context);
        mAppInfo = appInfo;
        mScreenSizes = context.getResources().getDisplayMetrics();
        mStatusBarSize = (int) context.getResources().getDimension(
                context.getResources().getIdentifier("status_bar_height",
                        "dimen", "android"));
        getWindow().setGravity(Gravity.LEFT);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        addAlignToLeftButton();
        addAlignToRightButton();
    }

    private void addAlignToLeftButton() {
        Button left = (Button) findViewById(R.id.pushLeft);
        final Rect leftScreenSize = new Rect(0, mStatusBarSize,
                mScreenSizes.widthPixels / 2, mScreenSizes.heightPixels
                        - MenuBar.HEIGHT);
        final Rect leftBottom = new Rect(0,
                ((mScreenSizes.heightPixels - MenuBar.HEIGHT) / 2)
                        + WINDOW_SPAN_RESIZE, mScreenSizes.widthPixels / 2,
                (mScreenSizes.heightPixels - MenuBar.HEIGHT));
        final Rect leftTop = new Rect(0, mStatusBarSize,
                mScreenSizes.widthPixels / 2, (leftBottom.top));

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMultiWindow.relayoutWindow(mAppInfo.getAppWindow()
                        .getStackId(), new Rect(leftScreenSize));
                relayoutToSide(leftScreenSize, leftTop, leftBottom);
                dismiss();
            }
        });
    }

    private void addAlignToRightButton() {
        Button right = (Button) findViewById(R.id.pushRight);
        final Rect rightScreenSize = new Rect(mScreenSizes.widthPixels / 2,
                mStatusBarSize, mScreenSizes.widthPixels,
                mScreenSizes.heightPixels - MenuBar.HEIGHT);
        final Rect rightBottom = new Rect(mScreenSizes.widthPixels / 2,
                ((mScreenSizes.heightPixels - MenuBar.HEIGHT) / 2)
                        + WINDOW_SPAN_RESIZE, mScreenSizes.widthPixels,
                (mScreenSizes.heightPixels - MenuBar.HEIGHT));
        final Rect rightTop = new Rect(mScreenSizes.widthPixels / 2,
                mStatusBarSize, mScreenSizes.widthPixels, rightBottom.top);

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMultiWindow.relayoutWindow(mAppInfo.getAppWindow()
                        .getStackId(), new Rect(rightScreenSize));
                relayoutToSide(rightScreenSize, rightTop, rightBottom);
                dismiss();
            }
        });
    }

    public void relayoutToSide(Rect side, Rect resizeTo, Rect appWindow) {
        for (Window window : mMultiWindow.getAllWindows()) {
            if (side.contains(window.getFrame())
                    & !window.equals(mAppInfo.getAppWindow())) {
                mMultiWindow.relayoutWindow(mAppInfo.getAppWindow()
                        .getStackId(), resizeTo);
                mMultiWindow.relayoutWindow(window.getStackId(), appWindow);
            }
        }
    }

    public void show(int x, int y) {
        mLayoutParams.width = MENU_WIDTH;
        mLayoutParams.height = MENU_HEIGHT;
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        getWindow().setAttributes(mLayoutParams);
        show();
    }
}
