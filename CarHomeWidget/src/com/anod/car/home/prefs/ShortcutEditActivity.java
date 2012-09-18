package com.anod.car.home.prefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.anod.car.home.R;
import com.anod.car.home.model.LauncherModel;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.utils.UtilitiesBitmap;

public class ShortcutEditActivity extends Activity {
	private static final String ACTION_ADW_PICK_ICON="org.adw.launcher.icons.ACTION_PICK_ICON";
	
	public static final String EXTRA_SHORTCUT_ID = "extra_id";
	public static final String EXTRA_CELL_ID = "extra_cell_id";
	

	private static final int PICK_CUSTOM_ICON = 0;
	private static final int PICK_ADW_ICON_PACK=1;
	
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
		
		mLabelEdit = (EditText)findViewById(R.id.label_edit);
		mIconView = (ImageView)findViewById(R.id.icon_edit);
		
		mIntent = getIntent();
    	int cellId = mIntent.getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, PickShortcutUtils.INVALID_CELL_ID);
    	long shortcutId = mIntent.getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, ShortcutInfo.NO_ID);
    	if (cellId == PickShortcutUtils.INVALID_CELL_ID || shortcutId == ShortcutInfo.NO_ID) {
    		setResult(RESULT_CANCELED);
    		finish();
    		return;
    	}
        mModel = new LauncherModel();    	
        mShortuctInfo = mModel.loadShortcut(this, shortcutId);
        mLabelEdit.setText(mShortuctInfo.title);
        mIconView.setImageBitmap(mShortuctInfo.getIcon());  
	}
	
	protected Dialog onCreateDialog(int id) {
	    switch(id) {
	    	case DIALOG_ICON_MENU:
		    	final CharSequence[] items = {
		    		getString(R.string.icon_custom), getString(R.string.icon_adw_icon_pack)
		    	};
	
		    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

	private void iconDialogClick(int item) {
		Intent chooseIntent;
		switch(item) {
			case  PICK_CUSTOM_ICON:
				chooseIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResultSafetly(chooseIntent, PICK_CUSTOM_ICON);
			break;
			case  PICK_ADW_ICON_PACK:
				chooseIntent=new Intent(ACTION_ADW_PICK_ICON);
				startActivityForResultSafetly(Intent.createChooser(chooseIntent, "Select icon pack"), PICK_ADW_ICON_PACK);
			break;
		}
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
        setIcon(icon);
	}

	private void setIcon(Bitmap icon)
	{
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
	
	public void startActivityForResultSafetly(Intent intent, int requestCode) {
		try
		{
			startActivityForResult(intent, requestCode);
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
	
	public void changeIcon(View view)
	{
		showDialog(DIALOG_ICON_MENU);
	}
	
	public void clickedOk(View view)
	{
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
