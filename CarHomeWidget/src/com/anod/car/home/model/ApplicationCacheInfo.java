package com.anod.car.home.model;


import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

/**
* Represents an app in AllAppsView.
*/
class ApplicationCacheInfo {
    /**
     * The application name.
     */
    CharSequence title;
	/**
     * The application icon.
     */
    Bitmap mIcon;
    /**
     * The intent used to start the application.
     */
    Intent intent;
    /**
     * A bitmap version of the application icon.
     */
    Bitmap iconBitmap;
    
    ComponentName componentName;
    /**
     * Must not hold the Context.
     */
    public ApplicationCacheInfo(ResolveInfo info, AppsListCache iconCache) {
        this.componentName = new ComponentName(
                info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name);

        this.setActivity(componentName,
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

    }
 
    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
    }    
}