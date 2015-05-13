// Copyright 2013 Google Inc. All Rights Reserved.

package com.anod.car.home.gms;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;

import java.util.concurrent.CountDownLatch;

/**
 * An AsyncTask that maintains a connected client.
 */
public abstract class ApiClientAsyncTask<P, I, R>
        extends AsyncTask<P, I, R> {

    private GoogleApiClient mClient;
    protected Context mContext;

    public ApiClientAsyncTask(Context context) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);
        mClient = builder.build();
		mContext = context;
    }

    @Override
    protected final R doInBackground(P... params) {
        Log.d("TAG", "in background");
        final CountDownLatch latch = new CountDownLatch(1);
        mClient.registerConnectionCallbacks(new ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int cause) {
            }

            @Override
            public void onConnected(Bundle arg0) {
                latch.countDown();
            }
        });
        mClient.registerConnectionFailedListener(new OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult arg0) {
                latch.countDown();
            }
        });
        mClient.connect();
        try {
            latch.await();
        } catch (InterruptedException e) {
            return null;
        }
        if (!mClient.isConnected()) {
            return null;
        }
        try {
            return doInBackgroundConnected(params);
        } finally {
            mClient.disconnect();
        }
    }

    /**
     * Override this method to perform a computation on a background thread, while the client is
     * connected.
     */
    protected abstract R doInBackgroundConnected(P... params);

    /**
     * Gets the GoogleApliClient owned by this async task.
     */
    protected GoogleApiClient getGoogleApiClient() {
        return mClient;
    }
}