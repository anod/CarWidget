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
import com.anod.car.home.databinding.FragmentWidgetsListBinding
import com.anod.car.home.prefs.ConfigurationActivity
import com.anod.car.home.prefs.ConfigurationInCar
import com.anod.car.home.utils.InCarStatus
import com.anod.car.home.utils.Version
import com.anod.car.home.utils.forProVersion

class WidgetsListFragment : Fragment(), WidgetsListAdapter.OnItemClickListener {

    private var _binding: FragmentWidgetsListBinding? = null
    private val binding get() = _binding!!
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
        _binding = FragmentWidgetsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.empty.visibility = View.GONE
        binding.list.visibility = View.GONE
        binding.inCarView.visibility = View.GONE

        binding.empty.setOnClickListener {
            val intent = Intent(activity, WizardActivity::class.java)
            intent.putExtra(WizardActivity.EXTRA_PAGE, 1)
            startActivity(intent)
        }

        binding.list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        binding.list.adapter = adapter

        binding.inCarView.setOnClickListener {
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
            binding.empty.visibility = View.VISIBLE
            binding.inCarView.visibility = View.GONE
            binding.list.visibility = View.GONE
        } else {
            binding.empty.visibility = View.GONE
            updateInCarHeader(binding.inCarView)
            binding.inCarView.visibility = View.VISIBLE
            binding.list.visibility = View.VISIBLE
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