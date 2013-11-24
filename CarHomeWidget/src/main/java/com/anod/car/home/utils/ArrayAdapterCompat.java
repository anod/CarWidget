package com.anod.car.home.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

/**
 * @author alex
 * @date 11/24/13
 */
public class ArrayAdapterCompat<T> extends android.widget.ArrayAdapter<T> {

	public ArrayAdapterCompat(Context context, int resource) {
		super(context, resource);
	}


	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void addAll(T ... items) {
		//If the platform supports it, use addAll, otherwise add in loop
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			super.addAll(items);
		}else{
			for(T item: items){
				super.add(item);
			}
		}
	}
}
