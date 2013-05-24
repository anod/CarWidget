package com.anod.car.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.Utils;

/**
 * @author alex
 * @date 5/22/13
 */
public class MainActivity extends Activity {

	private static final String DETAIL_MARKET_URL = "market://details?id=%s";
	public static final String URL_GOOGLE_PLUS = "https://plus.google.com/118206296686390552505/";
	public static final String RESOLVER_ACTIVITY = "com.android.internal.app.ResolverActivity";
	private Context mContext;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activty);
		mContext = this;
		initInformation();
		initDefaultApp();
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
				Button btn = (Button)view.findViewById(R.id.button1);
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
}