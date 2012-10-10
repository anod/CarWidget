package com.anod.car.home.model;

import android.net.Uri;
import android.provider.BaseColumns;

import com.anod.car.home.utils.Version;


/**
 * Settings related utilities.
 */
public class LauncherSettings {
	public static final class Favorites implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
		static Uri getContentUri(String packageName) {
        	if (Version.isFreeVersion(packageName)) {
        		return Uri.parse("content://" +
	                LauncherProvider.AUTHORITY_FREE + "/" + LauncherProvider.TABLE_FAVORITES );
        	}
    		return Uri.parse("content://" +
	                LauncherProvider.AUTHORITY_PRO + "/" + LauncherProvider.TABLE_FAVORITES );
		}
        /**
         * The content:// style URL for a given row, identified by its id.
         *
         * @param id The row id.
         *
         * @return The unique content URL for the specified row.
         */
        static Uri getContentUri(String packageName,long id) {
        	if (Version.isFreeVersion(packageName)) {
                return Uri.parse("content://" + LauncherProvider.AUTHORITY_FREE +
                        "/" + LauncherProvider.TABLE_FAVORITES + "/" + id
                );        		
        	}
            return Uri.parse("content://" + LauncherProvider.AUTHORITY_PRO +
                    "/" + LauncherProvider.TABLE_FAVORITES + "/" + id
            );
        }
        
        /**
         * Descriptive name of the gesture that can be displayed to the user.
         * <P>Type: TEXT</P>
         */
        static final String TITLE = "title";
        /**
         * The Intent URL of the gesture, describing what it points to. This
         * value is given to {@link android.content.Intent#parseUri(String, int)} to create
         * an Intent that can be launched.
         * <P>Type: TEXT</P>
         */
        static final String INTENT = "intent";
        /**
         * The type of the gesture
         *
         * <P>Type: INTEGER</P>
         */
        static final String ITEM_TYPE = "itemType";
        /**
         * The gesture is an application
         */
        public static final int ITEM_TYPE_APPLICATION = 0;
        /**
         * The gesture is an application created shortcut
         */
        public static final int ITEM_TYPE_SHORTCUT = 1;
        /**
         * The icon type.
         * <P>Type: INTEGER</P>
         */
        static final String ICON_TYPE = "iconType";
        /**
         * The icon is a resource identified by a package name and an integer id.
         */
        static final int ICON_TYPE_RESOURCE = 0;
        /**
         * The icon is a bitmap.
         */
        static final int ICON_TYPE_BITMAP = 1;
        /**
         * The icon package name, if icon type is ICON_TYPE_RESOURCE.
         * <P>Type: TEXT</P>
         */
        static final String ICON_PACKAGE = "iconPackage";
        /**
         * The icon resource id, if icon type is ICON_TYPE_RESOURCE.
         * <P>Type: TEXT</P>
         */
        static final String ICON_RESOURCE = "iconResource";
        /**
         * The custom icon bitmap, if icon type is ICON_TYPE_BITMAP.
         * <P>Type: BLOB</P>
         */
        static final String ICON = "icon";
        /**
         * Type: BOOLEAN
         */
        static final String IS_CUSTOM_ICON = "isCustomIcon";

    }
}
