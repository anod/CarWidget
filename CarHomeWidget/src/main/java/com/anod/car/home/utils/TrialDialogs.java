package com.anod.car.home.utils;

import com.anod.car.home.R;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

public class TrialDialogs {

    public static Dialog buildProInstalledDialog(Context context) {
        AlertDialog.Builder builder = getBuilder(context);
        builder.setTitle(R.string.dialog_donate_title_install);
        builder.setMessage(R.string.dialog_donate_message_installed);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }


    public static Dialog buildTrialDialog(int trialsLeft, final Context context) {
        AlertDialog.Builder builder = getBuilder(context);
        int negativeRes = 0;
        if (trialsLeft > 0) {
            String activationsLeftText = context.getResources()
                    .getQuantityString(R.plurals.notif_activations_left, trialsLeft, trialsLeft);

            builder.setTitle(R.string.dialog_donate_title_trial);
            builder.setMessage(
                    activationsLeftText + context.getString(R.string.dialog_donate_message_trial));
            negativeRes = R.string.dialog_donate_btn_trial;
        } else {
            builder.setTitle(R.string.dialog_donate_title_expired);
            builder.setMessage(R.string.dialog_donate_message_expired);
            negativeRes = R.string.dialog_donate_btn_no;
        }
        setTrialDialogButtons(context, builder, negativeRes);
        return builder.create();
    }


    public static Dialog buildProOnlyDialog(final Context context) {
        AlertDialog.Builder builder = getBuilder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.dialog_donate_message_expired);
        setTrialDialogButtons(context, builder, R.string.dialog_donate_btn_no);
        return builder.create();
    }

    private static void setTrialDialogButtons(final Context context, AlertDialog.Builder builder,
            int negativeRes) {
        builder.setNeutralButton(R.string.dialog_donate_btn_yes, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = IntentUtils.createProVersionIntent();
                Utils.startActivitySafely(intent, context);
                dialog.dismiss();
            }

        });
        builder.setNegativeButton(negativeRes, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }


    private static AlertDialog.Builder getBuilder(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        return builder;
    }

}
