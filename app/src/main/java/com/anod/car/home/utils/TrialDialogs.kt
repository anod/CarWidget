package com.anod.car.home.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.anod.car.home.R
import com.anod.car.home.app.App
import info.anodsplace.framework.app.DialogMessage

object TrialDialogs {

    fun buildProInstalledDialog(context: Context): Dialog = DialogMessage(
            context,
            App.theme(context).alert,
            R.string.dialog_donate_title_install,
            R.string.dialog_donate_message_installed
    ) {}.create()

    fun buildTrialDialog(trialsLeft: Int, context: Context): Dialog {
        val message = if (trialsLeft > 0) {
            val activationsLeftText = context.resources
                    .getQuantityString(R.plurals.notif_activations_left, trialsLeft, trialsLeft)
            activationsLeftText + context.getString(R.string.dialog_donate_message_trial)
        } else {
            context.getString(R.string.dialog_donate_message_expired)
        }
        val title = if (trialsLeft > 0) R.string.dialog_donate_title_trial else R.string.dialog_donate_title_expired
        return DialogMessage(
                context,
                App.theme(context).alert,
                title,
                message
        ) {
            val negativeRes = if  (trialsLeft > 0) R.string.dialog_donate_btn_trial else R.string.dialog_donate_btn_no
            setTrialDialogButtons(context, it, negativeRes)
        }.create()
    }

    fun buildProOnlyDialog(context: Context): Dialog = DialogMessage(
            context,
            App.theme(context).alert,
            R.string.app_name,
            R.string.dialog_donate_message_expired
    ) {
        setTrialDialogButtons(context, it, R.string.dialog_donate_btn_no)
    }.create()

    private fun setTrialDialogButtons(context: Context, builder: AlertDialog.Builder,
                                      negativeRes: Int) {
        builder.setNeutralButton(R.string.dialog_donate_btn_yes) { dialog, _ ->
            val intent = Intent().forProVersion()
            context.startActivitySafely(intent)
            dialog.dismiss()
        }
        builder.setNegativeButton(negativeRes) { dialog, _ -> dialog.dismiss() }
    }

}
