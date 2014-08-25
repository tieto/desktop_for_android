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
import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.AdapterView;

public class ApplicationMenu extends ListViewMenu {

    private final int numberOfRows = 5;
    public static final String sFreq = "freq";

    private Context mContext;
    private UserDataInterface mUserData;

    public ApplicationMenu(Context context, UserDataInterface userData) {
        super(context, R.style.ApplicationMenuTheme, userData);
        mContext = context;
        mUserData = userData;

        ArrayList<ListViewMenuItem> listViewItems = fillListViewItems();
        listViewItems.add(0, addRowToListViewItems(sFreq, mContext.getResources().getString(R.string.freq_menu_title)));
        fillListViewMenu(listViewItems);
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
        } else {
            super.onMenuItemClick(parent, position);
        }
    }

    @Override
    protected boolean onMenuItemLongClick(AdapterView<?> parent, View view, int position) {
        String packageName = ((ListViewMenuItem) parent.getAdapter()
                .getItem(position)).getPackageName();
        if (!packageName.equals(sFreq)) {
            return super.onMenuItemLongClick(parent, view, position);
        }
        return true;
    }
}
