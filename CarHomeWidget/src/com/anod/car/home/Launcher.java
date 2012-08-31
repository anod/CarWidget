package com.anod.car.home;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import com.anod.car.home.incar.ModeService;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.prefs.Configuration;
import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.prefs.ShortcutModel;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

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
    	
		ShortcutModel smodel = new ShortcutModel(context, appWidgetId);
		if (PreferencesStorage.isFirstTime(context,appWidgetId)) {
			smodel.createDefaultShortcuts();
			PreferencesStorage.setFirstTime(false,context,appWidgetId);
		}
		smodel.init();
    	Main prefs = PreferencesStorage.loadMain(context, appWidgetId);
    	
    	Resources resources = context.getResources();
    	String skinName = prefs.getSkin();
        RemoteViews views =  new RemoteViews(context.getPackageName(), getSkinLayout(skinName));

		String packageName = context.getPackageName();
		String type = "id";
		
		setInCarButton(prefs.isIncarTransparent(),packageName, skinName, context, views);
		
		if (prefs.isSettingsTransparent()) {
			views.setImageViewResource(R.id.btn_settings, R.drawable.btn_transparent);
		}
		
		SparseArray<ShortcutInfo> shortcuts = smodel.getShortcuts();

		setBackground(prefs,views);
		
		float iconScale = Utils.calcIconsScale(prefs.getIconsScale());
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		
		for (int cellId = 0; cellId < shortcuts.size(); cellId++) {
			int res = resources.getIdentifier("btn" + cellId, type, packageName);
			int resText = resources.getIdentifier("btn_text" + cellId, type, packageName);
			ShortcutInfo info = smodel.getShortcut(cellId);
			if (info == null) {
				setNoShortcut(res, resText, views, context, appWidgetId, cellId);
			} else {
				setShortcut(res, resText, iconScale, info, prefs, views, context, appWidgetId, cellId);
			}
			setFont(prefs, res, resText, scaledDensity, views);
			if (prefs.getTileColor() != null) {
				setTile(prefs.getTileColor(), res, views);
			}
		}

		PendingIntent configIntent = ShortcutPendingIntent.getSettingsPendingInent(appWidgetId, context, Configuration.INVALID_CELL_ID);
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
				notificationIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true);
		    	Uri data = Uri.parse("com.anod.car.home.pro://mode/0/1");
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
				notificationIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true);
		    	Uri data = Uri.parse("com.anod.car.home.pro://mode/1/1");
		    	notificationIntent.setData(data);
				PendingIntent contentIntent = PendingIntent.getService(context, 0, notificationIntent, 0);
        		views.setOnClickPendingIntent(R.id.btn_incar_switch, contentIntent);
			}
		} else {
			views.setViewVisibility(R.id.btn_incar_switch, View.GONE);
		}

	}

	private static void setFont(Main prefs,int res,int resText,float scaledDensity,RemoteViews views) {
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
    
    private static void setNoShortcut(int res, int resText, RemoteViews views, Context context, int appWidgetId, int cellId) {
    	views.setImageViewResource(res, R.drawable.ic_add_shortcut);
    	String title = context.getResources().getString(R.string.set_shortcut);
    	views.setTextViewText(resText, title);
        PendingIntent configIntent = ShortcutPendingIntent.getSettingsPendingInent(appWidgetId, context, cellId);
		views.setOnClickPendingIntent(res, configIntent);    	
    }
    
    private static void setShortcut(int res, int resText, float scale, ShortcutInfo info, Main prefs,  RemoteViews views, Context context, int appWidgetId, int cellId) {
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
		PendingIntent shortcutIntent = ShortcutPendingIntent.getShortcutPendingInent(info.intent, appWidgetId, context, cellId);
		views.setOnClickPendingIntent(res, shortcutIntent);
    }
    
    private static void setBackground(Main prefs, RemoteViews views) {
		int bgColor = prefs.getBackgroundColor();
		views.setInt(R.id.container, "setBackgroundColor",  bgColor);
    }

    
    
}
