package com.anod.car.home.model;

import android.content.ContentResolver;
import android.content.Context;
import androidx.annotation.Nullable;

import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

import static com.squareup.picasso.Picasso.LoadedFrom.DISK;

/**
 * @author algavris
 * @date 23/08/2016.
 */

public class ShortcutIconRequestHandler extends RequestHandler {
    private final String mAuthority;
    private final ShortcutModel mModel;

    public ShortcutIconRequestHandler(Context context) {
        mAuthority = LauncherSettings.Favorites.getContentUri(context.getPackageName()).getAuthority();
        mModel = new ShortcutModel(context);
    }

    @Override
    public boolean canHandleRequest(Request data) {
        if (ContentResolver.SCHEME_CONTENT.equals(data.uri.getScheme()))
        {
            return data.uri.getAuthority().equals(mAuthority);
        }
        return false;
    }

    @Nullable
    @Override
    public Result load(Request request, int networkPolicy) throws IOException {

        ShortcutIcon icon = mModel.loadShortcutIcon(request.uri);
        return new Result(icon.bitmap, DISK);
    }

}
