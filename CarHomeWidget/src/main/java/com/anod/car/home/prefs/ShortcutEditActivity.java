package com.anod.car.home.prefs;

import com.anod.car.home.R;
import com.anod.car.home.model.AbstractShortcutsContainerModel;
import com.anod.car.home.model.LauncherSettings;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutInfoUtils;
import com.anod.car.home.model.ShortcutModel;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.IconPackUtils;
import com.anod.car.home.utils.ShortcutPicker;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ShortcutEditActivity extends ActionBarActivity {

    private static final String MIME_IMAGE = "image/*";

    public static final String EXTRA_SHORTCUT_ID = "extra_id";

    public static final String EXTRA_CELL_ID = "extra_cell_id";

    private static final int PICK_CUSTOM_ICON = 0;

    private static final int PICK_ADW_ICON_PACK = 1;

    private static final int PICK_DEFAULT_ICON = 2;

    private Bitmap mCustomIcon;

    @InjectView(R.id.icon_edit)
    ImageView mIconView;

    @InjectView(R.id.label_edit)
    EditText mLabelEdit;

    private ShortcutModel mModel;

    private ShortcutInfo mShortcutInfo;

    private Intent mIntent;

    private Bitmap mIconDefault;

    private AbstractShortcutsContainerModel mContainerModel;

    private int mCellId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcutedit);
        setTitle(R.string.shortcut_edit_title);

        ButterKnife.inject(this);

        init(getIntent());
    }

    private void init(Intent intent) {
        mIntent = intent;
        mCellId = intent
                .getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, ShortcutPicker.INVALID_CELL_ID);
        final long shortcutId = mIntent
                .getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, ShortcutInfo.NO_ID);
        final int appWidgetId = mIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mCellId == ShortcutPicker.INVALID_CELL_ID || shortcutId == ShortcutInfo.NO_ID) {
            AppLog.e("Missing parameter");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            mContainerModel = new NotificationShortcutsModel(this);
        } else {
            mContainerModel = new WidgetShortcutsModel(this, appWidgetId);
        }
        mContainerModel.init();
        mModel = mContainerModel.getShortcutModel();

        mShortcutInfo = mModel.loadShortcut(shortcutId);
        mLabelEdit.setText(mShortcutInfo.title);
        mIconView.setImageBitmap(mShortcutInfo.getIcon());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        init(intent);
    }

    protected Dialog createIconMenu() {
        final CharSequence[] items;
        if (mShortcutInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
            items = new CharSequence[3];
            items[PICK_CUSTOM_ICON] = getString(R.string.icon_custom);
            items[PICK_ADW_ICON_PACK] = getString(R.string.icon_adw_icon_pack);
            items[PICK_DEFAULT_ICON] = getString(R.string.icon_default);
        } else {
            items = new CharSequence[2];
            items[PICK_CUSTOM_ICON] = getString(R.string.icon_custom);
            items[PICK_ADW_ICON_PACK] = getString(R.string.icon_adw_icon_pack);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_select));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                iconDialogClick(item);
            }
        });
        return builder.create();
    }

    private void iconDialogClick(final int item) {
        Intent chooseIntent;
        if (item == PICK_CUSTOM_ICON) {
            File tempFile = getFileStreamPath("tempImage");
            chooseIntent = new Intent(Intent.ACTION_GET_CONTENT);
            chooseIntent.setType(MIME_IMAGE);
            chooseIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
            chooseIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.name());
            Utils.startActivityForResultSafetly(chooseIntent, PICK_CUSTOM_ICON, this);
        } else if (item == PICK_ADW_ICON_PACK) {
            chooseIntent = new Intent();
            IconPackUtils.fillAdwIconPackIntent(chooseIntent);
            Utils.startActivityForResultSafetly(
                    Intent.createChooser(chooseIntent, getString(R.string.select_icon_pack)),
                    PICK_ADW_ICON_PACK, this);
        } else if (item == PICK_DEFAULT_ICON) {

            ComponentName componentName = mShortcutInfo.intent.getComponent();
            if (componentName == null) {
                Toast.makeText(this, R.string.failed_fetch_icon, Toast.LENGTH_LONG).show();
                return;
            }
            final PackageManager manager = getPackageManager();
            final ResolveInfo resolveInfo = manager.resolveActivity(mShortcutInfo.intent, 0);
            Bitmap icon = ShortcutInfoUtils.getIcon(componentName, resolveInfo, manager, this);
            if (icon != null) {
                mIconDefault = icon;
                mCustomIcon = null;
                mIconView.setImageBitmap(icon);
            } else {
                Toast.makeText(this, R.string.failed_fetch_icon, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateCustomIcon(final Intent data) {
        Uri imageUri = data.getData();
        Drawable icon = resolveUri(imageUri);
        if (icon == null) {
            final String errStr = getString(R.string.error_text,
                    getString(R.string.custom_image_error));
            Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
            return;
        }
        Bitmap bitmap = UtilitiesBitmap.createMaxSizeIcon(icon, this);
        setCustomIcon(bitmap);
    }

    /**
     * Source android.widget.ImageView
     */
    private Drawable resolveUri(Uri uri) {
        Drawable d = null;
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            d = getDrawableByUri(uri);
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme) || ContentResolver.SCHEME_FILE
                .equals(scheme)) {
            try {

                int maxIconSize = UtilitiesBitmap.getIconMaxSize(this);

                Bitmap bmp = decodeSampledBitmapFromStream(uri, maxIconSize, maxIconSize);
                DisplayMetrics dm = getResources().getDisplayMetrics();
                bmp.setDensity(dm.densityDpi);
                d = new BitmapDrawable(getResources(), bmp);
            } catch (Exception e) {
                Log.w("ShortcutEditActivity", "Unable to open content: " + uri, e);
            }

        } else {
            d = Drawable.createFromPath(uri.toString());
        }

        return d;
    }

    public Bitmap decodeSampledBitmapFromStream(Uri uri, int reqWidth, int reqHeight)
            throws FileNotFoundException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        InputStream is = getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(is, null, options);
        closeStream(is);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        is = getContentResolver().openInputStream(uri);
        Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
        closeStream(is);

        return bmp;
    }

    static void closeStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private Drawable getDrawableByUri(Uri uri) {
        Drawable d = null;
        try {
            // Load drawable through Resources, to get the source density information
            OpenResourceIdResult r = getResourceId(uri);
            d = r.r.getDrawableForDensity(r.id, UtilitiesBitmap.getTargetDensity(this));
        } catch (Exception e) {
            Log.w("ShortcutEditActivity", "Unable to open content: " + uri, e);
        }
        return d;
    }

    public class OpenResourceIdResult {

        public Resources r;

        public int id;
    }

    /**
     * From android.content.ContentResolver
     */
    public OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        String authority = uri.getAuthority();
        Resources r;
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        } else {
            try {
                r = getPackageManager().getResourcesForApplication(authority);
            } catch (NameNotFoundException ex) {
                throw new FileNotFoundException("No package found for authority: " + uri);
            }
        }
        List<String> path = uri.getPathSegments();
        if (path == null) {
            throw new FileNotFoundException("No path: " + uri);
        }
        int len = path.size();
        int id;
        if (len == 1) {
            try {
                id = Integer.parseInt(path.get(0));
            } catch (NumberFormatException e) {
                throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
            }
        } else if (len == 2) {
            id = r.getIdentifier(path.get(1), path.get(0), authority);
        } else {
            throw new FileNotFoundException("More than two path segments: " + uri);
        }
        if (id == 0) {
            throw new FileNotFoundException("No resource found for: " + uri);
        }
        OpenResourceIdResult res = new OpenResourceIdResult();
        res.r = r;
        res.id = id;
        return res;
    }


    @OnClick(R.id.icon_edit)
    public void changeIcon(View view) {
        createIconMenu().show();
    }

    private void setCustomIcon(Bitmap icon) {
        mCustomIcon = icon;
        mIconDefault = null;
        mIconView.setImageBitmap(mCustomIcon);
    }

    @OnClick(R.id.btn_ok)
    public void clickedOk(View view) {
        boolean needUpdate = false;
        if (mCustomIcon != null) {
            mShortcutInfo.setCustomIcon(mCustomIcon);
            needUpdate = true;
        } else if (mIconDefault != null) {
            mShortcutInfo.setActivityIcon(mIconDefault);
            needUpdate = true;
        }
        String title = mLabelEdit.getText().toString();
        if (!title.equals(mShortcutInfo.title)) {
            mShortcutInfo.title = title;
            needUpdate = true;
        }
        if (needUpdate) {
            mModel.updateItemInDatabase(this, mShortcutInfo);
        }

        setResult(RESULT_OK, mIntent);
        finish();
    }

    @OnClick(R.id.btn_delete)
    public void onDeleteClick(View view) {
        mContainerModel.dropShortcut(mCellId);
        setResult(RESULT_OK, mIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_CUSTOM_ICON) {
                updateCustomIcon(data);
            } else if (requestCode == PICK_ADW_ICON_PACK) {
                Bitmap bitmap = getBitmapIconPackIntent(data);
                if (bitmap != null) {
                    setCustomIcon(bitmap);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap getBitmapIconPackIntent(Intent data) {
        Bitmap bitmap = null;
        Uri imageUri = data.getData();
        if (imageUri != null) {
            String scheme = imageUri.getScheme();
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
                Drawable icon = resolveUri(imageUri);
                if (icon != null) {
                    bitmap = UtilitiesBitmap.createHiResIconBitmap(icon, this);
                }
            }
        } else {
            bitmap = (Bitmap) data.getParcelableExtra("icon");
        }
        return bitmap;
    }

}
