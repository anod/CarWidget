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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.anod.car.home.actionbarcompat.ActionBarActivity;
import com.anod.car.home.appwidget.WidgetHelper;
import com.anod.car.home.prefs.ConfigurationInCar;
import com.anod.car.home.prefs.ConfigurationRestore;
import com.anod.car.home.prefs.TrialDialogs;
import com.anod.car.home.prefs.backup.PreferencesBackupManager;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.ui.WidgetsListActivity;
import com.anod.car.home.ui.WizardActivity;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.Utils;
import com.anod.car.home.utils.Version;

/**
 * @author alex
 * @date 5/22/13
 */
public class MainActivity extends ActionBarActivity {

	private static final String DETAIL_MARKET_URL = "market://details?id=%s";
	private static final String URL_GOOGLE_PLUS = "https://plus.google.com/118206296686390552505/";
	private static final String RESOLVER_ACTIVITY = "com.android.internal.app.ResolverActivity";

	private static final int DIALOG_WAIT = 1;
	private static final int DIALOG_PRO = 3;

	private Context mContext;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		mContext = this;

		int[] appWidgetIds = WidgetHelper.getAllWidgetIds(mContext);
		final int widgetsCount = appWidgetIds.length;

		if (widgetsCount == 0) {
			startWizard();
			return;
		}
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

	}

	private void startWizard() {
		Intent intent = new Intent(mContext, WizardActivity.class);
		startActivity(intent);
	}

	private void initInCar(final int widgetsCount) {
		LinearLayout settings = (LinearLayout) findViewById(R.id.incarSettings);
		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(mContext, ConfigurationInCar.class);
				startActivity(intent);
			}
		});

		final Version version = new Version(mContext);

		TextView active = (TextView)findViewById(R.id.incarActive);
		if (widgetsCount == 0) {
			active.setText(R.string.not_active);
		} else {

			if (version.isProOrTrial()) {
				if (PreferencesStorage.isInCarModeEnabled(this)){
					active.setText(R.string.enabled);
				} else {
					active.setText(R.string.disabled);
				}
			} else {
				active.setText(R.string.disabled_trial_expired);
			}
		}

		Button backup = (Button) findViewById(R.id.incarBackup);
		backup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new InCarBackupTask().execute("");
			}
		});

		Button restore = (Button) findViewById(R.id.incarRestore);
		restore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (version.isFree()) {
					showDialog(DIALOG_PRO);
				} else {
					Intent intentInCar = new Intent(mContext, ConfigurationRestore.class);
					intentInCar.putExtra(ConfigurationRestore.EXTRA_TYPE, ConfigurationRestore.TYPE_INCAR);
					startActivity(intentInCar);
				}
			}
		});
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
		String versionText = getResources().getString(R.string.version_title);
		String appName = "";
		String versionName = "";
		try {
			PackageManager pm = getPackageManager();
			appName = getApplicationInfo().loadLabel(pm).toString();
			versionName = pm.getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Utils.logw(e.getMessage());
		}
		TextView title = (TextView)version.findViewById(android.R.id.title);
		title.setText(String.format(versionText, appName, versionName));
		version.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String url = DETAIL_MARKET_URL;
				Uri uri = Uri.parse(String.format(url, getPackageName()));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
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

	public class InCarBackupTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_WAIT);
		}

		protected Integer doInBackground(String... filenames) {
			PreferencesBackupManager backupManager = new PreferencesBackupManager(mContext);
			return backupManager.doBackupInCar();
		}

		protected void onPostExecute(Integer result) {
			try {
				dismissDialog(DIALOG_WAIT);
			} catch (IllegalArgumentException e) {
				Utils.logd(e.getMessage());
			}
			Toast.makeText(mContext, mContext.getString(R.string.backup_done), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_WAIT) {
			ProgressDialog waitDialog = new ProgressDialog(this);
			waitDialog.setCancelable(true);
			String message = getResources().getString(R.string.please_wait);
			waitDialog.setMessage(message);
			return waitDialog;
		} else if (id == DIALOG_PRO) {
			return TrialDialogs.buildProOnlyDialog(this);
		}
		return null;
	}
}