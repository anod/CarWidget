package com.anod.car.home.backup.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView

import com.anod.car.home.R
import com.anod.car.home.backup.PreferencesBackupManager
import info.anodsplace.android.log.AppLog
import com.anod.car.home.utils.Utils

class FragmentBackup : Fragment() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private var viewPager: ViewPager? = null
    private var tabs: TabLayout? = null

    private var refreshMenuItem: MenuItem? = null
    internal val backupManager: PreferencesBackupManager by lazy { PreferencesBackupManager(context) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_restore, container, false)
        viewPager = view.findViewById(R.id.pager)
        tabs = view.findViewById(R.id.tabs)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        activity!!.setTitle(R.string.pref_backup_title)
        appWidgetId = Utils.readAppWidgetId(savedInstanceState, activity!!.intent)
        super.onActivityCreated(savedInstanceState)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppLog.e("AppWidgetId required")
            activity!!.finish()
            return
        } else {
            val defaultResultValue = Intent()
            defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            activity!!.setResult(Activity.RESULT_OK, defaultResultValue)
        }

        viewPager?.adapter = RestorePagerAdapter(appWidgetId, childFragmentManager, context!!)
        tabs?.setupWithViewPager(viewPager)

        setHasOptionsMenu(true)
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.restore, menu)

        refreshMenuItem = menu!!.findItem(R.id.menu_refresh)
        refreshMenuItem!!.isVisible = false

        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * stop refresh button animation
     */
    internal fun stopRefreshAnim() {
        if (refreshMenuItem == null) {
            return
        }
        val actionView = refreshMenuItem!!.actionView
        if (actionView != null) {
            actionView.clearAnimation()
            refreshMenuItem!!.actionView = null
        }
        refreshMenuItem!!.isVisible = false
    }

    /**
     * Animate refresh button
     */
    internal fun startRefreshAnim() {
        if (refreshMenuItem == null) {
            return
        }
        val actionView = refreshMenuItem!!.actionView
        //already animating
        if (actionView != null) {
            return
        }
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val iv = inflater.inflate(R.layout.refresh_action_view, null) as ImageView

        val rotation = AnimationUtils.loadAnimation(context, R.anim.rotate)
        rotation.repeatCount = Animation.INFINITE
        iv.startAnimation(rotation)

        refreshMenuItem!!.isVisible = true
        refreshMenuItem!!.actionView = iv
    }

    override fun onPause() {
        stopRefreshAnim()
        super.onPause()
    }

    private class RestorePagerAdapter
        constructor(
                private val appWidgetId: Int,
                fm: FragmentManager,
                private val mContext: Context) : FragmentPagerAdapter(fm) {
        internal var titles = intArrayOf(R.string.backup_current_widget, R.string.backup_incar_settings)

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                FragmentRestoreWidget.create(appWidgetId)
            } else FragmentRestoreInCar()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mContext.getString(titles[position])
        }
    }

    companion object {

        internal const val DOWNLOAD_MAIN_REQUEST_CODE = 1
        internal const val DOWNLOAD_INCAR_REQUEST_CODE = 2

        internal const val DATE_FORMAT = (DateUtils.FORMAT_SHOW_DATE
                or DateUtils.FORMAT_SHOW_WEEKDAY
                or DateUtils.FORMAT_SHOW_TIME
                or DateUtils.FORMAT_SHOW_YEAR
                or DateUtils.FORMAT_ABBREV_ALL)
    }
}