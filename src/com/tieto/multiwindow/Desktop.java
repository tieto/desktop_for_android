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
import java.io.File;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Desktop extends Activity {

    public static final String sDesktopIcon = "DesktopIcon";
    public static final String sListViewMenuIcon = "ListViewMenuIcon";
    public static final String sMemDesktopIcons = "DesktopIcons";
    public static final String sMemWallpaperPath = "WallpaperPath";
    public static final String sMemAppsUsed = "AppsUsed";
    public static final String sMemAppsFav = "AppsFav";
    private ViewGroup mDesktopView;
    private MultiwindowManager mMultiwindowManager;
    private ApplicationMenu mAppMenu;
    public final boolean DRAG_DEBUG = false;
    private MenuBar mMenu;
    public final int SELECT_PICTURE = 1;
    private ArrayList<DesktopIcon> mDesktopIcons;
    private ArrayList<AppUsedCounter> mAppsUsed;
    private ArrayList<String> mAppsFav;
    private UserDataInterface mUserData;
    private String mWallpaperPath;
    private SharedPreferences mAppSharedPrefs;
    private Editor mPrefsEditor;
    private Gson mGson;
    private Rect mMaximizedSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop);
        mAppSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        mPrefsEditor = mAppSharedPrefs.edit();
        mGson = new Gson();
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mMaximizedSize = new Rect(0, getResources().getDimensionPixelSize(
                resourceId), metrics.widthPixels, metrics.heightPixels
                - MenuBar.HEIGHT);
        mMultiwindowManager = new MultiwindowManager(getBaseContext());
        mMultiwindowManager.setMaximizedWindowSize(mMaximizedSize);
        mDesktopView = (ViewGroup) findViewById(R.id.desktop);
        getWindow().getDecorView().setOnDragListener(new OnDragListener() {
            private int mX, mY = 0;
            private LayoutParams mLayoutParams;
            private final String TAG = "DESKTOP_DRAG_EVENT";
            private View view;
            private LinearLayout mDeleteBar;
            private LayoutParams mDeleteBarParams;
            private LayoutInflater mLayoutInflater = (LayoutInflater) getBaseContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            private boolean mIsInDeleteZone;

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

                    mDeleteBar = (LinearLayout) mLayoutInflater.inflate(R.layout.delete_icon_bar, mDesktopView, false);
                    ((ImageView) mDeleteBar.findViewById(R.id.delete_icon)).setImageResource(android.R.drawable.ic_delete);
                    mDeleteBarParams = new LayoutParams(mDeleteBar.getLayoutParams());
                    mDeleteBarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    mDesktopView.addView(mDeleteBar, mDeleteBarParams);
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
                    if (mX > mDesktopView.getWidth() - mDeleteBar.getWidth()) {
                        mDeleteBar.setBackgroundColor(getApplicationContext().getResources()
                                .getColor(R.color.black_full_opaque));
                        mIsInDeleteZone = true;
                    } else {
                        mDeleteBar.setBackgroundColor(getApplicationContext().getResources()
                                .getColor(R.color.black_half_opaque));
                        mIsInDeleteZone = false;
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_ENDED");
                    }
                    view.setAlpha(1.0f);
                    mDesktopView.removeView(mDeleteBar);
                    break;
                case DragEvent.ACTION_DROP:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DROP");
                    }
                    mX = (int) event.getX();
                    mY = (int) event.getY();
                    String dragItemSource = event.getClipData().getItemAt(0).getText().toString();
                    String packageName = event.getClipData().getItemAt(1).getText().toString();
                    if (dragItemSource.equals(sDesktopIcon)) {
                        if (mIsInDeleteZone) {
                            mDesktopView.removeView(view);
                            deleteFromIconList(packageName);
                        } else {
                            mLayoutParams.leftMargin = iconPosCorrection(mX, view.getWidth(), mDesktopView.getWidth());
                            mLayoutParams.topMargin = iconPosCorrection(mY, view.getHeight(), mDesktopView.getHeight());
                            mDesktopView.removeView(view);
                            mDesktopView.addView(view, mLayoutParams);
                            updateIconsList(packageName, mLayoutParams.leftMargin, mLayoutParams.topMargin);
                        }
                    }
                    if (dragItemSource.equals(sListViewMenuIcon)) {
                        if (iconExists(packageName)) {
                            Toast.makeText(getBaseContext(), R.string.icon_already_on_desktop,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            View handler = addIconToDesktop(mX, mY, packageName, true);
                            LayoutParams handlerParams = (LayoutParams) handler.getLayoutParams();
                            mDesktopIcons.add(new DesktopIcon(handlerParams.leftMargin,
                                                              handlerParams.topMargin,
                                                              packageName));
                        }
                        mAppMenu.dismiss();
                    }
                    break;
                }
                return true;
            }
        });

        loadIcons();
        loadWallpaper();
        loadFreqUsed();
        loadFavApps();

        mUserData = new UserDataInterface() {

            @Override
            public void incAppUsedCounter(String packageName) {
                boolean appFound = true;
                for (AppUsedCounter appUsed : mAppsUsed) {
                    if (appUsed.getPackage().equals(packageName)) {
                        appUsed.incCount();
                        appFound = false;
                        break;
                    }
                }
                if (appFound) {
                    AppUsedCounter appUsed = new AppUsedCounter(packageName);
                    appUsed.incCount();
                    mAppsUsed.add(appUsed);
                }
            }

            @Override
            public ArrayList<AppUsedCounter> getAppsUsedList() {
                return mAppsUsed;
            }

            @Override
            public ArrayList<String> getAppsFavList() {
                return mAppsFav;
            }
        };
        mAppMenu = new ApplicationMenu(this, mUserData);
        if (mMenu == null) {
            mMenu = new MenuBar(this, mAppMenu);
            mMenu.show();
        }

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
    protected void onResume() {
        mMultiwindowManager.setMaximizedWindowSize(mMaximizedSize);
        super.onResume();
    }

    @Override
    protected void onStop() {
        mMenu.maximizeMinimizedWindows();
        super.onStop();
        String json = mGson.toJson(mDesktopIcons);
        mPrefsEditor.putString(sMemDesktopIcons, json);
        mPrefsEditor.putString(sMemWallpaperPath, mWallpaperPath);
        mPrefsEditor.putString(sMemAppsUsed, mGson.toJson(mAppsUsed));
        mPrefsEditor.putString(sMemAppsFav, mGson.toJson(mAppsFav));
        mPrefsEditor.commit();
        mMultiwindowManager.setMaximizedWindowSize(new Rect());
    }

    @Override
    protected void onDestroy() {
        if (mMenu != null) {
            mMenu.dismiss();
            mMenu = null;
        }
        mMultiwindowManager.setMaximizedWindowSize(new Rect());
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                mPrefsEditor.putString(sMemWallpaperPath, getPath(data.getData()));
                mPrefsEditor.commit();
                loadWallpaper();
            }
        }
    }

    public View addIconToDesktop(int x, int y, final String packageName, boolean dropAction) {
        final String TAG = "DESKTOP_DRAG_EVENT";
        LayoutInflater li = (LayoutInflater) getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout desktopIcon = (RelativeLayout) li.inflate(R.layout.desktop_icon, mDesktopView, false);

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
            dropPosCorrection(desktopIcon, x, y, lp);
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
                mUserData.incAppUsedCounter(packageName);
            }
        });
        desktopIcon.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                ClipData.Item iconType = new ClipData.Item(sDesktopIcon);
                ClipData.Item packName = new ClipData.Item((CharSequence) packageName);
                String[] clipDescription = { ClipDescription.MIMETYPE_TEXT_PLAIN };
                ClipData dragData = new ClipData("", clipDescription, iconType);
                dragData.addItem(packName);

                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDrag(dragData, shadowBuilder, v, 0);

                return true;
            }
        });

        return desktopIcon;
    }

    private void loadIcons() {
        String json = mAppSharedPrefs.getString(sMemDesktopIcons, "");
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

    private void loadFreqUsed() {
        String json = mAppSharedPrefs.getString(sMemAppsUsed, "");
        Type type = new TypeToken<ArrayList<AppUsedCounter>>(){}.getType();
        if (mGson.fromJson(json, type) != null) {
            mAppsUsed = mGson.fromJson(json, type);
        } else {
            mAppsUsed = new ArrayList<AppUsedCounter>();
        }
    }

    private void loadFavApps() {
        String json = mAppSharedPrefs.getString(sMemAppsFav, "");
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        if (mGson.fromJson(json, type) != null) {
            mAppsFav = mGson.fromJson(json, type);
        } else {
            mAppsFav = new ArrayList<String>();
        }
    }

    private void loadWallpaper() {
        mWallpaperPath = mAppSharedPrefs.getString(sMemWallpaperPath, "");
        File file = new File(mWallpaperPath);
        if (file.exists()) {
            Bitmap imgBitmap = BitmapFactory.decodeFile(mWallpaperPath);
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            imgBitmap = Bitmap.createScaledBitmap(imgBitmap, size.x, size.y, false);
            mDesktopView.setBackground(new BitmapDrawable(imgBitmap));
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
                break;
            }
        }
    }

    private void deleteFromIconList(String packageName) {
        for (DesktopIcon desktopIcon : mDesktopIcons) {
            if (desktopIcon.getPackage().equals(packageName)) {
                mDesktopIcons.remove(desktopIcon);
                break;
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

    /**
     * This function is returning updated position of particular coordinate when icon is dragged
     * and dropped outside visible screen. It corrects icon position to fit visible screen size.
     *
     * @param coordinate x-coordinate or y-coordinate of drop location
     * @param viewSize width or height of dragged view
     * @param desktopSize width or height of visible screen
     * @return updated coordinate, that will fit to screen border
     */
    private int iconPosCorrection(int coordinate, int viewSize, int desktopSize) {
        int dropPosition = coordinate - viewSize/2;
        if (dropPosition < 0) {
            return 0;
        } else if (dropPosition + viewSize > desktopSize) {
            return desktopSize - viewSize;
        }
        return dropPosition;
    }

    /**
     * This function is applying updated position of icon during drop from ApplicationMenu to Desktop
     * when drop is outside visible screen. It corrects icon position to fit visible screen size.
     *
     * @param desktopIcon view to be dropped
     * @param x x-coordinate of drop
     * @param y y-coordinate of drop
     * @param lp layout parameters of desktopIcon
     */
    private void dropPosCorrection(View desktopIcon, int x, int y, LayoutParams lp) {
        int desktopIconWidth = desktopIcon.findViewById(R.id.desktop_icon).getLayoutParams().width;
        int desktopIconHeigth = desktopIcon.findViewById(R.id.desktop_icon).getLayoutParams().height;
        int dropPositionX = x - desktopIconWidth / 2;
        int dropPositionY = y - desktopIconHeigth / 2;

        if (dropPositionX < 0) {
            lp.leftMargin = 0;
        } else if (dropPositionX + desktopIconWidth > mDesktopView.getWidth()) {
            lp.leftMargin = mDesktopView.getWidth() - desktopIconWidth;
        } else {
            lp.leftMargin = dropPositionX;
        }

        if (dropPositionY < 0) {
            lp.topMargin = 0;
        } else if (dropPositionY + desktopIconHeigth > mDesktopView.getHeight()) {
            lp.topMargin = mDesktopView.getHeight() - desktopIconHeigth;
        } else {
            lp.topMargin = dropPositionY;
        }
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