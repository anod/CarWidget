package com.anod.car.home.app

import com.anod.car.home.R
import com.anod.car.home.model.AppsList

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.anod.car.home.CarWidgetApplication

interface AppsListResultCallback {
    fun onResult(result: List<AppsList.Entry>)
}

class AppsListViewModel(application: Application) : AndroidViewModel(application), AppsListResultCallback {

    val list = MutableLiveData<List<AppsList.Entry>>()
    var loader: AsyncTask<Void, Void, List<AppsList.Entry>>? = null
    var isRefreshCache = false
    var appsList = getApplication<CarWidgetApplication>().appComponent!!.appListCache

    fun load() {
        this.loader?.execute()
    }

    fun loadIfNeeded() {
        val apps = appsList.entries

        if (apps.isEmpty() || isRefreshCache) {
            this.load()
        } else {
            this.list.value = apps
        }
    }

    override fun onResult(result: List<AppsList.Entry>) {
        this.appsList.replace(result)
        this.list.value = result
    }
}

/**
 * @author alex
 * @date 2014-09-02
 */
abstract class AppsListActivity : AppCompatGridActivity(), AdapterView.OnItemClickListener {

    private val adapter: AppsListAdapter by lazy { AppsListAdapter(this, rowLayoutId, App.provide(this).appIconLoader) }

    protected open val isShowTitle: Boolean
        get() = false

    private val rowLayoutId = R.layout.list_item_app

    protected open val headEntries: List<AppsList.Entry> = emptyList()

    protected open val footerViewId: Int
        get() = 0

    protected open val isRefreshCache: Boolean
        get() = false

    protected abstract fun onEntryClick(position: Int, entry: AppsList.Entry)

    val viewModel: AppsListViewModel by lazy { ViewModelProviders.of(this).get(AppsListViewModel::class.java) }

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

        viewModel.list.observe(this, Observer {
            adapter.clear()

            val items = headEntries + it
            adapter.addAll(items)
            adapter.notifyDataSetChanged()
            onItemsSet(items)
        })

    }


    override fun onResume() {
        super.onResume()
        onResumeImpl()
        viewModel.loadIfNeeded()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val entry = adapter.getItem(position)!!
        onEntryClick(position, entry)
    }

    protected open fun onItemsSet(items: List<AppsList.Entry>) {
        // Nothing by default
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
