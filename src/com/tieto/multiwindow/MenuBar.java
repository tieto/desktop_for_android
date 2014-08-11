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

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

public class MenuBar extends Dialog {
    private WindowManager.LayoutParams mParameters;
    private ApplicationMenu mAppMenu;
    final static int HEIGHT = 150;

    public MenuBar(Context ctx, ApplicationMenu appMenu) {
        super(ctx, R.style.MenubarTheme);
        setContentView(R.layout.menu_bar);
        mAppMenu = appMenu;

        setFlags();
        resizeToFit();
        setStartButton();
        setCancelable(false);
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
        mParameters.height = HEIGHT;
    }

    private void setStartButton() {
        ImageButton startButton = (ImageButton) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAppMenu != null){
                    mAppMenu.show();
                }
            }
        });
    }
}
