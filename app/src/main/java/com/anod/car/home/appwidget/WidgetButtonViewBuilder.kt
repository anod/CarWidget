package com.anod.car.home.appwidget

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import com.anod.car.home.R
import com.anod.car.home.incar.ModeService
import info.anodsplace.carwidget.content.preferences.InCarStorage
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import com.anod.car.home.skin.SkinProperties
import info.anodsplace.carwidget.appwidget.PendingIntentFactory

/**
 * @author alex
 * @date 2015-01-31
 */
class WidgetButtonViewBuilder(
    private val context: Context,
    private val prefs: WidgetSettings,
    private val pendingIntentFactory: PendingIntentFactory,
    private val appWidgetId: Int) {

    var alternativeHidden = false

    fun setup(skinProperties: SkinProperties, views: RemoteViews) {
        if (skinProperties.hasWidgetButton1()) {
            setup(R.id.widget_btn1, prefs.widgetButton1, skinProperties, views, BUTTON_1)
        }
        setup(R.id.widget_btn2, prefs.widgetButton2, skinProperties, views, BUTTON_2)
    }

    private fun setup(@IdRes btnResId: Int, widgetButtonPref: Int, skinProperties: SkinProperties,
                      views: RemoteViews, buttonId: Int) {
        if (widgetButtonPref == info.anodsplace.carwidget.content.preferences.WidgetInterface.WIDGET_BUTTON_HIDDEN) {
            if (alternativeHidden) {
                views.setImageViewResource(btnResId, R.drawable.ic_action_cancel)
                val configIntent = pendingIntentFactory
                        .createSettings(appWidgetId, buttonId)
                views.setOnClickPendingIntent(btnResId, configIntent)
            } else {
                views.setViewVisibility(btnResId, View.GONE)
            }
        } else if (widgetButtonPref == info.anodsplace.carwidget.content.preferences.WidgetInterface.WIDGET_BUTTON_INCAR) {
            if (info.anodsplace.carwidget.content.preferences.InCarStorage.load(context).isInCarEnabled) {
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
        } else if (widgetButtonPref == info.anodsplace.carwidget.content.preferences.WidgetInterface.WIDGET_BUTTON_SETTINGS) {
            setSettingsButton(btnResId, skinProperties, views, buttonId)
        }
    }

    private fun setSettingsButton(@IdRes resId: Int, skinProperties: SkinProperties,
                                  views: RemoteViews, buttonId: Int) {
        if (prefs.isSettingsTransparent) {
            views.setImageViewResource(resId, R.drawable.btn_transparent)
        } else {
            views.setImageViewResource(resId, skinProperties.settingsButtonRes)
        }
        val configIntent = pendingIntentFactory.createSettings(appWidgetId, buttonId)
        views.setOnClickPendingIntent(resId, configIntent)
    }

    private fun setInCarButton(@IdRes btnId: Int, isInCarTrans: Boolean, skinProp: SkinProperties,
                               views: RemoteViews, buttonId: Int) {
        views.setViewVisibility(btnId, View.VISIBLE)
        if (ModeService.sInCarMode) {
            if (isInCarTrans) {
                views.setImageViewResource(btnId, R.drawable.btn_transparent)
            } else {
                val rImg = skinProp.inCarButtonExitRes
                views.setImageViewResource(btnId, rImg)
            }
        } else {
            if (isInCarTrans) {
                views.setImageViewResource(btnId, R.drawable.btn_transparent)
            } else {
                val rImg = skinProp.inCarButtonEnterRes
                views.setImageViewResource(btnId, rImg)
            }
        }
        val switchOn = !ModeService.sInCarMode
        val contentIntent = pendingIntentFactory.createInCar(switchOn, buttonId)
        views.setOnClickPendingIntent(btnId, contentIntent)
    }

    companion object {
        const val BUTTON_1 = 1
        const val BUTTON_2 = 2
    }
}
