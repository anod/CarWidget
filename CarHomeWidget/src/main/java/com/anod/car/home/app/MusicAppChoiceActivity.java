package com.anod.car.home.app;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.CheckBox;

import com.anod.car.home.R;
import com.anod.car.home.model.AppsList;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.MusicUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author alex
 * @date 2014-09-03
 */
public class MusicAppChoiceActivity extends MusicAppsActivity {
    @InjectView(R.id.defaultApp) CheckBox mDefaultApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
    }
    @Override
    protected boolean isShowTitle() {
        return false;
    }

    @Override
    protected int getFooterViewId() {
        return R.layout.music_app_choice_footer;
    }

    @Override
    protected void onEntryClick(int position, AppsList.Entry entry) {
        ComponentName musicCmp = entry.componentName;

        MusicUtils.sendKeyEventComponent(
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, this, musicCmp, true
        );

        if (mDefaultApp.isChecked()) {
            PreferencesStorage.saveMusicApp(this, musicCmp, true);
        }

        finish();
    }

}
