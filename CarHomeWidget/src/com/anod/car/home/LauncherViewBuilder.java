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
import com.anod.car.home.model.ShortcutsModel;
import com.anod.car.home.model.LauncherShortcutsModel;
import com.anod.car.home.prefs.PickShortcutUtils;
import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.skin.IconProcessor;
import com.anod.car.home.skin.PropertiesFactory;
import com.anod.car.home.skin.SkinProperties;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

public class LauncherViewBuilder {
	private Context mContext;
	private int mAppWidgetId;
	
	public LauncherViewBuilder(Context context) {
		mContext = context;
	}
	
	public LauncherViewBuilder setAppWidgetId(int appWidgetId) {
		mAppWidgetId = appWidgetId;
		return this;
	}
	
    public RemoteViews build() {
    	
		ShortcutsModel smodel = new LauncherShortcutsModel(mContext, mAppWidgetId);
		if (PreferencesStorage.isFirstTime(mContext,mAppWidgetId)) {
			smodel.createDefaultShortcuts();
			PreferencesStorage.setFirstTime(false,mContext,mAppWidgetId);
		}
		smodel.init();
    	Main prefs = PreferencesStorage.loadMain(mContext, mAppWidgetId);
    	
    	Resources resources = mContext.getResources();
    	String skinName = prefs.getSkin();
    	
    	SkinProperties skinProperties = PropertiesFactory.create(skinName);
    	
        RemoteViews views =  new RemoteViews(mContext.getPackageName(), skinProperties.getLayout());

		String packageName = mContext.getPackageName();
		String type = "id";
		
		setInCarButton(prefs.isIncarTransparent(),packageName, skinProperties, views);
		
		if (prefs.isSettingsTransparent()) {
			views.setImageViewResource(R.id.btn_settings, R.drawable.btn_transparent);
		}
		
		SparseArray<ShortcutInfo> shortcuts = smodel.getShortcuts();

		setBackground(prefs,views);
		
		float iconScale = Utils.calcIconsScale(prefs.getIconsScale());
		float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
		
		for (int cellId = 0; cellId < shortcuts.size(); cellId++) {
			int res = resources.getIdentifier("btn" + cellId, type, packageName);
			int resText = resources.getIdentifier("btn_text" + cellId, type, packageName);
			ShortcutInfo info = smodel.getShortcut(cellId);
			if (info == null) {
				setNoShortcut(res, resText, views, cellId, skinProperties);
			} else {
				setShortcut(res, resText, iconScale, info, prefs, views, cellId, skinProperties);
			}
			setFont(prefs, res, resText, scaledDensity, views);
			if (skinName.equals(PreferencesStorage.SKIN_WINDOWS7)) {
				setTile(prefs.getTileColor(), res, views);
			}
		}

		PendingIntent configIntent = ShortcutPendingIntent.getSettingsPendingInent(mAppWidgetId, mContext, PickShortcutUtils.INVALID_CELL_ID);
		views.setOnClickPendingIntent(R.id.btn_settings, configIntent);
		return views;
	}
	

    
    private void setInCarButton(boolean isInCarTrans, String packageName, SkinProperties skinProp, RemoteViews views) {
		if (!Utils.isFreeVersion(packageName) && PreferencesStorage.isInCarModeEnabled(mContext)) {
			views.setViewVisibility(R.id.btn_incar_switch, View.VISIBLE);
			if (ModeService.sInCarMode == true) {
				if (isInCarTrans) {
					views.setImageViewResource(R.id.btn_incar_switch, R.drawable.btn_transparent);
				} else {
					int rImg = skinProp.getInCarButtonExitRes();
					views.setImageViewResource(R.id.btn_incar_switch, rImg);
				}
				Intent notificationIntent = new Intent(mContext, ModeService.class);
				notificationIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_OFF);
				notificationIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true);
		    	Uri data = Uri.parse("com.anod.car.home.pro://mode/0/1");
		    	notificationIntent.setData(data);
				PendingIntent contentIntent = PendingIntent.getService(mContext, 0, notificationIntent, 0);
        		views.setOnClickPendingIntent(R.id.btn_incar_switch, contentIntent);
			} else {
				if (isInCarTrans) {
					views.setImageViewResource(R.id.btn_incar_switch, R.drawable.btn_transparent);
				} else {
					int rImg = skinProp.getInCarButtonEnterRes();
					views.setImageViewResource(R.id.btn_incar_switch, rImg);
				}
				Intent notificationIntent = new Intent(mContext, ModeService.class);
				notificationIntent.putExtra(ModeService.EXTRA_MODE, ModeService.MODE_SWITCH_ON);
				notificationIntent.putExtra(ModeService.EXTRA_FORCE_STATE, true);
		    	Uri data = Uri.parse("com.anod.car.home.pro://mode/1/1");
		    	notificationIntent.setData(data);
				PendingIntent contentIntent = PendingIntent.getService(mContext, 0, notificationIntent, 0);
        		views.setOnClickPendingIntent(R.id.btn_incar_switch, contentIntent);
			}
		} else {
			views.setViewVisibility(R.id.btn_incar_switch, View.GONE);
		}

	}

	private void setFont(Main prefs,int res,int resText,float scaledDensity,RemoteViews views) {
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
       
    private void setTile(int tileColor, int res, RemoteViews views) {
		if (Color.alpha(tileColor) == 0) {
			views.setViewVisibility(res, View.GONE);
		} else {
			views.setViewVisibility(res, View.VISIBLE);
			views.setInt(res, "setBackgroundColor",  tileColor);
		}
    }
    
   
    private void setNoShortcut(int res, int resText, RemoteViews views, int cellId, SkinProperties skinProp) {
		views.setImageViewResource(res, skinProp.getSetShortcutRes());
    	String title = mContext.getResources().getString(skinProp.getSetShortcutText());
    	views.setTextViewText(resText, title);
        PendingIntent configIntent = ShortcutPendingIntent.getSettingsPendingInent(mAppWidgetId, mContext, cellId);
		views.setOnClickPendingIntent(res, configIntent);
		views.setOnClickPendingIntent(resText, configIntent);
    }
    
    private void setShortcut(int res, int resText, float scale, ShortcutInfo info, Main prefs,  RemoteViews views, int cellId, SkinProperties skinProp) {
		Bitmap icon = info.getIcon();
		if (prefs.isIconsMono()) {
			icon = UtilitiesBitmap.applyBitmapFilter(icon,mContext);
			if (prefs.getIconsColor() != null) {
				icon = UtilitiesBitmap.tint(icon, prefs.getIconsColor());
			}
		};
		IconProcessor ip = skinProp.getIconProcessor();
		if (ip != null) {
			icon = ip.process(icon);
		}
		if (scale > 1.0f) {
			icon = UtilitiesBitmap.scaleBitmap(icon,scale,mContext);
		}
    	views.setBitmap(res, "setImageBitmap", icon);
        String title = String.valueOf(info.title);
    	views.setTextViewText(resText, title);
		PendingIntent shortcutIntent = ShortcutPendingIntent.getShortcutPendingInent(info.intent, mAppWidgetId, mContext, cellId);
		views.setOnClickPendingIntent(res, shortcutIntent);
		views.setOnClickPendingIntent(resText, shortcutIntent);
    }
    
    private void setBackground(Main prefs, RemoteViews views) {
		int bgColor = prefs.getBackgroundColor();
		views.setInt(R.id.container, "setBackgroundColor",  bgColor);
    }

    
    
}
