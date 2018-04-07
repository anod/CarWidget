package com.anod.car.home.acra

import com.anod.car.home.BuildConfig

import org.acra.ReportField
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

import android.content.Context
import android.content.Intent
import android.net.Uri

import info.anodsplace.android.log.AppLog

class BrowserUrlSender : ReportSender {
    private val baseUri = Uri.parse("https://anodsplace.info/acra/report/adapter.php")

    class Factory : ReportSenderFactory {
        override fun create(context: Context, config: CoreConfiguration): ReportSender {
            return BrowserUrlSender()
        }
    }

    override fun send(context: Context, errorContent: CrashReportData) {

        val builder = baseUri.buildUpon()

        var appId = if (BuildConfig.FLAVOR == "pro") 0x01 else 0x00
        if (BuildConfig.DEBUG) {
            appId = appId or 0x10
        }
        builder.appendQueryParameter("a", appId.toString())
        builder.appendQueryParameter("b", errorContent.getString(ReportField.APP_VERSION_NAME))
        builder.appendQueryParameter("c", errorContent.getString(ReportField.APP_VERSION_CODE))

        builder.appendQueryParameter("d", errorContent.getString(ReportField.ANDROID_VERSION))
        builder.appendQueryParameter("e", errorContent.getString(ReportField.USER_APP_START_DATE))
        builder.appendQueryParameter("f", errorContent.getString(ReportField.USER_CRASH_DATE))

        builder.appendQueryParameter("r", errorContent.getString(ReportField.REPORT_ID))

        builder.appendQueryParameter("g", errorContent.getString(ReportField.PHONE_MODEL))
        builder.appendQueryParameter("h", errorContent.getString(ReportField.BRAND))

        builder.appendQueryParameter("v", minifyTrace(
                errorContent.getString(ReportField.STACK_TRACE) ?: ""))

        builder.appendQueryParameter("w", errorContent.getString(ReportField.USER_COMMENT))

        val uri = builder.build()

        AppLog.d(uri.toString())

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        context.startActivity(intent)
    }

    private fun minifyTrace(fullTrace: String): String {
        AppLog.d(fullTrace)
        val lines = fullTrace.split("\n").dropLastWhile({ it.isEmpty() })

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