/**
 * Desktop for Android
 * Copyright (C) 2014 Tieto Poland Sp. z o.o.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.tieto.multiwindow;

import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class OptionsMenu extends Dialog {
    private Desktop mDesktop;
    private LayoutParams mLayoutParams;
    private int mMenuWidth;
    private int mMenuHeight;
    private LayoutInflater mInflater;

    public OptionsMenu(Context context, Desktop desktop) {
        super(context, R.style.popupMenu);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMenuHeight = (int) getContext().getResources().getDimension(
                R.dimen.options_menu_heigth);
        mMenuWidth = (int) getContext().getResources().getDimension(
                R.dimen.options_menu_width);
        mDesktop = desktop;
        setContentView(R.layout.options_menu);
        mLayoutParams = getWindow().getAttributes();
        getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        mLayoutParams.width = mMenuWidth;
        mLayoutParams.height = mMenuHeight;
        mInflater = mDesktop.getLayoutInflater();
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setExitButton();
        setWallpaperButton();
        setAboutButton();
    }

    private void setExitButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(mDesktop,
                        android.R.style.Theme_Holo_Dialog));
        View content = mInflater.inflate(R.layout.exit_dialog, null);
        builder.setView(content);
        final AlertDialog buildAlertDialog = builder.create();
        buildAlertDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        content.findViewById(R.id.yesButton).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mDesktop.finish();
                    }
                });

        content.findViewById(R.id.noButton).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        buildAlertDialog.dismiss();
                    }
                });

        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                buildAlertDialog.show();
                dismiss();
            }
        });
    }

    private void setWallpaperButton() {
        findViewById(R.id.wallpaper).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        mDesktop.startActivityForResult(intent,
                                mDesktop.SELECT_PICTURE);
                        dismiss();
                    }
                });
    }

    private void setAboutButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(mDesktop,
                        android.R.style.Theme_Holo_Dialog));
        View content = mInflater.inflate(R.layout.about_dialog, null);
        builder.setView(content);
        final AlertDialog buildAlertDialog = builder.create();
        buildAlertDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        content.findViewById(R.id.exitAbout).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        buildAlertDialog.dismiss();
                    }
                });
        try {
            InputStream stream = getContext().getAssets().open(
                    getContext().getString(R.string.about_txt));
            byte[] b = new byte[stream.available()];
            stream.read(b);
            ((TextView) content.findViewById(R.id.about))
                    .setText(new String(b));
        } catch (IOException e) {
            Toast.makeText(mDesktop,
                    getContext().getResources().getText(R.string.error_about),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                buildAlertDialog.show();
                dismiss();
            }
        });
    }

    public void show(int x, int y) {
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        getWindow().setAttributes(mLayoutParams);
        show();
    }

}
