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
import java.lang.reflect.Type;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Desktop extends Activity {

    private ViewGroup mDesktopView;
    private MultiwindowManager mMultiwindowManager;
    private ApplicationMenu mAppMenu;
    public final boolean DRAG_DEBUG = false;
    private MenuBar mMenu;
    public final int SELECT_PICTURE = 1;
    private ArrayList<DesktopIcon> mDesktopIcons;
    private String mWallpaperPath;
    private SharedPreferences mAppSharedPrefs;
    private Editor mPrefsEditor;
    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop);
        mAppSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        mPrefsEditor = mAppSharedPrefs.edit();
        mGson = new Gson();
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
                    String packageName = event.getClipData().getItemAt(1).getText().toString();
                    if (dragItemSource.equals("DesktopIcon")) {
                        mLayoutParams.leftMargin = mX - view.getWidth() / 2;
                        mLayoutParams.topMargin = mY - view.getHeight() / 2;
                        view.setLayoutParams(mLayoutParams);
                        updateIconsList(packageName, mX - view.getWidth() / 2, mY - view.getHeight() / 2);
                    }
                    if (dragItemSource.equals("AppMenuIcon")) {
                        if (iconExists(packageName)) {
                            Toast.makeText(getBaseContext(), R.string.icon_already_on_desktop,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            addIconToDesktop(mX, mY, packageName, true);
                            mDesktopIcons.add(new DesktopIcon(mX, mY, packageName));
                        }
                        mAppMenu.dismiss();
                    }
                    break;
                }
                return true;
            }
        });

        mAppMenu = new ApplicationMenu(this);
        mMenu = new MenuBar(this, mAppMenu);
        mMenu.show();
        loadIcons();
        loadWallpaper();

        findViewById(R.id.browseGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_PICTURE);
            }
        });
    }

    @Override
    protected void onStop() {
        mMenu.maximizeMinimizedWindows();
        super.onStop();
        String json = mGson.toJson(mDesktopIcons);
        mPrefsEditor.putString("DesktopIcons", json);
        mPrefsEditor.putString("WallpaperPath", mWallpaperPath);
        mPrefsEditor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                mWallpaperPath = getPath(data.getData());
                Drawable imgDrawable = Drawable.createFromPath(mWallpaperPath);
                mDesktopView.setBackground(imgDrawable);
            }
        }
    }

    public void addIconToDesktop(int x, int y, final String packageName, boolean dropAction) {
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
        lp.leftMargin = x;
        lp.topMargin = y;
        if (dropAction) {
            lp.leftMargin -= desktopIcon.findViewById(R.id.desktop_icon).getLayoutParams().width / 2;
            lp.topMargin -= desktopIcon.findViewById(R.id.desktop_icon).getLayoutParams().height / 2;
        }
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
                ClipData.Item packName = new ClipData.Item((CharSequence) packageName);
                String[] clipDescription = { ClipDescription.MIMETYPE_TEXT_PLAIN };
                ClipData dragData = new ClipData("", clipDescription, iconType);
                dragData.addItem(packName);

                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDrag(dragData, shadowBuilder, v, 0);

                return true;
            }
        });
    }

    private void loadIcons() {
        String json = mAppSharedPrefs.getString("DesktopIcons", "");
        Type type = new TypeToken<ArrayList<DesktopIcon>>(){}.getType();
        if (mGson.fromJson(json, type) != null) {
            mDesktopIcons = mGson.fromJson(json, type);
            for (DesktopIcon desktopIcon : mDesktopIcons) {
                addIconToDesktop(desktopIcon.getX(), desktopIcon.getY(), desktopIcon.getPackage(), false);
            }
        } else {
            mDesktopIcons = new ArrayList<DesktopIcon>();
        }
    }

    private void loadWallpaper() {
        mWallpaperPath = mAppSharedPrefs.getString("WallpaperPath", "");
        if (mWallpaperPath != null) {
            Drawable imgDrawable = Drawable.createFromPath(mWallpaperPath);
            mDesktopView.setBackground(imgDrawable);
        }
    }

    private String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    private void updateIconsList(String packageName, int newX, int newY) {
        for (DesktopIcon desktopIcon : mDesktopIcons) {
            if (desktopIcon.getPackage().equals(packageName)) {
                desktopIcon.setX(newX);
                desktopIcon.setY(newY);
            }
        }
    }

    private boolean iconExists(String packageName) {
        for (DesktopIcon desktopIcon : mDesktopIcons) {
            if (desktopIcon.getPackage().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private class DesktopIcon {
        private int mX;
        private int mY;
        private String mPackage;

        public DesktopIcon(int mX, int mY, String mPackage) {
            this.mX = mX;
            this.mY = mY;
            this.mPackage = mPackage;
        }

        public int getX() {
            return mX;
        }

        public void setX(int mX) {
            this.mX = mX;
        }

        public int getY() {
            return mY;
        }

        public void setY(int mY) {
            this.mY = mY;
        }

        public String getPackage() {
            return mPackage;
        }

        public void setPackage(String mPackage) {
            this.mPackage = mPackage;
        }
    }
}