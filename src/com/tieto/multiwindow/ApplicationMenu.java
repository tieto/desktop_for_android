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

public class ApplicationMenu extends ListViewMenu {

    private Context mContext;

    public ApplicationMenu(Context context) {
        super(context, R.style.ApplicationMenuTheme);
        mContext = context;

        ArrayList<ListViewMenuItem> listViewItems = fillListViewItems();
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
}
