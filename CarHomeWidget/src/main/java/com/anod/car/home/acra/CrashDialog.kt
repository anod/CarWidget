package com.anod.car.home.acra

import com.anod.car.home.R

import org.acra.ACRA
import org.acra.dialog.BaseCrashReportDialog
import org.acra.prefs.SharedPreferencesFactory

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText

/**
 * @author alex
 * @date 2015-05-28
 */
class CrashDialog : BaseCrashReportDialog(), DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private var userCommentView: EditText? = null

    private var dialog: AlertDialog? = null

    override fun init(savedInstanceState: Bundle?) {
        super.init(savedInstanceState)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(buildCustomView(savedInstanceState))
        dialogBuilder.setPositiveButton(R.string.crash_dialog_report_button, this)
        dialogBuilder.setNegativeButton(android.R.string.cancel, this)

        dialog = dialogBuilder.create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnDismissListener(this)
        dialog?.show()
    }

    private fun buildCustomView(savedInstanceState: Bundle?): View {

        val root = LayoutInflater.from(this).inflate(R.layout.dialog_crash_report, null, false)
        userCommentView = root.findViewById(android.R.id.edit)

        val savedValue = savedInstanceState?.getString(STATE_COMMENT)
        if (savedValue != null) {
            userCommentView?.setText(savedValue)
        }

        return root
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Retrieve user comment
            val comment = if (userCommentView != null) userCommentView!!.text.toString() else ""

            // Store the user email
            val userEmail: String?
            val prefs = SharedPreferencesFactory(applicationContext, config).create()
            userEmail = prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, "")
            sendCrash(comment, userEmail)
        } else {
            cancelReports()
        }

        finish()
    }

    override fun onDismiss(dialog: DialogInterface) {
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!userCommentView?.text.isNullOrEmpty()) {
            outState.putString(STATE_COMMENT, userCommentView!!.text.toString())
        }
    }

    companion object {
        private const val STATE_COMMENT = "comment"
    }
}
