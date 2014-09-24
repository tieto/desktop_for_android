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
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.tieto.extension.multiwindow.MultiwindowManager;
import com.tieto.extension.multiwindow.OnWindowChangeListener;
import com.tieto.extension.multiwindow.Window;

public class MenuBar extends Dialog {
    private WindowManager.LayoutParams mParameters;
    private ApplicationMenu mAppMenu;
    private LinearLayout mLayout;
    private Context mContext;

    private MultiwindowManager mMultiwindowManager;
    private OptionsMenu mOptionsMenu;

    public MenuBar(Context ctx, ApplicationMenu appMenu, OptionsMenu optionsMenu) {
        super(ctx, R.style.MenubarTheme);
        setContentView(R.layout.menu_bar);
        mOptionsMenu = optionsMenu;
        mAppMenu = appMenu;
        mLayout = ((LinearLayout) findViewById(R.id.bottomBar));
        mContext = ctx;
        mMultiwindowManager = new MultiwindowManager(ctx);
        mMultiwindowManager
                .setOnWindowChangeListener(new OnWindowChangeListener() {
                    @Override
                    public void onWindowAdd(final Window window) {
                        addButton(window);
                    }

                    @Override
                    public void onWindowRemoved(final Window window) {
                        removeButton(window);
                    }
                });
        setFlags();
        resizeToFit();
        setStartButton();
        setOptionsButton();
        initBar();
        setCancelable(false);
    }

    private void setOptionsButton() {

        findViewById(R.id.optionsButton).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mOptionsMenu.show(0, (int) v.getY() + Desktop.MENUBAR_HEIGHT);
                    }
                });
    }

    private void setFlags() {
        mParameters = getWindow().getAttributes();
        getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
    }

    private void resizeToFit() {
        mParameters.gravity = Gravity.BOTTOM | Gravity.LEFT;
        mParameters.width = WindowManager.LayoutParams.MATCH_PARENT;
        mParameters.height = Desktop.MENUBAR_HEIGHT;
    }

    public void addButton(final Window window) {
        mLayout.post(new Runnable() {
            @Override
            public void run() {
                AppInfo appInfo = new AppInfo(mContext, window);
                addButtonToView(appInfo);
            }
        });
    }

    public void removeButton(Window window) {
        for (int i = 0; i< mLayout.getChildCount(); i++) {
            if (mLayout.getChildAt(i) instanceof AppButton) {
                final AppButton appButton = (AppButton) mLayout.getChildAt(i);
                if (appButton.getAppInfo().getAppWindow().equals(window)) {
                    mLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            mLayout.removeView(appButton);
                        }
                    });
                    return;
                }
            }
        }
    }

    private void initBar() {
        for (Window window : mMultiwindowManager.getAllWindows()) {
            AppInfo appInfo = new AppInfo(mContext, window);
            addButtonToView(appInfo);
        }
    }

    private void setStartButton() {
        ImageButton startButton = (ImageButton) findViewById(R.id.startButton);
        mAppMenu.setOnAppListener(new OnAppStartListener() {
            @Override
            public void actionPerformed(Intent intent) {
                mMultiwindowManager.startActivity(intent);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAppMenu != null){
                    mAppMenu.show();
                }
            }
        });
    }

    private void addButtonToView(AppInfo appInfo) {
        AppButton appButton = new AppButton(mContext, appInfo);
        mLayout.addView(appButton);
    }

    public void maximizeMinimizedWindows() {
        for (int i = 0; i< mLayout.getChildCount(); i++) {
            if (mLayout.getChildAt(i) instanceof AppButton) {
                AppButton appButton = (AppButton) mLayout.getChildAt(i);
                if (appButton.isMinimized()) {
                    appButton.maximizeWindow();
                }
            }
        }
    }
}
