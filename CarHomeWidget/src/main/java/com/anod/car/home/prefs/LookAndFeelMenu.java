package com.anod.car.home.prefs;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.anod.car.home.R;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.prefs.preferences.WidgetSharedPreferences;
import com.anod.car.home.prefs.views.CarHomeColorPickerDialog;
import com.anod.car.home.utils.FastBitmapDrawable;
import com.anod.car.home.utils.Utils;

/**
 * @author alex
 * @date 2014-10-20
 */
public class LookAndFeelMenu {
    public static final int REQUEST_LOOK_ACTIVITY = 1;
    public static final int REQUEST_PICK_ICON_THEME = 2;
    private final int mAppWidgetId;
    private MenuItem mMenuTileColor;

    private LookAndFeelActivity mActivity;
    private WidgetSharedPreferences mSharedPrefs;
    private boolean mInitialized;

    public LookAndFeelMenu(LookAndFeelActivity activity) {
        mActivity = activity;
        mAppWidgetId = mActivity.getAppWidgetId();

        mSharedPrefs = new WidgetSharedPreferences(mActivity);
        mSharedPrefs.setAppWidgetId(mAppWidgetId);
    }

    public void onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = mActivity.getMenuInflater();
        menuInflater.inflate(R.menu.look_n_feeel, menu);

        mMenuTileColor = menu.findItem(R.id.tile_color);
        menu.findItem(R.id.icons_mono).setChecked(mActivity.getPrefs().isIconsMono());
        mInitialized = true;
        refreshTileColorButton();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.apply) {
            Main prefs = PreferencesStorage.loadMain(mActivity,mAppWidgetId);
            prefs.setSkin(mActivity.getCurrentSkinItem().value);
            PreferencesStorage.saveMain(mActivity, prefs, mAppWidgetId);
            mActivity.beforeFinish();
            mActivity.finish();
            return true;
        }
        if (itemId == R.id.tile_color) {
            Main prefs = PreferencesStorage.loadMain(mActivity, mAppWidgetId);
            Integer value = prefs.getTileColor();
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int color = ((CarHomeColorPickerDialog) dialog).getColor();
                    final WidgetSharedPreferences.WidgetEditor edit = mSharedPrefs.edit();
                    edit.putInt(PreferencesStorage.BUTTON_COLOR, color);
                    edit.commit();
                    showTileColorButton();
                    mActivity.refreshSkinPreview();
                }

            };
            final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mActivity, value, listener);
            d.setAlphaSliderVisible(true);
            d.show();
            return true;
        }
        if (itemId == R.id.more) {
            Intent intent = ConfigurationActivity.createFragmentIntent(mActivity, ConfigurationLook.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            mActivity.startActivityForResult(intent, REQUEST_LOOK_ACTIVITY);
            return true;
        }
        if (itemId == R.id.bg_color) {
            int value = mActivity.getPrefs().getBackgroundColor();
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int color = ((CarHomeColorPickerDialog) dialog).getColor();
                    final WidgetSharedPreferences.WidgetEditor edit = mSharedPrefs.edit();
                    edit.putInt(PreferencesStorage.BG_COLOR, color);
                    edit.commit();
                    mActivity.refreshSkinPreview();
                }
            };
            final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mActivity, value, listener);
            d.setAlphaSliderVisible(true);
            d.show();
            return true;
        }
        if (itemId == R.id.icons_theme) {
            Intent mainIntent = new Intent(mActivity, IconThemesActivity.class);
            mainIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            Utils.startActivityForResultSafetly(mainIntent, REQUEST_PICK_ICON_THEME, mActivity);

            return true;
        }
        if (itemId == R.id.icons_mono) {
            mActivity.getPrefs().setIconsMono(!item.isChecked());
            mActivity.persistPrefs();
            item.setChecked(!item.isChecked());
            mActivity.refreshSkinPreview();
            return true;
        }
        if (itemId == R.id.icons_scale) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            final String[] titles = mActivity.getResources().getStringArray(R.array.icon_scale_titles);
            final String[] values = mActivity.getResources().getStringArray(R.array.icon_scale_values);
            int idx = -1;
            for (int i = 0; i < values.length; i++) {
                if (mActivity.getPrefs().getIconsScale().equals(values[i])) {
                    idx = i;
                    break;
                }
            }
            builder.setTitle(R.string.pref_scale_icon);
            builder.setSingleChoiceItems(titles, idx, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    mActivity.getPrefs().setIconsScaleString(values[item]);
                    mActivity.persistPrefs();
                    dialog.dismiss();
                    mActivity.refreshSkinPreview();
                }
            });
            builder.create().show();
            return true;
        }
        return false;
    }

    public void refreshTileColorButton() {
        if (mInitialized) {
            showTileColorButton();
        }
    }

    public void showTileColorButton() {
        if (mActivity.getCurrentSkinItem().value.equals(Main.SKIN_WINDOWS7)) {
            Main prefs = PreferencesStorage.loadMain(mActivity, mAppWidgetId);
            int size = (int) mActivity.getResources().getDimension(R.dimen.color_preview_size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            c.drawColor(prefs.getTileColor());
            Drawable d = new FastBitmapDrawable(bitmap);

            mMenuTileColor.setIcon(d);
            mMenuTileColor.setVisible(true);
        } else {
            mMenuTileColor.setVisible(false);
        }
    }


}
