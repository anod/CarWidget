package com.anod.car.home;

import android.app.Application;
import android.content.Context;

import com.anod.car.home.model.AppsList;
import com.anod.car.home.prefs.preferences.AppTheme;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

public class CarWidgetApplication extends Application {
	public AppsList mAppListCache;
	public AppsList mIconThemesCache;
    private ObjectGraph mObjectGraph;

	private int mThemeIdx;

	public static CarWidgetApplication get(Context context) {
		return (CarWidgetApplication) context.getApplicationContext();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		//ACRA.init(this);

		mThemeIdx = AppTheme.getTheme(this);
        mObjectGraph = ObjectGraph.create(getModules().toArray());
    }

    /**
     * A list of modules to use for the application graph. Subclasses can override this method to
     * provide additional modules provided they call {@code super.getModules()}.
     */
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new AndroidModule(this));
    }

    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }


	public AppsList getAppListCache() {
		return mAppListCache;
	}


	public int getThemeIdx() {
		return mThemeIdx;
	}

	public int setThemeIdx(int theme) {
		return mThemeIdx = theme;
	}

	public void initAppListCache() {
		if (mAppListCache == null) {
			mAppListCache = new AppsList(this);
		}
	}

	public AppsList getIconThemesCache() {
		return mIconThemesCache;
	}

	public void initIconThemesCache() {
		if (mIconThemesCache == null) {
			mIconThemesCache = new AppsList(this);
		}
	}

}
