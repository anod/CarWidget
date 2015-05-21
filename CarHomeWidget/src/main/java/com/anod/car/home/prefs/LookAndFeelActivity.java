package com.anod.car.home.prefs;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.Provider;
import com.anod.car.home.R;
import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.appwidget.WidgetViewBuilder;
import com.anod.car.home.drawer.NavigationDrawer;
import com.anod.car.home.drawer.NavigationList;
import com.anod.car.home.model.WidgetShortcutsModel;
import com.anod.car.home.prefs.drag.ShortcutDragListener;
import com.anod.car.home.prefs.lookandfeel.LookAndFeelMenu;
import com.anod.car.home.prefs.lookandfeel.SkinPagerAdapter;
import com.anod.car.home.prefs.lookandfeel.WidgetButtonChoiceActivity;
import com.anod.car.home.prefs.model.SkinList;
import com.anod.car.home.prefs.preferences.Main;
import com.anod.car.home.prefs.preferences.PreferencesStorage;
import com.anod.car.home.utils.AppLog;
import com.anod.car.home.utils.IntentUtils;
import com.anod.car.home.utils.Utils;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.LruCache;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LookAndFeelActivity extends CarWidgetActivity
        implements ViewPager.OnPageChangeListener, WidgetViewBuilder.PendingIntentHelper,
        ShortcutDragListener.DropCallback {

    private static final int SKINS_COUNT = 6;

    private Context mContext;

    private int mCurrentPage;

    private int mAppWidgetId;

    private boolean[] mPreviewInitialized = {false, false, false, false, false, false};

    private WidgetViewBuilder mBuilder;

    private Main mPrefs;

    private final SparseArray<SkinRefreshListener> mSkinRefreshListeners
            = new SparseArray<SkinRefreshListener>(SKINS_COUNT);

    private boolean mPendingRefresh;

    private SkinList mSkinList;

    private LruCache<String, Bitmap> mBitmapMemoryCache;

    @InjectView(R.id.skin_info)
    TextView mTextView;

    @InjectView(R.id.gallery)
    ViewPager mGallery;

    @InjectView(R.id.loading)
    View mLoaderView;

    private LookAndFeelMenu mMenu;

    private NavigationDrawer mDrawer;

    private WidgetShortcutsModel mModel;

    private ShortcutDragListener mDragListener;

    public SkinList.Item getCurrentSkinItem() {
        return getSkinItem(mCurrentPage);
    }

    public Main getPrefs() {
        return mPrefs;
    }

    @Override
    public boolean onDelete(int srcCellId) {
        mModel.dropShortcut(srcCellId);
        refreshSkinPreview();
        return true;
    }

    @Override
    public boolean onDrop(int srcCellId, int dstCellId) {
        if (srcCellId == dstCellId) {
            return false;
        }
        mModel.move(srcCellId, dstCellId);
        refreshSkinPreview();
        return true;
    }

    @Override
    public void onDragFinish() {
        View deleteView = findViewById(R.id.drag_delete_shortcut);
        deleteView.setVisibility(View.GONE);
    }

    public void onBeforeDragStart() {
        View deleteView = findViewById(R.id.drag_delete_shortcut);
        deleteView.setTag(ShortcutDragListener.TAG_DELETE_SHORTCUT);
        deleteView.setOnDragListener(mDragListener);
        deleteView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        deleteView.startAnimation(animation);
    }


    public ShortcutDragListener getDragListener() {
        return mDragListener;
    }

    public interface SkinRefreshListener {

        void refresh();
    }

    @Override
    protected boolean isTransparentAppTheme() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            AppLog.w("No intent");
            finish();
            return;
        }

        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppLog.e("Invalid app widget id");
            finish();
            return;
        }
        setContentView(R.layout.activity_lookandfeel);

        ButterKnife.inject(this);

        mContext = this;

        boolean keyguard = false;
        if (Utils.IS_JELLYBEAN_OR_GREATER) {
            keyguard = isKeyguard();
        }

        mBuilder = createBuilder();
        mBuilder.init();

        mPrefs = mBuilder.getPrefs();
        mModel = new WidgetShortcutsModel(mContext, mAppWidgetId);

        mSkinList = SkinList.newInstance(mPrefs.getSkin(), keyguard, mContext);
        mCurrentPage = mSkinList.getSelectedSkinPosition();

        mDrawer = new NavigationDrawer(this, mAppWidgetId);
        mDrawer.setSelected(NavigationList.ID_CURRENT_WIDGET);
        mMenu = new LookAndFeelMenu(this, mModel);

        mTextView.setMovementMethod(LinkMovementMethod.getInstance());

        int count = mSkinList.getCount();

        mGallery.setOnPageChangeListener(this);

        mDragListener = new ShortcutDragListener(this, this);

        SkinPagerAdapter adapter = new SkinPagerAdapter(this, count, getSupportFragmentManager());
        mGallery.setAdapter(adapter);
        mGallery.setCurrentItem(mCurrentPage);

        mBitmapMemoryCache = createBitmapMemoryCache();
        showText(mCurrentPage);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawer.syncState();
    }

    private LruCache<String, Bitmap> createBitmapMemoryCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        return new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.

                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean isKeyguard() {
        Bundle widgetOptions = AppWidgetManager.getInstance(mContext)
                .getAppWidgetOptions(mAppWidgetId);
        int category = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
        return category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;
    }


    @Override
    public void onResume() {
        super.onResume();
        mModel.init();
        refreshSkinPreview();
        mDrawer.refresh();
    }

    public WidgetViewBuilder createBuilder() {
        WidgetViewBuilder builder = new WidgetViewBuilder(mContext);
        builder
                .setPendingIntentHelper(this)
                .setAppWidgetId(mAppWidgetId)
                .setBitmapMemoryCache(mBitmapMemoryCache)
        ;
        builder.setWidgetButtonAlternativeHidden(true);
        return builder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu.onCreateOptionsMenu(menu);
        // Calling super after populating the menu is necessary here to ensure
        // that the
        // action bar helpers have a chance to handle this event.
        boolean result = super.onCreateOptionsMenu(menu);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawer.onOptionsItemSelected(item)) {
            return true;
        }
        return mMenu.onOptionsItemSelected(item);
    }

    public void persistPrefs() {
        PreferencesStorage.saveMain(mContext, mPrefs, mAppWidgetId);
    }

    public SkinList.Item getSkinItem(int position) {
        return mSkinList.get(position);
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPage = position;
        showText(position);

        if (!mPreviewInitialized[position]) {
            mLoaderView.setVisibility(View.VISIBLE);
        }

        mMenu.refreshTileColorButton();
    }


    private void showText(int position) {
        int textRes = getSkinItem(position).textRes;
        if (textRes > 0) {
            Spanned text = Html.fromHtml(getString(textRes));
            mTextView.setText(text);
            mTextView.setVisibility(View.VISIBLE);
        } else {
            mTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Auto-generated method stub

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // Auto-generated method stub

    }

    public void refreshSkinPreview() {
        AppLog.d("Refresh Skin Requested");

        mPrefs = mBuilder
                .reloadShortcuts()
                .reloadPrefs()
                .getPrefs();
        if (mPreviewInitialized[mCurrentPage]) {
            mPendingRefresh = false;
            for (int i = 0; i < mSkinRefreshListeners.size(); i++) {
                SkinRefreshListener listener = mSkinRefreshListeners.valueAt(i);
                if (listener != null) {
                    listener.refresh();
                    AppLog.d("Call refresh on listener for page: " + i);
                } else {
                    if (i == mCurrentPage) {
                        mPendingRefresh = true;
                        AppLog.d("No listener for current page, set pending flag");
                    }
                }
            }
        }
    }

    public int getAppWidgetId() {
        return mAppWidgetId;
    }

    @Override
    public PendingIntent createNew(int appWidgetId, int cellId) {
        Intent intent = IntentUtils.createNewShortcutIntent(mContext, appWidgetId, cellId);
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public PendingIntent createSettings(int appWidgetId, int buttonId) {

        Intent intent = WidgetButtonChoiceActivity
                .createIntent(mAppWidgetId, getSkinItem(mCurrentPage).value, buttonId, this);
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public PendingIntent createInCar(boolean on, int buttonId) {
        Intent intent = WidgetButtonChoiceActivity
                .createIntent(mAppWidgetId, getSkinItem(mCurrentPage).value, buttonId, this);
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public PendingIntent createShortcut(Intent intent, int appWidgetId, int position,
            long shortcutId) {
        Intent editIntent = IntentUtils
                .createShortcutEditIntent(mContext, position, shortcutId, appWidgetId);
        String path = appWidgetId + "/" + position;
        Uri data = Uri.withAppendedPath(Uri.parse("com.anod.car.home://widget/id/"), path);
        editIntent.setData(data);
        return PendingIntent
                .getActivity(mContext, 0, editIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void onFragmentAttach(SkinRefreshListener listener, int position) {
        AppLog.d("Register listener for page: " + position);
        mSkinRefreshListeners.put(position, listener);
        if (mPendingRefresh && mCurrentPage == position) {
            AppLog.d("Pending refresh");
            listener.refresh();
        }
    }

    public void onFragmentDetach(int position) {
        AppLog.d("UnRegister listener for page: " + position);
        mSkinRefreshListeners.delete(position);
    }

    public void onPreviewStart(int position) {
        mPreviewInitialized[position] = false;
    }

    public void onPreviewCreated(int position) {
        if (mCurrentPage == position) {
            mLoaderView.setVisibility(View.GONE);
        }
        mPreviewInitialized[position] = true;
    }

    public void beforeFinish() {
        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(getIntent().getAction())
                && mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Provider.getInstance().requestUpdate(this, mAppWidgetId);
        }
        CarWidgetApplication.provide(this).cleanAppListCache();
    }

    @Override
    public void onBackPressed() {
        beforeFinish();
        super.onBackPressed();
    }

}
