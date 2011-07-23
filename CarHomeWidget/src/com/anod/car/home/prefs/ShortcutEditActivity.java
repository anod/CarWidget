package com.anod.car.home.prefs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.R.id;
import com.anod.car.home.R.layout;
import com.anod.car.home.R.string;
import com.anod.car.home.model.LauncherModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.utils.UtilitiesBitmap;

public class ShortcutEditActivity extends Activity {
	public static final String EXTRA_SHORTCUT_ID = "extra_id";
	public static final String EXTRA_CELL_ID = "extra_cell_id";
	
	private static final int REQUEST_PICK_ICON = 1;
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
		
		mLabelEdit = (EditText)findViewById(R.id.label_edit);
		mIconView = (ImageView)findViewById(R.id.icon_edit);
		
		mIntent = getIntent();
    	int cellId = mIntent.getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, Configuration.INVALID_CELL_ID);
    	long shortcutId = mIntent.getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, ShortcutInfo.NO_ID);
    	if (cellId == Configuration.INVALID_CELL_ID || shortcutId == ShortcutInfo.NO_ID) {
    		setResult(RESULT_CANCELED);
    		finish();
    		return;
    	}
        mModel = new LauncherModel();    	
        mShortuctInfo = mModel.loadShortcut(this, shortcutId);
        mLabelEdit.setText(mShortuctInfo.title);
        mIconView.setImageBitmap(mShortuctInfo.getIcon());  
	}
	
	private void updateCustomIcon(Intent data)
	{
		String generalError = "An error was encountered while trying to open the selected image";
	    Uri localUri = data.getData();
	    if (localUri == null) {
	    	String errStr = String.format(getResources().getString(R.string.error_text), generalError);
	    	Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
	    	return;
	    }
	    String imagePath = getPath(localUri);
        if (imagePath == null || imagePath.equals("")) {
	    	String errStr = String.format(getResources().getString(R.string.error_text), generalError);
        	Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
        	return;
        }
        Bitmap icon = BitmapFactory.decodeFile(imagePath);
        if (icon == null) {
	    	String errStr = String.format(getResources().getString(R.string.error_text), generalError);
        	Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
        	return;
        }
        mCustomIcon = icon;
        mIconView.setImageBitmap(mCustomIcon);  
	}

	public String getPath(Uri uri) {
	    ContentResolver cr = getContentResolver();
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = cr.query(uri, projection, null, null, null);
		cursor.moveToFirst();
		String str = cursor.getString(0);
		cursor.close();
		return str;
	}
	
	public void changeIcon(View view)
	{
		Intent chooseIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		try
		{
			startActivityForResult(chooseIntent, REQUEST_PICK_ICON);
		}
		catch (ActivityNotFoundException activityNotFoundException)
		{
			Toast.makeText(this, R.string.photo_picker_not_found, Toast.LENGTH_LONG).show();
		}
		catch (Exception exception)
		{
			String errStr = String.format(getResources().getString(R.string.error_text), exception.getMessage());
			Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
		}
	}
	
	public void clickedOk(View view)
	{
		boolean needUpdate = false;
		if (mCustomIcon != null) {
			Bitmap icon = UtilitiesBitmap.createIconBitmap(new BitmapDrawable(mCustomIcon), this);
			mShortuctInfo.customIcon = true;
			mShortuctInfo.iconResource = null;
			mShortuctInfo.setIcon(icon);
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
		if (requestCode == REQUEST_PICK_ICON) {
			if (resultCode == RESULT_OK) {
				updateCustomIcon(data);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
