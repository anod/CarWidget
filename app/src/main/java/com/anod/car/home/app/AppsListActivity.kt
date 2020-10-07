package com.anod.car.home.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.lifecycle.*
import com.anod.car.home.CarWidgetApplication
import com.anod.car.home.R
import com.anod.car.home.model.AppsList
import kotlinx.coroutines.launch

interface AppsListLoader{
    suspend fun loadAppsList(): List<AppsList.Entry>
}

class AppsListViewModel(application: Application) : AndroidViewModel(application) {

    val list = MutableLiveData<List<AppsList.Entry>>()
    lateinit var loader: AppsListLoader
    var isRefreshCache = false
    var appsList = getApplication<CarWidgetApplication>().appComponent.appListCache

    fun load() {
        viewModelScope.launch {
            val result = loader.loadAppsList()
            appsList.replace(result)
            list.value = result
        }
    }

    fun loadIfNeeded() {
        val apps = appsList.entries
        if (apps.isEmpty() || isRefreshCache) {
            this.load()
        } else {
            this.list.value = apps
        }
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

    protected open val isRefreshCache: Boolean
        get() = false

    protected abstract fun onEntryClick(position: Int, entry: AppsList.Entry)

    val viewModel: AppsListViewModel by viewModels()

    protected open fun onResumeImpl() {
        // Nothing by default
    }

    protected open fun inflateFooterView(layoutInflater: LayoutInflater, parent: ViewGroup): View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apps_list)
        gridView.onItemClickListener = this
        val panel = findViewById<View>(R.id.panel) as FrameLayout
        val footerView = inflateFooterView(layoutInflater, panel)
        if (footerView != null) {
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
