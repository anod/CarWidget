package com.anod.car.home.drawer

import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View

import com.anod.car.home.R

/**
 * @author alex
 * @date 2014-10-20
 */
class NavigationDrawer(activity: AppCompatActivity, appWidgetId: Int) {

    private val drawerToggle: ActionBarDrawerToggle

    private val title = activity.title

    private val drawerTitle = activity.title

    private val drawerLayout: androidx.drawerlayout.widget.DrawerLayout = activity.findViewById(R.id.drawer_layout)

    private val drawerView: NavigationView? = activity.findViewById(R.id.left_drawer)

    private val selection: NavigationDrawerSelection = NavigationDrawerSelection(activity, appWidgetId)

    init {
        if (appWidgetId == 0) {
            drawerView?.menu?.findItem(R.id.nav_current_widget_group)?.isVisible = false
        }

        drawerView?.setNavigationItemSelectedListener { menuItem ->
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            selection.onClick(menuItem.itemId)

            // close drawer when item is tapped
            drawerLayout.closeDrawers()

            true
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

    fun setSelected(itemId: Int) {
        drawerView?.menu?.findItem(itemId)?.isChecked = true
    }

}
