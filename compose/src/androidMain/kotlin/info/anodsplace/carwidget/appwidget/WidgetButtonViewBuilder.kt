package info.anodsplace.carwidget.appwidget

import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import info.anodsplace.carwidget.content.SkinProperties
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface.Companion.BUTTON_ID_1
import info.anodsplace.carwidget.content.preferences.WidgetInterface.Companion.BUTTON_ID_2

/**
 * @author alex
 * @date 2015-01-31
 */
class WidgetButtonViewBuilder(
    private val skinProperties: SkinProperties,
    private val inCarMode: Boolean,
    private val prefs: WidgetInterface,
    private val pendingIntentFactory: PendingIntentFactory,
    private val inCarSettings: InCarInterface,
    private val appWidgetId: Int,
    private val alternativeHidden: Boolean = false,
) {

    fun apply(views: RemoteViews) {
        if (skinProperties.supportsWidgetButton1()) {
            apply(skinProperties.widgetButton1Id, prefs.widgetButton1, skinProperties, views, BUTTON_ID_1)
        }
        apply(skinProperties.widgetButton2Id, prefs.widgetButton2, skinProperties, views, BUTTON_ID_2)
    }

    private fun apply(@IdRes btnResId: Int, widgetButtonPref: Int, skinProperties: SkinProperties,
                      views: RemoteViews, buttonId: Int) {
        if (widgetButtonPref == WidgetInterface.WIDGET_BUTTON_HIDDEN) {
            if (alternativeHidden) {
                views.setImageViewResource(btnResId, skinProperties.buttonAlternativeHiddenResId)
                val configIntent = pendingIntentFactory
                        .createSettings(appWidgetId, buttonId)
                views.setOnClickPendingIntent(btnResId, configIntent)
            } else {
                views.setViewVisibility(btnResId, View.GONE)
            }
        } else if (widgetButtonPref == WidgetInterface.WIDGET_BUTTON_INCAR) {
            if (inCarSettings.isInCarEnabled) {
                setInCarButton(btnResId, prefs.isIncarTransparent, skinProperties, views,
                        buttonId)
            } else {
                if (alternativeHidden) {
                    setInCarButton(btnResId, prefs.isIncarTransparent, skinProperties, views,
                            buttonId)
                } else {
                    views.setViewVisibility(btnResId, View.GONE)
                }
            }
        } else if (widgetButtonPref == WidgetInterface.WIDGET_BUTTON_SETTINGS) {
            setSettingsButton(btnResId, skinProperties, views, buttonId)
        }
    }

    private fun setSettingsButton(@IdRes resId: Int, skinProperties: SkinProperties,
                                  views: RemoteViews, buttonId: Int) {
        if (prefs.isSettingsTransparent) {
            views.setImageViewResource(resId, skinProperties.buttonTransparentResId)
        } else {
            views.setImageViewResource(resId, skinProperties.settingsButtonRes)
        }
        val configIntent = pendingIntentFactory.createSettings(appWidgetId, buttonId)
        views.setOnClickPendingIntent(resId, configIntent)
    }

    private fun setInCarButton(@IdRes btnId: Int, isInCarTrans: Boolean, skinProp: SkinProperties,
                               views: RemoteViews, buttonId: Int) {
        views.setViewVisibility(btnId, View.VISIBLE)
        if (inCarMode) {
            if (isInCarTrans) {
                views.setImageViewResource(btnId, skinProperties.buttonTransparentResId)
            } else {
                val rImg = skinProp.inCarButtonExitRes
                views.setImageViewResource(btnId, rImg)
            }
        } else {
            if (isInCarTrans) {
                views.setImageViewResource(btnId, skinProperties.buttonTransparentResId)
            } else {
                val rImg = skinProp.inCarButtonEnterRes
                views.setImageViewResource(btnId, rImg)
            }
        }
        val switchOn = !inCarMode
        val contentIntent = pendingIntentFactory.createInCar(switchOn, buttonId)
        views.setOnClickPendingIntent(btnId, contentIntent)
    }
}