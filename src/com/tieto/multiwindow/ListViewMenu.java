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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class ListViewMenu extends Dialog {

    private final int mDragShadowSize;
    private final int mListViewMenuWidth;
    private final int mListViewMenuVerticalOffset;

    private ListView mListViewMenu;
    private OnAppStartListener mOnAppListener;
    private UserDataInterface mUserData;
    protected ListViewMenu mParentMenu;
    private ArrayList<ListViewMenuItem> mListViewMenuItems;

    private Context mContext;
    private Resources mResources;
    private PackageManager mPackerManager;

    protected int mMouseButton;
    protected int mMouseClickX;
    protected int mMouseClickY;

    protected boolean stopDismiss;

    public ListViewMenu(Context context, int theme, UserDataInterface userData) {
        super(context, theme);
        mContext = context;
        mResources = context.getResources();
        mPackerManager = context.getPackageManager();
        mUserData = userData;
        mParentMenu = null;
        stopDismiss = false;

        mDragShadowSize = (int) mResources.getDimension(R.dimen.drag_shadow_size);
        mListViewMenuVerticalOffset = (int) mResources.getDimension(R.dimen.list_view_vertical_offset);
        mListViewMenuWidth = (int) mResources.getDimension(R.dimen.list_view_menu_width);

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
        int screenHeight = mResources.getDisplayMetrics().heightPixels;

        getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount(0.0f);
        getWindow().setGravity(Gravity.TOP | Gravity.LEFT);
        getWindow().getAttributes().x = 0;
        getWindow().getAttributes().y = mListViewMenuVerticalOffset;
        getWindow().getAttributes().width = mListViewMenuWidth;
        getWindow().getAttributes().height = screenHeight
                - Desktop.MENUBAR_HEIGHT - mListViewMenuVerticalOffset;
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

    public ListViewMenuItem addRowToListViewItems(String id, String title) {
        return new ListViewMenuItem(
                mContext.getResources().getDrawable(android.R.drawable.ic_menu_send), title, id);
    }

    protected int getListViewMenuHeight() {
        ListViewMenuAdapter adapter = (ListViewMenuAdapter) mListViewMenu.getAdapter();
        int listViewMenuHeight = 0;
        int numberOfItems = adapter.getCount();
        for (int i = 0; i < numberOfItems; i++) {
            View mView = adapter.getView(i, null, mListViewMenu);
            mView.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            listViewMenuHeight += mView.getMeasuredHeight();
        }
        if (numberOfItems > 1) {
            listViewMenuHeight += mListViewMenu.getDividerHeight() * (numberOfItems - 1);
        }

        return listViewMenuHeight;
    }

    protected int getListViewMenuItemHeight(int position) {
        ListViewMenuAdapter adapter = (ListViewMenuAdapter) mListViewMenu.getAdapter();
        View mView = adapter.getView(position, null, mListViewMenu);
        mView.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        return mView.getMeasuredHeight();
    }

    /**
     * Calculate offset form top of ListView to top edge of particular ListView item.
     *
     * @param position to calculate offset.
     * @return offset.
     */
    public int getYOffsetAtPosition(int position) {
        int yOffset = 0;
        int firstVisiblePosition = mListViewMenu.getFirstVisiblePosition();
        for (int i = firstVisiblePosition; i < position; i++) {
            yOffset += getListViewMenuItemHeight(i);
        }
        if (position - firstVisiblePosition > 0) {
            yOffset += mListViewMenu.getDividerHeight() * (position - firstVisiblePosition);
            yOffset += mListViewMenu.getChildAt(0).getY();
        }

        return yOffset;
    }

    protected void setBaseMenuWindowParams() {
        int menuHeight = getListViewMenuHeight();
        int screenHeight = mResources.getDisplayMetrics().heightPixels;
        int yPosition = getWindow().getAttributes().y;
        if (screenHeight - Desktop.MENUBAR_HEIGHT < menuHeight + yPosition) {
            getWindow().getAttributes().height = screenHeight - Desktop.MENUBAR_HEIGHT - yPosition;
        } else {
            getWindow().getAttributes().height = menuHeight;
            getWindow().getAttributes().y = screenHeight - Desktop.MENUBAR_HEIGHT - menuHeight;
        }
    }

    protected void setSubMenuWindowParams(int yOffset) {
        WindowManager.LayoutParams parentAttr = mParentMenu.getWindow().getAttributes();

        getWindow().getAttributes().x = parentAttr.x + mListViewMenuWidth + 1;
        getWindow().getAttributes().y = parentAttr.y + yOffset;

        int menuHeight = getListViewMenuHeight();
        int screenHeight = mResources.getDisplayMetrics().heightPixels;
        int yPosition = getWindow().getAttributes().y;
        if (screenHeight - Desktop.MENUBAR_HEIGHT < menuHeight + yPosition) {
            getWindow().getAttributes().height = screenHeight - Desktop.MENUBAR_HEIGHT - yPosition;
        } else {
            getWindow().getAttributes().height = menuHeight;
        }
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

    public ArrayList<ListViewMenuItem> getListViewMenuItems() {
        return mListViewMenuItems;
    }

    public void setListViewMenuItems(ArrayList<ListViewMenuItem> listViewMenuItems) {
        mListViewMenuItems = listViewMenuItems;
    }

    @Override
    public void dismiss() {
        if (!stopDismiss) {
            super.dismiss();
            if (mParentMenu != null) {
                mParentMenu.dismiss();
            }
        } else {
            stopDismiss = false;
        }
    }

    @Override
    public void show() {
        if (!mListViewMenu.getAdapter().isEmpty()) {
            super.show();
        }
    }

    /**
     * Just like {@link Dialog#show()}, but also LayoutParameters of Window are set,
     * according to parent ListViewMenu.
     *
     * @param yOffset difference between y of parent ListViewMenu and y position,
     * that SubMenu should be showed. See {@link ListViewMenu#getYOffsetAtPosition(int)}.
     */
    public void show(int yOffset) {
        setSubMenuWindowParams(yOffset);
        show();
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

    private boolean isOutOfBounds(int x, int y) {
        WindowManager.LayoutParams attr = getWindow().getAttributes();
        int startX = attr.x;
        int startY = attr.y;
        int endX = startX + attr.width;
        int endY = startY + attr.height;
        return (x < startX || x > endX || y < startY || y > endY);
    }

    private void markDismissBound(int x, int y) {
        if (isOutOfBounds(x, y)) {
            if (mParentMenu != null) {
                mParentMenu.markDismissBound(x, y);
            }
        } else {
            stopDismiss = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        markDismissBound((int) event.getRawX(), (int) event.getRawY());
        dismiss();
        return true;
    }
}
