package com.anod.car.home.prefs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.model.LauncherModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.utils.IconPackUtils;
import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;

public class ShortcutEditActivity extends Activity {

	private static final String MIME_IMAGE = "image/*";
	public static final String EXTRA_SHORTCUT_ID = "extra_id";
	public static final String EXTRA_CELL_ID = "extra_cell_id";

	private static final int PICK_CUSTOM_ICON = 0;
	private static final int PICK_ADW_ICON_PACK = 1;

	private static final int DIALOG_ICON_MENU = 1;

	private Bitmap mCustomIcon;
	private ImageView mIconView;
	private EditText mLabelEdit;
	private LauncherModel mModel;
	private ShortcutInfo mShortuctInfo;
	private Intent mIntent;
	private File mTempFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shortcut_edit);
		setTitle(R.string.shortcut_edit_title);

		mLabelEdit = (EditText) findViewById(R.id.label_edit);
		mIconView = (ImageView) findViewById(R.id.icon_edit);

		mIntent = getIntent();
		final int cellId = mIntent.getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, PickShortcutUtils.INVALID_CELL_ID);
		final long shortcutId = mIntent.getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, ShortcutInfo.NO_ID);
		if (cellId == PickShortcutUtils.INVALID_CELL_ID || shortcutId == ShortcutInfo.NO_ID) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		mModel = new LauncherModel(this);
		mShortuctInfo = mModel.loadShortcut(shortcutId);
		mLabelEdit.setText(mShortuctInfo.title);
		mIconView.setImageBitmap(mShortuctInfo.getIcon());

		mIconView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeIcon(v);
			}
		});

		final Button okButton = (Button) findViewById(R.id.ok_button);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clickedOk(v);
			}
		});

	}

	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_ICON_MENU) {
			final CharSequence[] items = { getString(R.string.icon_custom), getString(R.string.icon_adw_icon_pack) };
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.dialog_title_select));
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					iconDialogClick(item);
				}
			});
			return builder.create();
		}
		return null;
	}

	private void iconDialogClick(final int item) {
		Intent chooseIntent;
		if (item == PICK_CUSTOM_ICON) {
			mTempFile = getFileStreamPath("tempImage");
			chooseIntent = new Intent(Intent.ACTION_GET_CONTENT);
			chooseIntent.setType(MIME_IMAGE);
			chooseIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
			chooseIntent.putExtra("outputFormat",Bitmap.CompressFormat.PNG.name());
			Utils.startActivityForResultSafetly(chooseIntent, PICK_CUSTOM_ICON, this);
		} else if (item == PICK_ADW_ICON_PACK) {
			chooseIntent = new Intent();
			IconPackUtils.fillAdwIconPackIntent(chooseIntent);
			Utils.startActivityForResultSafetly(Intent.createChooser(chooseIntent, getString(R.string.select_icon_pack)), PICK_ADW_ICON_PACK, this);
		}
	}

	private void updateCustomIcon(final Intent data) {
		Uri imageUri = data.getData();
		Drawable icon = resolveUri(imageUri);
		if (icon == null) {
			final String errStr = getString(R.string.error_text, getString(R.string.custom_image_error));
			Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
			return;
		}
		Bitmap bitmap = UtilitiesBitmap.createIconBitmap(icon, this);
		setIcon(bitmap);
		return;
	}
	
	/**
	 * Source android.widget.ImageView
	 * @param uri
	 * @return
	 */
	private Drawable resolveUri(Uri uri) {
		Drawable d = null;
		String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            try {
                // Load drawable through Resources, to get the source density information
                OpenResourceIdResult r = getResourceId(uri);
                d = r.r.getDrawable(r.id);
            } catch (Exception e) {
                Log.w("ShortcutEditActivity", "Unable to open content: " + uri, e);
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_FILE.equals(scheme)) {
            try {
                d = Drawable.createFromStream(
                   getContentResolver().openInputStream(uri),
                    null);
            } catch (Exception e) {
                Log.w("ShortcutEditActivity", "Unable to open content: " + uri, e);
            }
        } else {
            d = Drawable.createFromPath(uri.toString());
        }
        
        return d;
	}
	
    public class OpenResourceIdResult {
        public Resources r;
        public int id;
    }
    
    /**
     * From android.content.ContentResolver
     * @param uri
     * @return
     * @throws FileNotFoundException
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

	/*
	private void updateCustomIcon(final Intent data) {
		final String generalError = "An error was encountered while trying to open the selected image";
		final Uri localUri = data.getData();
		if (localUri == null) {
			final String errStr = String.format(getResources().getString(R.string.error_text), generalError);
			Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
			return;
		}
		final String imagePath = getPath(localUri);
		if (imagePath == null || imagePath.equals("")) {
			final String errStr = String.format(getResources().getString(R.string.error_text), generalError);
			Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
			return;
		}
		final Bitmap icon = BitmapFactory.decodeFile(imagePath);
		if (icon == null) {
			final String errStr = String.format(getResources().getString(R.string.error_text), generalError);
			Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
			return;
		}
		setIcon(icon);
	}
*/
	/**
	 * Called from view directly
	 * 
	 * @param view
	 */
	@SuppressWarnings("deprecation")
	private void changeIcon(View view) {
		showDialog(DIALOG_ICON_MENU);
	}

	private void setIcon(Bitmap icon) {
		mCustomIcon = icon;
		mIconView.setImageBitmap(mCustomIcon);
	}

	public String getPath(Uri uri) {
		ContentResolver cr = getContentResolver();
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = cr.query(uri, projection, null, null, null);
		if (cursor == null) {
			return null;
		}
		int columnIndex;

		try {
			columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		} catch (Exception e) {
			return null;
		}
		cursor.moveToFirst();
		String str = cursor.getString(columnIndex);
		cursor.close();
		return str;
	}

	private void clickedOk(View view) {
		boolean needUpdate = false;
		if (mCustomIcon != null) {
			Bitmap icon = UtilitiesBitmap.createBitmapThumbnail(mCustomIcon, this);
			mShortuctInfo.setCustomIcon(icon);
			needUpdate = true;
		}
		String title = mLabelEdit.getText().toString();
		if (!title.equals(mShortuctInfo.title)) {
			mShortuctInfo.title = title;
			needUpdate = true;
		}
		if (needUpdate) {
			mModel.updateItemInDatabase(this, mShortuctInfo);
		}

		setResult(RESULT_OK, mIntent);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == PICK_CUSTOM_ICON) {
				updateCustomIcon(data);
			} else if (requestCode == PICK_ADW_ICON_PACK) {
				Bitmap icon = (Bitmap) data.getParcelableExtra("icon");
				if (icon != null) {
					setIcon(icon);
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

}
