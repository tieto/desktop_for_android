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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class ApplicationMenu extends ListViewMenu {

    private final int numberOfRows = 5;
    public static final String sFreq = "freq";
    public final static String sFav = "fav";

    private Context mContext;
    private UserDataInterface mUserData;

    public ApplicationMenu(Context context, UserDataInterface userData) {
        super(context, R.style.ApplicationMenuTheme, userData);
        mContext = context;
        mUserData = userData;

        ArrayList<ListViewMenuItem> listViewItems = fillListViewItems();
        listViewItems.add(0, addRowToListViewItems(sFav, mContext.getResources().getString(R.string.fav_menu_title)));
        listViewItems.add(1, addRowToListViewItems(sFreq, mContext.getResources().getString(R.string.freq_menu_title)));
        fillListViewMenu( listViewItems);

        ListView listViewMenu = getListViewMenu();
        listViewMenu.setOnDragListener(new OnDragListener() {

            private View mFavRow;
            private boolean mDropOnFav = false;
            private Resources mResources = mContext.getResources();
            private PackageManager mPackerManager = mContext
                    .getPackageManager();

            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    int itemsCount = ((ListView) v).getCount();
                    for (int i = 0; i< itemsCount; i++) {
                        ListViewMenuItem item = (ListViewMenuItem) ((ListView) v).getItemAtPosition(i);
                        if (item.getPackageName().equals(sFav)) {
                            mFavRow = ((ListView) v).getChildAt(i);
                            break;
                        }
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    mFavRow.setBackground(null);
                    mDropOnFav = false;
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    int position = ((ListView) v).pointToPosition((int) event.getX(), (int) event.getY());
                    if (position != -1) {
                        ListViewMenuItem item = (ListViewMenuItem) ((ListView) v).getItemAtPosition(position);
                        if (item.getPackageName().equals(sFav)) {
                            mFavRow.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_blue_dark));
                            mDropOnFav = true;
                        } else {
                            mFavRow.setBackground(null);
                            mDropOnFav = false;
                        }
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    mFavRow.setBackground(null);
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

    private ListViewMenuItem addRowToListViewItems(String id, String title) {
        return new ListViewMenuItem(
                mContext.getResources().getDrawable(android.R.drawable.ic_menu_send), title, id);
    }

    @Override
    protected void onMenuItemClick(AdapterView<?> parent, int position) {
        String packageName = ((ListViewMenuItem) parent.getAdapter()
                .getItem(position)).getPackageName();
        if (packageName.equals(sFreq)) {
            FreqMenu freqMenu = new FreqMenu(mContext, mUserData, numberOfRows);
            freqMenu.setOnAppListener(getOnAppListener());
            freqMenu.show();
            dismiss();
        } else if (packageName.equals(sFav)) {
            FavMenu mFavMenu = new FavMenu(mContext, mUserData);
            mFavMenu.setOnAppListener(getOnAppListener());
            mFavMenu.show();
            dismiss();
        } else {
            super.onMenuItemClick(parent, position);
        }
    }

    @Override
    protected boolean onMenuItemLongClick(AdapterView<?> parent, View view, int position) {
        String packageName = ((ListViewMenuItem) parent.getAdapter()
                .getItem(position)).getPackageName();
        if (!packageName.equals(sFreq) && !packageName.equals(sFav)) {
            return super.onMenuItemLongClick(parent, view, position);
        }
        return true;
    }
}
