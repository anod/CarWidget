package info.anodsplace.carwidget.content

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager

class Version(private val context: Context) {

    val isFree = isFree(context.packageName)

    private var trialCounterCache = -1

    private var prefs: SharedPreferences? = null

    val trialTimesLeft: Int
        get() {
            initTrialCounter()
            return maxTrialTimes - trialCounterCache
        }

    private val isTrialExpired: Boolean
        get() = trialTimesLeft <= 0

    val isFreeAndTrialExpired: Boolean
        get() = isFree && isTrialExpired

    val isProOrTrial: Boolean
        get() = if (!isFree) {
            true
        } else !isTrialExpired

    val isProInstalled: Boolean
        get() = try {
            context.packageManager.getApplicationInfo("com.anod.car.home.pro", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    val isFreeInstalled: Boolean
        get() = try {
            context.packageManager.getApplicationInfo("com.anod.car.home.free", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

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
        const val maxTrialTimes = 30
        private const val PREF_TRIAL_TIMES = "trial-times"
        private const val FREE_PACKAGE_NAME = "com.anod.car.home.free"
        const val PRO_PACKAGE_NAME = "com.anod.car.home.pro"

        fun isFree(packageName: String): Boolean {
            return packageName.startsWith(FREE_PACKAGE_NAME)
        }
    }
}