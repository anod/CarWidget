package info.anodsplace.carwidget.content

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Version(private val context: Context) {

    val isFree = isFree(context.packageName)

    private var trialCounterCache = -1

    private var prefs: SharedPreferences? = null

    val maxTrialTimes: Int
        get() = MAX_TRIAL_TIMES

    val trialTimesLeft: Int
        get() {
            initTrialCounter()
            return MAX_TRIAL_TIMES - trialCounterCache
        }

    private val isTrialExpired: Boolean
        get() = trialTimesLeft <= 0

    val isFreeAndTrialExpired: Boolean
        get() = isFree && isTrialExpired

    val isProOrTrial: Boolean
        get() = if (!isFree) {
            true
        } else !isTrialExpired


    fun increaseTrialCounter() {
        initTrialCounter()
        trialCounterCache++
        prefs!!.edit().putInt(PREF_TRIAL_TIMES, trialCounterCache).apply()
    }

    private fun initTrialCounter() {
        if (trialCounterCache == -1) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
            trialCounterCache = prefs!!.getInt(PREF_TRIAL_TIMES, 0)
        }
    }

    companion object {
        private const val MAX_TRIAL_TIMES = 30
        private const val PREF_TRIAL_TIMES = "trial-times"
        private const val FREE_PACKAGE_NAME = "com.anod.car.home.free"
        const val PRO_PACKAGE_NAME = "com.anod.car.home.pro"

        fun isFree(packageName: String): Boolean {
            return packageName.startsWith(FREE_PACKAGE_NAME)
        }
    }
}
