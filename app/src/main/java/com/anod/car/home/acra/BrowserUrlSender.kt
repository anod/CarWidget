package com.anod.car.home.acra

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.anod.car.home.BuildConfig
import info.anodsplace.applog.AppLog
import org.acra.ReportField
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender

class BrowserUrlSender : ReportSender {
    private val baseUri = Uri.parse("https://anodsplace.info/acra/report/adapter.php")

    override fun requiresForeground(): Boolean = true

    override fun send(context: Context, errorContent: CrashReportData) {
        var appId = 0x00
        if (BuildConfig.DEBUG) {
            appId = appId or 0x10
        }

        val uri = baseUri.buildUpon().apply {
            appendQueryParameter("a", appId.toString())
            appendQueryParameter("b", errorContent.getString(ReportField.APP_VERSION_NAME))
            appendQueryParameter("c", errorContent.getString(ReportField.APP_VERSION_CODE))

            appendQueryParameter("d", errorContent.getString(ReportField.ANDROID_VERSION))
            appendQueryParameter("e", errorContent.getString(ReportField.USER_APP_START_DATE))
            appendQueryParameter("f", errorContent.getString(ReportField.USER_CRASH_DATE))

            appendQueryParameter("r", errorContent.getString(ReportField.REPORT_ID))

            appendQueryParameter("g", errorContent.getString(ReportField.PHONE_MODEL))
            appendQueryParameter("h", errorContent.getString(ReportField.BRAND))

            appendQueryParameter("v", minifyTrace(
                errorContent.getString(ReportField.STACK_TRACE) ?: ""))

            appendQueryParameter("l", errorContent.getString(ReportField.LOGCAT))
        }.build()

        AppLog.d(uri.toString())

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        context.startActivity(intent)
    }

    private fun minifyTrace(fullTrace: String): String {
        AppLog.d(fullTrace)
        val lines = fullTrace.split("\n").dropLastWhile { it.isEmpty() }

        if (lines.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append(lines[0])
            var findLines = 3
            var causedLines = 2
            for (i in 1 until lines.size) {
                if (findLines > 0 && lines[i].contains("at com.anod.car.home")) {
                    findLines--
                    sb.append("\n").append(lines[i])
                } else if (causedLines > 0 && lines[i].contains("Caused by:")) {
                    causedLines--
                    sb.append("\n").append(lines[i])
                }

                if (findLines == 0 && causedLines == 0) {
                    break
                }
            }
            return sb.toString()
        }
        return ""
    }
}