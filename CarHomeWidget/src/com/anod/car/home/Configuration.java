package com.anod.car.home;

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

import com.anod.car.home.incar.Bluetooth;
import com.anod.car.home.incar.BluetoothClassHelper;

public class Configuration extends PreferenceActivity {
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private static final int REQUEST_PICK_SHORTCUT = 2;
	private static final int REQUEST_PICK_APPLICATION = 3;
	private static final int REQUEST_CREATE_SHORTCUT=4;
	private static final int REQUEST_EDIT_SHORTCUT=5;
	
	public static final String EXTRA_CELL_ID = "CarHomeWidgetCellId";
	public static final int INVALID_CELL_ID=-1;

    private LauncherModel mModel;
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

    public static final String VERSION = "version";
    public static final String ISSUE_TRACKER = "issue-tracker";
    public static final String OTHER = "other";
    
	public static final boolean BUILD_AMAZON = false;   
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
        mModel = new LauncherModel();
        mContext = (Context)this;

       	initActivityChooser();
       	initButtonSkin();
       	initBackground();
       	initIcon();
       	initFont();
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
			Preferences.INCAR_MODE_ENABLED,
			Preferences.POWER_BT_DISABLE,
			Preferences.POWER_BT_ENABLE,
			Preferences.HEADSET_REQUIRED,
			Preferences.POWER_REQUIRED,
			SCREEN_BT_DEVICE
		};
		for(String prefName : prefNames) {
	    	final Preference pref = (Preference)findPreference(prefName);
	    	pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (preference instanceof CheckBoxPreference) {
						((CheckBoxPreference) preference).setChecked(false);
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
    
    private void initButtonSkin() {  	
    	final Preference btnColor = (Preference)findPreference(Preferences.BUTTON_COLOR);
    	btnColor.setKey(Preferences.getName(Preferences.BUTTON_COLOR, mAppWidgetId));
    	btnColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				int btnColorValue = Preferences.getTileColor(mContext, mAppWidgetId);
				int value = (btnColorValue != Preferences.COLOR_UNDEFINED) ? btnColorValue : getResources().getColor(R.color.w7_tale_default_background);
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = Preferences.getName(Preferences.BUTTON_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog)dialog).getColor();
						Preferences.saveColor(mContext, prefName, color);
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.show();
				return false;

			}
    	});
    	
	   	ListPreference skin =  (ListPreference)findPreference(Preferences.SKIN);
	   	skin.setKey(Preferences.getName(Preferences.SKIN, mAppWidgetId));
		String skinValue = Preferences.getSkin(this, mAppWidgetId);
		skin.setValue(skinValue);
		if (skinValue.equals(Preferences.SKIN_WINDOWS7)) {
			btnColor.setEnabled(true);
		} else {
			btnColor.setEnabled(false);
		}
		
		skin.setOnPreferenceChangeListener(
			new Preference.OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String skinName = (String)newValue;
					if (skinName.equals(Preferences.SKIN_WINDOWS7)) {
						btnColor.setEnabled(true);
					} else {
						btnColor.setEnabled(false);
					}
					return true;
				}
			}	
		);
    }

    private void initBackground() { 	
    	Preference bgColor = (Preference)findPreference(Preferences.BG_COLOR);
    	bgColor.setKey(Preferences.getName(Preferences.BG_COLOR, mAppWidgetId));

    	bgColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
		    	int bgColorValue = Preferences.getBackgroundColor(mContext, mAppWidgetId);
		    	int value = (bgColorValue != Preferences.COLOR_UNDEFINED) ? bgColorValue : getResources().getColor(R.color.default_background);
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = Preferences.getName(Preferences.BG_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog)dialog).getColor();
						Preferences.saveColor(mContext, prefName, color);
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.show();
				return false;

			}
    	});

    }
    
    private void initIcon() {  	
    	CheckBoxPreference icnMono = (CheckBoxPreference)findPreference(Preferences.ICONS_MONO);
    	String key = Preferences.getName(Preferences.ICONS_MONO, mAppWidgetId);
    	icnMono.setKey(key);
    	icnMono.setChecked(Preferences.isIconsMono(this, mAppWidgetId));

    	Preference icnColor = (Preference)findPreference(Preferences.ICONS_COLOR);
    	icnColor.setKey(Preferences.getName(Preferences.ICONS_COLOR, mAppWidgetId));
    	icnColor.setDependency(key);
       	icnColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
		    	int icnColorValue = Preferences.getIconsColor(mContext, mAppWidgetId);
		       	int value = (icnColorValue != Preferences.COLOR_UNDEFINED) ? icnColorValue : Color.WHITE;
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = Preferences.getName(Preferences.ICONS_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog)dialog).getColor();
						Preferences.saveColor(mContext, prefName, color);
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.show();
				return false;

			}
    	});
       	

    }

    private void initFont() {
    	SeekBarPreference sbPref = (SeekBarPreference)findPreference(Preferences.FONT_SIZE);
       	sbPref.setKey(Preferences.getName(Preferences.FONT_SIZE, mAppWidgetId));
       	int fontSize = Preferences.getFontSize(this, mAppWidgetId);
       	if (fontSize != Preferences.FONT_SIZE_UNDEFINED) {
       		sbPref.setValue(fontSize);	
       	} else {
       		float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
       		float size = 18 * scaledDensity;
       		sbPref.setValue((int)size);
       	}
       	
    	Preference fontColor = (Preference)findPreference(Preferences.FONT_COLOR);
    	fontColor.setKey(Preferences.getName(Preferences.FONT_COLOR, mAppWidgetId));
    	fontColor.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
		    	int icnColorValue = Preferences.getFontColor(mContext, mAppWidgetId);
		       	int value = (icnColorValue != Preferences.COLOR_UNDEFINED) ? icnColorValue : getResources().getColor(R.color.default_font_color);
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String prefName = Preferences.getName(Preferences.FONT_COLOR, mAppWidgetId);
						int color = ((CarHomeColorPickerDialog)dialog).getColor();
						Preferences.saveColor(mContext, prefName, color);
					}
				};
				final CarHomeColorPickerDialog d = new CarHomeColorPickerDialog(mContext, value, listener);
				d.show();
				return false;

			}
    	});
    }
    
    private void initActivityChooser() {
    	ArrayList<Long> currentShortcutIds = Preferences.getLauncherComponents(this, mAppWidgetId);
        for (int i=0; i<Preferences.LAUNCH_COMPONENT_NUMBER;i++) {
        	initLauncher(i,currentShortcutIds.get(i));
        }
    }
    
    private void initLauncher(final int launchComponentId, long shortcutId) {
    	String key = Preferences.getLaunchComponentKey(launchComponentId);
    	IconPreference p = (IconPreference)findPreference(key);
    	if (shortcutId != ShortcutInfo.NO_ID) {
    		ShortcutInfo info = mModel.loadShortcut(this,shortcutId);
    		if (info != null) {
    			setShortcutPreference(p,info);
    		}
    	}
    	p.setKey(Preferences.getLaunchComponentName(launchComponentId, mAppWidgetId));
    	p.setOnPreferenceClickListener( new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				pickShortcut(launchComponentId);
				return true;
			}
    		
    	});
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
				Uri uri = Uri.parse("market://details?id="+getPackageName());
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
				Uri uri = Uri.parse("https://bitbucket.org/anod/carhomewidget/issues");
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
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id=com.anod.calendar"));
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
    
    private void setShortcutPreference(IconPreference preference, ShortcutInfo info) {
    	preference.setIconBitmap(info.getIcon());
        preference.setTitle(info.title);
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
        shortcutNames.add("Applications");
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

    void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = "Applications";
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        mCurrentCellId = INVALID_CELL_ID;
        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            pickIntent.putExtra(Intent.EXTRA_TITLE, "");
            startActivityForResultSafely(pickIntent, REQUEST_PICK_APPLICATION);
        } else {
            mCurrentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID);
            startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
        }
    }
   
    private void completeAddShortcut(Intent data, boolean isApplicationShortcut) {
		if (mCurrentCellId == INVALID_CELL_ID) {
			return;
		}

    	final ShortcutInfo info = mModel.addShortcut(this, data, mCurrentCellId, mAppWidgetId, isApplicationShortcut);

    	if (info != null && info.id != ShortcutInfo.NO_ID) {   	
    		Preferences.saveShortcut(this,info.id,mCurrentCellId,mAppWidgetId);

    		String key = Preferences.getLaunchComponentName(mCurrentCellId, mAppWidgetId);
			IconPreference p = (IconPreference)findPreference(key);
	    	setShortcutPreference(p,info);
	    	
            Intent editIntent = new Intent(this, ShortcutEditActivity.class);
            editIntent.putExtra(ShortcutEditActivity.EXTRA_SHORTCUT_ID, info.id);
            editIntent.putExtra(ShortcutEditActivity.EXTRA_CELL_ID, mCurrentCellId);            
            startActivityForResultSafely(editIntent, REQUEST_CREATE_SHORTCUT);
    	}
        mCurrentCellId = INVALID_CELL_ID;
    	try {
    		dismissDialog(DIALOG_WAIT);
    	} catch (IllegalArgumentException e) {
    		
    	}
    }
    
    private void completeEditShortcut(Intent data) {
    	int cellId = data.getIntExtra(ShortcutEditActivity.EXTRA_CELL_ID, INVALID_CELL_ID);
    	long shortcutId = data.getLongExtra(ShortcutEditActivity.EXTRA_CELL_ID, ShortcutInfo.NO_ID);
    	if (cellId != INVALID_CELL_ID && shortcutId != ShortcutInfo.NO_ID) {
    		ShortcutInfo info = mModel.loadShortcut(this, shortcutId);
    		String key = Preferences.getLaunchComponentName(mCurrentCellId, mAppWidgetId);
    		IconPreference p = (IconPreference)findPreference(key);
    		setShortcutPreference(p,info);
    	}
    }
    
    void startActivityForResultSafely(Intent intent, int requestCode) {
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
            	HashMap<String,String> devices=Preferences.getBtDevices(mContext);
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
					HashMap<String,String> devices=Preferences.getBtDevices(mContext);
					String address = preference.getKey();
					Boolean checked = (Boolean)newValue;
					
					if (checked) {
						if (devices == null) {
							devices = new HashMap<String,String>();
						}
						devices.put(address, address);
						Preferences.saveBtDevices(mContext, devices);
						((CheckBoxPreference)preference).setChecked(true);
					} else {
						if (devices == null) {
							return true;
						}
						devices.remove(address);
						Preferences.saveBtDevices(mContext, devices);
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
