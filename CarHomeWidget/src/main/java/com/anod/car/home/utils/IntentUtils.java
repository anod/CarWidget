package com.anod.car.home.utils;

import com.anod.car.home.ShortcutActivity;
import com.anod.car.home.app.NewShortcutActivity;
import com.anod.car.home.incar.SwitchInCarActivity;
import com.anod.car.home.prefs.CarWidgetShortcutsPicker;
import com.anod.car.home.prefs.LookAndFeelActivity;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ImageView;

import java.util.List;

public class IntentUtils {

    private static final String SCHEME = "package";

    private static final String DETAIL_MARKET_URL = "market://details?id=%s";
    public static final int IDX_SWITCH_CAR_MODE = 0;
    public static final int IDX_DIRECT_CALL = 1;


    public static Intent createNewShortcutIntent(Context context, int appWidgetId, int cellId) {
        Intent intent = new Intent(context, NewShortcutActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(ShortcutPicker.EXTRA_CELL_ID, cellId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
        String path = appWidgetId + "/" + cellId;
        Uri data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"), path);
        intent.setData(data);
        return intent;
    }

    public static Intent createSettingsIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, LookAndFeelActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        Uri data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"),
                String.valueOf(appWidgetId));
        intent.setData(data);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent createApplicationDetailsIntent(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts(SCHEME, packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }


    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty();
    }


    public static Intent createProVersionIntent() {
        String url = DETAIL_MARKET_URL;
        Uri uri = Uri.parse(String.format(url, Version.PRO_PACKAGE_NAME));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        return intent;
    }

    private static Intent createShortcutIntent(Context context, int i) {

        switch (i) {
            case IDX_SWITCH_CAR_MODE:
                return new Intent(context, SwitchInCarActivity.class);
            case IDX_DIRECT_CALL:
                return new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            case 2:
                return createMediaButtonIntent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            case 3:
                return createMediaButtonIntent(context, KeyEvent.KEYCODE_MEDIA_NEXT);
            case 4:
                return createMediaButtonIntent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            default:
        }
        return null;
    }

    public static Intent createMediaButtonIntent(Context context, int keyCode) {
        Intent shortcutIntent = new Intent(context, ShortcutActivity.class);
        shortcutIntent.setAction(ShortcutActivity.ACTION_MEDIA_BUTTON);
        shortcutIntent.putExtra(ShortcutActivity.EXTRA_MEDIA_BUTTON, keyCode);

        return shortcutIntent;
    }

    public static Intent createPickShortcutLocalIntent(int i, String title, int icnResId,
            Context ctx) {
        Intent shortcutIntent = IntentUtils.createShortcutIntent(ctx, i);
        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent = commonPickShortcutIntent(title, shortcutIntent);
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(ctx, icnResId);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        return intent;
    }

    private static Intent commonPickShortcutIntent(String title, Intent shortcutIntent) {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        return intent;
    }

    public static Intent createDirectCallIntent(Uri contactUri, Context context) {
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        };
        Cursor cursor = context.getContentResolver().query(contactUri, projection,
                null, null, null);

        // If the cursor returned is valid, get the phone number
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);

                String number = cursor.getString(numberIndex);
                String name = cursor.getString(nameIndex);
                String photoUri = cursor.getString(photoIndex);

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+number));

                Intent intent = commonPickShortcutIntent(name, callIntent);
                if (!TextUtils.isEmpty(photoUri))
                {
                    Drawable d = new DrawableUri(context).resolve(Uri.parse(photoUri));
                    if (d != null)
                    {
                        Bitmap bitmap = UtilitiesBitmap.createHiResIconBitmap(d, context);
                        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
                    }
                }
                return intent;
            }
            cursor.close();
        }
        return null;
    }
}