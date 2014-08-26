package com.anod.car.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.prefs.ConfigurationActivity;
import com.anod.car.home.prefs.ConfigurationInCar;
import com.anod.car.home.prefs.preferences.AppTheme;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.ui.WidgetsListActivity;
import com.anod.car.home.ui.WizardActivity;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

/**
 * @author alex
 * @date 5/22/13
 */
public class MainActivity extends CarWidgetActivity {

	private static final String DETAIL_MARKET_URL = "market://details?id=%s";
	private static final String URL_GOOGLE_PLUS = "https://plus.google.com/118206296686390552505/";
	private static final String RESOLVER_ACTIVITY = "com.android.internal.app.ResolverActivity";

	private static final int DIALOG_WAIT = 1;
	private static final int DIALOG_PRO = 3;

	private Context mContext;
	private Version mVersion;
    private boolean mWizardShown;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		mVersion = new Version(this);

		mContext = this;

		int[] appWidgetIds = WidgetHelper.getAllWidgetIds(mContext);
		final int widgetsCount = appWidgetIds.length;

		boolean isFreeInstalled = !mVersion.isFree() && Utils.isFreeInstalled(this);

        if (savedInstanceState!=null) {
            mWizardShown = savedInstanceState.getBoolean("wizard-shown");
        }

		if (!mWizardShown && widgetsCount == 0 && !isFreeInstalled) {
            mWizardShown = true;
			startWizard();
			return;
		}

	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("wizard-shown", mWizardShown);
        super.onSaveInstanceState(outState);
    }

    @Override
	protected void onResume() {
		super.onResume();

		int[] appWidgetIds = WidgetHelper.getAllWidgetIds(mContext);
		final int widgetsCount = appWidgetIds.length;

		initWidgets(widgetsCount);
		initInCar(widgetsCount);
		initInformation();
		initDefaultApp();
		initAppTheme();

	}

	private void startWizard() {
		Intent intent = new Intent(mContext, WizardActivity.class);
		startActivity(intent);
	}

	private void initInCar(final int widgetsCount) {
		Button settings = (Button) findViewById(R.id.incarSettings);
		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			Intent intent = ConfigurationActivity.createFragmentIntent(mContext, ConfigurationInCar.class);
			startActivity(intent);
			}
		});

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

		String active = getActiveString(widgetsCount);

		TextView incarTitleView = (TextView)findViewById(R.id.incarTitle);
		incarTitleView.setText(getString(R.string.pref_incar_mode_title) + " - " + active);

	}

	private String getActiveString(int widgetsCount) {
		String active;
		if (widgetsCount == 0) {
			active = getString(R.string.not_active);
		} else {

			if (mVersion.isProOrTrial()) {
				if (PreferencesStorage.isInCarModeEnabled(this)){
					active = getString(R.string.enabled);
				} else {
					active = getString(R.string.disabled);
				}
			} else {
				active = getString(R.string.disabled);
			}
		}
		return active;
	}

	private void initWidgets(final int widgetsCount) {
		LinearLayout widgets = (LinearLayout)findViewById(R.id.widgets);

		TextView active = (TextView) widgets.findViewById(R.id.widgetsActive);
		active.setText(getResources().getQuantityString(R.plurals.active_widgets, widgetsCount, widgetsCount));

		widgets.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (widgetsCount > 0) {
					Intent intent = new Intent(mContext, WidgetsListActivity.class);
					startActivity(intent);
				} else {
					startWizard();
				}
			}
		});
	}

	private void initInformation() {
		LinearLayout version = (LinearLayout)findViewById(R.id.version);
		String versionText = getString(R.string.version_title);
		String appName = getString(R.string.app_name);
		String versionName = "";
		try {
			PackageManager pm = getPackageManager();
			versionName = pm.getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			AppLog.w(e.getMessage());
		}
		TextView title = (TextView)version.findViewById(android.R.id.title);
		title.setText(String.format(versionText, appName, versionName));
		version.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String url = DETAIL_MARKET_URL;
				Uri uri = Uri.parse(String.format(url, getPackageName()));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				Utils.startActivitySafely(intent, mContext);
			}
		});

		Button feedback = (Button) findViewById(R.id.feedback);
		feedback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Uri uri = Uri.parse(URL_GOOGLE_PLUS);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});

	}


	private void initDefaultApp() {
		LinearLayout defaultApp = (LinearLayout) findViewById(R.id.defaultApp);

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_CAR_DOCK);
		final PackageManager pm = getPackageManager();
		final ResolveInfo info = pm.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);


		TextView summary = (TextView) defaultApp.findViewById(android.R.id.summary);
		if (info == null || info.activityInfo.name.equals(RESOLVER_ACTIVITY)) {
			summary.setText(R.string.not_set);
		} else {
			summary.setText(info.loadLabel(pm));
		}

		defaultApp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

				View dialogView = getLayoutInflater().inflate(R.layout.default_car_dock_app, null);
				Button btn = (Button)dialogView.findViewById(R.id.button1);
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
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
		});

	}

	@Override
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_WAIT) {
			ProgressDialog waitDialog = new ProgressDialog(this);
			waitDialog.setCancelable(true);
			String message = getResources().getString(R.string.please_wait);
			waitDialog.setMessage(message);
			return waitDialog;
		}
		return null;
	}

	private void initAppTheme() {
		LinearLayout appTheme = (LinearLayout) findViewById(R.id.appTheme);
		final TextView summary =(TextView) appTheme.findViewById(android.R.id.summary);
		summary.setText(AppTheme.getNameResource(getApp().getThemeIdx()));
		appTheme.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				createThemesDialog(summary).show();
			}
		});
	}


	protected AlertDialog createThemesDialog(final TextView summary) {
		final MainActivity act = this;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setTitle(getString(R.string.choose_a_theme))
			.setItems(R.array.app_themes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					AppTheme.saveAppTheme(mContext, which);
					getApp().setThemeIdx(which);
					summary.setText(AppTheme.getNameResource(which));
					act.setTheme(AppTheme.getMainResource(which));
					act.recreate();
				}
			});
		return builder.create();
	}


}