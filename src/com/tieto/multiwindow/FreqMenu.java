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
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.widget.AdapterView;

public class FreqMenu extends ListViewMenu {

    private Context mContext;
    private UserDataInterface mUserData;

    public FreqMenu(Context context, UserDataInterface userData, int numberOfRows) {
        super(context, R.style.AppMenuWithTitleTheme, userData);
        mContext = context;
        mUserData = userData;

        ArrayList<ListViewMenuItem> listViewItems = fillListViewItems(numberOfRows);
        fillListViewMenu(listViewItems);
        this.setTitle(mContext.getResources().getString(R.string.freq_menu_description));
    }

    private ArrayList<ListViewMenuItem> fillListViewItems(int numberOfRows) {
        ArrayList<ListViewMenuItem> listViewItems = new ArrayList<ListViewMenuItem>();
        int count = 0;
        sortAppsUsedList(mUserData.getAppsUsedList());
        for (AppUsedCounter appUsed : mUserData.getAppsUsedList()) {
            try {
                ApplicationInfo ai = mContext.getPackageManager()
                        .getApplicationInfo(appUsed.getPackage(), 0);
                listViewItems.add(createListViewMenuItem(ai));
                count++;
            } catch (Exception e) {
                mUserData.getAppsUsedList().remove(appUsed);
            }
            if (count >= numberOfRows) {
                break;
            }
        }
        return listViewItems;
    }

    private void sortAppsUsedList(ArrayList<AppUsedCounter> appsUsedList) {
        Collections.sort(appsUsedList, new Comparator<AppUsedCounter>() {

            @Override
            public int compare(AppUsedCounter lhs, AppUsedCounter rhs) {
                return rhs.getCount() - lhs.getCount();
            }
        });
    }

    @Override
    protected boolean onMenuItemLongClick(AdapterView<?> parent, View view, int position) {
        return true;
    }
}
