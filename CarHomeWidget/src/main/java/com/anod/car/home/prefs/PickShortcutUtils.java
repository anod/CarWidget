package com.anod.car.home.prefs;

import com.anod.car.home.R;
import com.anod.car.home.model.Shortcut;
import com.anod.car.home.model.ShortcutIcon;
import com.anod.car.home.model.ShortcutsContainerModel;
import com.anod.car.home.prefs.views.ShortcutPreference;
import com.anod.car.home.utils.ShortcutPicker;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;

public class PickShortcutUtils implements ShortcutPicker.Handler {


    private final ConfigurationPreferenceFragment mConfigurationFragment;

    private final ConfigurationActivity mActivity;

    final ShortcutsContainerModel mModel;

    private final PreferenceKey mPreferenceKey;

    private final ShortcutPicker mPicker;

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        mConfigurationFragment.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onAddShortcut(int cellId, final Shortcut info) {
        if (info != null && info.id != Shortcut.NO_ID) {
            String key = mPreferenceKey.getCompiledKey(cellId);
            ShortcutPreference p = (ShortcutPreference) mConfigurationFragment.findPreference(key);
            refreshPreference(p);
        }
    }

    @Override
    public void onEditComplete(int cellId) {
        String key = mPreferenceKey.getCompiledKey(cellId);
        ShortcutPreference p = (ShortcutPreference) mConfigurationFragment.findPreference(key);
        refreshPreference(p);
    }

    interface PreferenceKey {

        String getInitialKey(int position);

        String getCompiledKey(int position);
    }


    public PickShortcutUtils(ConfigurationPreferenceFragment fragment,
            ShortcutsContainerModel model, PreferenceKey key) {
        mConfigurationFragment = fragment;
        mActivity = (ConfigurationActivity) mConfigurationFragment.getActivity();
        mModel = model;
        mPreferenceKey = key;
        mPicker = new ShortcutPicker(model, this, mActivity);
    }

    public ShortcutPreference initLauncherPreference(int position, ShortcutPreference p) {
        p.setKey(mPreferenceKey.getCompiledKey(position));
        p.setShortcutPosition(position);
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ShortcutPreference pref = (ShortcutPreference) preference;
                int position = pref.getShortcutPosition();
                Shortcut info = mModel.getShortcut(position);
                if (info == null) {
                    showActivityPicker(position);
                } else {
                    startEditActivity(position, info.id);
                }
                return true;
            }
        });
        p.setOnDeleteClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ShortcutPreference pref = (ShortcutPreference) preference;
                mModel.dropShortcut(pref.getShortcutPosition());
                refreshPreference(pref);
                return true;
            }

        });
        refreshPreference(p);
        return p;
    }

    public void showActivityPicker(int position) {
        mPicker.showActivityPicker(position);
    }


    void startEditActivity(int cellId, long shortcutId) {
        mPicker.showEditActivity(cellId, shortcutId, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void refreshPreference(ShortcutPreference pref) {
        int cellId = pref.getShortcutPosition();
        Shortcut info = mModel.getShortcut(cellId);
        pref.setAppTheme(mActivity.getApp().getThemeIdx());
        if (info == null) {
            pref.setTitle(R.string.set_shortcut);
            pref.setIconResource(R.drawable.ic_add_shortcut_holo);
            pref.showButtons(false);
        } else {
            ShortcutIcon icon = mModel.loadIcon(info.id);
            pref.setIconBitmap(icon.bitmap);
            pref.setTitle(info.title);
            pref.showButtons(true);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    public void onSaveInstanceState(Bundle outState) {
        mPicker.onSaveInstanceState(outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mPicker.onRestoreInstanceState(savedInstanceState, null);
    }
}
