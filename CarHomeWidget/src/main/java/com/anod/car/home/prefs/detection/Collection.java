package com.anod.car.home.prefs.detection;

import android.content.Context;

import com.anod.car.home.prefs.preferences.InCar;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alex
 * @date 1/15/14
 */
public class Collection {

	private Detection[] mList = {
		new BluetoothDevice(),
		new Headset(),
		new Power(),
		new ActivityRecognition(),
		new CarDock()
	};

	public Collection(Context context, InCar prefs) {
		init(context, prefs);
	}

	private void init(Context context, InCar prefs) {
		for(int i=0; i < mList.length; i++) {
			mList[i].setPrefs(prefs);
			mList[i].setContext(context);
		}
	}

	public int getTotalCount() {
		return mList.length;
	}

	public Detection[] getAll() {
		return mList;
	}

	public List<Detection> getActive() {
		List<Detection> list = new ArrayList<Detection>(mList.length);
		for(int i=0; i < mList.length; i++) {
			if (mList[i].isActive()) {
				list.add(mList[i]);
			}
		}
		return list;
	}

}
