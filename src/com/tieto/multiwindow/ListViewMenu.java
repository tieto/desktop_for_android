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

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class ListViewMenu extends Dialog {

    private final int mDragShadowSize = 150;
    private final int mListViewMenuWidth = 450;
    private final int mListViewMenuVerticalOffset = 50;

    private ListView mListViewMenu;
    private OnAppStartListener mOnAppListener;
    private UserDataInterface mUserData;

    private Context mContext;
    private Resources mResources;
    private PackageManager mPackerManager;

    protected int mMouseButton;
    protected int mMouseClickX;
    protected int mMouseClickY;

    public ListViewMenu(Context context, int theme, UserDataInterface userData) {
        super(context, theme);
        mContext = context;
        mResources = context.getResources();
        mPackerManager = context.getPackageManager();
        mUserData = userData;

        initWindowParams();
        setContentView(R.layout.application_menu);
        mListViewMenu = (ListView) findViewById(R.id.listview);

        /**
         * This onTouch event is added just to obtain information about device
         * that performed Click or LongClick and raw position of event.
         */
        mListViewMenu.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mMouseButton = event.getButtonState();
                    mMouseClickX = (int) event.getRawX();
                    mMouseClickY = (int) event.getRawY();
                }
                return false;
            }
        });
        mListViewMenu.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                onMenuItemClick(parent, position);
            }
        });
        mListViewMenu.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                return onMenuItemLongClick(parent, view, position);
            }
        });
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
    }

    public void fillListViewMenu(ArrayList<ListViewMenuItem> listViewItems) {
        ListViewMenuAdapter adapter = new ListViewMenuAdapter(mContext, R.layout.application_menu_item, listViewItems);
        mListViewMenu.setAdapter(adapter);
    }

    public ListViewMenuItem createListViewMenuItem(ApplicationInfo ai) {
        return new ListViewMenuItem(
                mPackerManager.getApplicationIcon(ai),
                mPackerManager.getApplicationLabel(ai).toString(),
                ai.packageName);
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

    protected void onMenuItemClick(AdapterView<?> parent, int position) {
        String packageName = ((ListViewMenuItem) parent.getAdapter()
                .getItem(position)).getPackageName();
        getOnAppListener().actionPerformed(mPackerManager
                .getLaunchIntentForPackage(packageName));
        dismiss();
        mUserData.incAppUsedCounter(packageName);
    }

    protected boolean onMenuItemLongClick(AdapterView<?> parent, View view, int position) {
        String packageName = ((ListViewMenuItem) parent.getAdapter()
                .getItem(position)).getPackageName();
        ClipData.Item clipIconType = new ClipData.Item(Desktop.sListViewMenuIcon);
        ClipData.Item clipPackageName = new ClipData.Item(packageName);
        String[] clipDescription = { ClipDescription.MIMETYPE_TEXT_PLAIN };
        ClipData dragData = new ClipData("", clipDescription, clipIconType);
        dragData.addItem(clipPackageName);

        ImageView dragIcon = (ImageView) view.findViewById(R.id.icon);
        ListViewMenuItemDSB shadowBuilder = new ListViewMenuItemDSB(dragIcon,
                mDragShadowSize, mDragShadowSize);
        view.startDrag(dragData, shadowBuilder, view, 0);

        return true;
    }
}
