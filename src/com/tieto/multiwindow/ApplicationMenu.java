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

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class ApplicationMenu extends ListViewMenu {

    private final int mDragShadowSize = 150;
    private final PackageManager mPackerManager;

    public ApplicationMenu(final Context context) {
        super(context, R.style.ApplicationMenuTheme);

        mPackerManager = getContext().getPackageManager();
        List<ResolveInfo> mAppInfo = mPackerManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null)
                        .addCategory(Intent.CATEGORY_LAUNCHER), 0);

        ArrayList<ListViewMenuItem> listViewItems = new ArrayList<ListViewMenuItem>();
        for (ResolveInfo singleAppInfo : mAppInfo) {
            ApplicationInfo ai = singleAppInfo.activityInfo.applicationInfo;
            listViewItems.add(new ListViewMenuItem(
                    mPackerManager.getApplicationIcon(ai),
                    mPackerManager.getApplicationLabel(ai).toString(),
                    ai.packageName));
        }
        fillListViewMenu(listViewItems);

        ListView listView = getListViewMenu();
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                    int position, long id) {
                ListViewMenuItem menuItem = (ListViewMenuItem) parent.getAdapter().getItem(position);
                getOnAppListener().actionPerformed(mPackerManager
                        .getLaunchIntentForPackage(menuItem.getPackageName()));
                dismiss();
            }
        });
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ListViewMenuItem menuItem = (ListViewMenuItem) parent.getAdapter().getItem(position);
                ClipData.Item iconType = new ClipData.Item("AppMenuIcon");
                ClipData.Item packageName = new ClipData.Item(menuItem.getPackageName());
                String[] clipDescription = { ClipDescription.MIMETYPE_TEXT_PLAIN };
                ClipData dragData = new ClipData("", clipDescription, iconType);
                dragData.addItem(packageName);

                ImageView dragIcon = (ImageView) view.findViewById(R.id.icon);
                AppMenuDragShadowBuilder shadowBuilder = new AppMenuDragShadowBuilder(dragIcon, mDragShadowSize, mDragShadowSize);
                view.startDrag(dragData, shadowBuilder, view, 0);

                return true;
            }
        });
    }
}
