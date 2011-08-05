package com.anod.car.home.prefs;

import com.anod.car.home.prefs.preferences.InCar;
import com.anod.car.home.prefs.preferences.Main;


public class Preferences {
	private Main main;
	private InCar incar;
	
	public Main getMain() {
		if (main == null) {
			this.main = new Main();
		}
		return main;
	}

	public InCar getIncar() {
		if (incar == null) {
			this.incar = new InCar();
		}
		return incar;
	}
}
