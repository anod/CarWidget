package com.anod.car.home.prefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
			chooseIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			Utils.startActivityForResultSafetly(chooseIntent, PICK_CUSTOM_ICON, this);
		} else if (item == PICK_ADW_ICON_PACK) {
			chooseIntent = new Intent();
			IconPackUtils.fillAdwIconPackIntent(chooseIntent);
			Utils.startActivityForResultSafetly(Intent.createChooser(chooseIntent, getString(R.string.select_icon_pack)), PICK_ADW_ICON_PACK, this);
		}
	}

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
		cursor.moveToFirst();
		String str = cursor.getString(0);
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
