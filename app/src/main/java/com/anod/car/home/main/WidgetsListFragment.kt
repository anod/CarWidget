package com.anod.car.home.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.anod.car.home.R
import com.anod.car.home.appwidget.WidgetHelper
import com.anod.car.home.prefs.ConfigurationActivity
import com.anod.car.home.prefs.ConfigurationInCar
import com.anod.car.home.utils.InCarStatus
import com.anod.car.home.utils.Version
import com.anod.car.home.utils.forProVersion
import kotlinx.android.synthetic.main.fragment_widgets_list.*

class WidgetsListFragment : Fragment(), WidgetsListAdapter.OnItemClickListener {

    private val adapter: WidgetsListAdapter by lazy { WidgetsListAdapter(requireContext(), this) }
    private var appWidgetIds: IntArray = intArrayOf()
    private val version: Version by lazy { Version(requireContext()) }
    private val viewModel: WidgetsListViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        appWidgetIds = WidgetHelper.getAllWidgetIds(requireContext())
        viewModel.loadList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_widgets_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        empty.visibility = View.GONE
        list.visibility = View.GONE
        inCarView.visibility = View.GONE

        empty.setOnClickListener {
            val intent = Intent(activity, WizardActivity::class.java)
            intent.putExtra(WizardActivity.EXTRA_PAGE, 1)
            startActivity(intent)
        }

        list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        list.adapter = adapter

        inCarView.setOnClickListener {
            val status = InCarStatus(appWidgetIds.size, version, requireContext())
            if (status.isEnabled) {
                when {
                    version.isFreeAndTrialExpired -> startActivity(Intent().forProVersion())
                    version.isFree -> startActivity(Intent().forProVersion())
                    else -> {
                        if (activity is WidgetsListActivity) {
                            (activity as WidgetsListActivity).showInCarSettings()
                        }
                    }
                }
            } else {
                val intent = ConfigurationActivity
                        .createFragmentIntent(requireActivity(), ConfigurationInCar::class.java)
                startActivity(intent)
            }
        }

        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.setResult(it ?: WidgetList())
            updateViews()
        })
    }

    private fun updateViews() {
        if (adapter.isEmpty) {
            empty.visibility = View.VISIBLE
            inCarView.visibility = View.GONE
            list.visibility = View.GONE
        } else {
            empty.visibility = View.GONE
            updateInCarHeader(inCarView)
            inCarView.visibility = View.VISIBLE
            list.visibility = View.VISIBLE
        }
    }

    private fun updateInCarHeader(cardView: androidx.cardview.widget.CardView) {
        val status = InCarStatus(appWidgetIds.size, version, requireContext())
        val active = getString(status.resId)

        val incarTitleView = cardView.findViewById<View>(R.id.incarTitle) as TextView
        incarTitleView.text = getString(R.string.pref_incar_mode_title) + " - " + active

        val trialText = cardView.findViewById<View>(R.id.incarTrial) as TextView
        when {
            version.isFreeAndTrialExpired -> trialText.text = getString(R.string.dialog_donate_title_expired) + " " + getString(
                    R.string.notif_consider)
            version.isFree -> {
                val activationsLeft = resources
                        .getQuantityString(R.plurals.notif_activations_left,
                                version.trialTimesLeft, version.trialTimesLeft)
                trialText.text = getString(R.string.dialog_donate_title_trial) + " " + activationsLeft
            }
            else -> trialText.visibility = View.GONE
        }
    }

    override fun onItemClick(item: WidgetsListAdapter.Item) {
        if (item is WidgetsListAdapter.LargeItem) {
            (activity as WidgetsListActivity).startConfigActivity(item.appWidgetId)
        }
    }
}