package com.anod.car.home.appwidget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.anod.car.home.ShortcutActivity;
import com.anod.car.home.incar.ModeService;
import com.anod.car.home.utils.IntentUtils;

public class ShortcutPendingIntent implements WidgetViewBuilder.PendingIntentHelper {
	public static final String INTENT_ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED";
	final private Context mContext;

	public ShortcutPendingIntent(Context context) {
		mContext = context;
	}

    @Override
    public PendingIntent createNew(int appWidgetId, int cellId) {
        Intent intent = IntentUtils.createNewShortcutIntent(mContext, appWidgetId, cellId);
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Create an Intent to launch Configuration
     */
    @Override
    public PendingIntent createSettings(int appWidgetId, int buttonId) {
		Intent intent = IntentUtils.createSettingsIntent(mContext, appWidgetId);
    	return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

	@Override
    public PendingIntent createShortcut(Intent intent,int appWidgetId, int position, long shortcutId) {
    	return createShortcut(intent,String.valueOf(appWidgetId),position);
    }
    
    public PendingIntent createShortcut(Intent intent,String prefix, int position) {
    	String action = intent.getAction();
    	boolean isCallPrivileged = (action != null && action.equals(INTENT_ACTION_CALL_PRIVILEGED));
    	if (intent.getExtras() == null && !isCallPrivileged) { // Samsung s3 bug
    		return PendingIntent.getActivity(mContext, 0 /* no requestCode */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    	}
    	String path = prefix + " - " + position;
    	Uri data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"),path);
    	
    	if (action != null && action.equals(ShortcutActivity.ACTION_MEDIA_BUTTON)) {
    		intent.setData(data);
    		return PendingIntent.getActivity(mContext, 0 /* no requestCode */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    	}
    	
    	Intent launchIntent = new Intent(mContext, ShortcutActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
    	launchIntent.setData(data);
    	launchIntent.setAction(Intent.ACTION_MAIN);
    	launchIntent.putExtra(ShortcutActivity.EXTRA_INTENT, intent);
		return PendingIntent.getActivity(mContext, 0 /* no requestCode */, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

	@Override
	public PendingIntent createInCar(boolean on, int buttonId) {
		if (on) {
			return getInCarOnIntent();
		}
		return getInCarOffIntent();
	}

	private PendingIntent getInCarOnIntent() {
		Intent onIntent = new Intent(mContext, ModeService.class);
		onIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_ON);
		onIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true);
		Uri data = Uri.parse("com.anod.car.home.pro://mode/1/1");
		onIntent.setData(data);
		return PendingIntent.getService(mContext, 0, onIntent, 0);
	}

	private PendingIntent getInCarOffIntent() {
		Intent offIntent = new Intent(mContext, ModeService.class);
		offIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_OFF);
		offIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true);
		Uri data = Uri.parse("com.anod.car.home.pro://mode/0/1");
		offIntent.setData(data);
		return PendingIntent.getService(mContext, 0, offIntent, 0);
	}
}
