package com.anod.car.home.utils;

import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.anod.car.home.R;


public class Utils {
	
	final public static boolean IS_HONEYCOMB_OR_GREATER = (Build.VERSION.SDK_INT >= 11);
	
	public static final String PACKAGE_FREE = "com.anod.car.home.free";

	public static boolean isFreeVersion(String packageName) {
		return PACKAGE_FREE.equals(packageName);
	}

	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	public static float calcIconsScale(String scaleString) {
        return 1.0f+0.1f*Integer.valueOf(scaleString);
	}
	
    public static String componentToString(ComponentName component) {
    	return component.getPackageName() + "/" + component.getClassName();
    }
    
    public static ComponentName stringToComponent(String compString) {
        String[] compParts = compString.split("/");
        return new ComponentName(compParts[0],compParts[1]);
    }
    
    public static void startActivitySafely(Intent intent, Context context) {
        try {
        	context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(context, context.getString(R.string.activity_not_found), Toast.LENGTH_SHORT).show();
            Log.e("CarHomeWidget", "Widget does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }    
}
