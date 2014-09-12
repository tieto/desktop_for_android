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
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class RightClickContextMenu extends Dialog {
    private Context mContext;

    private LinearLayout mMainLayout;
    private final int PADDING_SIZE = 25;

    public RightClickContextMenu(Context context, int x, int y) {
        super(context, R.style.popupMenu);
        mContext = context;
        initWindow(x, y);

        mMainLayout = (LinearLayout) findViewById(R.id.rightClickPopup);
    }

    private void initWindow(int x, int y) {
        setContentView(R.layout.right_click_popup);
        getWindow().setType(LayoutParams.TYPE_PRIORITY_PHONE);
        getWindow().clearFlags(LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setGravity(Gravity.TOP | Gravity.LEFT);
        getWindow().getAttributes().height = LayoutParams.WRAP_CONTENT;
        getWindow().getAttributes().width = LayoutParams.WRAP_CONTENT;
        getWindow().getAttributes().x = x;
        getWindow().getAttributes().y = y;
    }

    public void addButton (String title, View.OnClickListener listener) {
        Button button = new Button(mContext, null, R.style.functional_button);
        button.setText(title);
        button.setPadding(PADDING_SIZE, PADDING_SIZE, PADDING_SIZE, PADDING_SIZE);
        button.setOnClickListener(listener);
        mMainLayout.addView(button);
    }
}
