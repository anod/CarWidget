package com.anod.car.home.prefs.lookandfeel

import android.util.SparseArray
import android.view.ViewGroup
import com.anod.car.home.prefs.LookAndFeelActivity

/**
 * @author alex
 * @date 2014-10-20
 */
class SkinPagerAdapter(private val activity: LookAndFeelActivity, private val count: Int, fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {
    private val fragments: SparseArray<SkinPreviewFragment> = SparseArray(count)

    override fun getCount(): Int {
        return count
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return SkinPreviewFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return activity.getSkinItem(position).title
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as SkinPreviewFragment
        fragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        fragments.delete(position)
        super.destroyItem(container, position, `object`)
    }

    fun refresh() {
        for (key in 0 until count) {
            val fragment = fragments.get(key)
            fragment?.refresh()
        }
    }
}
