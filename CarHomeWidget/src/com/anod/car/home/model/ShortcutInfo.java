package com.anod.car.home.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.anod.car.home.utils.UtilitiesBitmap;
import com.anod.car.home.utils.Utils;


/**
 * Represents a launchable icon on the widget
 */
public class ShortcutInfo implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;

    /**
     * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     */
    public int itemType;

    /**
     * The application name.
     */
    public CharSequence title;

    /**
     * The intent used to start the application.
     */
    transient public Intent intent;

    /**
     * Indicates whether the icon comes from an application's resource (if false)
     * or from a custom Bitmap (if true.)
     */
    private boolean customIcon;

	/**
     * Indicates whether we're using the default fallback icon instead of something from the
     * app.
     */
    private boolean usingFallbackIcon;

    /**
     * If isShortcut=true and customIcon=false, this contains a reference to the
     * shortcut icon as an application's resource.
     */
    transient private Intent.ShortcutIconResource iconResource;

    /**
     * The application icon.
     */
    transient private Bitmap mIcon;
    
    ShortcutInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
    }

    public ShortcutInfo(ShortcutInfo info) {
        id = info.id;
        itemType = info.itemType;

        title = info.title.toString();
        intent = new Intent(info.intent);
        if (info.iconResource != null) {
            iconResource = new Intent.ShortcutIconResource();
            iconResource.packageName = info.iconResource.packageName;
            iconResource.resourceName = info.iconResource.resourceName;
        }
        mIcon = info.mIcon; // TODO: should make a copy here.  maybe we don't need this ctor at all
        customIcon = info.customIcon;
    }

    public void setActivityIcon(Bitmap icon) {
    	customIcon = false;
    	iconResource = null;
    	usingFallbackIcon = false;
        mIcon = icon;
    }
    
    public void setFallbackIcon(Bitmap icon) {
        mIcon = icon;
        iconResource = null;
        usingFallbackIcon = true;
        customIcon = false;
    }

    public void setCustomIcon(Bitmap icon) {
    	customIcon = true;
    	iconResource = null;
    	usingFallbackIcon = false;
        mIcon = icon;
    }
    
    public void setIconResource(Bitmap icon,Intent.ShortcutIconResource res) {
    	mIcon = icon;
    	iconResource = res;
    	usingFallbackIcon = false;
    	customIcon = false;
    }
    
    public Bitmap getIcon() {
        return mIcon;
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    public final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
    }

    public void setCustomIcon(boolean customIcon) {
    	this.customIcon = customIcon;
    }
    
    public boolean isCustomIcon() {
		return customIcon;
	}    

    public boolean isUsingFallbackIcon() {
		return usingFallbackIcon;
	}

	public Intent.ShortcutIconResource getIconResource() {
		return iconResource;
	}

	@Override
    public String toString() {
        return "ShortcutInfo(title=" + title.toString() + ")";
    }

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		byte[] data = null;
		if (mIcon != null) {
			data = UtilitiesBitmap.flattenBitmap(mIcon);
		}
		if (data != null) {
			out.writeInt(data.length);
			out.write(data, 0, data.length);
		} else {
			out.writeInt(0);
		}
		
		if (intent != null) {
        	out.writeBoolean(true);
			out.writeUTF(intent.toUri(0));
		} else {
        	out.writeBoolean(false);
		}
		
        if (iconResource != null) {
        	out.writeBoolean(true);
        	out.writeUTF(iconResource.packageName);
        	out.writeUTF(iconResource.resourceName);
        } else {
        	out.writeBoolean(false);
        }
        
    }    

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		int length = in.readInt();
		if (length > 0) {
			byte[] dst = new byte[length];
			in.readFully(dst, 0, length);
			mIcon = getIconByteArray(dst);
		}
		
		boolean hasIntent = in.readBoolean();
		if (hasIntent) {
			String intentDescription = in.readUTF();
			if (intentDescription != null) {
		        try {
		        	intent = Intent.parseUri(intentDescription, 0);
		        } catch (URISyntaxException e) {
		        	Utils.logd(e.getMessage());
		        }
	        }
		}
		
		boolean hasIconResource = in.readBoolean();
		if (hasIconResource) {
			String packageName = in.readUTF();
			String resourceName = in.readUTF();
			iconResource = new Intent.ShortcutIconResource();
			iconResource.packageName = packageName;
			iconResource.resourceName = resourceName;
		}
  	}
	
    private Bitmap getIconByteArray(byte[] data) {
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
    }	
}