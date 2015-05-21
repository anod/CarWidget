package com.anod.car.home.appscache;

import com.anod.car.home.app.AppsListActivity;
import com.anod.car.home.model.AppsList;

import android.content.Loader;
import android.os.Bundle;

import java.util.ArrayList;

public abstract class AppsCacheActivity extends AppsListActivity
        implements AppsCacheLoader.Callback {


    @Override
    public Loader<ArrayList<AppsList.Entry>> onCreateLoader(int id, Bundle args) {
        return new AppsCacheLoader(this, this, mAppsList);
    }


}