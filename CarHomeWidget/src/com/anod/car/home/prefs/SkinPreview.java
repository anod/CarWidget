package com.anod.car.home.prefs;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.prefs.views.EcoGallery;

public class SkinPreview extends Activity {

	private EcoGallery mGallery;
	private TextView mThemeNameView;
	private TextView mCurrentPositionView;

	private ThemeChooserAdapter mAdapter;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		SkinItem[] skins = createSkinList();
		mAdapter = new ThemeChooserAdapter(this, skins);
		inflateActivity();

		// mGallery.setSelection(mAdapter.getMarkedPosition());
	}

	private SkinItem[] createSkinList() {
		Resources r = getResources();
		String[] titles = r.getStringArray(R.array.skin_titles);
		String[] values = r.getStringArray(R.array.skin_values);
		SkinItem[] skins = new SkinItem[titles.length];
		for (int i=0; i<titles.length; i++) {
			SkinItem item = new SkinItem();
			item.title = titles[i];
			item.value = values[i];
			item.previewRes = R.drawable.widget_preview;
			skins[i] = item;
		}
		return skins;
	}

	private void inflateActivity() {
		setContentView(R.layout.skin_preview);

		mCurrentPositionView = (TextView) findViewById(R.id.adapter_position);
		mThemeNameView = (TextView) findViewById(R.id.theme_name);

		mGallery = (EcoGallery) findViewById(R.id.gallery);
		mGallery.setAdapter(mAdapter);
//		mGallery.setAdapter(mAdapter);
//		mGallery.setOnItemSelectedListener(mItemSelected);
		Button button = (Button) findViewById(R.id.apply);
		button.setOnClickListener(mApplyClicked);
	}

	private final OnItemSelectedListener mItemSelected = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			SkinItem item = (SkinItem) parent.getItemAtPosition(position);
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	private final OnClickListener mApplyClicked = new OnClickListener() {
		public void onClick(View v) {
		//	int selectedPos = mGallery.getSelectedItemPosition();

		}
	};

	class SkinItem {
		public String value;
		public String title;
		public int previewRes;
	}

	private static class ThemeChooserAdapter extends ArrayAdapter<SkinItem> {
		private Context mContext;
		
		public ThemeChooserAdapter(Context context, SkinItem[] skins) {
			super(context, R.layout.skin_item, android.R.id.text1, skins);
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				row = newView(parent);
			}
			bindView(row, position);
			return row;
		}
		
		public View newView(ViewGroup parent) { 
			View row = LayoutInflater.from(mContext).inflate(R.layout.skin_item, parent,false); 
			row.setTag(new ViewHolder(row)); 
			return row; 
		}

		 public void bindView(View view, int position) { 
			SkinItem item = getItem(position);
			ViewHolder holder = (ViewHolder)view.getTag();
			holder.preview.setImageResource(item.previewRes);
		}
	}

	private static class ViewHolder {
		public ImageView preview;

		public ViewHolder(View row) {
			preview = (ImageView) row.findViewById(R.id.theme_preview);
		}
	}
}
 