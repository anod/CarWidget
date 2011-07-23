package com.anod.car.home.model;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.anod.car.home.model.views.FastBitmapDrawable;
import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.utils.UtilitiesBitmap;

public class LauncherModel {
    static final String TAG = "CarHomeWidget.Model";

    private Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo, PackageManager manager, Context context) {

        if (resolveInfo == null || component == null) {
            return null;
        }

        return UtilitiesBitmap.createIconBitmap(
        		resolveInfo.activityInfo.loadIcon(manager), context
   	   );
    }
    
    public ShortcutInfo addShortcut(Context context, Intent data, int cellId, long appWidgetId, boolean isAppShortcut) {
    	ShortcutInfo info = null;
    	if (isAppShortcut) {
    		info = infoFromApplicationIntent(context, data);
    	} else {
    		info = infoFromShortcutIntent(context, data);
    	}
    	if (info != null) {
    		addItemToDatabase(context, info, cellId, appWidgetId);
    	}
        return info;
    }
    
    private ShortcutInfo infoFromShortcutIntent(Context context, Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Bitmap icon = null;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = UtilitiesBitmap.createIconBitmap(new FastBitmapDrawable((Bitmap)bitmap), context);
            customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = UtilitiesBitmap.createIconBitmap(resources.getDrawable(id), context);
                } catch (Exception e) {
                    Log.w(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        final ShortcutInfo info = new ShortcutInfo();

        if (icon == null) {
        	final PackageManager packageManager = context.getPackageManager();
            icon = UtilitiesBitmap.makeDefaultIcon(packageManager);
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);

        info.title = name;
        info.intent = intent;
        info.customIcon = customIcon;
        info.iconResource = iconResource;

        return info;
    }
    
    
    public void initShortcuts(Context context,int appWidgetId) {
    	ComponentName s1 = new ComponentName("com.google.android.apps.maps", "com.google.android.maps.driveabout.app.DestinationActivity");
    	ComponentName s2 = new ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity");
    	ComponentName s3 = new ComponentName("com.android.music", "com.android.music.MusicBrowserActivity");
    	ComponentName s4 = new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity");
//    	ComponentName s2 = new ComponentName("com.waze", "com.waze.FreeMapAppActivity");
//    	ComponentName s4 = new ComponentName("com.maxmpz.audioplayer", "com.maxmpz.audioplayer.StartupActivity");
//    	ComponentName s5 = new ComponentName("tunein.player", "tunein.player.Main");

    	ArrayList<ComponentName> list = new ArrayList<ComponentName>(4);
    	list.add(s1);
    	list.add(s2);
    	list.add(s3);
    	list.add(s4);
        //Intent localIntent = new Intent("android.speech.action.WEB_SEARCH");
    	int cellId = 0;
    	for (int i=0; i<list.size(); i++) {
    		ShortcutInfo info = null;
    		Intent data = new Intent();
    		data.setComponent(list.get(i));
    		if (!isIntentAvailable(context,data))
    			continue;
    		info = infoFromApplicationIntent(context, data);
    		Log.d("CarHomeWidget", "Init shortcut - " + info + " Widget - " + appWidgetId);
    		addItemToDatabase(context, info, cellId, appWidgetId);
    		PreferencesStorage.saveShortcut(context,info.id,cellId,appWidgetId);
    		cellId++;
    	}

    }
    
    private static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    
    public ShortcutInfo loadShortcut(Context context, long shortcutId) {
        final ContentResolver contentResolver = context.getContentResolver();
        final PackageManager manager = context.getPackageManager();

        String selection = LauncherSettings.Favorites._ID + "=?";
        String[] selectionArgs = { String.valueOf(shortcutId) };
        
        ShortcutInfo info = null;
        
        final Cursor c = contentResolver.query(
                LauncherSettings.Favorites.getContentUri(context.getPackageName()), null, selection, selectionArgs, null
        );

        try {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
            final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
            final int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
            final int iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            
            c.moveToFirst();
            Intent intent;
            Bitmap icon = null;
            String intentDescription = c.getString(intentIndex);
            try {
            	intent = Intent.parseUri(intentDescription, 0);
            } catch (URISyntaxException e) {
            	c.close();
            	return null;
            }
            
            info = new ShortcutInfo();
            info.id = c.getLong(idIndex);
            info.title =  c.getString(titleIndex);
            info.itemType = c.getInt(itemTypeIndex);
            info.intent = intent;
            
            if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
            	icon = getIconFromCursor(c, iconIndex);

            	info.setIcon(icon);
            } else {
            	info.customIcon = false;
            	int iconType = c.getInt(iconTypeIndex);
                switch (iconType) {
                	case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
                		String packageName = c.getString(iconPackageIndex);
                		String resourceName = c.getString(iconResourceIndex);

                       	info.iconResource = new Intent.ShortcutIconResource();
                       	info.iconResource.packageName = packageName;
                       	info.iconResource.resourceName = resourceName;
                       	
                		// the resource
                		try {
                			Resources resources = manager.getResourcesForApplication(packageName);
                			if (resources != null) {
                            	final int id = resources.getIdentifier(resourceName, null, null);
                            	icon = UtilitiesBitmap.createIconBitmap(resources.getDrawable(id), context);
                        	}
                		} catch (Exception e) {
                			// drop this.  we have other places to look for icons	
                		}
                		// the db
                		if (icon == null) {
                			icon = getIconFromCursor(c, iconIndex);
                		}
                    break;
                	case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
                		icon = getIconFromCursor(c, iconIndex);
                		if (icon == null) {
                			info.customIcon = false;
                		} else {
                			info.customIcon = true;
                		}
                    break;
                }
            }
                
            if (icon == null) {
            	icon = UtilitiesBitmap.makeDefaultIcon(manager);
                info.usingFallbackIcon = true;
            }
            info.setIcon(icon);
        } finally {
            c.close();
        }

        return info;
    }
    
    Bitmap getIconFromCursor(Cursor c, int iconIndex) {
        byte[] data = c.getBlob(iconIndex);
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
    }
    
      
    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public ShortcutInfo infoFromApplicationIntent(Context context, Intent intent) {
        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }
        Log.d("CarHomeWidget", "Component Name - " + componentName);
        
        final PackageManager manager = context.getPackageManager();
        
        info.setActivity(componentName, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // TODO: See if the PackageManager knows about this case.  If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.
        final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);
        if (resolveInfo != null) {
            icon = getIcon(componentName, resolveInfo, manager, context);
        }
        // the fallback icon
        if (icon == null) {
            icon = UtilitiesBitmap.makeDefaultIcon(manager);
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);

        // from the resource
        if (resolveInfo != null) {
            info.title = resolveInfo.activityInfo.loadLabel(manager);
        }

        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }
    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExists(Context context, String title, Intent intent) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.getContentUri(context.getPackageName()),
            new String[] { "title", "intent" }, "title=? and intent=?",
            new String[] { title, intent.toUri(0) }, null);
        boolean result = false;
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }
        return result;
    }

     /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    public void addItemToDatabase(Context context, ShortcutInfo item, int cellId, long appWidgetId) {

        final ContentResolver cr = context.getContentResolver();
        final ContentValues values = createShortcutContentValues(item);

        Uri result = cr.insert(LauncherSettings.Favorites.getContentUri(context.getPackageName()),values);

        if (result != null) {
            item.id = Integer.parseInt(result.getPathSegments().get(1));
        }
    }

    /**
     * Update an item to the database in a specified container.
     */
    public void updateItemInDatabase(Context context, ShortcutInfo item) {
        final ContentResolver cr = context.getContentResolver();

        final ContentValues values = createShortcutContentValues(item);

        cr.update(LauncherSettings.Favorites.getContentUri(context.getPackageName(),item.id), values, null, null);
    }

    private ContentValues createShortcutContentValues(ShortcutInfo item) {
        final ContentValues values = new ContentValues();
    	values.put(LauncherSettings.Favorites.ITEM_TYPE, item.itemType);

        String titleStr = item.title != null ? item.title.toString() : null;
        values.put(LauncherSettings.Favorites.TITLE, titleStr);

        String uri = item.intent != null ? item.intent.toUri(0) : null;
        values.put(LauncherSettings.Favorites.INTENT, uri);

        if (item.customIcon) {
            values.put(LauncherSettings.Favorites.ICON_TYPE,
                    LauncherSettings.Favorites.ICON_TYPE_BITMAP);
            writeBitmap(values, item.getIcon());
        } else {
            if (!item.usingFallbackIcon) {
                writeBitmap(values, item.getIcon());
            }
            values.put(LauncherSettings.Favorites.ICON_TYPE,
                    LauncherSettings.Favorites.ICON_TYPE_RESOURCE);
            if (item.iconResource != null) {
                values.put(LauncherSettings.Favorites.ICON_PACKAGE,
                		item.iconResource.packageName);
                values.put(LauncherSettings.Favorites.ICON_RESOURCE,
                		item.iconResource.resourceName);
            }
        }
        return values;
    }    

    private static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = UtilitiesBitmap.flattenBitmap(bitmap);
            values.put(LauncherSettings.Favorites.ICON, data);
        }
    }
    
    /**
     * Removes the specified item from the database
     * @param context
     * @param item
     */
    public static void deleteItemFromDatabase(Context context, long shortcutId) {
        final ContentResolver cr = context.getContentResolver();
        final Uri uriToDelete = LauncherSettings.Favorites.getContentUri(context.getPackageName(),shortcutId);
        new Runnable() {
                public void run() {
                    cr.delete(uriToDelete, null, null);
                }
        };
    }

}