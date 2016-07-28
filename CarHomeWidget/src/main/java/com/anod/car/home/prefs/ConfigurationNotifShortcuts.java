package com.anod.car.home.prefs;

import android.content.Intent;
import android.os.Bundle;

import com.anod.car.home.R;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.prefs.PickShortcutUtils.PreferenceKey;
import com.anod.car.home.prefs.model.InCarStorage;
import com.anod.car.home.prefs.views.ShortcutPreference;

public class ConfigurationNotifShortcuts extends ConfigurationPreferenceFragment
        implements PreferenceKey, ShortcutPreference.DropCallback {

    private PickShortcutUtils mPickShortcutUtils;

    private NotificationShortcutsModel mModel;

    @Override
    protected boolean isAppWidgetIdRequired() {
        return false;
    }

    @Override
    protected int getXmlResource() {
        return R.xml.preference_notif_shortcuts;
    }

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {

        mModel = NotificationShortcutsModel.init(getActivity());
        mPickShortcutUtils = new PickShortcutUtils(this, mModel, this);
        mPickShortcutUtils.onRestoreInstanceState(savedInstanceState);

        for (int i = 0; i < InCarStorage.NOTIFICATION_COMPONENT_NUMBER; i++) {
            ShortcutPreference p = (ShortcutPreference) findPreference(getInitialKey(i));
            mPickShortcutUtils.initLauncherPreference(i, p);
            p.setDropCallback(this);
        }
    }

    @Override
    protected String getSharedPreferencesName() {
        return InCarStorage.PREF_NAME;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPickShortcutUtils.onSaveInstanceState(outState);
    }

    @Override
    public String getInitialKey(int position) {
        return InCarStorage.getNotifComponentName(position);
    }

    @Override
    public String getCompiledKey(int position) {
        return InCarStorage.getNotifComponentName(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPickShortcutUtils.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public int onScrollRequest(int top) {
        return 0;
    }

    @Override
    public boolean onDrop(int oldCellId, int newCellId) {
        if (oldCellId == newCellId) {
            return false;
        }
        mModel.move(oldCellId, newCellId);
        refreshShortcuts();
        return true;
    }

    private void refreshShortcuts() {
        mModel.init();
        for (int i = 0; i < InCarStorage.NOTIFICATION_COMPONENT_NUMBER; i++) {
            String key = getCompiledKey(i);
            ShortcutPreference p = (ShortcutPreference) findPreference(key);
            mPickShortcutUtils.refreshPreference(p);
        }
    }
}
