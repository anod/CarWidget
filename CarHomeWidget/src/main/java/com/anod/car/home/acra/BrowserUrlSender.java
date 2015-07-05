package com.anod.car.home.acra;

import com.anod.car.home.BuildConfig;
import com.anod.car.home.utils.AppLog;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class BrowserUrlSender implements ReportSender {
    private final Uri mBaseUri = Uri.parse("https://anodsplace.info/acra/report/adapter.php");

    public BrowserUrlSender(){
    }

    @Override
    public void send(Context context, CrashReportData errorContent) throws ReportSenderException {

        Uri.Builder builder = mBaseUri.buildUpon();

        int appId = BuildConfig.
                FLAVOR.equals("pro") ? 0x01 : 0x00;
        if (BuildConfig.DEBUG) {
            appId = appId | 0x10;
        }
        builder.appendQueryParameter("a", String.valueOf(appId));
        builder.appendQueryParameter("b", errorContent.getProperty(ReportField.APP_VERSION_NAME));
        builder.appendQueryParameter("c", errorContent.getProperty(ReportField.APP_VERSION_CODE));

        builder.appendQueryParameter("d", errorContent.getProperty(ReportField.ANDROID_VERSION));
        builder.appendQueryParameter("e", errorContent.getProperty(ReportField.USER_APP_START_DATE));
        builder.appendQueryParameter("f", errorContent.getProperty(ReportField.USER_CRASH_DATE));

        builder.appendQueryParameter("r", errorContent.getProperty(ReportField.REPORT_ID));

        builder.appendQueryParameter("g", errorContent.getProperty(ReportField.PHONE_MODEL));
        builder.appendQueryParameter("h", errorContent.getProperty(ReportField.BRAND));

        builder.appendQueryParameter("v", minifyTrace(
                errorContent.getProperty(ReportField.STACK_TRACE)));

        builder.appendQueryParameter("w", errorContent.getProperty(ReportField.USER_COMMENT));

        Uri uri = builder.build();

        AppLog.d(uri.toString());

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }

    private String minifyTrace(String fullTrace) {
        AppLog.d(fullTrace);
        if (fullTrace != null) {
            String[] lines = fullTrace.split("\n");

            if(lines.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(lines[0]);
                int findLines = 3;
                for (int i=1; i<lines.length; i++) {
                    if (lines[i].contains("at com.anod.car.home")) {
                        findLines--;
                        sb.append("\n").append(lines[i]);
                        if (findLines == 0) {
                            break;
                        }
                    }
                }
                return sb.toString();
            }
        }
        return null;
    }
}