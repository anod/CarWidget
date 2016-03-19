package com.anod.car.home.prefs;

import com.anod.car.home.R;
import com.anod.car.home.app.MusicAppsActivity;
import com.anod.car.home.model.AppsList;
import com.anod.car.home.prefs.preferences.AppStorage;
import com.anod.car.home.prefs.preferences.PreferencesStorage;

import java.util.ArrayList;

/**
 * @author alex
 * @date 2014-09-03
 */
public class MusicAppSettingsActivity extends MusicAppsActivity {

    @Override
    protected ArrayList<AppsList.Entry> getHeadEntries() {
        ArrayList<AppsList.Entry> head = new ArrayList<AppsList.Entry>(1);
        AppsList.Entry none = new AppsList.Entry();
        none.iconRes = R.drawable.ic_action_list;
        none.title = getString(R.string.show_choice);
        head.add(none);
        return head;
    }

    @Override
    protected boolean isShowTitle() {
        return true;
    }

    @Override
    protected int getFooterViewId() {
        return R.layout.music_app_settings_footer;
    }

    @Override
    protected void onEntryClick(int position, AppsList.Entry entry) {
        if (position == 0) {
            AppStorage.saveMusicApp(this, null, true);
        } else {
            AppStorage.saveMusicApp(this, entry.componentName, false);
        }
        finish();
    }
}
