package com.anod.car.home.app;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.anod.car.home.R;
import com.anod.car.home.model.AppsList;

import java.util.ArrayList;

/**
 * @author alex
 * @date 2014-09-02
 */
abstract public class AppsListActivity extends ActionBarListActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<ArrayList<AppsList.Entry>> {
    private AppsListAdapter mAdapter;
    protected AppsList mAppsList;

    abstract protected boolean isShowTitle();
    abstract protected int getRowLayoutId();
    abstract protected void onEntryClick(int position, AppsList.Entry entry);
    abstract protected AppsList getAppList(Context context);

    protected ArrayList<AppsList.Entry> getHeadEntries() {
        return null;
    }
    protected int getFooterViewId() {
        return 0;
    }
    protected void onResumeImpl() {
        // Nothing by default
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!isShowTitle()) {
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apps_list);
        getListView().setOnItemClickListener(this);
        int footerViewId = getFooterViewId();
        if (footerViewId > 0) {
            FrameLayout panel = (FrameLayout) findViewById(R.id.panel);
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View footerView = inflater.inflate(footerViewId, null);
            panel.addView(footerView);
            panel.setVisibility(View.VISIBLE);
        }
        setResult(Activity.RESULT_OK);

        mAppsList = getAppList(this);

        mAdapter = new AppsListAdapter(this, getRowLayoutId(),mAppsList.getAppIconLoader());
        setListAdapter(mAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        onResumeImpl();
        ArrayList<AppsList.Entry> apps = (mAppsList == null) ? null : mAppsList.getEntries();

        if (apps == null || apps.isEmpty() || isRefreshCache()) {
            getLoaderManager().initLoader(0, null, this).forceLoad();
        } else {
            onLoadFinished(null, apps);
        }
    }

    protected boolean isRefreshCache() {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppsList.Entry entry = mAdapter.getItem(position);
        onEntryClick(position, entry);
    }

    protected void onItemsSet(ArrayList<AppsList.Entry> items) {
        // Nothing by default
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<AppsList.Entry>> loader, ArrayList<AppsList.Entry> cacheEntries) {
        ArrayList<AppsList.Entry> items = getHeadEntries();
        if (items == null) {
            items = cacheEntries;
        } else {
            items.addAll(cacheEntries);
        }
        mAdapter.clear();
        mAdapter.addAll(items);
        mAdapter.notifyDataSetChanged();
        onItemsSet(items);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<AppsList.Entry>> loader) {

    }
}
