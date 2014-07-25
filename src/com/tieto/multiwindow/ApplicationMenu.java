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
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ApplicationMenu extends Dialog {

    private final int mApplicationMenuWidth = 700;
    private final int mApplicationMenuVerticalOffset = 50;
    private final int mDragShadowSize = 150;
    private final PackageManager mPackerManager;
    private final List<ResolveInfo> mAppInfo;
    private OnAppStartListener mOnAppListener;

    public ApplicationMenu(final Context ctx) {
        super(ctx, R.style.ApplicationMenuTheme);

        int useableScreenHeight = ctx.getApplicationContext().getResources()
                .getDisplayMetrics().heightPixels;

        getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount(0.0f);
        getWindow().setGravity(Gravity.TOP | Gravity.LEFT);
        getWindow().getAttributes().x = 0;
        getWindow().getAttributes().y = mApplicationMenuVerticalOffset;
        getWindow().getAttributes().width = mApplicationMenuWidth;
        getWindow().getAttributes().height = useableScreenHeight - MenuBar.HEIGHT
                - mApplicationMenuVerticalOffset;
        setContentView(R.layout.application_menu);

        mPackerManager = getContext().getPackageManager();
        mAppInfo = mPackerManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null)
                        .addCategory(Intent.CATEGORY_LAUNCHER), 0);

        List<ApplicationMenuItem> rowItems = new ArrayList<ApplicationMenuItem>();

        for (ResolveInfo singleAppInfo : mAppInfo) {
            rowItems.add(new ApplicationMenuItem(
                    singleAppInfo.activityInfo.loadIcon(mPackerManager),
                    singleAppInfo.activityInfo.loadLabel(mPackerManager).toString(),
                    singleAppInfo.activityInfo.packageName));
        }

        ListView listview = (ListView) findViewById(R.id.listview);
        ListViewAdapter adapter = new ListViewAdapter(ctx, R.layout.application_menu_item, rowItems);

        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                    int position, long id) {
                mOnAppListener.actionPerformed(mPackerManager
                        .getLaunchIntentForPackage(mAppInfo.get(position).activityInfo.packageName));
                dismiss();
            }
        });
        listview.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ClipData.Item iconType = new ClipData.Item("AppMenuIcon");
                ClipData.Item packageName = new ClipData.Item(
                        mAppInfo.get(position).activityInfo.packageName);
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

    public void setOnAppListener(OnAppStartListener onAppListener) {
        this.mOnAppListener = onAppListener;
    }

    private class ListViewAdapter extends ArrayAdapter<ApplicationMenuItem> {

        public ListViewAdapter(Context context, int resourceId,
                List<ApplicationMenuItem> items) {
            super(context, resourceId, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.application_menu_item, null);
            }

            ApplicationMenuItem rowItem = getItem(position);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
            TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

            imageView.setImageDrawable(rowItem.getImage());
            txtTitle.setText(rowItem.getTitle());

            return convertView;
        }
    }
}
