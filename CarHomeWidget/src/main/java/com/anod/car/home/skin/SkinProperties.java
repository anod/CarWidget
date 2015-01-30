package com.anod.car.home.skin;


import com.anod.car.home.skin.icon.BackgroundProcessor;
import com.anod.car.home.skin.icon.IconProcessor;

public interface SkinProperties {
	int getInCarButtonExitRes();
	int getInCarButtonEnterRes();
	int getSetShortcutRes();
	int getLayout(int number);
	IconProcessor getIconProcessor();
    BackgroundProcessor getBackgroundProcessor();
	int getSetShortcutText();
	int getIconPaddingRes();
	int getSettingsButtonRes();
	int getRowLayout();

}
