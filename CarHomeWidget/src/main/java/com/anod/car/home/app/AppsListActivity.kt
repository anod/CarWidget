package com.anod.car.home.app

import com.anod.car.home.R
import com.anod.car.home.model.AppsList

import android.app.Activity
import android.app.LoaderManager
import android.content.Context
import android.content.Loader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout

/**
 * @author alex
 * @date 2014-09-02
 */
abstract class AppsListActivity : AppCompatGridActivity(), AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<List<AppsList.Entry>> {

    private val adapter: AppsListAdapter by lazy { AppsListAdapter(this, rowLayoutId, appsList.appIconLoader) }

    protected val appsList: AppsList by lazy { createAppList(this) }

    protected open val isShowTitle: Boolean
        get() = false

    private val rowLayoutId = R.layout.list_item_app

    protected open val headEntries: List<AppsList.Entry> = emptyList()

    protected open val footerViewId: Int
        get() = 0

    protected open val isRefreshCache: Boolean
        get() = false

    protected abstract fun onEntryClick(position: Int, entry: AppsList.Entry)

    protected abstract fun createAppList(context: Context): AppsList

    protected open fun onResumeImpl() {
        // Nothing by default
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apps_list)
        gridView.onItemClickListener = this
        val footerViewId = footerViewId
        if (footerViewId > 0) {
            val panel = findViewById<View>(R.id.panel) as FrameLayout
            val inflater = getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val footerView = inflater.inflate(footerViewId, null)
            panel.addView(footerView)
            panel.visibility = View.VISIBLE
        }
        setResult(Activity.RESULT_OK)

        if (!isShowTitle && supportActionBar != null) {
            supportActionBar!!.hide()
        }

        listAdapter = adapter
    }


    override fun onResume() {
        super.onResume()
        onResumeImpl()
        val apps = appsList.entries

        if (apps.isEmpty() || isRefreshCache) {
            loaderManager.initLoader<List<AppsList.Entry>>(0, null, this).forceLoad()
        } else {
            onLoadFinished(null, apps)
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val entry = adapter.getItem(position)
        onEntryClick(position, entry)
    }

    protected open fun onItemsSet(items: List<AppsList.Entry>) {
        // Nothing by default
    }

    override fun onLoadFinished(loader: Loader<List<AppsList.Entry>>?, data: List<AppsList.Entry>) {
        adapter.clear()

        val items = headEntries + data
        adapter.addAll(items)
        adapter.notifyDataSetChanged()
        onItemsSet(items)
    }

    override fun onLoaderReset(loader: Loader<List<AppsList.Entry>>) {

    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
