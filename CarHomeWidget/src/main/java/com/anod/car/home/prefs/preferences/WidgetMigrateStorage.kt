package com.anod.car.home.prefs.preferences

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Color
import android.preference.PreferenceManager
import androidx.core.content.res.ResourcesCompat
import com.anod.car.home.R
import com.anod.car.home.model.Shortcut
import com.anod.car.home.prefs.model.WidgetInterface
import com.anod.car.home.utils.BitmapTransform.RotateDirection
import java.util.*

object WidgetMigrateStorage {

    private const val CMP_NUMBER = "cmp-number-%d"
    private const val LAUNCH_COMPONENT_NUMBER_DEFAULT = 6
    private const val LAUNCH_COMPONENT = "launch-component-%d"
    private const val SKIN = "skin-%d"
    private const val BG_COLOR = "bg-color-%d"
    private const val BUTTON_COLOR = "button-color-%d"
    private const val ICONS_MONO = "icons-mono-%d"
    private const val ICONS_COLOR = "icons-color-%d"
    private const val ICONS_SCALE = "icons-scale-%d"
    private const val FONT_SIZE = "font-size-%d"
    private const val FONT_COLOR = "font-color-%d"
    private const val FIRST_TIME = "first-time-%d"
    private const val TRANSPARENT_BTN_SETTINGS = "transparent-btn-settings-%d"
    private const val TRANSPARENT_BTN_INCAR = "transparent-btn-incar-%d"
    private const val ICONS_THEME = "icons-theme-%d"
    private const val ICONS_ROTATE = "icons-rotate-%d"
    private const val TITLES_HIDE = "titles-hide-%d"
    private const val WIDGET_BUTTON_1 = "widget-button-1-%d"
    private const val WIDGET_BUTTON_2 = "widget-button-2-%d"
    private const val ICONS_DEF_VALUE = "5"

    fun loadMain(context: Context, appWidgetId: Int): Main {
        val prefs = WidgetSharedPreferences(context)
        prefs.setAppWidgetId(appWidgetId)
        val res = context.resources

        val p = Main()
        val skinName = prefs.getString(SKIN, WidgetInterface.SKIN_CARDS)
        p.skin = skinName

        val defTileColor = ResourcesCompat.getColor(res, R.color.w7_tale_default_background, null)
        val tileColor = prefs.getInt(BUTTON_COLOR, defTileColor)
        p.tileColor = tileColor

        p.setIconsScaleString(prefs.getString(ICONS_SCALE, ICONS_DEF_VALUE)!!)
        p.isIconsMono = prefs.getBoolean(ICONS_MONO, false)
        p.backgroundColor = prefs.getInt(BG_COLOR, ResourcesCompat.getColor(res, R.color.default_background, null))
        p.iconsColor = prefs.getColor(ICONS_COLOR)
        p.fontColor = prefs.getInt(FONT_COLOR, ResourcesCompat.getColor(res, R.color.default_font_color, null))
        p.fontSize = prefs.getInt(FONT_SIZE, WidgetInterface.FONT_SIZE_UNDEFINED)
        p.isSettingsTransparent = prefs.getBoolean(TRANSPARENT_BTN_SETTINGS, false)
        p.isIncarTransparent = prefs.getBoolean(TRANSPARENT_BTN_INCAR, false)
        p.iconsTheme = prefs.getString(ICONS_THEME, null)

        p.iconsRotate = RotateDirection.valueOf(prefs.getString(ICONS_ROTATE, RotateDirection.NONE.name)!!)
        p.isTitlesHide = prefs.getBoolean(TITLES_HIDE, false)

        p.widgetButton1 = prefs.getInt(WIDGET_BUTTON_1, WidgetInterface.WIDGET_BUTTON_INCAR)
        p.widgetButton2 = prefs.getInt(WIDGET_BUTTON_2, WidgetInterface.WIDGET_BUTTON_SETTINGS)

        return p
    }

    private fun getLaunchComponentKey(id: Int): String {
        return String.format(Locale.US, LAUNCH_COMPONENT, id) + "-%d"
    }

    private fun getLaunchComponentName(id: Int, aAppWidgetId: Int): String {
        return String.format(getLaunchComponentKey(id), aAppWidgetId)
    }

    fun getLauncherComponents(context: Context, appWidgetId: Int,
                              count: Int): ArrayList<Long> {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val ids = ArrayList<Long>(count)
        for (i in 0 until count) {
            val key = getLaunchComponentName(i, appWidgetId)
            val id = prefs.getLong(key, Shortcut.idUnknown)
            ids.add(i, id)
        }
        return ids
    }

    fun getLaunchComponentNumber(context: Context, appWidgetId: Int): Int {
        val prefs = WidgetSharedPreferences(context)
        prefs.setAppWidgetId(appWidgetId)
        val num = prefs
                .getInt(CMP_NUMBER, prefs.getInt("cmp-number", LAUNCH_COMPONENT_NUMBER_DEFAULT))
        return if (num == 0) LAUNCH_COMPONENT_NUMBER_DEFAULT else num
    }

    fun isFirstTime(context: Context, appWidgetId: Int): Boolean {
        val prefs = WidgetSharedPreferences(context)
        prefs.setAppWidgetId(appWidgetId)
        return prefs.getBoolean(FIRST_TIME, true)
    }

    internal class WidgetSharedPreferences /* implements SharedPreferences */(context: Context) {

        private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        private var mAppWidgetId: Int = 0

        private var mWidgetEdit: WidgetEditor? = null

        fun setAppWidgetId(appWidgetId: Int) {
            mAppWidgetId = appWidgetId
        }

        @SuppressLint("CommitPrefEdits")
        fun edit(): WidgetEditor {
            if (mWidgetEdit == null) {
                val edit = mPrefs.edit()
                mWidgetEdit = WidgetEditor(mAppWidgetId, edit)
            }
            return mWidgetEdit!!
        }

        fun getBoolean(key: String, defValue: Boolean): Boolean {
            val keyId = getName(key, mAppWidgetId)
            return mPrefs.getBoolean(keyId, defValue)
        }

        fun getBoolean(key: String, listId: Int, defValue: Boolean): Boolean {
            val keyId = getListName(key, listId, mAppWidgetId)
            return mPrefs.getBoolean(keyId, defValue)
        }

        fun getFloat(key: String, defValue: Float): Float {
            val keyId = getName(key, mAppWidgetId)
            return mPrefs.getFloat(keyId, defValue)
        }

        fun getInt(key: String, defValue: Int): Int {
            val keyId = getName(key, mAppWidgetId)
            return mPrefs.getInt(keyId, defValue)
        }

        fun getLong(key: String, defValue: Long): Long {
            val keyId = getName(key, mAppWidgetId)
            return mPrefs.getLong(keyId, defValue)
        }

        fun getString(key: String, defValue: String?): String? {
            val keyId = getName(key, mAppWidgetId)
            return mPrefs.getString(keyId, defValue)
        }

        fun getColor(key: String): Int? {
            val prefName = getName(key, mAppWidgetId)
            return if (!mPrefs.contains(prefName)) {
                null
            } else mPrefs.getInt(prefName, Color.WHITE)
        }

        fun getComponentName(key: String): ComponentName? {
            val compString = getString(key, null) ?: return null
            val compParts = compString.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return ComponentName(compParts[0], compParts[1])
        }

        inner class WidgetEditor(private var mEditAppWidgetId: Int, private val mEdit: Editor) : Editor {

            fun setAppWidgetId(appWidgetId: Int) {
                mEditAppWidgetId = appWidgetId
            }

            override fun apply() {
                mEdit.commit()
            }

            override fun clear(): Editor {
                mEdit.clear()
                return this
            }

            override fun commit(): Boolean {
                return mEdit.commit()
            }

            override fun putBoolean(key: String, value: Boolean): Editor {
                val keyId = getName(key, mEditAppWidgetId)
                mEdit.putBoolean(keyId, value)
                return this
            }

            fun putBoolean(key: String, listId: Int, value: Boolean): Editor {
                val keyId = getListName(key, listId, mEditAppWidgetId)
                mEdit.putBoolean(keyId, value)
                return this
            }

            override fun putFloat(key: String, value: Float): Editor {
                val keyId = getName(key, mEditAppWidgetId)
                mEdit.putFloat(keyId, value)
                return this
            }

            override fun putInt(key: String, value: Int): Editor {
                val keyId = getName(key, mEditAppWidgetId)
                mEdit.putInt(keyId, value)
                return this
            }

            /**
             * @param key
             * @param value
             * @return
             */
            fun putIntOrRemove(key: String, value: Int): WidgetEditor {
                val keyId = getName(key, mEditAppWidgetId)
                if (value > 0) {
                    mEdit.putInt(keyId, value)
                } else {
                    mEdit.remove(keyId)
                }
                return this
            }

            override fun putLong(key: String, value: Long): Editor {
                val keyId = getName(key, mEditAppWidgetId)
                mEdit.putLong(keyId, value)
                return this
            }

            override fun putString(key: String, value: String?): Editor {
                val keyId = getName(key, mEditAppWidgetId)
                mEdit.putString(keyId, value)
                return this
            }

            fun putStringOrRemove(key: String, value: String?): WidgetEditor {
                val keyId = getName(key, mEditAppWidgetId)
                if (value != null) {
                    mEdit.putString(keyId, value)
                } else {
                    mEdit.remove(keyId)
                }
                return this
            }

            fun putComponentOrRemove(key: String, component: ComponentName?): WidgetEditor {
                val keyId = getName(key, mEditAppWidgetId)
                if (component != null) {
                    val value = component.packageName + "/" + component.className
                    mEdit.putString(keyId, value)
                } else {
                    mEdit.remove(keyId)
                }
                return this
            }

            override fun putStringSet(key: String, value: Set<String>?): Editor {
                //String keyId = getName(key, mEditAppWidgetId);
                //mEdit.putStringSet(keyId, value);
                throw IllegalAccessError("Not implemented")
                //return this;
            }

            override fun remove(key: String): Editor {
                val keyId = getName(key, mEditAppWidgetId)
                mEdit.remove(keyId)
                return this
            }

            fun remove(key: String, listId: Int): Editor {
                val keyId = getListName(key, listId, mEditAppWidgetId)
                mEdit.remove(keyId)
                return this
            }
        }

        companion object {

            fun getName(prefName: String, aAppWidgetId: Int): String {
                return String.format(prefName, aAppWidgetId)
            }

            fun getListName(prefName: String, listId: Int, aAppWidgetId: Int): String {
                return String.format(prefName, aAppWidgetId, listId)
            }
        }

    }
}
