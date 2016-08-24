package com.anod.car.home.acra;

import com.anod.car.home.R;

import org.acra.ACRA;
import org.acra.dialog.BaseCrashReportDialog;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author alex
 * @date 2015-05-28
 */
public class CrashDialog extends BaseCrashReportDialog implements DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener {

    private static final String STATE_COMMENT = "comment";
    @BindView(android.R.id.edit)
    EditText mUserCommentView;

    AlertDialog mDialog;

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        super.init(savedInstanceState);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(buildCustomView(savedInstanceState));
        dialogBuilder.setPositiveButton(R.string.crash_dialog_report_button, this);
        dialogBuilder.setNegativeButton(android.R.string.cancel, this);

        mDialog = dialogBuilder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    protected View buildCustomView(Bundle savedInstanceState) {

        View root = LayoutInflater.from(this).inflate(R.layout.dialog_crash_report, null, false);
        ButterKnife.bind(this, root);

        if (savedInstanceState != null) {
            String savedValue = savedInstanceState.getString(STATE_COMMENT);
            if (savedValue != null) {
                mUserCommentView.setText(savedValue);
            }
        }

        return root;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Retrieve user comment
            final String comment = mUserCommentView != null ? mUserCommentView.getText().toString() : "";

            // Store the user email
            final String userEmail;
            final SharedPreferences prefs = ACRA.getACRASharedPreferences();
            userEmail = prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, "");
            sendCrash(comment, userEmail);
        } else {
            cancelReports();
        }

        finish();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mUserCommentView != null && mUserCommentView.getText() != null) {
            outState.putString(STATE_COMMENT, mUserCommentView.getText().toString());
        }
    }
}
