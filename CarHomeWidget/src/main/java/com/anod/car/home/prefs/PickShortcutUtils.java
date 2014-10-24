package com.anod.car.home.prefs;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.anod.car.home.R;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutsModel;
import com.anod.car.home.prefs.views.ShortcutPreference;
import com.anod.car.home.utils.ShortcutPicker;

public class PickShortcutUtils implements ShortcutPicker.Handler {


	private final ConfigurationPreferenceFragment mConfigurationFragment;
	private final ConfigurationActivity mActivity;
	private final ShortcutsModel mModel;
	private final PreferenceKey mPreferenceKey;
    private final ShortcutPicker mPicker;

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {

    }

    @Override
    public void onAddShortcut(int cellId, final ShortcutInfo info) {
        if (info != null && info.id != ShortcutInfo.NO_ID) {
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

	
	public PickShortcutUtils(ConfigurationPreferenceFragment fragment, ShortcutsModel model, PreferenceKey key) {
		mConfigurationFragment = fragment;
		mActivity = (ConfigurationActivity)mConfigurationFragment.getActivity();
		mModel = model;
		mPreferenceKey = key;
        mPicker = new ShortcutPicker(model,this,mActivity);
	}
	
	public ShortcutPreference initLauncherPreference(int position, ShortcutPreference p) {
		p.setKey(mPreferenceKey.getCompiledKey(position));
		p.setShortcutPosition(position);
		p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ShortcutPreference pref = (ShortcutPreference) preference;
				int position = pref.getShortcutPosition();
				ShortcutInfo info = mModel.getShortcut(position);
				if (info == null) {
					showActivityPicker(position);
				} else {
					startEditActivity(position, info.id);
				}
				return true;
			}
		});
		p.setOnDeleteClickListener(new OnPreferenceClickListener() {
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


	private void startEditActivity(int cellId, long shortcutId) {
        mPicker.showEditActivity(cellId, shortcutId);
	}

	public void refreshPreference(ShortcutPreference pref) {
		int cellId = pref.getShortcutPosition();
		ShortcutInfo info = mModel.getShortcut(cellId);
		pref.setAppTheme(mActivity.getApp().getThemeIdx());
		if (info == null) {
			pref.setTitle(R.string.set_shortcut);
			pref.setIconResource(R.drawable.ic_add_shortcut_holo);
			pref.showButtons(false);
		} else {
			pref.setIconBitmap(info.getIcon());
			pref.setTitle(info.title);
			pref.showButtons(true);
		}
	}

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, requestCode, data);
    }

	public void onSaveInstanceState(Bundle outState) {
		mPicker.onSaveInstanceState(outState);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
        mPicker.onRestoreInstanceState(savedInstanceState);
	}
}
