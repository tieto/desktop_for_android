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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class ApplicationMenu extends ListViewMenu {

    private final int numberOfRows = 5;
    public final static String sFreq = "freq";
    public final static String sFav = "fav";
    private final int FREQ_POSITION = 1;
    private final int FAV_POSITION = 0;
    private final boolean SCROLL_DEBUG = false;

    private Context mContext;
    private UserDataInterface mUserData;

    private FreqMenu mFreqMenu;
    private FavMenu mFavMenu;

    public ApplicationMenu(Context context, UserDataInterface userData) {
        super(context, R.style.ApplicationMenuTheme, userData);
        mContext = context;
        mUserData = userData;

        updateListViewMenu();
        setBaseMenuWindowParams();

        mFreqMenu = new FreqMenu(mContext, mUserData, this, numberOfRows);
        mFreqMenu.setOnAppListener(getOnAppListener());
        mFavMenu = new FavMenu(mContext, mUserData, this);
        mFavMenu.setOnAppListener(getOnAppListener());

        ListView listViewMenu = getListViewMenu();
        listViewMenu.setOnDragListener(new OnDragListener() {

            private int mFavRowPos;
            private boolean mDropOnFav = false;
            private Resources mResources = mContext.getResources();
            private PackageManager mPackerManager = mContext
                    .getPackageManager();
            private ListView mListView;
            private int mXPosition;
            private int mYPosition;
            private final int NO_COLOR = 0x0;
            private final int LAST_SCROLL_NOSCROLL = 0;
            private final int LAST_SCROLL_UP = 1;
            private final int LAST_SCROLL_DOWN = 2;
            private int mLastScrollAction;

            @Override
            public boolean onDrag(View v, DragEvent event) {
                mListView = (ListView) v;
                switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    int itemsCount = mListView.getCount();
                    for (int i = 0; i< itemsCount; i++) {
                        ListViewMenuItem item = (ListViewMenuItem) mListView.getItemAtPosition(i);
                        if (item.getPackageName().equals(sFav)) {
                            mFavRowPos = i;
                            break;
                        }
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    hoverItemBackground(mFavRowPos, NO_COLOR);
                    mDropOnFav = false;
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    mXPosition = (int) event.getX();
                    mYPosition = (int) event.getY();
                    int position = mListView.pointToPosition(mXPosition, mYPosition);
                    dragScrollByItemPos(position);
                    if (position != -1) {
                        ListViewMenuItem item = (ListViewMenuItem) mListView.getItemAtPosition(position);
                        if (item.getPackageName().equals(sFav)) {
                            hoverItemBackground(mFavRowPos, mResources.getColor(android.R.color.holo_blue_dark));
                            mDropOnFav = true;
                        } else {
                            hoverItemBackground(mFavRowPos, NO_COLOR);
                            mDropOnFav = false;
                        }
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    hoverItemBackground(mFavRowPos, NO_COLOR);
                    break;
                case DragEvent.ACTION_DROP:
                    if (mDropOnFav) {
                        boolean appOnFavList = false;
                        String packageName = event.getClipData().getItemAt(1).getText().toString();
                        for (String appUsed : mUserData.getAppsFavList()) {
                            if (appUsed.equals(packageName)) {
                                appOnFavList = true;
                                break;
                            }
                        }

                        if (appOnFavList) {
                            Toast.makeText(mContext, mResources.getString(R.string.app_already_on_fav_list),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                ApplicationInfo ai = mPackerManager.getApplicationInfo(packageName, 0);
                                String appName = mPackerManager.getApplicationLabel(ai).toString();
                                mUserData.getAppsFavList().add(packageName);
                                Toast.makeText(mContext, String.format(mResources.getString(
                                        R.string.added_app_to_fav_list), appName),
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(mContext, mResources.getString(R.string.failed_to_add_to_fav_list),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    break;
                }
                return true;
            }

            private void hoverItemBackground (int position, int color) {
                if (position >= mListView.getFirstVisiblePosition()
                        && position <= mListView.getLastVisiblePosition()) {
                    View item = mListView.getChildAt(position);
                    item.setBackgroundColor(color);
                }
            }

            /**
             * TODO separate scrolling during drag from ACTION_DRAG_LOCATION event
             */
            private void dragScrollByItemPos(int position) {
                int firstVisibleItemPosition = mListView.getFirstVisiblePosition();
                int lastVisibleItemPosition = mListView.getLastVisiblePosition();

                if ((position == firstVisibleItemPosition
                        || position == firstVisibleItemPosition + 1)
                        && position != 0) {
                    mListView.smoothScrollToPosition(
                                    firstVisibleItemPosition - 1);
                            mLastScrollAction = LAST_SCROLL_UP;
                } else if ((position == lastVisibleItemPosition
                        || position == lastVisibleItemPosition - 1)
                        && position < mListView.getCount()) {
                    mListView.smoothScrollToPosition(
                                    lastVisibleItemPosition + 1);
                            mLastScrollAction = LAST_SCROLL_DOWN;
                } else if (position == -1) {
                    if (SCROLL_DEBUG) {
                        Log.d("ScrollDrag", "No position (separate line)");
                    }
                    if (mLastScrollAction == LAST_SCROLL_UP) {
                        if (SCROLL_DEBUG) {
                            Log.d("ScrollDrag", "Last time scroll UP");
                        }
                        if (firstVisibleItemPosition > 0) {
                            mListView.smoothScrollToPosition(
                                    firstVisibleItemPosition - 1);
                        } else {
                            mListView.smoothScrollToPosition(
                                    firstVisibleItemPosition);
                        }
                    } else if (mLastScrollAction == LAST_SCROLL_DOWN) {
                        if (SCROLL_DEBUG) {
                            Log.d("ScrollDrag", "Last time scroll DOWN)");
                        }
                        if (lastVisibleItemPosition < mListView.getCount()) {
                            mListView.smoothScrollToPosition(
                                    lastVisibleItemPosition + 1);
                        } else {
                            mListView.smoothScrollToPosition(
                                    lastVisibleItemPosition);
                        }
                    } else {
                        if (SCROLL_DEBUG) {
                            Log.d("ScrollDrag", "No recent scroll");
                        }
                        mLastScrollAction = LAST_SCROLL_NOSCROLL;
                    }
                } else {
                    if (SCROLL_DEBUG) {
                        Log.d("ScrollDrag", "Out of scrolling area");
                    }
                    mLastScrollAction = LAST_SCROLL_NOSCROLL;
                }
            }

            /**
             * TODO separate scrolling during drag from ACTION_DRAG_LOCATION event
             */
            private void dragScrollByTouchPos() {
                int listViewHeight = mListView.getHeight();
                int topSlowScroll = (int) (0.3 * listViewHeight);
                int botSlowScroll = (int) (0.7 * listViewHeight);
                int topFastScroll = (int) (0.15 * listViewHeight);
                int botFastScroll = (int) (0.85 * listViewHeight);
                int ratio = 10;
                int duration = 100;

                if (mYPosition < topSlowScroll) {
                    if (mYPosition < topFastScroll) {
                        mListView.smoothScrollBy(-ratio * 4, duration);
                    } else {
                        mListView.smoothScrollBy(-ratio, duration);
                    }
                } else if (mYPosition > botSlowScroll) {
                    if (mYPosition > botFastScroll) {
                        mListView.smoothScrollBy(ratio * 4, duration);
                    } else {
                        mListView.smoothScrollBy(ratio, duration);
                    }
                }
            }
        });
    }

    private ArrayList<ListViewMenuItem> fillListViewItems() {
        ArrayList<ListViewMenuItem> listViewItems = new ArrayList<ListViewMenuItem>();
        List<ResolveInfo> resolveInfoList = mContext.getPackageManager()
                .queryIntentActivities(new Intent(Intent.ACTION_MAIN, null)
                        .addCategory(Intent.CATEGORY_LAUNCHER), 0);
        for (ResolveInfo resolveInfo : resolveInfoList) {
            ApplicationInfo ai = resolveInfo.activityInfo.applicationInfo;
            listViewItems.add(createListViewMenuItem(ai));
        }
        return listViewItems;
    }

    @Override
    protected void onMenuItemClick(AdapterView<?> parent, int position) {
        final String packageName = ((ListViewMenuItem) parent.getAdapter()
                .getItem(position)).getPackageName();
        boolean isRightClick = (mMouseButton == MotionEvent.BUTTON_SECONDARY);
        if (isRightClick) {
            if (!packageName.equals(sFreq) && !packageName.equals(sFav)) {
                final RightClickContextMenu contextMenu = new RightClickContextMenu(
                        mContext, mMouseClickX, mMouseClickY);
                contextMenu.addButton(mContext.getResources().getString(
                        R.string.add_icon_to_favorites),
                        new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        boolean appOnFavList = false;
                        for (String appUsed : mUserData.getAppsFavList()) {
                            if (appUsed.equals(packageName)) {
                                appOnFavList = true;
                                break;
                            }
                        }
                        if (appOnFavList) {
                            Toast.makeText(mContext, mContext.getResources().getString(
                                    R.string.app_already_on_fav_list),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mUserData.getAppsFavList().add(packageName);
                        }
                        contextMenu.dismiss();
                    }
                });
                contextMenu.show();
            }
        } else if (packageName.equals(sFreq)) {
            mFreqMenu.updateListViewMenu();
            mFreqMenu.show(getYOffsetAtPosition(FREQ_POSITION));
        } else if (packageName.equals(sFav)) {
            mFavMenu.updateListViewMenu();
            mFavMenu.show(getYOffsetAtPosition(FAV_POSITION));
        } else {
            super.onMenuItemClick(parent, position);
        }
    }

    @Override
    protected boolean onMenuItemLongClick(AdapterView<?> parent, View view, int position) {
        if (mMouseButton != MotionEvent.BUTTON_SECONDARY) {
            String packageName = ((ListViewMenuItem) parent.getAdapter()
                    .getItem(position)).getPackageName();
            if (!packageName.equals(sFreq) && !packageName.equals(sFav)) {
                return super.onMenuItemLongClick(parent, view, position);
            }
        }
        return true;
    }

    public void updateListViewMenu() {
        setListViewMenuItems(fillListViewItems());
        getListViewMenuItems().add(FAV_POSITION, addRowToListViewItems(sFav,
                mContext.getResources().getString(R.string.fav_menu_title)));
        getListViewMenuItems().add(FREQ_POSITION, addRowToListViewItems(sFreq,
                mContext.getResources().getString(R.string.freq_menu_title)));
        fillListViewMenu(getListViewMenuItems());
    }
}
