package com.anod.car.home.app;

import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.ObjectGraph;

import android.content.Context;

/**
 * @author alex
 * @date 2015-07-05
 */
public class App {

    public static CarWidgetApplication get(Context context) {
        return (CarWidgetApplication) context.getApplicationContext();
    }

    public static ObjectGraph provide(Context context) {
        return ((CarWidgetApplication) context.getApplicationContext()).getObjectGraph();
    }
}
