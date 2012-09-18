package com.anod.car.home.model;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.anod.car.home.prefs.PreferencesStorage;
import com.anod.car.home.utils.UtilitiesBitmap;

public class LauncherModel {
	static final String TAG = "CarHomeWidget.Model";

	public ShortcutInfo loadShortcut(Context context, long shortcutId) {
		final ContentResolver contentResolver = context.getContentResolver();
		final PackageManager manager = context.getPackageManager();

		String selection = LauncherSettings.Favorites._ID + "=?";
		String[] selectionArgs = { String.valueOf(shortcutId) };

		ShortcutInfo info = null;

		final Cursor c = contentResolver.query(LauncherSettings.Favorites.getContentUri(context.getPackageName()), null, selection, selectionArgs, null);

		if (c == null) {
			return null;
		}

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
			info.title = c.getString(titleIndex);
			info.itemType = c.getInt(itemTypeIndex);
			info.intent = intent;

			if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
				icon = getIconFromCursor(c, iconIndex);
				info.setActivityIcon(icon);
			} else {
				int iconType = c.getInt(iconTypeIndex);
				switch (iconType) {
				case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
					String packageName = c.getString(iconPackageIndex);
					String resourceName = c.getString(iconResourceIndex);

					Intent.ShortcutIconResource iconResource = new Intent.ShortcutIconResource();
					iconResource.packageName = packageName;
					iconResource.resourceName = resourceName;

					// the resource
					try {
						Resources resources = manager.getResourcesForApplication(packageName);
						if (resources != null) {
							final int id = resources.getIdentifier(resourceName, null, null);
							icon = UtilitiesBitmap.createIconBitmap(resources.getDrawable(id), context);
						}
					} catch (Exception e) {
						// drop this. we have other places to look for icons
					}
					// the db
					if (icon == null) {
						icon = getIconFromCursor(c, iconIndex);
					}
					info.setIconResource(icon, iconResource);
					break;
				case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
					icon = getIconFromCursor(c, iconIndex);
					if (icon != null) {
						info.setCustomIcon(icon);
					}
					break;
				}
			}

			if (icon == null) {
				icon = UtilitiesBitmap.makeDefaultIcon(manager);
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

		String titleStr = item.title != null ? item.title.toString() : null;
		values.put(LauncherSettings.Favorites.TITLE, titleStr);

		String uri = item.intent != null ? item.intent.toUri(0) : null;
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
	public void deleteItemFromDatabase(Context context, long shortcutId) {
		final ContentResolver cr = context.getContentResolver();
		final Uri uriToDelete = LauncherSettings.Favorites.getContentUri(context.getPackageName(), shortcutId);
		new Runnable() {
			public void run() {
				cr.delete(uriToDelete, null, null);
			}
		};
	}

}