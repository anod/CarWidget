package com.anod.car.home.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutsContainerModel;
import com.anod.car.home.prefs.ActivityPicker;
import com.anod.car.home.prefs.AllAppsActivity;
import com.anod.car.home.prefs.CarWidgetShortcutsPicker;
import com.anod.car.home.prefs.ShortcutEditActivity;

import java.util.ArrayList;

/**
 * @author alex
 * @date 2014-10-24
 */
public class ShortcutPicker {
    private final Handler mHandler;
    private final Context mContext;
    private final ShortcutsContainerModel mModel;

    private int mCurrentCellId = INVALID_CELL_ID;

    private static final int REQUEST_PICK_SHORTCUT = 2;
    private static final int REQUEST_PICK_APPLICATION = 3;
    private static final int REQUEST_CREATE_SHORTCUT = 4;
    private static final int REQUEST_EDIT_SHORTCUT = 5;

    public static final String EXTRA_CELL_ID = "CarHomeWidgetCellId";
    public static final int INVALID_CELL_ID = -1;

    public interface Handler {
        void startActivityForResult(Intent intent, int requestCode);
        void onAddShortcut(int cellId, final ShortcutInfo info);
        void onEditComplete(int cellId);
    }

    public ShortcutPicker(ShortcutsContainerModel model, Handler handler, Context context) {
        mContext = context;
        mHandler = handler;
        mModel = model;
    }

    public void showEditActivity(int cellId, long shortcutId, int appWidgetId) {
        Intent editIntent = IntentUtils.createShortcutEditIntent(mContext, cellId, shortcutId, appWidgetId);
        startActivityForResultSafely(editIntent, REQUEST_EDIT_SHORTCUT);
    }

    public void showActivityPicker(int position) {
        Intent pickIntent = createPickIntent(position);
        mHandler.startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_APPLICATION:
                    completeAddShortcut(data, true);
                    return true;
                case REQUEST_CREATE_SHORTCUT:
                    completeAddShortcut(data, false);
                    return true;
                case REQUEST_EDIT_SHORTCUT:
                    completeEditShortcut(data);
                    return true;
                case REQUEST_PICK_SHORTCUT:
                    pickShortcut(data);
                    return true;
                default:
            }
        }
        return false;
    }

    private Intent createPickIntent(int position) {
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();

        shortcutNames.add(mContext.getString(R.string.applications));
        shortcutNames.add(mContext.getString(R.string.car_widget_shortcuts));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<Intent.ShortcutIconResource> shortcutIcons = new ArrayList<Intent.ShortcutIconResource>();
        shortcutIcons.add(Intent.ShortcutIconResource.fromContext(mContext, R.drawable.ic_launcher_application));
        shortcutIcons.add(Intent.ShortcutIconResource.fromContext(mContext, R.mipmap.ic_launcher_carwidget));

        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

        Intent dataIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        dataIntent.putExtra(EXTRA_CELL_ID, position);

        Intent pickIntent = new Intent(mContext, ActivityPicker.class);
        pickIntent.putExtras(bundle);
        pickIntent.putExtra(Intent.EXTRA_INTENT, dataIntent);
        pickIntent.putExtra(Intent.EXTRA_TITLE, mContext.getString(R.string.select_shortcut_title));
        return pickIntent;
    }

    private void pickShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = mContext.getString(R.string.applications);
        String shortcutsName = mContext.getString(R.string.car_widget_shortcuts);

        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(mContext, AllAppsActivity.class);
            startActivityForResultSafely(mainIntent, REQUEST_PICK_APPLICATION);
        } else	if (shortcutsName != null && shortcutsName.equals(shortcutName)) {
            Intent mainIntent = new Intent(mContext, CarWidgetShortcutsPicker.class);
            startActivityForResultSafely(mainIntent, REQUEST_CREATE_SHORTCUT);
        } else {
            startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
        }
    }

    private void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            mHandler.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, mContext.getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(mContext, mContext.getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
            Log.e("CarHomeWidget", "Widget does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity " + "or use the exported attribute for this activity.", e);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentCellId", mCurrentCellId);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentCellId = savedInstanceState.getInt("currentCellId", INVALID_CELL_ID);
        }
    }

    private void completeAddShortcut(Intent data, boolean isApplicationShortcut) {
        if (mCurrentCellId == INVALID_CELL_ID || data == null) {
            return;
        }

        final ShortcutInfo info = mModel.saveShortcutIntent(mCurrentCellId, data, isApplicationShortcut);
        mHandler.onAddShortcut(mCurrentCellId, info);
        mCurrentCellId = INVALID_CELL_ID;
    }

    private void completeEditShortcut(Intent data) {
        int cellId = data.getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, INVALID_CELL_ID);
        long shortcutId = data.getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, ShortcutInfo.NO_ID);
        if (cellId != INVALID_CELL_ID) {
            mModel.reloadShortcut(cellId, shortcutId);
            mHandler.onEditComplete(cellId);
        }
    }
}
