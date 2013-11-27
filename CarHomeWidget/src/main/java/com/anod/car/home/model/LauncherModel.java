package com.anod.car.home.model;

import java.net.URISyntaxException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

public class LauncherModel {
	static final String TAG = "CarHomeWidget.Model";
	private final ContentResolver mContentResolver;
	private final PackageManager mPackageManager;
	private final Context mContext;
	public LauncherModel(Context context) {
		mContentResolver = context.getContentResolver();
		mPackageManager = context.getPackageManager();
		mContext = context;
	}
	
	public ShortcutInfo loadShortcut(long shortcutId) {
		String selection = LauncherSettings.Favorites._ID + "=?";
		String[] selectionArgs = { String.valueOf(shortcutId) };

		final Cursor c = mContentResolver.query(LauncherSettings.Favorites.getContentUri(mContext.getPackageName()), null, selection, selectionArgs, null);

		if (c == null) {
			return null;
		}

		ShortcutInfo info;
		try {
			final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
			c.moveToFirst();
			Intent intent;
			String intentDescription = c.getString(intentIndex);
			try {
				intent = Intent.parseUri(intentDescription, 0);
			} catch (URISyntaxException e) {
				c.close();
				return null;
			}

			final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
			final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
			final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE);
			final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
			final int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
			final int iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
			final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
			final int isCustomIconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.IS_CUSTOM_ICON);

			info = new ShortcutInfo();
			info.id = c.getLong(idIndex);
			info.title = c.getString(titleIndex);
			info.itemType = c.getInt(itemTypeIndex);
			info.intent = intent;
			
			Bitmap icon = null;
			if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
				icon = getIconFromCursor(c, iconIndex);
				info.setActivityIcon(icon);
				info.setCustomIcon(c.getInt(isCustomIconIndex) == 1);
			} else {
				int iconType = c.getInt(iconTypeIndex);
				if (iconType == LauncherSettings.Favorites.ICON_TYPE_RESOURCE) {
					String packageName = c.getString(iconPackageIndex);
					String resourceName = c.getString(iconResourceIndex);
					Intent.ShortcutIconResource iconResource = new Intent.ShortcutIconResource();
					iconResource.packageName = packageName;
					iconResource.resourceName = resourceName;
					// the resource
					try {
						Resources resources = mPackageManager.getResourcesForApplication(packageName);
						if (resources != null) {
							final int id = resources.getIdentifier(resourceName, null, null);
							if (id > 0) {
								icon = UtilitiesBitmap.createHiResIconBitmap(resources.getDrawable(id), mContext);
							}
						}
					} catch (NameNotFoundException e) {
						// drop this. we have other places to look for icons
						AppLog.d(e.getMessage());
					} catch (NotFoundException e) {
						AppLog.d(e.getMessage());
					}
					// the db
					if (icon == null) {
						icon = getIconFromCursor(c, iconIndex);
					}
					info.setIconResource(icon, iconResource);
				} else if (iconType == LauncherSettings.Favorites.ICON_TYPE_BITMAP) {
					icon = getIconFromCursor(c, iconIndex);
					if (icon != null) {
						info.setCustomIcon(icon);
					}
				}
			}

			if (icon == null) {
				icon = UtilitiesBitmap.makeDefaultIcon(mPackageManager);
				info.setFallbackIcon(icon);
			}
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
	 * Returns true if the shortcuts already exists in the database. we identify
	 * a shortcut by its title and intent.
	 */
	static boolean shortcutExists(Context context, String title, Intent intent) {
		final ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(LauncherSettings.Favorites.getContentUri(context.getPackageName()), new String[] { "title", "intent" }, "title=? and intent=?", new String[] { title, intent.toUri(0) }, null);
		boolean result = false;
		try {
			result = c.moveToFirst();
		} finally {
			c.close();
		}
		return result;
	}

	/**
	 * Add an item to the database in a specified container. Sets the container,
	 * screen, cellX and cellY fields of the item. Also assigns an ID to the
	 * item.
	 */
	public void addItemToDatabase(Context context, ShortcutInfo item, int cellId) {

		final ContentResolver cr = context.getContentResolver();
		final ContentValues values = createShortcutContentValues(item);

		Uri result = cr.insert(LauncherSettings.Favorites.getContentUri(context.getPackageName()), values);

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

		cr.update(LauncherSettings.Favorites.getContentUri(context.getPackageName(), item.id), values, null, null);
	}

	private ContentValues createShortcutContentValues(ShortcutInfo item) {
		final ContentValues values = new ContentValues();
		values.put(LauncherSettings.Favorites.ITEM_TYPE, item.itemType);

		String titleStr = item.title == null ? null : item.title.toString();
		values.put(LauncherSettings.Favorites.TITLE, titleStr);

		String uri = item.intent == null ? null : item.intent.toUri(0);
		values.put(LauncherSettings.Favorites.INTENT, uri);

		if (item.isCustomIcon()) {
			values.put(LauncherSettings.Favorites.ICON_TYPE, LauncherSettings.Favorites.ICON_TYPE_BITMAP);
			writeBitmap(values, item.getIcon());
		} else {
			if (!item.isUsingFallbackIcon()) {
				writeBitmap(values, item.getIcon());
			}
			values.put(LauncherSettings.Favorites.ICON_TYPE, LauncherSettings.Favorites.ICON_TYPE_RESOURCE);
			if (item.getIconResource() != null) {
				values.put(LauncherSettings.Favorites.ICON_PACKAGE, item.getIconResource().packageName);
				values.put(LauncherSettings.Favorites.ICON_RESOURCE, item.getIconResource().resourceName);
			}
		}
		values.put(LauncherSettings.Favorites.IS_CUSTOM_ICON,item.isCustomIcon() ? 1 : 0);
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
	 * 
	 * @param context
	 * @param item
	 */
	public void deleteItemFromDatabase(long shortcutId) {
		final Uri uriToDelete = LauncherSettings.Favorites.getContentUri(mContext.getPackageName(), shortcutId);
		new Runnable() {
			public void run() {
				mContentResolver.delete(uriToDelete, null, null);
			}
		};
	}

}