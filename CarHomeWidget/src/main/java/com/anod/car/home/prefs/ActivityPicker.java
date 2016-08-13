package com.anod.car.home.prefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.anod.car.home.R;
import com.anod.car.home.app.AppCompatGridActivity;
import info.anodsplace.android.log.AppLog;
import com.anod.car.home.utils.FastBitmapDrawable;
import com.anod.car.home.utils.UtilitiesBitmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityPicker extends AppCompatGridActivity {

    /**
     * Adapter of items that are displayed in this dialog.
     */
    private PickAdapter mAdapter;

    /**
     * Base {@link Intent} used when building list.
     */
    private Intent mBaseIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

        initPicker(intent);
    }

    protected void initPicker(final Intent intent) {

        // Read base intent from extras
        Parcelable parcel = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        if (parcel instanceof Intent) {
            mBaseIntent = (Intent) parcel;
        }

        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        if (title != null) {
            setTitle(title);
        }

        // Build list adapter of pickable items
        List<PickAdapter.Item> items = getItems();
        mAdapter = new PickAdapter(this, items);
        setListAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = getIntentForPosition(position);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    /**
     * Build the specific {@link Intent} for a given list position. Convenience
     * method that calls through to {@link PickAdapter.Item#getIntent(Intent)}.
     */
    protected Intent getIntentForPosition(int position) {
        PickAdapter.Item item = (PickAdapter.Item) mAdapter.getItem(position);
        return item.getIntent(mBaseIntent);
    }

    /**
     * Build and return list of items to be shown in dialog. Default
     * implementation mixes activities matching {@link #mBaseIntent} from
     * {@link #putIntentItems(Intent, List)} with any injected items from
     * {@link Intent#EXTRA_SHORTCUT_NAME}. Override this method in subclasses to
     * change the items shown.
     */
    protected List<PickAdapter.Item> getItems() {
        PackageManager packageManager = getPackageManager();
        List<PickAdapter.Item> items = new ArrayList<PickAdapter.Item>();

        // Add any injected pick items
        final Intent intent = getIntent();
        ArrayList<String> labels = intent.getStringArrayListExtra(Intent.EXTRA_SHORTCUT_NAME);
        ArrayList<ShortcutIconResource> icons = intent
                .getParcelableArrayListExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);

        if (labels != null && icons != null && labels.size() == icons.size()) {
            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                Drawable icon = null;

                try {
                    // Try loading icon from requested package
                    ShortcutIconResource iconResource = icons.get(i);
                    Resources res = packageManager
                            .getResourcesForApplication(iconResource.packageName);
                    icon = ResourcesCompat.getDrawable(res, res.getIdentifier(iconResource.resourceName, null, null), null);
                } catch (NameNotFoundException e) {
                    AppLog.w(e.getMessage());
                }

                items.add(new PickAdapter.Item(this, label, icon));
            }
        }

        // Add any intent items if base was given
        if (mBaseIntent != null) {
            putIntentItems(mBaseIntent, items);
        }

        return items;
    }


    /**
     * Fill the given list with any activities matching the base {@link Intent}.
     */
    protected void putIntentItems(Intent baseIntent, List<PickAdapter.Item> items) {
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                baseIntent, 0 /* no flags */
        );
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(packageManager));

        final int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
            items.add(new PickAdapter.Item(this, packageManager, resolveInfo));
        }
    }

    /**
     * Adapter which shows the set of activities that can be performed for a
     * given {@link Intent}.
     */
    protected static class PickAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;

        private final List<Item> mItems;

        /**
         * Item that appears in a {@link PickAdapter} list.
         */
        public static class Item {

            CharSequence label;

            Drawable icon;

            String packageName;

            String className;

            Bundle extras;

            Intent intent;

            /**
             * Create a list item from given label and icon.
             */
            Item(Context context, CharSequence label, Drawable icon, Intent intent) {
                this.label = label;
                this.icon = createThumbnail(icon, context);
                this.intent = intent;
            }

            /**
             * Create a list item from given label and icon.
             */
            Item(Context context, CharSequence label, Drawable icon) {
                this.label = label;
                this.icon = createThumbnail(icon, context);
            }

            /**
             * Create a list item and fill it with details from the given
             * {@link ResolveInfo} object.
             */
            Item(Context context, PackageManager pm, ResolveInfo resolveInfo) {
                label = resolveInfo.loadLabel(pm);
                if (label == null && resolveInfo.activityInfo != null) {
                    label = resolveInfo.activityInfo.name;
                }

                icon = createThumbnail(resolveInfo.loadIcon(pm), context);
                packageName = resolveInfo.activityInfo.applicationInfo.packageName;
                className = resolveInfo.activityInfo.name;
            }

            /**
             *
             * @param source
             * @param context
             * @return
             */
            private Drawable createThumbnail(Drawable source, Context context) {
                Bitmap bitmap = UtilitiesBitmap.createSystemIconBitmap(source, context);
                if (bitmap == null) {
                    final PackageManager manager = context.getPackageManager();
                    bitmap = UtilitiesBitmap.makeDefaultIcon(manager);
                }
                return new FastBitmapDrawable((Bitmap) bitmap);
            }

            /**
             * Build the {@link Intent} described by this item. If this item
             * can't create a valid {@link android.content.ComponentName}, it
             * will return {@link Intent#ACTION_CREATE_SHORTCUT} filled with the
             * item label.
             */
            Intent getIntent(Intent baseIntent) {
                if (this.intent != null) {
                    return this.intent;
                }
                Intent intent;
                if (baseIntent != null) {
                    intent = new Intent(baseIntent);
                } else {
                    intent = new Intent(Intent.ACTION_MAIN);
                }
                if (packageName != null && className != null) {
                    // Valid package and class, so fill details as normal intent
                    intent.setClassName(packageName, className);
                    if (extras != null) {
                        intent.putExtras(extras);
                    }
                } else {
                    // No valid package or class, so treat as shortcut with
                    // label
                    intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
                }
                return intent;
            }

            public CharSequence getLabel() {
                return label;
            }
        }

        /**
         * Create an adapter for the given items.
         */
        public PickAdapter(Context context, List<Item> items) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItems = items;
        }

        /**
         * {@inheritDoc}
         */
        public int getCount() {
            return mItems.size();
        }

        /**
         * {@inheritDoc}
         */
        public Object getItem(int position) {
            return mItems.get(position);
        }

        /**
         * {@inheritDoc}
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * {@inheritDoc}
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = (TextView) mInflater.inflate(R.layout.pick_item, parent, false);
            } else {
                textView = (TextView) convertView;
            }

            Item item = (Item) getItem(position);
            textView.setText(item.label);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null);

            return textView;
        }
    }
}
