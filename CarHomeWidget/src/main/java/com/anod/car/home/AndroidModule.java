package com.anod.car.home;

import android.content.Context;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.content.Context.WINDOW_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
/**
 * @author alex
 * @date 2014-10-12
 */
@Module(
        injects = {
                CarWidgetApplication.class,
                TelephonyManager.class
        },
        library = true
)
public class AndroidModule {
    private final CarWidgetApplication app;

    public AndroidModule(CarWidgetApplication application) {
        this.app = application;
    }

    @Provides
    @Singleton
    @Application
    Context provideApplicationContext() {
        return app.getApplicationContext();
    }

    @Provides
    @Singleton
    CarWidgetApplication provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    WindowManager provideWindowManager() {
        return (WindowManager) app.getSystemService(WINDOW_SERVICE);
    }

    @Provides
    @Singleton
    TelephonyManager provideTelephonyManager() {
       return  (TelephonyManager) app.getSystemService(TELEPHONY_SERVICE);
    }

    @Provides
    @Singleton
    AudioManager provideAudioManager() {
        return (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
    }
    /**
     * Defines an qualifier annotation which can be used in conjunction with a type to identify dependencies within
     * this module's object graph.
     *
     * @see <a href="http://square.github.io/dagger/">the dagger documentation</a>
     */
    @Qualifier
    @Target({FIELD, PARAMETER, METHOD})
    @Documented
    @Retention(RUNTIME)
    public @interface Application {
    }
}
