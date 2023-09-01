package com.anod.car.home.acra

import android.content.Context
import com.google.auto.service.AutoService
import info.anodsplace.applog.AppLog
import org.acra.config.CoreConfiguration
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

@AutoService(ReportSenderFactory::class)
class BrowserUrlSenderFactory : ReportSenderFactory {

    init {
        AppLog.d("BrowserUrlSender registered")
    }

    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        return BrowserUrlSender()
    }
}
