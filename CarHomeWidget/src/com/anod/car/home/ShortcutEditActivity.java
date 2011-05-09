package com.anod.car.home;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
	
	private void updateCustomIcon(Intent intent)
	{
		//TODO
	}

	public void changeIcon(View view)
	{
		Intent chooseIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		try
		{
			startActivityForResult(chooseIntent, 1);
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
		setResult(RESULT_OK, mIntent);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
			
	}

}
