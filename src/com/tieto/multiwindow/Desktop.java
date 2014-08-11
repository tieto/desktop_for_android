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

import com.tieto.extension.multiwindow.MultiwindowManager;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class Desktop extends Activity {

    private ViewGroup mDesktopView;
    private MultiwindowManager mMultiwindowManager;
    private ApplicationMenu mAppMenu;
    public final boolean DRAG_DEBUG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop);
        mMultiwindowManager = new MultiwindowManager(getBaseContext());
        mDesktopView = (ViewGroup) findViewById(R.id.desktop);
        getWindow().getDecorView().setOnDragListener(new OnDragListener() {
            private int mX, mY = 0;
            private LayoutParams mLayoutParams;
            private final String TAG = "DESKTOP_DRAG_EVENT";
            private View view;

            @Override
            public boolean onDrag(View v, DragEvent event) {
                view = (View) event.getLocalState();
                switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_STARTED");
                    }
                    mLayoutParams = (LayoutParams) view.getLayoutParams();
                    view.setAlpha(0.3f);
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_ENTERED");
                    }
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_EXITED");
                    }
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_LOCATION");
                    }
                    mX = (int) event.getX();
                    mY = (int) event.getY();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_ENDED");
                    }
                    view.setAlpha(1.0f);
                    break;
                case DragEvent.ACTION_DROP:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DROP");
                    }
                    mX = (int) event.getX();
                    mY = (int) event.getY();
                    String dragItemSource = event.getClipData().getItemAt(0).getText().toString();
                    if (dragItemSource.equals("DesktopIcon")) {
                        mLayoutParams.leftMargin = mX - view.getWidth() / 2;
                        mLayoutParams.topMargin = mY - view.getHeight() / 2;
                        view.setLayoutParams(mLayoutParams);
                    }
                    if (dragItemSource.equals("AppMenuIcon")) {
                        String packageName = event.getClipData().getItemAt(1).getText().toString();
                        addIconToDesktop(mX, mY, packageName);
                        mAppMenu.dismiss();
                    }
                    break;
                default:
                    break;
                }
                return true;
            }
        });

        mAppMenu = new ApplicationMenu(this);
        MenuBar menu = new MenuBar(this, mAppMenu);
        menu.show();
    }

    public void addIconToDesktop(int x, int y, final String packageName) {
        final String TAG = "DESKTOP_DRAG_EVENT";
        LayoutInflater li = (LayoutInflater) getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout desktopIcon = (LinearLayout) li.inflate(R.layout.desktop_icon, mDesktopView, false);

        try {
            ApplicationInfo app = getPackageManager().getApplicationInfo(packageName, 0);
            Drawable icon = getPackageManager().getApplicationIcon(app);
            String name = getPackageManager().getApplicationLabel(app).toString();
            ((ImageView) desktopIcon.findViewById(R.id.desktop_icon)).setImageDrawable(icon);
            ((TextView) desktopIcon.findViewById(R.id.desktop_title)).setText(name);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e);
        }

        LayoutParams lp = new LayoutParams(desktopIcon.getLayoutParams());
        lp.leftMargin = x - desktopIcon.findViewById(R.id.desktop_icon).getLayoutParams().width / 2;
        lp.topMargin = y - desktopIcon.findViewById(R.id.desktop_icon).getLayoutParams().height / 2;
        desktopIcon.setLayoutParams(lp);
        mDesktopView.addView(desktopIcon);

        desktopIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent launchIntent = getBaseContext()
                        .getPackageManager()
                        .getLaunchIntentForPackage(packageName);
                mMultiwindowManager.startActivity(launchIntent);

            }
        });
        desktopIcon.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                ClipData.Item iconType = new ClipData.Item("DesktopIcon");
                String[] clipDescription = { ClipDescription.MIMETYPE_TEXT_PLAIN };
                ClipData dragData = new ClipData("", clipDescription, iconType);
                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDrag(dragData, shadowBuilder, v, 0);
                return true;
            }
        });
    }
}