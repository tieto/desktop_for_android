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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

public class FavMenu extends ListViewMenu {

    private Context mContext;
    private UserDataInterface mUserData;

    public FavMenu(Context context, UserDataInterface userData) {
        super(context, R.style.AppMenuWithTitleTheme, userData);
        mContext = context;
        mUserData = userData;

        ArrayList<ListViewMenuItem> listViewItems = fillListViewItems();
        fillListViewMenu(listViewItems);
        this.setTitle(mContext.getResources().getString(R.string.fav_menu_description));
    }

    private ArrayList<ListViewMenuItem> fillListViewItems() {
        ArrayList<ListViewMenuItem> listViewItems = new ArrayList<ListViewMenuItem>();
        for (String appFav : mUserData.getAppsFavList()) {
            try {
                ApplicationInfo ai = mContext.getPackageManager()
                        .getApplicationInfo(appFav, 0);
                listViewItems.add(createListViewMenuItem(ai));
            } catch (Exception e) {
                mUserData.getAppsFavList().remove(appFav);
            }
        }
        return listViewItems;
    }

    @Override
    protected void onMenuItemClick(final AdapterView<?> parent, final int position) {
        if (mMouseButton == MotionEvent.BUTTON_SECONDARY) {
            final RightClickContextMenu contextMenu = new RightClickContextMenu(
                    mContext, mMouseClickX, mMouseClickY);
            contextMenu.addButton(mContext.getResources().getString(
                    R.string.remove_icon_from_menu),
                    new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    deleteItemFromMenu(parent, position);
                    contextMenu.dismiss();
                }
            });
            contextMenu.show();
        } else {
            super.onMenuItemClick(parent, position);
        }
    }

    @Override
    protected boolean onMenuItemLongClick(AdapterView<?> parent, View view, int position) {
        if (mMouseButton != MotionEvent.BUTTON_SECONDARY) {
            deleteItemFromMenu(parent, position);
        }
        return true;
    }

    private void deleteItemFromMenu(AdapterView<?> parent, int position) {
        ListViewMenuItem item = (ListViewMenuItem) parent.getItemAtPosition(position);
        String packageName = item.getPackageName();
        for (String appToDelete : mUserData.getAppsFavList()) {
            if (appToDelete.equals(packageName)) {
                ListViewMenuAdapter adapter = (ListViewMenuAdapter) parent.getAdapter();
                adapter.remove(item);
                adapter.notifyDataSetChanged();
                mUserData.getAppsFavList().remove(appToDelete);
                Toast.makeText(mContext, String.format(mContext.getResources().getString(
                        R.string.deleted_from_fav_list),item.getTitle()),
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }
}
