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

import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tieto.extension.multiwindow.MultiwindowManager;
import com.tieto.extension.multiwindow.Window;

public class AppButton extends RelativeLayout {
    /**
     * mMinimized is responsible for marking the window is minimised(true) or
     * not(false).
     */
    private boolean mMinimized;
    /**
     * mAppInfo keeps the information about the application connected to button.
     */
    private AppInfo mAppInfo;
    /**
     * mMultiwindow manager is responsible for handling multiple windows on screen
     */
    private Context mContext;
    private MultiwindowManager mMultiwindowManager;
    private int mLeftShift;
    private int mTopShift;
    private int mMouseButton;

    public AppButton(Context context, AppInfo appInfo) {
        super(context);
        mContext = context;
        mAppInfo= appInfo;
        mMinimized = false;
        mLeftShift = getContext().getResources().getDisplayMetrics().widthPixels;
        mTopShift = getContext().getResources().getDisplayMetrics().heightPixels;
        mMultiwindowManager = new MultiwindowManager(context);
        initButton(appInfo,context);
    }

    private void initButton(AppInfo appInfo, Context context) {
        LayoutInflater li = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout buttonLayout = (RelativeLayout) li.inflate(R.layout.app_button,
                null, false);
        ImageView iv = (ImageView) buttonLayout.findViewById(R.id.buttonIcon);

        if (appInfo.getAppIcon() == null) {
            iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_shadow));
        } else {
            iv.setImageDrawable(appInfo.getAppIcon());
        }

        /**
         * This onTouch event is added just to obtain information about device
         * that performed Click or LongClick.
         */
        buttonLayout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mMouseButton = event.getButtonState();
                }
                return false;
            }
        });

        buttonLayout.setOnClickListener(new OnClickListener() {
            ButtonContextMenu mPopUp = new ButtonContextMenu(getContext(), mAppInfo);

            @Override
            public void onClick(View v) {
                if (mMouseButton == MotionEvent.BUTTON_SECONDARY) {
                    int[] location = new int[2];
                    v.getLocationInWindow(location);
                    mPopUp.show(location[0], (int) v.getY() + Desktop.MENUBAR_HEIGHT);
                } else {
                    if (isMinimized()) {
                        maximizeWindow();
                    } else {
                        minimizeWindow();
                    }
                }
            }
        });

        buttonLayout.setOnLongClickListener(new OnLongClickListener() {
            ButtonContextMenu mPopUp = new ButtonContextMenu(getContext(), mAppInfo);

            public boolean onLongClick(View v) {
                if (mMouseButton != MotionEvent.BUTTON_SECONDARY) {
                    int[] location = new int[2];
                    v.getLocationInWindow(location);
                    mPopUp.show(location[0], (int) v.getY() + Desktop.MENUBAR_HEIGHT);
                    return true;
                }
                return false;
            }
        });
        addView(buttonLayout);
    }

    /**
     * maximizeWindows method maximizes window from their actual state, first it
     * takes actual posistion of window on the screen and pushing it in to
     * before-minimalisation position and saves data of actual position in to appInfo.
     */
    public void maximizeWindow() {
        if (isMinimized()) {
            Vector<Window> actualWindows = mMultiwindowManager.getAllWindows();
            for (Window window : actualWindows) {
                if (window.getPackage().equals(
                        mAppInfo.getAppWindow().getPackage())) {
                    window.getFrame().offset(-mLeftShift, -mTopShift);
                    setMinimized(!mMultiwindowManager.relayoutWindow(
                            window.getStackId(), window.getFrame()));
                    mAppInfo = new AppInfo(getContext(), window);
                    break;
                }
            }
        }
    }

    public void minimizeWindow() {
        if (!isMinimized()) {
            Vector<Window> actualWindows = mMultiwindowManager.getAllWindows();
            for (Window window : actualWindows) {
                if (window.getPackage().equals(
                        mAppInfo.getAppWindow().getPackage())) {
                    window.getFrame().offsetTo(
                            window.getFrame().left + mLeftShift,
                            window.getFrame().top + mTopShift);
                    setMinimized(mMultiwindowManager.relayoutWindow(
                            window.getStackId(), window.getFrame()));
                    break;
                }
            }

        }
    }

    public AppInfo getAppInfo() {
        return mAppInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }

        AppButton appButton = (AppButton) o;

        if (appButton.getAppInfo().getAppWindow()
                .equals(this.getAppInfo().getAppWindow())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isMinimized() {
        return mMinimized;
    }

    private void setMinimized(boolean mMinimized) {
        this.mMinimized = mMinimized;
    }
}