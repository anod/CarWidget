package com.anod.car.home.drawer;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.MainActivity;
import com.anod.car.home.R;
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.prefs.ConfigurationActivity;
import com.anod.car.home.prefs.ConfigurationInCar;
import com.anod.car.home.prefs.ConfigurationRestore;
import com.anod.car.home.prefs.MusicAppSettingsActivity;
import com.anod.car.home.prefs.preferences.AppTheme;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

import java.util.ArrayList;


/**
 * @author alex
 * @date 2014-10-21
 */
public class NavigationList extends ArrayList<NavigationList.Item> {
    private static final String DETAIL_MARKET_URL = "market://details?id=%s";
    private static final String URL_GOOGLE_PLUS = "https://plus.google.com/communities/106765737887289122631";
    private static final String RESOLVER_ACTIVITY = "com.android.internal.app.ResolverActivity";

    public static final int ID_CAR_SETTINGS = 1;
    public static final int ID_WIDGETS = 7;
    private static final int ID_CAR_DOCK_APP = 2;
    private static final int ID_THEME = 3;
    private static final int ID_MUSIC_APP = 4;
    private static final int ID_VERSION = 5;
    private static final int ID_FEEDBACK = 6;
    private static final int ID_BACKUP = 8;
    private static final int ID_CURRENT_WIDGET = 9;

    private final PackageManager mPackageManager;
    private final Activity mActivity;
    private final Context mContext;
    private final int mAppWidgetId;

    public NavigationList(Activity activity, int appWidgetId) {
        mActivity = activity;
        mContext = activity;
        mPackageManager = activity.getPackageManager();
        mAppWidgetId = appWidgetId;
    }

    public static class Item {
        int id;
        int titleRes = 0;
        String titleText;

        public Item(int id, int titleRes) {
            this.id = id;
            this.titleRes = titleRes;
        }
    }

    public static class TitleItem extends Item {
        public TitleItem(int titleRes) {
            super(0, titleRes);
        }
        public TitleItem(String titleText) {
            super(0, 0);
            this.titleText = titleText;
        }
    }
    public static class ActionItem extends Item {
        int summaryRes;
        String summaryText;
        int iconRes;

        public ActionItem(int id, int titleRes, int summaryRes, int iconRes) {
            super(id, titleRes);
            this.summaryRes = summaryRes;
            this.iconRes = iconRes;
        }

        public ActionItem(int id, String title, int summaryRes, int iconRes) {
            this(id, 0, summaryRes, iconRes);
            titleText = title;
        }

        public ActionItem(int id, int titleRes, String summary, int iconRes) {
            this(id, titleRes, 0, iconRes);
            this.summaryText = summary;
        }
    }

    public void addDefaults() {

        int[] appWidgetIds = WidgetHelper.getAllWidgetIds(mContext);
        final int widgetsCount = appWidgetIds.length;

        Version version = new Version(mContext);

        String active = renderActiveString(widgetsCount, version);

        if (mAppWidgetId > 0) {
            addTitle("Current widget");
            addButton(ID_CURRENT_WIDGET,R.string.settings,0,R.drawable.ic_now_widgets_white_24dp);
            addButton(ID_BACKUP, R.string.pref_backup_title, R.string.pref_backup_summary, R.drawable.ic_backup_white_24dp);
        }

        add(new TitleItem(0));
        addButton(ID_WIDGETS, R.string.widgets, "List of active widgets", R.drawable.ic_now_widgets_white_24dp);

        String incarSummary =mContext.getString(R.string.settings) + ". " + active;
        addButton(ID_CAR_SETTINGS, R.string.pref_incar_mode_title, incarSummary, R.drawable.ic_settings_white_24dp);

        add(new TitleItem(0));
        int themeNameRes = AppTheme.getNameResource(CarWidgetApplication.get(mContext).getThemeIdx());
        addButton(ID_THEME,R.string.app_theme,themeNameRes,R.drawable.ic_invert_colors_on_white_24dp);

        String musicApp = renderMusicApp();
        addButton(ID_MUSIC_APP,R.string.music_app,musicApp,R.drawable.ic_headset_white_24dp);

        String carDockApp=renderCarDockApp();
        addButton(ID_CAR_DOCK_APP,R.string.default_car_dock_app,carDockApp,R.drawable.ic_android_white_24dp);


        addTitle(R.string.information_title);
        String versionTitle = renderVersion();
        addButton(ID_VERSION,versionTitle,R.string.version_summary,R.drawable.ic_stars_white_24dp);
        addButton(ID_FEEDBACK,R.string.issue_title,0,R.drawable.ic_action_gplus);

    }

    private String renderCarDockApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_CAR_DOCK);
        final PackageManager pm = mContext.getPackageManager();
        final ResolveInfo info = pm.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);


        if (info == null || info.activityInfo.name.equals(RESOLVER_ACTIVITY)) {
            return mContext.getString(R.string.not_set);
        }
        return info.loadLabel(pm).toString();
    }


    private void addInCar(final int widgetsCount) {
        /* TODO: Custom title
        TextView trialText = (TextView)findViewById(R.id.incarTrial);
        LinearLayout incarHeader = (LinearLayout) findViewById(R.id.incarHeader);
        if (mVersion.isFreeAndTrialExpired()) {
            trialText.setText(getString(R.string.dialog_donate_title_expired) + " " + getString(R.string.notif_consider));
            incarHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(IntentUtils.createProVersionIntent());
                }
            });
        } else if (mVersion.isFree()) {
            String activationsLeft = getResources().getQuantityString(R.plurals.notif_activations_left, mVersion.getTrialTimesLeft(), mVersion.getTrialTimesLeft());
            trialText.setText(getString(R.string.dialog_donate_title_trial) + " " + activationsLeft);
            incarHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(IntentUtils.createProVersionIntent());
                }
            });
        } else {
            trialText.setVisibility(View.GONE);
        }
        */


    }

    private void addButton(int id, int titleRes, int summaryRes, int iconRes) {
        add(new ActionItem(id, titleRes, summaryRes, iconRes));
    }
    private void addButton(int id, int titleRes, String summary, int iconRes) {
        add(new ActionItem(id, titleRes, summary, iconRes));
    }
    private void addButton(int id, String title, int summaryRes, int iconRes) {
        add(new ActionItem(id, title, summaryRes, iconRes));
    }

    private void addTitle(String s) {
        add(new TitleItem(s));
    }
    private void addTitle(int titleId) {
        add(new TitleItem(titleId));
    }

    public boolean onClick(int id) {
        Intent intent;
        switch (id) {
            case ID_WIDGETS:
                if (mActivity instanceof MainActivity) {
                    return true;
                }
                mActivity.finish();
                mContext.startActivity(new Intent(mContext, MainActivity.class));
                return true;
            case ID_CAR_SETTINGS:
                mActivity.finish();
                intent = ConfigurationActivity.createFragmentIntent(mContext, ConfigurationInCar.class);
                mContext.startActivity(intent);
                return true;
            case ID_CAR_DOCK_APP:
                onCarDockAppClick();
                return false;
            case ID_MUSIC_APP:
                Intent musicAppsIntent = new Intent(mContext, MusicAppSettingsActivity.class);
                mContext.startActivity(musicAppsIntent);
                return false;
            case ID_VERSION:
                String url = DETAIL_MARKET_URL;
                Uri uri = Uri.parse(String.format(url, mContext.getPackageName()));
                intent = new Intent(Intent.ACTION_VIEW, uri);
                Utils.startActivitySafely(intent, mContext);
                return true;
            case ID_FEEDBACK:
                Uri feedbackUri = Uri.parse(URL_GOOGLE_PLUS);
                intent = new Intent(Intent.ACTION_VIEW, feedbackUri);
                mContext.startActivity(intent);
                return true;
            case ID_THEME:
                createThemesDialog().show();
                return false;
            case ID_BACKUP:
                intent = ConfigurationActivity.createFragmentIntent(mContext, ConfigurationRestore.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                mContext.startActivity(intent);
                return true;
        }
        return true;
    }


    protected AlertDialog createThemesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder
            .setTitle(mContext.getString(R.string.choose_a_theme))
            .setItems(R.array.app_themes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    AppTheme.saveAppTheme(mContext, which);
                    CarWidgetApplication.get(mContext).setThemeIdx(which);
                    mContext.setTheme(AppTheme.getMainResource(which));
                    mActivity.recreate();
                }
            });
        return builder.create();
    }

    private void onCarDockAppClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.default_car_dock_app, null);
        Button btn = (Button)dialogView.findViewById(R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_CAR_DOCK);
                final ResolveInfo info = mPackageManager.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);
                Utils.startActivitySafely(
                        IntentUtils.createApplicationDetailsIntent(info.activityInfo.applicationInfo.packageName), mContext
                );
            }
        });

        builder
                .setTitle(R.string.default_car_dock_app)
                .setCancelable(true)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private String renderMusicApp() {
        ComponentName musicAppCmp = PreferencesStorage.getMusicApp(mContext);
        if (musicAppCmp == null) {
            return mContext.getString(R.string.show_choice);
        } else {
            try {
                ApplicationInfo info = mPackageManager.getApplicationInfo(musicAppCmp.getPackageName(), 0);
                return info.loadLabel(mPackageManager).toString();
            } catch (PackageManager.NameNotFoundException e) {
                AppLog.ex(e);
                return musicAppCmp.flattenToShortString();
            }
        }
    }
    private String renderVersion() {
        String versionText = mContext.getString(R.string.version_title);
        String appName = mContext.getString(R.string.app_name);
        String versionName = "";
        try {
            versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.w(e.getMessage());
        }
        return String.format(versionText, appName, versionName);
    }

    private String renderActiveString(int widgetsCount,Version version) {
        String active;
        if (widgetsCount == 0) {
            active = mContext.getString(R.string.not_active);
        } else {

            if (version.isProOrTrial()) {
                if (PreferencesStorage.isInCarModeEnabled(mContext)){
                    active = mContext.getString(R.string.enabled);
                } else {
                    active = mContext.getString(R.string.disabled);
                }
            } else {
                active = mContext.getString(R.string.disabled);
            }
        }
        return active;
    }
}
