package com.anod.car.home;

import android.app.ListActivity;
import android.os.Bundle;

/**
 * @author alex
 * @date 5/24/13
 */
public class WidgetSettingsListActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_list);

		int[] appWidgetIds = CalWidgetManager.getAllWidgetIds(this);
		if (appWidgetIds.length == 1 && appWidgetIds[0] != AppWidgetManager.INVALID_APPWIDGET_ID) {
			startConfigActivity(appWidgetIds[0]);
			return;
		}
		;
		setListAdapter(new SettingsAdapter(this, asList(appWidgetIds)));
	}


	private void startConfigActivity(int appWidgetId) {
		Intent configIntent = Utils.createConfigIntent(this, appWidgetId);
		startActivity(configIntent);
		finish();
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Integer appWidgetId = (Integer)getListAdapter().getItem(position);
		startConfigActivity(appWidgetId);
	}


	public static class SettingsAdapter extends ArrayAdapter<Integer> {

		private AppWidgetManager mAppWidgetManager;

		public SettingsAdapter(Context context,List<Integer> objects) {
			super(context, android.R.layout.simple_list_item_1, objects);
			mAppWidgetManager = AppWidgetManager.getInstance(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = null;
			if (convertView == null) {
				LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = li.inflate(android.R.layout.simple_list_item_1, null);
			} else {
				row = convertView;
			}

			AppWidgetProviderInfo info = mAppWidgetManager.getAppWidgetInfo(getItem(position));
			TextView title = (TextView)row.findViewById(android.R.id.text1);
			title.setText(info.label);

			return row;
		}



	}

	public List<Integer> asList(final int[] is)
	{
		return new AbstractList<Integer>() {
			public Integer get(int i) { return is[i]; }
			public int size() { return is.length; }
		};
	}
}