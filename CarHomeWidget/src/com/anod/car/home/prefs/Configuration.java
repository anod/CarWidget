package com.anod.car.home.prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.anod.car.home.AllAppsListCache;
import com.anod.car.home.CarWidgetApplication;
import com.anod.car.home.Provider;
import com.anod.car.home.R;
import com.anod.car.home.incar.Bluetooth;
import com.anod.car.home.incar.BluetoothClassHelper;
import com.anod.car.home.model.Launcher;
import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.prefs.views.CarHomeColorPickerDialog;
import com.anod.car.home.prefs.views.LauncherItemPreference;
import com.anod.car.home.prefs.views.SeekBarPreference;

public class Configuration extends PreferenceActivity {
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private static final int REQUEST_PICK_SHORTCUT = 2;
	private static final int REQUEST_PICK_APPLICATION = 3;
	private static final int REQUEST_CREATE_SHORTCUT=4;
	private static final int REQUEST_EDIT_SHORTCUT=5;
	
	public static final String EXTRA_CELL_ID = "CarHomeWidgetCellId";
	public static final int INVALID_CELL_ID=-1;

    private ShortcutModel mModel;
    private Context mContext;
    
    private static final IntentFilter INTENT_FILTER = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    private PreferenceCategory mBluetoothDevicesCategory;
    private BroadcastReceiver mBluetoothReceiver;
    
	private static final int DIALOG_WAIT=1;
	private static final int DIALOG_DONATE=2;
	private static final int DIALOG_INIT=3;

	private int mCurrentCellId = INVALID_CELL_ID; 
	private boolean mFreeVersion = false; 
   
    public static final String SCREEN_BT_DEVICE = "bt-device-screen";
    public static final String CATEGORY_BT_DEVICE = "bt-device-category";
    public static final String PREF_BT_SWITCH = "bt-switch";
    public static final String CATEGORY_TRANSPARENT = "transparent-category";
    
    public static final String VERSION = "version";
    public static final String ISSUE_TRACKER = "issue-tracker";
    public static final String OTHER = "other";
    
	public static final boolean BUILD_AMAZON = false;
	private static final String PRO_PACKAGE_NAME="com.anod.car.home.pro";

    private static final String DETAIL_MARKET_URL="market://details?id=%s";
    private static final String DETAIL_AMAZON_URL="http://www.amazon.com/gp/mas/dl/android?p=%s";
    private static final String OTHER_MARKET_URL="market://search?q=pub:\"Alex Gavrishev\"";
    private static final String OTHER_AMAZON_URL="http://www.amazon.com/gp/mas/dl/android?p=com.anod.car.home.free&showAll=1";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	addPreferencesFromResource(R.xml.preferences);
    	
    	Intent launchIntent = getIntent();
        Bundle extras = launchIntent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
       
            Intent defaultResultValue = new Intent();
            defaultResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, defaultResultValue);
        } else {
            finish();
        }
        mFreeVersion = Launcher.isFreeVersion(this.getPackageName());
        mContext = (Context)this;
        mModel = new ShortcutModel(mContext);
        
        Preferences.Main prefs = PreferencesStorage.loadMain(this, mAppWidgetId);
        
       	initActivityChooser(prefs);
       	initButtonSkin(prefs);
       	initBackground(prefs);
       	initIcon(prefs);
       	initFont(prefs);
       	initTransparent(mFreeVersion,prefs);
       	if (mFreeVersion) {
       		initInCarFreeDialog();
       	} else {
       		initInCar();
       	}
       	initOther();
       	
       	int cellId = extras.getInt(EXTRA_CELL_ID,INVALID_CELL_ID);
       	if (cellId!=INVALID_CELL_ID) {
       		pickShortcut(cellId);
       	}
       	
    }
    
    private void initTransparent(boolean isFree, final Preferences.Main prefs) {
    	CheckBoxPreference setTrans = (CheckBoxPreference)findPreference(PreferencesStorage.TRANSPARENT_BTN_SETTINGS);
    	String key = PreferencesStorage.getName(PreferencesStorage.TRANSPARENT_BTN_SETTINGS, mAppWidgetId);
    	setTrans.setKey(key);
    	setTrans.setChecked(prefs.isSettingsTransparent());
    	
    	CheckBoxPreference incarTrans = (CheckBoxPreference)findPreference(PreferencesStorage.TRANSPARENT_BTN_INCAR);

    	if (isFree) {
        	PreferenceCategory transCat = (PreferenceCategory)findPreference(CATEGORY_TRANSPARENT);
        	transCat.removePreference(incarTrans);
    	} else {
        	key = PreferencesStorage.getName(PreferencesStorage.TRANSPARENT_BTN_INCAR, mAppWidgetId);
        	incarTrans.setKey(key);
        	incarTrans.setChecked(prefs.isIncarTransparent());
    	}
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	if (mBluetoothReceiver != null) {
    		unregisterReceiver(mBluetoothReceiver);
    	}
    }

    @Override
    protected void onResume()
    {
    	super.onResume();
    	if (mBluetoothReceiver != null) {
	    	registerReceiver(mBluetoothReceiver, INTENT_FILTER);
    	}
    }
    
    private void initInCarFreeDialog() {
		String[] prefNames = {
			PreferencesStorage.INCAR_MODE_ENABLED,
			PreferencesStorage.POWER_BT_DISABLE,
			PreferencesStorage.POWER_BT_ENABLE,
			PreferencesStorage.HEADSET_REQUIRED,
			PreferencesStorage.POWER_REQUIRED,
			PreferencesStorage.SCREEN_TIMEOUT,
			PreferencesStorage.BRIGHTNESS,
			PreferencesStorage.BLUETOOTH,
			PreferencesStorage.ADJUST_VOLUME_LEVEL,
			PreferencesStorage.VOLUME_LEVEL,
			PreferencesStorage.ADJUST_WIFI,
			PreferencesStorage.AUTO_SPEAKER
		};
		final PreferenceScreen prefScr = (PreferenceScreen)findPreference(SCREEN_BT_DEVICE);
		prefScr.setEnabled(false);
		for(String prefName : prefNames) {
	    	final Preference pref = (Preference)findPreference(prefName);
	    	pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (preference instanceof CheckBoxPreference) {
						((CheckBoxPreference) preference).setChecked(false);
					} else if (preference instanceof ListPreference) {
						((ListPreference) preference).getDialog().hide();
					}
					showDialog(DIALOG_DONATE);
					return true;
				}
	    	});
		}
    }
    
    private void initInCar() {
    	initBluetooth();
    }
    
    private void initBluetooth() {
    	CheckBoxPreference btSwitch = (CheckBoxPreference)findPreference(PREF_BT_SWITCH);
    	btSwitch.setChecked(Bluetooth.getState() == BluetoothAdapter.STATE_ON);
    	btSwitch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean switchOn = (Boolean)newValue;
				showDialog(DIALOG_WAIT);
				mBluetoothReceiver = new BluetoothStateReceiver();
				registerReceiver(mBluetoothReceiver, INTENT_FILTER);
				if (Bluetooth.getState() == BluetoothAdapter.STATE_ON) {
					Bluetooth.switchOff();
					return (switchOn == false);
				} else {
					Bluetooth.switchOn();
					return (switchOn == true);
				}
			}
		});
    	mBluetoothDevicesCategory = (PreferenceCategory)findPreference(CATEGORY_BT_DEVICE);
    	PreferenceScreen bluetoothDevicesScreen = (PreferenceScreen)findPreference(SCREEN_BT_DEVICE);
     	bluetoothDevicesScreen.setOnPreferenceClickListener( new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new InitBluetoothDevicesTask().execute(0);
				return true;
			}
    		
    	});

    }
    
    private void initButtonSkin(final Preferences.Main prefs) {  	
    	final Preference btnColor = (Preference)findPreference(PreferencesStorage.BUTTON_COLOR);
    	btnColor.setKey(PreferencesStorage.getName(PreferencesStorage.BUTTON_COLOR, mAppWidgetId));
    	btnColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Integer value = prefs.getTileColor();
				if (value == null) {
					value = mContext.getResources().getColor(R.color.w7_tale_default_background);
				}
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = PreferencesStorage.getName(PreferencesStorage.BUTTON_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog)dialog).getColor();
						PreferencesStorage.saveColor(mContext, prefName, color);
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.setAlphaSliderVisible(true);
				d.show();
				return false;

			}
    	});
    	
	   	ListPreference skin =  (ListPreference)findPreference(PreferencesStorage.SKIN);
	   	skin.setKey(PreferencesStorage.getName(PreferencesStorage.SKIN, mAppWidgetId));
		String skinValue = prefs.getSkin();
		skin.setValue(skinValue);
		if (skinValue.equals(PreferencesStorage.SKIN_WINDOWS7)) {
			btnColor.setEnabled(true);
		} else {
			btnColor.setEnabled(false);
		}
		
		skin.setOnPreferenceChangeListener(
			new Preference.OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String skinName = (String)newValue;
					if (skinName.equals(PreferencesStorage.SKIN_WINDOWS7)) {
						btnColor.setEnabled(true);
					} else {
						btnColor.setEnabled(false);
					}
					return true;
				}
			}	
		);
    }

    private void initBackground(final Preferences.Main prefs) { 	
    	Preference bgColor = (Preference)findPreference(PreferencesStorage.BG_COLOR);
    	bgColor.setKey(PreferencesStorage.getName(PreferencesStorage.BG_COLOR, mAppWidgetId));

    	bgColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
		    	int value = prefs.getBackgroundColor();
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = PreferencesStorage.getName(PreferencesStorage.BG_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog)dialog).getColor();
						PreferencesStorage.saveColor(mContext, prefName, color);
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.setAlphaSliderVisible(true);
				d.show();
				return false;

			}
    	});

    }
    
    private void initIcon(final Preferences.Main prefs) {  	
    	CheckBoxPreference icnMono = (CheckBoxPreference)findPreference(PreferencesStorage.ICONS_MONO);
    	String key = PreferencesStorage.getName(PreferencesStorage.ICONS_MONO, mAppWidgetId);
    	icnMono.setKey(key);
    	icnMono.setChecked(prefs.isIconsMono());

    	Preference icnColor = (Preference)findPreference(PreferencesStorage.ICONS_COLOR);
    	icnColor.setKey(PreferencesStorage.getName(PreferencesStorage.ICONS_COLOR, mAppWidgetId));
    	icnColor.setDependency(key);
       	icnColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
		    	Integer icnTintColor = prefs.getIconsColor();
		    	int value = (icnTintColor != null) ? icnTintColor : Color.WHITE;
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = PreferencesStorage.getName(PreferencesStorage.ICONS_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog)dialog).getColor();
						PreferencesStorage.saveColor(mContext, prefName, color);
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.show();
				return false;

			}
    	});
       	
    	ListPreference icnScale = (ListPreference)findPreference(PreferencesStorage.ICONS_SCALE);
    	icnScale.setKey(PreferencesStorage.getName(PreferencesStorage.ICONS_SCALE, mAppWidgetId));
    	icnScale.setValue(prefs.getIconsScale());
    }

    private void initFont(final Preferences.Main prefs) {
    	SeekBarPreference sbPref = (SeekBarPreference)findPreference(PreferencesStorage.FONT_SIZE);
       	sbPref.setKey(PreferencesStorage.getName(PreferencesStorage.FONT_SIZE, mAppWidgetId));
       	int fontSize = prefs.getFontSize();
       	if (fontSize != PreferencesStorage.FONT_SIZE_UNDEFINED) {
       		sbPref.setValue(fontSize);	
       	} else {
       		float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
       		float size = 18 * scaledDensity;
       		sbPref.setValue((int)size);
       	}
       	
    	Preference fontColor = (Preference)findPreference(PreferencesStorage.FONT_COLOR);
    	fontColor.setKey(PreferencesStorage.getName(PreferencesStorage.FONT_COLOR, mAppWidgetId));
    	fontColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
		    	int value = prefs.getFontColor();
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = PreferencesStorage.getName(PreferencesStorage.FONT_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog)dialog).getColor();
						PreferencesStorage.saveColor(mContext, prefName, color);
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.setAlphaSliderVisible(true);
				d.show();
				return false;

			}
    	});
    }
    
    private void initActivityChooser(Preferences.Main prefs) {
    	ArrayList<Long> currentShortcutIds = prefs.getLauncherComponents();
    	mModel.init(currentShortcutIds);
        for (int i=0; i<PreferencesStorage.LAUNCH_COMPONENT_NUMBER;i++) {
        	initLauncherPreference(i);
        }
    }
    
    private void refreshPreference(LauncherItemPreference pref) {
		int cellId = pref.getCellId();
		ShortcutInfo info = mModel.getShortcut(cellId);
		if (info == null) {
			pref.setTitle(R.string.set_shortcut);
			pref.setIconResource(R.drawable.ic_add_shortcut);
			pref.showButtons(false);
		} else {
	    	pref.setIconBitmap(info.getIcon());
	    	pref.setTitle(info.title);
	        pref.showButtons(true);
		}    	
    }
    
    private void initLauncherPreference(int launchComponentId) {
    	String key = PreferencesStorage.getLaunchComponentKey(launchComponentId);
    	LauncherItemPreference p = (LauncherItemPreference)findPreference(key);  	
    	p.setKey(PreferencesStorage.getLaunchComponentName(launchComponentId, mAppWidgetId));
    	p.setCellId(launchComponentId);
    	p.setOnPreferenceClickListener( new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				LauncherItemPreference pref = (LauncherItemPreference)preference;
				int cellId = pref.getCellId();
				ShortcutInfo info = mModel.getShortcut(cellId);
				if (info == null) {
					pickShortcut(cellId);
				} else {
					startEditActivity(cellId, info.id);
				}
				return true;
			}
    	});
    	p.setOnDeleteClickListener( new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				LauncherItemPreference pref = (LauncherItemPreference)preference;
				mModel.dropShortcut(pref.getCellId(),mAppWidgetId);
				refreshPreference(pref);
				return true;
			}

		});
    	refreshPreference(p);
    }
    
    private void initOther() {
    	Preference version = (Preference)findPreference(VERSION);
    	String versionText = getResources().getString(R.string.version_title);
    	String appName = "";
    	String versionName = "";
		try {
			PackageManager pm = getPackageManager();
			appName = getApplicationInfo().loadLabel(pm).toString();
			versionName = pm.getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {	}
    	version.setTitle(String.format(versionText, appName, versionName));
    	version.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url = (BUILD_AMAZON) ? DETAIL_AMAZON_URL : DETAIL_MARKET_URL;
				Uri uri = Uri.parse(String.format(url,getPackageName()));
				Intent intent = new Intent (Intent.ACTION_VIEW, uri); 
				startActivity(intent);
				return false;
			}
    	});
    	
    	Preference other = (Preference)findPreference(OTHER);
    	other.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url = (BUILD_AMAZON) ? OTHER_AMAZON_URL : OTHER_MARKET_URL;
				Uri uri = Uri.parse(url);

				Intent intent = new Intent (Intent.ACTION_VIEW, uri); 
				startActivity(intent);
				return false;
			}
    	});
    	
    	Preference issue = (Preference)findPreference(ISSUE_TRACKER);
    	issue.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("http://www.facebook.com/pages/Car-Widget-for-Android/220355141336206");
				Intent intent = new Intent (Intent.ACTION_VIEW, uri); 
				startActivity(intent);
				return false;
			}
    	});
   	
    }
    
    @Override
    public Dialog onCreateDialog(int id) {
    	switch(id) {
	    	case DIALOG_INIT :
	    		ProgressDialog initDialog = new ProgressDialog(this);
	    		initDialog.setCancelable(true);
	    		initDialog.setMessage(getResources().getString(R.string.load_paired_device));
	    		initDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						finish();
					}
				});
	    		return initDialog;    	
	    	case DIALOG_WAIT :
	    		ProgressDialog waitDialog = new ProgressDialog(this);
	    		waitDialog.setCancelable(true);
	    		String message = getResources().getString(R.string.please_wait);
	    		waitDialog.setMessage(message);
	    		return waitDialog;
			case DIALOG_DONATE :
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.dialog_donate_title);
				builder.setMessage(R.string.dialog_donate_message);
				builder.setCancelable(true);
				builder.setPositiveButton(R.string.dialog_donate_btn_yes, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String url = (BUILD_AMAZON) ? DETAIL_AMAZON_URL : DETAIL_MARKET_URL;
						Uri uri = Uri.parse(String.format(url,PRO_PACKAGE_NAME));
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(uri);
						startActivity(intent);
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(R.string.dialog_donate_btn_no, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				return builder.create();
		}
    	return null;
    }
    
	@Override
	public void onBackPressed() {
		if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(getIntent().getAction())
		&& mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID		
		) {
	       	int[] appWidgetIds = new int[1];
	       	appWidgetIds[0] = mAppWidgetId; 
	        Provider appWidgetProvider = Provider.getInstance();
	        appWidgetProvider.performUpdate(this, appWidgetIds);
		}
		AllAppsListCache allAppsList = ((CarWidgetApplication)this.getApplicationContext()).getAllAppCache();
		allAppsList.flush();
		super.onBackPressed();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_APPLICATION:
                	completeAddShortcut(data,true);
                    break;
                case REQUEST_CREATE_SHORTCUT:
                	completeAddShortcut(data,false);
                	break;
                case REQUEST_EDIT_SHORTCUT:
                	completeEditShortcut(data);
                	break;
                case REQUEST_PICK_SHORTCUT:
                    processShortcut(data);
                    break;
            }
        } else {
        	try {
        		dismissDialog(DIALOG_WAIT);
        	} catch (IllegalArgumentException e) {
        		
        	}
        }
		super.onActivityResult(requestCode, resultCode, data);
		
	}
    
    private void pickShortcut(int cellId) {
        showDialog(DIALOG_WAIT);
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();

        shortcutNames.add(getResources().getString(R.string.applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        shortcutIcons.add(ShortcutIconResource.fromContext(this,
                        R.drawable.ic_launcher_application));
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

        Intent dataIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        dataIntent.putExtra(EXTRA_CELL_ID, cellId);
        
        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, dataIntent);
        pickIntent.putExtra(Intent.EXTRA_TITLE, "Select shortcut");
        pickIntent.putExtras(bundle);

        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
    }
    
    private void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        mCurrentCellId = INVALID_CELL_ID;
        if (applicationName != null && applicationName.equals(shortcutName)) {
        	Intent mainIntent = new Intent(Configuration.this, AllAppsActivity.class);
            mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
            startActivityForResultSafely(mainIntent, REQUEST_PICK_APPLICATION);
        } else {
            mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
            startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
        }
    }
   
    private void completeAddShortcut(Intent data, boolean isApplicationShortcut) {
		if (mCurrentCellId == INVALID_CELL_ID) {
			return;
		}

    	final ShortcutInfo info = mModel.putShortcut(mCurrentCellId, mAppWidgetId, data, isApplicationShortcut);

    	if (info != null && info.id != ShortcutInfo.NO_ID) {   	

    		String key = PreferencesStorage.getLaunchComponentName(mCurrentCellId, mAppWidgetId);
			LauncherItemPreference p = (LauncherItemPreference)findPreference(key);
	    	refreshPreference(p);
    	}
        mCurrentCellId = INVALID_CELL_ID;
    	try {
    		dismissDialog(DIALOG_WAIT);
    	} catch (IllegalArgumentException e) {
    	
    	}
    }
    
    private void startEditActivity(int cellId,long shortcutId) {
        Intent editIntent = new Intent(this, ShortcutEditActivity.class);
        editIntent.putExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, shortcutId);
        editIntent.putExtra(ShortcutEditActivity.EXTRA_CELL_ID, cellId);            
        startActivityForResultSafely(editIntent, REQUEST_EDIT_SHORTCUT);
    }
    
    private void completeEditShortcut(Intent data) {
    	int cellId = data.getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, INVALID_CELL_ID);
    	long shortcutId = data.getLongExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, ShortcutInfo.NO_ID);
    	if (cellId != INVALID_CELL_ID) {
    		String key = PreferencesStorage.getLaunchComponentName(cellId, mAppWidgetId);
    		LauncherItemPreference p = (LauncherItemPreference)findPreference(key);
    		mModel.reloadShortcut(cellId, shortcutId);
    		refreshPreference(p);
    	}
    }
    
    private void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Activity not found", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Activity not found", Toast.LENGTH_SHORT).show();
            Log.e("CarHomeWidget", "Widget does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    private class InitBluetoothDevicesTask extends AsyncTask<Integer, Integer, Boolean> { 
    	private ArrayList<CheckBoxPreference> mPairedList;
    	private BluetoothAdapter mBtAdapter;
    	
    	protected void onPreExecute() {
    		showDialog(DIALOG_INIT);
    		mBluetoothDevicesCategory.removeAll();
            // Get the local Bluetooth adapter
        	mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    	}

    	protected void onPostExecute(Boolean result) {
    		if (result == true) {
    			for (int i = 0; i<mPairedList.size(); i++) { 
    				mBluetoothDevicesCategory.addPreference(mPairedList.get(i));
    			}
    		} else {
    			Preference emptyPref = new Preference(mContext); 
    			Resources r = mContext.getResources();
    			emptyPref.setTitle(r.getString(R.string.no_paired_devices_found_title));
    			emptyPref.setSummary(r.getString(R.string.no_paired_devices_found_summary));    			
    			mBluetoothDevicesCategory.addPreference(emptyPref);    			    			
    		}
    		mPairedList = null;
    		try {
    			dismissDialog(DIALOG_INIT);
    		}  catch (IllegalArgumentException e) {}
        }
        public void onProgressUpdate(Integer... values) {

        }
    	@Override
    	protected Boolean doInBackground(Integer... arg0) {
    		return loadPairedDevices();
    	}
    	
        private Boolean loadPairedDevices()  {

            // Get a set of currently paired devices
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {
            	HashMap<String,String> devices=PreferencesStorage.getBtDevices(mContext);
            	mPairedList = new ArrayList<CheckBoxPreference>(pairedDevices.size());
                for (BluetoothDevice device : pairedDevices) {
                	boolean checked = (devices == null) ? false : devices.containsKey(device.getAddress());
                	CheckBoxPreference pref = createPref(device, checked);
                	mPairedList.add(pref);
                }
                return true;
            }
            return false;
        }
        
        private CheckBoxPreference createPref(BluetoothDevice device, boolean checked) {
        	CheckBoxPreference pref = new CheckBoxPreference(mContext);
        	pref.setPersistent(false);
        	pref.setChecked(checked);
        	pref.setDefaultValue(checked);
        	pref.setKey(device.getAddress());
	        pref.setTitle(device.getName());
	        BluetoothClass btClass = device.getBluetoothClass();
	        int res = BluetoothClassHelper.getBtClassString(btClass);
	        if (res > 0) {
	        	String title = mContext.getResources().getString(res);
	        	pref.setSummary(title);
	        }
	        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {	
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					HashMap<String,String> devices=PreferencesStorage.getBtDevices(mContext);
					String address = preference.getKey();
					Boolean checked = (Boolean)newValue;
					
					if (checked) {
						if (devices == null) {
							devices = new HashMap<String,String>();
						}
						devices.put(address, address);
						PreferencesStorage.saveBtDevices(mContext, devices);
						((CheckBoxPreference)preference).setChecked(true);
					} else {
						if (devices == null) {
							return true;
						}
						devices.remove(address);
						PreferencesStorage.saveBtDevices(mContext, devices);
						((CheckBoxPreference)preference).setChecked(false);
					}
					return true;
				}
			});
	        return pref;
 	
        }
    }
    
    class BluetoothStateReceiver extends BroadcastReceiver
    {
    	public void onReceive(Context paramContext, Intent paramIntent)
    	{
    		int state = paramIntent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
    		if (state == BluetoothAdapter.STATE_ON) {
        		try{
        			dismissDialog(DIALOG_WAIT);
        		}  catch (IllegalArgumentException e) {}
	    		unregisterReceiver(mBluetoothReceiver);
		    	mBluetoothReceiver = null;
    			new InitBluetoothDevicesTask().execute(0);
    		} else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR) {
        		try{
        			dismissDialog(DIALOG_WAIT);
        		}  catch (IllegalArgumentException e) {}
	    		unregisterReceiver(mBluetoothReceiver);
		    	mBluetoothReceiver = null;
    		}
    	}
    }
}
