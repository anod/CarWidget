package com.anod.car.home.prefs.lookandfeel;

import com.anod.car.home.R;
import com.anod.car.home.app.ActionBarListActivity;
import com.anod.car.home.appwidget.WidgetButtonViewBuilder;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.skin.PropertiesFactory;
import com.anod.car.home.skin.SkinProperties;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.Utils;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * @author alex
 * @date 2015-01-18
 */
public class WidgetButtonChoiceActivity extends ActionBarListActivity {

    public static final String EXTRA_SKIN = "skin";

    public static final String EXTRA_BTN = "btn";

    private int mAppWidgetId;

    private String mSkin;

    private int mButton;

    public static Intent createIntent(int appWidgetId, String skin, int buttonId, Context context) {
        Intent intent = new Intent(context, WidgetButtonChoiceActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(WidgetButtonChoiceActivity.EXTRA_SKIN, skin);
        intent.putExtra(WidgetButtonChoiceActivity.EXTRA_BTN, buttonId);
        String path = appWidgetId + "/widgetButton" + buttonId;
        Uri data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"), path);
        intent.setData(data);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAppWidgetId = Utils.readAppWidgetId(savedInstanceState, getIntent());
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppLog.d("Invalid AppWidgetId");
            finish();
            return;
        }
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mSkin = extras.getString(EXTRA_SKIN, null);
                mButton = extras.getInt(EXTRA_BTN, -1);
            }
        } else {
            mSkin = savedInstanceState.getString(EXTRA_SKIN, null);
            mButton = savedInstanceState.getInt(EXTRA_BTN, -1);
        }
        if (mSkin == null || mButton == -1) {
            AppLog.d("Invalid params");
            finish();
            return;
        }

        SkinProperties skinProperties = PropertiesFactory.create(mSkin, false);
        List<ChoiceAdapter.Item> items = createItems(skinProperties);

        Main prefs = PreferencesStorage.loadMain(this, mAppWidgetId);
        initCheckedItem(items, prefs, skinProperties);

        final ListView listView = getListView();
        listView.setItemsCanFocus(false);

        setListAdapter(new ChoiceAdapter(this, items));
    }

    private void initCheckedItem(List<ChoiceAdapter.Item> items, Main prefs,
            SkinProperties skinProperties) {
        int value = 0;
        if (mButton == WidgetButtonViewBuilder.BUTTON_1) {
            value = prefs.getWidgetButton1();
        } else {
            value = prefs.getWidgetButton2();
        }

        for (int i = 0; i < items.size(); i++) {
            ChoiceAdapter.Item item = items.get(i);
            if (item.value == value) {
                item.setChecked(true);
            } else {
                item.setChecked(false);
            }
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Main prefs = PreferencesStorage.loadMain(this, mAppWidgetId);
        ChoiceAdapter.Item item = (ChoiceAdapter.Item) getListAdapter().getItem(position);
        if (mButton == WidgetButtonViewBuilder.BUTTON_1) {
            prefs.setWidgetButton1(item.value);
        } else {
            prefs.setWidgetButton2(item.value);
        }
        PreferencesStorage.saveMain(this, prefs, mAppWidgetId);

        finish();
    }

    private List<ChoiceAdapter.Item> createItems(SkinProperties skinProperties) {
        Resources r = getResources();
        ArrayList<ChoiceAdapter.Item> items = new ArrayList<ChoiceAdapter.Item>(3);
        items.add(new ChoiceAdapter.Item(r.getString(R.string.pref_settings_transparent),
                skinProperties.getSettingsButtonRes(), Main.WIDGET_BUTTON_SETTINGS));
        items.add(new ChoiceAdapter.Item(r.getString(R.string.pref_incar_transparent),
                skinProperties.getInCarButtonEnterRes(), Main.WIDGET_BUTTON_INCAR));
        items.add(new ChoiceAdapter.Item(r.getString(R.string.hidden), R.drawable.ic_action_cancel,
                Main.WIDGET_BUTTON_HIDDEN));
        return items;
    }

    private static class ChoiceAdapter extends ArrayAdapter<ChoiceAdapter.Item> {

        public static class Item {

            int value;

            String title;

            int icon;

            boolean checked;

            private Item(String title, int icon, int value) {
                this.title = title;
                this.icon = icon;
                this.value = value;
            }

            public void setChecked(boolean checked) {
                this.checked = checked;
            }

            @Override
            public String toString() {
                return this.title;
            }

        }

        public ChoiceAdapter(Context context, List<ChoiceAdapter.Item> items) {
            super(context, R.layout.simple_list_item_checkbox, android.R.id.text1);
            addAll(items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            Item item = getItem(position);
            ImageView icon = ButterKnife.findById(view, android.R.id.icon);
            icon.setImageResource(item.icon);

            CheckedTextView textView = ButterKnife.findById(view, android.R.id.text1);
            textView.setChecked(item.checked);

            return view;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Utils.saveAppWidgetId(outState, mAppWidgetId);
        outState.putString(EXTRA_SKIN, mSkin);
        outState.putInt(EXTRA_BTN, mButton);
    }
}
