package com.anod.car.home;

import java.util.ArrayList;

import com.anod.car.home.incar.ModeService;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class Launcher {
	public static final String PACKAGE_FREE = "com.anod.car.home.free";
	
	public static boolean isFreeVersion(String packageName) {
		return PACKAGE_FREE.equals(packageName);
	}
	
	private static int getSkinLayout(String skin) {
		if (skin.equals(PreferencesStorage.SKIN_CARHOME)) {
			return R.layout.carhome;
		} else if (skin.equals(PreferencesStorage.SKIN_WINDOWS7)) {
			return R.layout.windows7;			
		}
		return R.layout.glass;
	}
	
    public static RemoteViews update(int appWidgetId, Context context) {
    	
    	LauncherModel model = new LauncherModel();
		if (PreferencesStorage.isFirstTime(context,appWidgetId)) {
			model.initShortcuts(context,appWidgetId);
			PreferencesStorage.setFirstTime(false,context,appWidgetId);
		}
    	
    	Preferences.Main prefs = PreferencesStorage.loadMain(context, appWidgetId);
    	
    	Resources resources = context.getResources();
    	String skinName = prefs.getSkin();
        RemoteViews views =  new RemoteViews(context.getPackageName(), getSkinLayout(skinName));

		String packageName = context.getPackageName();
		String type = "id";
		
		setInCarButton(prefs.isIncarTransparent(),packageName, skinName, context, views);
		
		if (prefs.isSettingsTransparent()) {
			views.setImageViewResource(R.id.btn_settings, R.drawable.btn_transparent);
		}
		
        ArrayList<Long> launchers = prefs.getLauncherComponents();
        
		setBackground(prefs,views);
		
		float iconScale = Utils.calcIconsScale(prefs.getIconsScale());
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		
        for (int i=0;i<launchers.size();i++) {
        	int res = resources.getIdentifier("btn"+i, type, packageName);
        	int resText = resources.getIdentifier("btn_text"+i, type, packageName);
        	Long shortcutId = launchers.get(i);
        	ShortcutInfo info = null;
        	if (shortcutId != ShortcutInfo.NO_ID) {
        		info = model.loadShortcut(context, shortcutId);
        	}
        	if (info == null) {
                PendingIntent configIntent = getSettingsPendingInent(appWidgetId, context, i);
        		views.setOnClickPendingIntent(res, configIntent);
        	} else {
        		setShortcut(res,resText,iconScale,info,prefs,views,context,appWidgetId);
        	}
        	setFont(prefs,res,resText,scaledDensity,views);
        	if (prefs.getTileColor() != null) {
        		setTile(prefs.getTileColor(),res,views);
        	}
        }
    	
        PendingIntent configIntent = getSettingsPendingInent(appWidgetId, context, Configuration.INVALID_CELL_ID);
      	views.setOnClickPendingIntent(R.id.btn_settings, configIntent);
		return views;
	}
	
    private static void setInCarButton(boolean isInCarTrans, String packageName, String skinName,
			Context context, RemoteViews views) {
		if (!isFreeVersion(packageName) && PreferencesStorage.isInCarModeEnabled(context)) {
			views.setViewVisibility(R.id.btn_incar_switch, View.VISIBLE);
			if (ModeService.sInCarMode == true) {
				if (isInCarTrans) {
					views.setImageViewResource(R.id.btn_incar_switch, R.drawable.btn_transparent);
				} else {
					int rImg = (skinName.equals(PreferencesStorage.SKIN_WINDOWS7)) ? R.drawable.ic_incar_exit_win7 : R.drawable.ic_incar_exit;
					views.setImageViewResource(R.id.btn_incar_switch, rImg);
				}
				Intent notificationIntent = new Intent(context, ModeService.class);
				notificationIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_OFF);
		    	Uri data = Uri.parse("com.anod.car.home.pro://mode/0/");
		    	notificationIntent.setData(data);
				PendingIntent contentIntent = PendingIntent.getService(context, 0, notificationIntent, 0);
        		views.setOnClickPendingIntent(R.id.btn_incar_switch, contentIntent);
			} else {
				if (isInCarTrans) {
					views.setImageViewResource(R.id.btn_incar_switch, R.drawable.btn_transparent);
				} else {
					int rImg = (skinName.equals(PreferencesStorage.SKIN_WINDOWS7)) ? R.drawable.ic_incar_enter_win7 : R.drawable.ic_incar_enter;
					views.setImageViewResource(R.id.btn_incar_switch, rImg);
				}
				Intent notificationIntent = new Intent(context, ModeService.class);
				notificationIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_ON);
		    	Uri data = Uri.parse("com.anod.car.home.pro://mode/1/");
		    	notificationIntent.setData(data);
				PendingIntent contentIntent = PendingIntent.getService(context, 0, notificationIntent, 0);
        		views.setOnClickPendingIntent(R.id.btn_incar_switch, contentIntent);
			}
		} else {
			views.setViewVisibility(R.id.btn_incar_switch, View.GONE);
		}

	}

	private static void setFont(Preferences.Main prefs,int res,int resText,float scaledDensity,RemoteViews views) {
   		views.setTextColor(resText, prefs.getFontColor());
    	if (prefs.getFontSize() != PreferencesStorage.FONT_SIZE_UNDEFINED) {
    		if (prefs.getFontSize() == 0) {
    			views.setViewVisibility(resText, View.GONE);    			
    		} else {
    			/*
    			 * Limitation of RemoteViews to use setTextSize with only one argument
    			 * (without providing scale unit) 
    			 * size already in scaled pixel format so we revert it to pixels 
    			 * to get properly converted after re-applying setTextSize function
    			 */
    			float cSize = (float)prefs.getFontSize() /	scaledDensity;
    			
    			views.setFloat(resText, "setTextSize", cSize);	
    			views.setViewVisibility(resText, View.VISIBLE);
    		}
    	}
    		
    }
       
    private static void setTile(int tileColor, int res, RemoteViews views) {
		Log.d("Launcher.Update", " Tile color " + tileColor);
		if (Color.alpha(tileColor) == 0) {
			views.setViewVisibility(res, View.GONE);
		} else {
			views.setViewVisibility(res, View.VISIBLE);
			views.setInt(res, "setBackgroundColor",  tileColor);
		}
    }
    
    private static void setShortcut(int res, int resText, float scale, ShortcutInfo info, Preferences.Main prefs,  RemoteViews views, Context context, int appWidgetId) {
		Bitmap icon = info.getIcon();
		if (prefs.isIconsMono()) {
			icon = UtilitiesBitmap.applyBitmapFilter(icon,context);
			if (prefs.getIconsColor() != null) {
				icon = UtilitiesBitmap.tint(icon, prefs.getIconsColor());
			}
		};
		if (scale > 1.0f) {
			icon = UtilitiesBitmap.scaleBitmap(icon,scale,context);
		}
    	views.setBitmap(res, "setImageBitmap", icon);
        String title = String.valueOf(info.title);
    	views.setTextViewText(resText, title);
		PendingIntent shortcutIntent = getShortcutPendingInent(info.intent, appWidgetId, context);
		views.setOnClickPendingIntent(res, shortcutIntent);
    }
    
    private static void setBackground(Preferences.Main prefs, RemoteViews views) {
		int bgColor = prefs.getBackgroundColor();
		views.setInt(R.id.container, "setBackgroundColor",  bgColor);
    }
    /**
     * Create an Intent to launch Configuration
     * @param appWidgetId
     * @param context
     * @return
     */
    private static PendingIntent getSettingsPendingInent(int appWidgetId, Context context, int cellId) {
    	Intent intent = new Intent(context, Configuration.class);
    	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    	if (cellId != Configuration.INVALID_CELL_ID) {
    		intent.putExtra(Configuration.EXTRA_CELL_ID, cellId);
    	}
    	String path = String.valueOf(appWidgetId) + " - " + String.valueOf(cellId);
    	Uri data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"),path);
    	intent.setData(data);
    	intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
    	return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    /**
     * 
     * @param componentName
     * @param appWidgetId
     * @param context
     * @return
     */
    private static PendingIntent getShortcutPendingInent(Intent intent,int appWidgetId, Context context) {
        return PendingIntent.getActivity(context, 0 /* no requestCode */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    
}
