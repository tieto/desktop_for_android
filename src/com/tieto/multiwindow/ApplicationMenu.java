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

import com.tieto.extension.multiwindow.MultiwindowManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
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
    private final PackageManager mPackerManager;
    private final List<ResolveInfo> mAppInfo;
    private AddIconToDesktopListener mAddIconToDesktopListener;
    public final boolean DRAG_DEBUG = false;
    private MultiwindowManager mMultiwindowManager;

    public ApplicationMenu(final Context ctx, AddIconToDesktopListener listener) {
        super(ctx, R.style.ApplicationMenuTheme);
        mAddIconToDesktopListener = listener;
        mMultiwindowManager = new MultiwindowManager(ctx);

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
                Intent launchIntent = mPackerManager.getLaunchIntentForPackage(mAppInfo
                        .get(position).activityInfo.packageName);
                mMultiwindowManager.startActivity(launchIntent);
                dismiss();
            }
        });
        listview.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ClipData.Item item = new ClipData.Item(
                        mAppInfo.get(position).activityInfo.packageName);
                String[] clipDescription = { ClipDescription.MIMETYPE_TEXT_PLAIN };
                ClipData dragData = new ClipData("", clipDescription, item);

                ImageView dragIcon = (ImageView) view.findViewById(R.id.icon);
                DragShadowBuilder shadowBuilder = new DragShadowBuilder(dragIcon);

                view.startDrag(dragData, shadowBuilder, view, 0);

                return true;
            }
        });
        getWindow().getDecorView().setOnDragListener(new OnDragListener() {
            private final String TAG = "APPMENU_DRAG_EVENT";
            private int mX, mY;

            @Override
            public boolean onDrag(View v, DragEvent event) {

                switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_STARTED");
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACeTION_DRAG_ENTERED");
                    }
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_EXITED");
                    }
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_LOCATION");
                    }
                    mX = (int) event.getX();
                    mY = (int) event.getY();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DRAG_ENDED");
                    }
                    dismiss();
                    break;
                case DragEvent.ACTION_DROP:
                    if (DRAG_DEBUG) {
                        Log.d(TAG, "ACTION_DROP");
                    }
                    View view = (View) event.getLocalState();
                    String packageName = event.getClipData().getItemAt(0)
                            .getText().toString();
                    mAddIconToDesktopListener.onAddIcon(view, mX, mY, packageName);
                    break;
                default:
                    break;
                }
                return true;
            }
        });
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
