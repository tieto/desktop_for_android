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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ListViewMenu extends Dialog {

    private final int mListViewMenuWidth = 700;
    private final int mListViewMenuVerticalOffset = 50;
    private ListView mListViewMenu;
    private OnAppStartListener mOnAppListener;
    private Context mContext;

    public ListViewMenu(Context context, int theme) {
        super(context, theme);
        mContext = context;

        initWindowParams();
        mListViewMenu = (ListView) findViewById(R.id.listview);
    }

    private void initWindowParams() {
        int useableScreenHeight = mContext.getApplicationContext()
                .getResources().getDisplayMetrics().heightPixels;

        getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount(0.0f);
        getWindow().setGravity(Gravity.TOP | Gravity.LEFT);
        getWindow().getAttributes().x = 0;
        getWindow().getAttributes().y = mListViewMenuVerticalOffset;
        getWindow().getAttributes().width = mListViewMenuWidth;
        getWindow().getAttributes().height = useableScreenHeight
                - MenuBar.HEIGHT - mListViewMenuVerticalOffset;
        setContentView(R.layout.application_menu);
    }

    public void fillListViewMenu(ArrayList<ListViewMenuItem> listViewItems) {
        ListViewAdapter adapter = new ListViewAdapter(mContext, R.layout.application_menu_item, listViewItems);
        mListViewMenu.setAdapter(adapter);
    }

    public ListView getListViewMenu() {
        return mListViewMenu;
    }

    public OnAppStartListener getOnAppListener() {
        return mOnAppListener;
    }

    public void setOnAppListener(OnAppStartListener onAppListener) {
        this.mOnAppListener = onAppListener;
    }

    private class ListViewAdapter extends ArrayAdapter<ListViewMenuItem> {

        public ListViewAdapter(Context context, int resourceId, List<ListViewMenuItem> items) {
            super(context, resourceId, items);
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

            return convertView;
        }
    }
}
