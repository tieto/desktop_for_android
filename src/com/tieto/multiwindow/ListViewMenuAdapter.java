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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewMenuAdapter extends ArrayAdapter<ListViewMenuItem> {

    private UserDataInterface mUserData;

    public ListViewMenuAdapter(Context context, int resourceId, List<ListViewMenuItem> items) {
        super(context, resourceId, items);
        mUserData = null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.application_menu_item, null);
        }

        ListViewMenuItem rowItem = getItem(position);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        imageView.setImageDrawable(rowItem.getImage());
        txtTitle.setText(rowItem.getTitle());
        if (rowItem.getPackageName().equals(ApplicationMenu.sFav) ||
                rowItem.getPackageName().equals(ApplicationMenu.sFreq)) {
            txtTitle.setTypeface(null, Typeface.BOLD);
        } else {
            txtTitle.setTypeface(null, Typeface.NORMAL);
        }

        if (mUserData != null) {
            if ((rowItem.getPackageName().equals(ApplicationMenu.sFav) && mUserData.getAppsFavList().size() == 0) ||
                    (rowItem.getPackageName().equals(ApplicationMenu.sFreq) && mUserData.getAppsUsedList().size() == 0)) {
                rowItem.setActive(false);
            } else {
                rowItem.setActive(true);
            }
            txtTitle.setTextColor(getTextColor(rowItem));
        }

        return convertView;
    }

    public void setUserData(UserDataInterface userData) {
        mUserData = userData;
    }

    public int getTextColor(ListViewMenuItem item) {
        if (item.isActive()) {
            return getContext().getResources().getColor(R.color.base_text_color);
        } else {
            return getContext().getResources().getColor(R.color.inactive_text_color);
        }
    }
}