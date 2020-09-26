package androidx.core.app;

import android.app.job.JobParameters;
import android.app.job.JobServiceEngine;
import android.app.job.JobWorkItem;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import info.anodsplace.framework.AppLog;

/**
 * Catch SecurityException
 */
public abstract class SafeJobIntentService extends JobIntentService {

    @Override
    GenericWorkItem dequeueWork() {
        try {
            return super.dequeueWork();
        } catch (SecurityException e) {
            AppLog.Companion.e("SafeJobIntentService: SecurityException", e);
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // override mJobImpl with safe class to ignore SecurityException
        if (Build.VERSION.SDK_INT >= 26) {
            mJobImpl = new SafeJobServiceEngineImpl(this);
        } else {
            mJobImpl = null;
        }
    }

    @RequiresApi(26)
    static final class SafeJobServiceEngineImpl extends JobServiceEngine implements JobIntentService.CompatJobEngine {

        final JobIntentService mService;
        final Object mLock = new Object();
        JobParameters mParams;

        final class WrapperWorkItem implements JobIntentService.GenericWorkItem {
            final JobWorkItem mJobWork;

            WrapperWorkItem(JobWorkItem jobWork) {
                mJobWork = jobWork;
            }

            @Override
            public Intent getIntent() {
                return mJobWork.getIntent();
            }

            @Override
            public void complete() {
                synchronized (mLock) {
                    if (mParams != null) {
                        mParams.completeWork(mJobWork);
                    }
                }
            }
        }

        SafeJobServiceEngineImpl(JobIntentService service) {
            super(service);
            mService = service;
        }

        @Override
        public IBinder compatGetBinder() {
            return getBinder();
        }

        @Override
        public boolean onStartJob(JobParameters params) {
            AppLog.Companion.e("SafeJobServiceEngineImpl: onStartJob: " + params);
            mParams = params;
            // We can now start dequeuing work!
            mService.ensureProcessorRunningLocked(false);
            return true;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            AppLog.Companion.e("SafeJobServiceEngineImpl: onStopJob: " + params);
            boolean result = mService.doStopCurrentWork();
            synchronized (mLock) {
                // Once we return, the job is stopped, so its JobParameters are no
                // longer valid and we should not be doing anything with them.
                mParams = null;
            }
            return result;
        }

        /**
         * Dequeue some work.
         */
        @Override
        public JobIntentService.GenericWorkItem dequeueWork() {
            JobWorkItem work;
            synchronized (mLock) {
                if (mParams == null) {
                    return null;
                }
                work = mParams.dequeueWork();
            }
            if (work != null) {
                work.getIntent().setExtrasClassLoader(mService.getClassLoader());
                return new WrapperWorkItem(work);
            } else {
                return null;
            }
        }
    }
}