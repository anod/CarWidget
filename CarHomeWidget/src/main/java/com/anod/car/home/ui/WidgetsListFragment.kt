package com.anod.car.home.ui

import com.anod.car.home.R
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.prefs.ConfigurationActivity
import com.anod.car.home.prefs.ConfigurationInCar
import com.anod.car.home.utils.InCarStatus
import com.anod.car.home.utils.IntentUtils
import com.anod.car.home.utils.Version

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView

class WidgetsListFragment : ListFragment(), LoaderManager.LoaderCallbacks<WidgetsListLoader.Result> {
    private val adapter: WidgetsListAdapter by lazy { WidgetsListAdapter(activity!!) }
    private var appWidgetIds: IntArray = intArrayOf()
    private val version: Version by lazy { Version(activity!!) }
    private val headerView: View by lazy { activity!!.layoutInflater.inflate(R.layout.widgets_incar, listView, false) }

    override fun onResume() {
        super.onResume()
        appWidgetIds = WidgetHelper.getAllWidgetIds(activity)
        loaderManager.initLoader<WidgetsListLoader.Result>(0, null, this).forceLoad()
        updateInCarHeader(headerView)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Start out with a progress indicator.
        listView.addHeaderView(headerView)
        listView.emptyView.setOnClickListener { startWizard(1) }
        listAdapter = adapter
    }

    private fun startWizard(page: Int) {
        val intent = Intent(activity, WizardActivity::class.java)
        intent.putExtra(WizardActivity.EXTRA_PAGE, page)
        startActivity(intent)
    }

    private fun updateInCarHeader(view: View) {

        //var cardView = view.findViewById<CardView>(R.id.card_view)

        val status = InCarStatus.get(appWidgetIds.size, version, activity)
        val active = activity!!.getString(InCarStatus.render(status))

        val incarTitleView = view.findViewById<View>(R.id.incarTitle) as TextView
        incarTitleView.text = getString(R.string.pref_incar_mode_title) + " - " + active

        val trialText = view.findViewById<View>(R.id.incarTrial) as TextView
        if (version.isFreeAndTrialExpired) {
            trialText.text = getString(R.string.dialog_donate_title_expired) + " " + getString(
                    R.string.notif_consider)
        } else if (version.isFree) {
            val activationsLeft = resources
                    .getQuantityString(R.plurals.notif_activations_left,
                            version.trialTimesLeft, version.trialTimesLeft)
            trialText.text = getString(R.string.dialog_donate_title_trial) + " " + activationsLeft
        } else {
            trialText.visibility = View.GONE
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.widgets_list, container, false)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        if (position == INCAR_HEADER) {
            val status = InCarStatus.get(appWidgetIds.size, version, activity)
            if (status == InCarStatus.ENABLED) {
                if (version.isFreeAndTrialExpired) {
                    startActivity(IntentUtils.createProVersionIntent())
                } else if (version.isFree) {
                    startActivity(IntentUtils.createProVersionIntent())
                } else {
                    val intent = ConfigurationActivity
                            .createFragmentIntent(activity!!, ConfigurationInCar::class.java)
                    startActivity(intent)
                }
            } else {
                val intent = ConfigurationActivity
                        .createFragmentIntent(activity!!, ConfigurationInCar::class.java)
                startActivity(intent)
            }
            return
        }
        if (position - 1 < appWidgetIds.size) {
            val item = adapter.getItem(position - 1)
            if (item is WidgetsListAdapter.LargeItem) {
                (activity as WidgetsListActivity).startConfigActivity(item.appWidgetId)
            }
        }
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<WidgetsListLoader.Result> {
        return WidgetsListLoader(context!!)
    }

    override fun onLoadFinished(loader: Loader<WidgetsListLoader.Result>,
                                result: WidgetsListLoader.Result) {
        adapter.setResult(result)
    }

    override fun onLoaderReset(loader: Loader<WidgetsListLoader.Result>) {
        adapter.setResult(null)
    }

    companion object {

        private const val INCAR_HEADER = 0

        fun newInstance(): WidgetsListFragment {
            return WidgetsListFragment()
        }
    }

}