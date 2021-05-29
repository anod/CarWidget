package com.anod.car.home.app

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.anod.car.home.R
import info.anodsplace.carwidget.chooser.AppsListViewModel
import info.anodsplace.carwidget.chooser.ChooserEntry
import kotlinx.coroutines.flow.collect

/**
 * @author alex
 * @date 2014-09-02
 */
abstract class AppsListActivity : AppCompatGridActivity(), AdapterView.OnItemClickListener {

    private val adapter: AppsListAdapter by lazy { AppsListAdapter(this, rowLayoutId, App.provide(this).appIconLoader) }

    protected open val isShowTitle: Boolean
        get() = false

    private val rowLayoutId = R.layout.list_item_app

    protected open val headEntries: List<ChooserEntry> = emptyList()

    protected open val isRefreshCache: Boolean
        get() = false

    protected abstract fun onEntryClick(position: Int, entry: ChooserEntry)

    abstract fun viewModelFactory(): AppsListViewModel.Factory
    val viewModel: AppsListViewModel by viewModels { viewModelFactory() }

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

        lifecycleScope.launchWhenCreated {
            viewModel.load().collect {
                adapter.clear()

                val items = headEntries + it
                adapter.addAll(items)
                adapter.notifyDataSetChanged()
                onItemsSet(items)
            }
        }

    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val entry = adapter.getItem(position)!!
        onEntryClick(position, entry)
    }

    protected open fun onItemsSet(items: List<ChooserEntry>) {
        // Nothing by default
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
