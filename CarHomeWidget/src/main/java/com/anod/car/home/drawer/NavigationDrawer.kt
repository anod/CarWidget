package com.anod.car.home.drawer

import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.ListView

import com.anod.car.home.R

/**
 * @author alex
 * @date 2014-10-20
 */
class NavigationDrawer(activity: AppCompatActivity, appWidgetId: Int) {

    private val drawerToggle: ActionBarDrawerToggle

    private val title = activity.title

    private val drawerTitle = activity.title

    private val adapter =  NavigationAdapter(activity, NavigationList(activity, appWidgetId))

    private val drawerLayout: DrawerLayout = activity.findViewById(R.id.drawer_layout)

    private val drawerList: ListView? = activity.findViewById(R.id.left_drawer)

    init {
        drawerList?.adapter = adapter
        drawerList?.setOnItemClickListener { _, _, position, _ ->
            selectItem(position)
        }

        drawerToggle = object : ActionBarDrawerToggle(activity, drawerLayout, R.string.drawer_open,
                R.string.drawer_close) {

            // Called when a drawer has settled in a completely closed state.
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                activity.supportActionBar!!.title = title
                activity.invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            // Called when a drawer has settled in a completely open state.
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                activity.supportActionBar!!.title = drawerTitle
                activity.invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }
        }

        // Set the drawer toggle as the DrawerListener
        drawerLayout.addDrawerListener(drawerToggle)

        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar!!.setHomeButtonEnabled(true)
    }

    fun syncState() {
        drawerToggle.syncState()
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        return drawerToggle.onOptionsItemSelected(item)
    }

    fun setSelected(navigationItem: Int) {
        adapter.selected = navigationItem
    }

    fun refresh() {
        adapter.refresh()
    }

    /** Swaps fragments in the main content view  */
    private fun selectItem(position: Int) {
        if (adapter.onClick(position)) {
            // Highlight the selected item, update the title, and close the drawer
            drawerList!!.setItemChecked(position, true)
            drawerLayout.closeDrawer(drawerList)
        }
    }
}
