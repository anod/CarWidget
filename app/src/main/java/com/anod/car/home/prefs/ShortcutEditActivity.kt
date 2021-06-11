package com.anod.car.home.prefs

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.databinding.ActivityShortcutEditBinding

class ShortcutEditActivity : CarWidgetActivity() {

    private lateinit var binding: ActivityShortcutEditBinding

    override val appThemeRes: Int
        get() = R.style.Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortcutEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, ShortcutEditFragment.create(intent.extras ?: Bundle.EMPTY))
                addToBackStack("ShortcutEditFragment")
            }
            supportFragmentManager.addOnBackStackChangedListener {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    finish()
                }
            }
        }
    }

    companion object {
        fun createIntent(context: Context, cellId: Int, shortcutId: Long, appWidgetId: Int): Intent {
            val editIntent = Intent(context, ShortcutEditActivity::class.java)
            editIntent.putExtra(ShortcutEditFragment.extraShortcutId, shortcutId)
            editIntent.putExtra(ShortcutEditFragment.extraCellId, cellId)
            editIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            editIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return editIntent
        }
    }

}
