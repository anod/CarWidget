package com.anod.car.home.incar;

import android.content.Context;
import android.media.AudioManager;
import android.view.WindowManager;

import com.anod.car.home.AndroidModule;
import com.anod.car.home.CarWidgetApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author alex
 * @date 2014-10-12
 */
@Module(injects = {
            ModeService.class,
            ModePhoneStateListener.class,
            ScreenOrientation.class,
            ModeDetector.class
        },
        addsTo = AndroidModule.class,
        library = true,
        complete=false
)
public class IncarModule {


    @Provides
    ScreenOrientation provideScreenOrientation(@AndroidModule.Application Context context, WindowManager wm) {
        return new ScreenOrientation(context, wm);
    }

    @Provides
    ModePhoneStateListener providesModePhoneStateListener(@AndroidModule.Application Context context, AudioManager am) {
        return new ModePhoneStateListener(context, am);
    }
}
