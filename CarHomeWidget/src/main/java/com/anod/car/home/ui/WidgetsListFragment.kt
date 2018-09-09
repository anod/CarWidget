package com.anod.car.home.ui

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.anod.car.home.R
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.prefs.ConfigurationActivity
import com.anod.car.home.prefs.ConfigurationInCar

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.anod.car.home.utils.*

class WidgetsListFragment : androidx.fragment.app.Fragment(), WidgetsListAdapter.OnItemClickListener {

    private val adapter: WidgetsListAdapter by lazy { WidgetsListAdapter(activity!!, this) }
    private var appWidgetIds: IntArray = intArrayOf()
    private val version: Version by lazy { Version(activity!!) }

    private val inCarView: androidx.cardview.widget.CardView by lazy { view!!.findViewById<androidx.cardview.widget.CardView>(R.id.widgets_incar) }
    private val emptyView: View by lazy { view!!.findViewById<View>(android.R.id.empty) }
    private val listView: androidx.recyclerview.widget.RecyclerView by lazy { view!!.findViewById<androidx.recyclerview.widget.RecyclerView>(android.R.id.list) }

    private val viewModel: WidgetsListViewModel by lazy { ViewModelProviders.of(activity!!).get(WidgetsListViewModel::class.java) }

    override fun onResume() {
        super.onResume()
        appWidgetIds = WidgetHelper.getAllWidgetIds(activity)
        viewModel.loadList()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_widgets_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyView.visibility = View.GONE
        listView.visibility = View.GONE
        inCarView.visibility = View.GONE

        emptyView.setOnClickListener {
            val intent = Intent(activity, WizardActivity::class.java)
            intent.putExtra(WizardActivity.EXTRA_PAGE, 1)
            startActivity(intent)
        }

        listView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        listView.adapter = adapter

        inCarView.setOnClickListener {
            val status = InCarStatus.get(appWidgetIds.size, version, activity)
            if (status == InCarStatus.ENABLED) {
                when {
                    version.isFreeAndTrialExpired -> startActivity(IntentUtils.createProVersionIntent())
                    version.isFree -> startActivity(IntentUtils.createProVersionIntent())
                    else -> {
                        val intent = ConfigurationActivity
                                .createFragmentIntent(activity!!, ConfigurationInCar::class.java)
                        startActivity(intent)
                    }
                }
            } else {
                val intent = ConfigurationActivity
                        .createFragmentIntent(activity!!, ConfigurationInCar::class.java)
                startActivity(intent)
            }
        }

        viewModel.list.observe(this, Observer {
            adapter.setResult(it ?: WidgetList())
            updateViews()
        })
    }

    private fun updateViews() {
        if (adapter.isEmpty) {
            emptyView.visibility = View.VISIBLE
            inCarView.visibility = View.GONE
            listView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            updateInCarHeader(inCarView)
            inCarView.visibility = View.VISIBLE
            listView.visibility = View.VISIBLE
        }
    }

    private fun updateInCarHeader(cardView: androidx.cardview.widget.CardView) {
        val status = InCarStatus.get(appWidgetIds.size, version, activity)
        val active = activity!!.getString(InCarStatus.render(status))

        val incarTitleView = cardView.findViewById<View>(R.id.incarTitle) as TextView
        incarTitleView.text = getString(R.string.pref_incar_mode_title) + " - " + active

        val trialText = cardView.findViewById<View>(R.id.incarTrial) as TextView
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

    override fun onItemClick(item: WidgetsListAdapter.Item) {
        if (item is WidgetsListAdapter.LargeItem) {
            (activity as WidgetsListActivity).startConfigActivity(item.appWidgetId)
        }
    }

    companion object {
        fun newInstance(): WidgetsListFragment {
            return WidgetsListFragment()
        }
    }

}