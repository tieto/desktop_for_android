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

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class Desktop extends Activity {

    private View mDesktopView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop);

        mDesktopView = findViewById(R.id.desktop);
        mDesktopView.setOnDragListener(new OnDragListener() {
            private int mX, mY = 0;
            private LayoutParams mLayoutParams;

            @Override
            public boolean onDrag(View v, DragEvent event) {
                View desktopIcon = (View) event.getLocalState();
                switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    mLayoutParams = (LayoutParams) desktopIcon.getLayoutParams();
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    mX = (int) event.getX();
                    mY = (int) event.getY();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
                case DragEvent.ACTION_DROP:
                    mLayoutParams.leftMargin = mX - desktopIcon.getWidth() / 2;
                    mLayoutParams.topMargin = mY - desktopIcon.getHeight() / 2;
                    desktopIcon.setLayoutParams(mLayoutParams);
                    desktopIcon.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
                }
                return true;
            }
        });

        ApplicationMenu appMenu = new ApplicationMenu(this, new AddIconToDesktopListener() {

            @Override
            public void onAddIcon(View view, int x, int y, final String packageName) {
                LayoutParams lp = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                lp.leftMargin = x;
                lp.topMargin = y;

                LayoutInflater li = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout desktopIcon = (LinearLayout) li.inflate(
                        R.layout.desktop_icon, null);

                ((ImageView) desktopIcon.findViewById(R.id.desktop_icon)).setImageDrawable(
                        ((ImageView) view.findViewById(R.id.icon)).getDrawable());
                ((TextView) desktopIcon.findViewById(R.id.desktop_title)).setText(
                        ((TextView) view.findViewById(R.id.title)).getText());

                desktopIcon.setLayoutParams(lp);
                ((RelativeLayout) mDesktopView).addView(desktopIcon);

                desktopIcon.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent launchIntent = getBaseContext()
                                .getPackageManager()
                                .getLaunchIntentForPackage(packageName);
                        getBaseContext().startActivity(launchIntent);

                    }
                });

                desktopIcon.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        ClipData dragData = ClipData.newPlainText("", "");
                        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                        v.startDrag(dragData, shadowBuilder, v, 0);
                        v.setVisibility(View.INVISIBLE);
                        return true;
                    }
                });
            }
        });

        MenuBar menu = new MenuBar(this, appMenu);
        menu.show();
    }
}