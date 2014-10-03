package com.anod.car.home.app;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.model.AppsList;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.MusicUtils;

import java.util.List;

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


        boolean isRunning = isMusicCmpRunning(musicCmp);

        MusicUtils.sendKeyEventComponent(
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, this, musicCmp, isRunning == false
        );

        if (mDefaultApp.isChecked()) {
            PreferencesStorage.saveMusicApp(this, musicCmp, true);
        }

        finish();
    }

    private boolean isMusicCmpRunning(ComponentName musicCmp) {
        ActivityManager activityManager = (ActivityManager) getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos == null) {
            return false;
        }
        for(int i = 0; i < procInfos.size(); i++)
        {
            if(procInfos.get(i).processName.startsWith(musicCmp.getPackageName()))
            {
                Toast.makeText(getApplicationContext(), musicCmp.getPackageName() + " is running", Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }

}
