package com.anod.car.home.prefs;

import com.anod.car.home.R;
import com.anod.car.home.app.MusicAppsActivity;
import com.anod.car.home.model.AppsList;
import com.anod.car.home.prefs.model.AppSettings;

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
        AppSettings appSettings = AppSettings.create(this);
        if (position == 0) {
            appSettings.setMusicApp(null);
        } else {
            appSettings.setMusicApp(entry.componentName);
        }
        appSettings.apply();
        finish();
    }
}
