package com.anod.car.home.prefs;

import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.model.AbstractShortcutsContainerModel;
import com.anod.car.home.model.LauncherSettings;
import com.anod.car.home.model.NotificationShortcutsModel;
import com.anod.car.home.model.Shortcut;
import com.anod.car.home.model.ShortcutIcon;
import com.anod.car.home.model.ShortcutInfoUtils;
import com.anod.car.home.model.ShortcutModel;
import com.anod.car.home.model.WidgetShortcutsModel;
import info.anodsplace.android.log.AppLog;

import com.anod.car.home.utils.DrawableUri;
import com.anod.car.home.utils.IconPackUtils;
import com.anod.car.home.utils.ShortcutPicker;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShortcutEditActivity extends AppCompatActivity {

    public static Intent createIntent(Context context, int cellId, long shortcutId,
                                      int appWidgetId) {
        Intent editIntent = new Intent(context, ShortcutEditActivity.class);
        editIntent.putExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, shortcutId);
        editIntent.putExtra(ShortcutEditActivity.EXTRA_CELL_ID, cellId);
        editIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        editIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return editIntent;
    }

    private static final String MIME_IMAGE = "image/*";

    public static final String EXTRA_SHORTCUT_ID = "extra_id";

    public static final String EXTRA_CELL_ID = "extra_cell_id";

    private static final int PICK_CUSTOM_ICON = 0;

    private static final int PICK_ADW_ICON_PACK = 1;

    private static final int PICK_DEFAULT_ICON = 2;

    private Bitmap mCustomIcon;

    @BindView(R.id.icon_edit)
    ImageView mIconView;

    @BindView(R.id.label_edit)
    EditText mLabelEdit;

    private ShortcutModel mModel;

    private Shortcut mShortcut;

    private ShortcutIcon mShortcutIcon;

    private Intent mIntent;

    private Bitmap mIconDefault;

    private AbstractShortcutsContainerModel mContainerModel;

    private int mCellId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcutedit);
        setTitle(R.string.shortcut_edit_title);

        ButterKnife.bind(this);

        init(getIntent());
    }

    private void init(Intent intent) {
        mIntent = intent;
        mCellId = intent
                .getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, ShortcutPicker.INVALID_CELL_ID);
        final long shortcutId = mIntent
                .getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, Shortcut.NO_ID);
        final int appWidgetId = mIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mCellId == ShortcutPicker.INVALID_CELL_ID || shortcutId == Shortcut.NO_ID) {
            AppLog.e("Missing parameter");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            mContainerModel = NotificationShortcutsModel.init(this);
        } else {
            mContainerModel = WidgetShortcutsModel.init(this, appWidgetId);
        }
        mModel = mContainerModel.getShortcutModel();

        mShortcut = mModel.loadShortcut(shortcutId);
        mShortcutIcon = mModel.loadShortcutIcon(shortcutId);
        mLabelEdit.setText(mShortcut.title);
        mIconView.setImageBitmap(mShortcutIcon.bitmap);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        init(intent);
    }

    protected Dialog createIconMenu() {
        final CharSequence[] items;
        if (mShortcut.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
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

    void iconDialogClick(final int item) {
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

            ComponentName componentName = mShortcut.intent.getComponent();
            if (componentName == null) {
                Toast.makeText(this, R.string.failed_fetch_icon, Toast.LENGTH_LONG).show();
                return;
            }
            final PackageManager manager = getPackageManager();
            final ResolveInfo resolveInfo = manager.resolveActivity(mShortcut.intent, 0);
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
        Drawable icon = new DrawableUri(this).resolve(imageUri);
        if (icon == null) {
            final String errStr = getString(R.string.error_text,
                    getString(R.string.custom_image_error));
            Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
            return;
        }
        Bitmap bitmap = UtilitiesBitmap.createMaxSizeIcon(icon, this);
        setCustomIcon(bitmap);
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
            mShortcutIcon = ShortcutIcon.forCustomIcon(mShortcutIcon.id, mCustomIcon);
            needUpdate = true;
        } else if (mIconDefault != null) {
            mShortcutIcon = ShortcutIcon.forActivity(mShortcutIcon.id, mIconDefault);
            needUpdate = true;
        }
        CharSequence title = mLabelEdit.getText();
        if (!title.equals(mShortcut.title)) {
            mShortcut = new Shortcut(mShortcut.id, mShortcut.itemType, title, mShortcutIcon.isCustom, mShortcut.intent);
            needUpdate = true;
        }
        if (needUpdate) {
            mModel.updateItemInDatabase(this, mShortcut, mShortcutIcon);
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
                Drawable icon = new DrawableUri(this).resolve(imageUri);
                if (icon != null) {
                    bitmap = UtilitiesBitmap.createHiResIconBitmap(icon, this);
                }
            }
        } else {
            bitmap = data.getParcelableExtra("icon");
        }
        return bitmap;
    }

}
