package com.anod.car.home.prefs.action;

import com.anod.car.home.prefs.detection.ActivityRecognition;
import com.anod.car.home.prefs.detection.BluetoothDevice;
import com.anod.car.home.prefs.detection.CarDock;
import com.anod.car.home.prefs.detection.Detection;
import com.anod.car.home.prefs.detection.Headset;
import com.anod.car.home.prefs.detection.Power;
import com.anod.car.home.prefs.preferences.InCar;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alex
 * @date 1/15/14
 */
public class Collection {

	private Action[] mList = {
		new ScreenTimeout(),
		new Bluetooth(),
		new RouteToSpeaker(),
		new AutoAnswer(),
		new Volume(),
		new CarMode(),
		new SamsungHandsfree()
	};

	public Collection(InCar prefs) {
		init(prefs);
	}

	private void init(InCar prefs) {
		for(int i=0; i < mList.length; i++) {
			mList[i].setPrefs(prefs);
		}
	}

	public int getTotalCount() {
		return mList.length;
	}

	public Action[] getAll() {
		return mList;
	}

	public List<Action> getActive() {
		List<Action> list = new ArrayList<Action>(mList.length);
		for(int i=0; i < mList.length; i++) {
			if (mList[i].isActive()) {
				list.add(mList[i]);
			}
		}
		return list;
	}

}
