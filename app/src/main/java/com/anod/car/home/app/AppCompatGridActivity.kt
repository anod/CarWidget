package com.anod.car.home.app

import com.anod.car.home.R

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ListAdapter

/**
 * @author alex
 * @date 11/19/13
 */
abstract class AppCompatGridActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    /**
     * Get the ListAdapter associated with this activity's ListView.
     */
    /**
     * Provide the cursor for the list view.
     */
    var listAdapter: ListAdapter? = null
        set(adapter) = synchronized(this) {
            ensureView()
            field = adapter
            view!!.adapter = adapter
        }

    private var view: GridView? = null

    private val mHandler = Handler()

    private var mFinishedStart = false

    private val mRequestFocus = Runnable { view!!.focusableViewAvailable(view) }

    /**
     * Get the activity's list view widget.
     */
    val gridView: GridView
        get() {
            return ensureView()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_content)
        setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

    }

    /**
     * Ensures the list view has been created before Activity restores all
     * of the view states.
     */
    override fun onRestoreInstanceState(state: Bundle) {
        ensureView()
        super.onRestoreInstanceState(state)
    }

    /**
     */
    override fun onDestroy() {
        mHandler.removeCallbacks(mRequestFocus)
        super.onDestroy()
    }

    /**
     * Updates the screen state (current list and other views) when the
     * content changes.
     */
    override fun onContentChanged() {
        val emptyView = findViewById<View>(android.R.id.empty)
        view = findViewById<View>(android.R.id.list) as GridView
        if (view == null) {
            return
        }
        if (emptyView != null) {
            view!!.emptyView = emptyView
        }
        view!!.onItemClickListener = this
        if (mFinishedStart) {
            listAdapter = listAdapter
        }
        mHandler.post(mRequestFocus)
        mFinishedStart = true
    }

    private fun ensureView(): GridView {
        if (view != null) {
            return view!!
        }
        view = findViewById<View>(android.R.id.list) as GridView
        return view!!
    }
}