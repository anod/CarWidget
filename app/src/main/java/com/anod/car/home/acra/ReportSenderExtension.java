package com.anod.car.home.acra;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.data.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

@AutoService(ReportSender.class)
public class ReportSenderExtension implements ReportSender {
    private final BrowserUrlSender browserUrlSender = new BrowserUrlSender();
    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData errorContent) throws ReportSenderException {
        browserUrlSender.send(context, errorContent);
    }
}
