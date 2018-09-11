package com.anod.car.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.prefs.model.AppTheme
import com.anod.car.home.utils.HtmlCompat
import com.anod.car.home.utils.Version
import kotlinx.android.synthetic.main.wizard_activity.*

/**
 * @author alex
 * @date 5/24/13
 */
class WizardActivity : CarWidgetActivity() {

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private val pagerAdapter: androidx.viewpager.widget.PagerAdapter by lazy { ScreenSlidePagerAdapter(supportFragmentManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wizard_activity)

        // Instantiate a ViewPager and a PagerAdapter.
        pager.adapter = pagerAdapter

        val current = intent?.getIntExtra(EXTRA_PAGE, 0) ?: 0
        pager.currentItem = current
        buttonNext.tag = TYPE_NEXT

        strip.setOnPageSelectedListener { position ->
            val newPosition = Math.min(pagerAdapter.count - 1, position)
            if (pager.currentItem != newPosition) {
                pager.currentItem = newPosition
            }
        }

        pager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                strip.setCurrentPage(position)
                if (position == NUM_PAGES - 1) {
                    buttonNext.tag = TYPE_FINISH
                    buttonNext.setText(R.string.finish)
                    buttonSkip.visibility = View.GONE
                }
            }
        })
        strip.setPageCount(NUM_PAGES)

        findViewById<Button>(R.id.buttonSkip).setOnClickListener {
            finishWizard()
        }

        findViewById<Button>(R.id.buttonNext).setOnClickListener {
            if (buttonNext.tag == TYPE_FINISH) {
                finishWizard()
            } else {
                pager.currentItem = pager.currentItem + 1
            }
        }
    }

    override fun getAppThemeRes(theme: Int): Int {
        return AppTheme.getNoActionBarResource(theme)
    }

    private fun finishWizard() {
        this@WizardActivity.finish()
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            pager.currentItem = pager.currentItem - 1
        }
    }

    class ScreenSlidePagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            val f = ScreenSlidePageFragment()
            // Supply index input as an argument.
            val args = Bundle()
            args.putInt("position", position)
            f.arguments = args
            return f
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }

    class ScreenSlidePageFragment: Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val pos = arguments?.getInt("position", 0) ?: 0
            val layoutId = sFragments[pos]
            val rootView = inflater.inflate(layoutId, container, false) as ViewGroup

            val desc1 = rootView.findViewById<TextView>(R.id.desc1)
            val desc2 = rootView.findViewById<TextView?>(R.id.desc2)
            if (layoutId == R.layout.wizard_fragment_1) {
                desc1.text = HtmlCompat.fromHtml(getString(R.string.welcome_text))
            } else if (layoutId == R.layout.wizard_fragment_2) {
                desc1.text = HtmlCompat.fromHtml(getString(R.string.install_widget))
            } else if (layoutId == R.layout.wizard_fragment_3) {
                desc1.text = HtmlCompat.fromHtml(getString(R.string.configure_text))
                desc2?.text = HtmlCompat.fromHtml(getString(R.string.open_settings_description))
            } else if (layoutId == R.layout.wizard_fragment_4) {
                desc1.text = HtmlCompat.fromHtml(getString(R.string.detect_incar_description))
                desc2?.text = HtmlCompat.fromHtml(getString(R.string.adjust_incar_description))
                val desc3 = rootView.findViewById<TextView>(R.id.desc3)
                desc3.text = HtmlCompat.fromHtml(getString(R.string.enable_incar_description))

                val v = Version(activity)
                val desc4 = rootView.findViewById<TextView>(R.id.desc4)
                if (v.isFree) {
                    desc4.text = HtmlCompat.fromHtml(
                            getString(R.string.trial_description, v.maxTrialTimes))
                } else {
                    desc4.visibility = View.GONE
                }
            }

            return rootView
        }

        companion object {
            private val sFragments = intArrayOf(
                    R.layout.wizard_fragment_1,
                    R.layout.wizard_fragment_2,
                    R.layout.wizard_fragment_3,
                    R.layout.wizard_fragment_4)
        }
    }

    companion object {
        /**
         * The number of pages (wizard steps) to show in this demo.
         */
        private const val TYPE_NEXT = 1
        private const val TYPE_FINISH = 2

        /**
         * The number of pages (wizard steps) to show in this demo.
         */
        private const val NUM_PAGES = 4
        const val EXTRA_PAGE = "page"
    }
}