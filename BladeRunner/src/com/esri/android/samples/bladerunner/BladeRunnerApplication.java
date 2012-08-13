/* Copyright 2012 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 */

package com.esri.android.samples.bladerunner;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

/**
 */
public class BladeRunnerApplication extends Application {
	public static final String TAG = BladeRunnerApplication.class.getSimpleName();

	protected static String MAP_STATE = "map_state";
	protected static String MAP_STATE_SAVED = "map_state_saved";
	
	private static String APP_PREFERENCES = "bladerunner_preferences";
	protected static String MOCKUP_PROVIDER = "bladerunner_provider";
	
	protected static String OFFLINE_FILE_EXTENSION = ".json";
	protected static final String BASEMAP_PREFIX = "file:";

	protected static final int REQUEST_CONNECT_BLUETOOTH_DEVICE = 1;
	protected static final int REQUEST_ENABLE_BLUETOOTH = 2;
	
	public static final int BLUETOOTH_MESSAGE_STATE_CHANGE = 1;
	public static final int BLUETOOTH_MESSAGE_READ = 2;
	public static final int BLUETOOTH_MESSAGE_WRITE = 3;
	public static final int BLUETOOTH_MESSAGE_DEVICE_NAME = 4;
	public static final int BLUETOOTH_MESSAGE_TOAST = 5;
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	public static final String BLUETOOTH_DEVICE_NAME = "device_name";
	public static final String BLUETOOTH_TOAST = "toast";
    
	protected static String LAYERS_VISIBILITY = "layers_visibility";
	
	public static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public static int NEAREST_TOLERANCE = 15;
	protected static int LOCATION_RADIUS_TOLERANCE2 = 40;
	protected static int ENLARGE_ZOOM = 1000;
	protected static int DEFAULT_ACCURACY = 10;
	
	public File sdCard;
	private File demoDataFile;
	public File internalDataFile;
	private String state = Environment.getExternalStorageState();
	private boolean externalStorageAvailable = false;
	private boolean externalStorageWriteable = false;
	
	private static String dataSDCardDirRootName;
	private static String offlineDataSDCardDirName;
	
	protected static String basemapName;
	private static String basemapDataFrameName;
	protected static String basemapPath;
	
	public static String windTurbineName;
	private static String windTurbineUrl; 
	protected static String windTurbineLayerDefinition;
	public static String windTurbineIDField;
	public static String windTurbineTypeField;
	
	protected static String teamAreasName;
	protected static String teamAreasLabelName;
	private static String teamAreasUrl; 
	protected static String teamAreasLayerDefinition;
	protected static String teamAreasIDField;
	protected static String teamAreasNameField;
	protected static String teamAreasLabelField;
	
	protected static String windFarmZonesName;
	private static String windFarmZonesUrl; 
	protected static String windFarmZonesLayerDefinition;
	
	protected static String blocksName;
	private static String blocksUrl;
	protected static String blocksLayerDefinition;
	
	protected static String laserRangeFinderName;
	protected static String foundName;
	
	private ConnectivityManager connectivityManager;

	@Override
  public void onCreate() {
		super.onCreate();
		
		dataSDCardDirRootName = this.getResources().getString(R.string.config_data_sdcard_dir);
		offlineDataSDCardDirName = this.getResources().getString(R.string.config_data_sdcard_offline_dir);
		
		basemapName = this.getResources().getString(R.string.config_basemap_name);
		basemapDataFrameName = this.getResources().getString(R.string.config_basemap_dataframe_name);
		
		windTurbineName = this.getResources().getString(R.string.config_windturbine_name);
		windTurbineUrl = this.getResources().getString(R.string.config_windturbine_url);
		windTurbineLayerDefinition = this.getResources().getString(R.string.config_windturbine_layer_definition);
		windTurbineIDField = this.getResources().getString(R.string.config_windturbine_id_field);
		windTurbineTypeField = this.getResources().getString(R.string.config_windturbine_turbinetype_field);
		
		teamAreasName = this.getResources().getString(R.string.config_teamareas_name);
		teamAreasUrl = this.getResources().getString(R.string.config_teamareas_url);
		teamAreasLayerDefinition = this.getResources().getString(R.string.config_teamareas_layer_definition);
		teamAreasIDField = this.getResources().getString(R.string.config_teamareas_id_field);
		teamAreasNameField = this.getResources().getString(R.string.config_teamareas_name_field);
		teamAreasLabelName = this.getResources().getString(R.string.config_teamareaslabel_name);
		teamAreasLabelField = this.getResources().getString(R.string.config_teamareas_label_field);
			
		windFarmZonesName = this.getResources().getString(R.string.config_windfarmzones_name);
		windFarmZonesUrl = this.getResources().getString(R.string.config_windfarmzones_url);
		windFarmZonesLayerDefinition = this.getResources().getString(R.string.config_windfarmzones_layer_definition);
		
		blocksName = this.getResources().getString(R.string.config_blocks_name);
		blocksUrl = this.getResources().getString(R.string.config_blocks_url);
		blocksLayerDefinition = this.getResources().getString(R.string.config_blocks_layer_definition);
		
		laserRangeFinderName = this.getResources().getString(R.string.config_laserrangefinder_name);
		foundName = this.getResources().getString(R.string.config_found_name);
	}
	
	/**
	 * Method writeApplicationPreference.
	 * @param key String
	 * @param value Object
	 */
	void writeApplicationPreference(String key, Object value){
		SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        
        if (value instanceof String)
        	sharedPreferencesEditor.putString(key, value.toString());
        else if (value instanceof Boolean)
        	sharedPreferencesEditor.putBoolean(key, (Boolean) value);
        else if (value instanceof Integer)
        	sharedPreferencesEditor.putInt(key, (Integer) value);
        
        sharedPreferencesEditor.commit();
	}
	
	/**
	 * Method readBooleanApplicationPrefecence.
	 * @param key String
	 * @return boolean
	 */
	public boolean readBooleanApplicationPrefecence(String key) {
		SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_WORLD_READABLE);	
		return sharedPreferences.getBoolean(key, false);
	}
	
	/**
	 * Method readMapState.
	 * @return String
	 */
	public String readMapState(){
		SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_WORLD_READABLE);	
		return sharedPreferences.getString(MAP_STATE, null);
	}
	
    /**
     * Method isConnected.
     * @return boolean
     */
    public boolean isConnected() {
    	connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

    	boolean hasConnection = false;
    	NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					hasConnection = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					hasConnection = true;
			
		}
		
		return hasConnection;
    }

	/**
	 * Method getDataStorage.
	 * @param removableStorage boolean
	 */
	public void getDataStorage(boolean removableStorage) {
		StringBuilder sb = new StringBuilder();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    externalStorageAvailable = externalStorageWriteable = true;
		    if (removableStorage) {
			    sb.append("/");
			    sb.append(File.separator);
			    sb.append("Removable");
			    sdCard = new File(sb.toString());
			    
			    sb.append(File.separator);
			    sb.append("MicroSD");
			    sb.append(File.separator);
			    sb.append(dataSDCardDirRootName);
				demoDataFile = new File(sb.toString());
		    } else {
		    	sdCard = Environment.getExternalStorageDirectory();
		    	sb.append(sdCard.getAbsolutePath());
			    sb.append(File.separator);
			    sb.append(dataSDCardDirRootName);
				demoDataFile = new File(sb.toString());
		    }
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    externalStorageAvailable = true;
		    externalStorageWriteable = false;
		} else {
		    externalStorageAvailable = externalStorageWriteable = false;
		}
	}
	
	/**
	 * Method isExternalSDCardPresent.
	 * @return boolean
	 */
	public boolean isExternalSDCardPresent() {
		if (sdCard == null)
			return false;
		
		return sdCard.exists();
	}
	
	/**
	 * Method isExternalStorageAvailable.
	 * @return boolean
	 */
	public boolean isExternalStorageAvailable() {
		return externalStorageAvailable;
	}

	/**
	 * Method isExternalStorageWriteable.
	 * @return boolean
	 */
	public boolean isExternalStorageWriteable() {
		return externalStorageWriteable;
	}
	
	/**
	 * Method getStorageBaseMap.
	 * @return String
	 */
	public String getStorageBaseMap() {
		if (!externalStorageAvailable || demoDataFile == null)
			return null;
		
        File[] listFiles = demoDataFile.listFiles();
        
		int count = listFiles.length;
		
		if (count == 0)
			return null;
		
		StringBuilder sb = new StringBuilder();
        for (File file : listFiles) {
        	if (file.getName().equals(basemapName)) {
        		sb.append(BASEMAP_PREFIX);
        		sb.append(File.separator);
        		sb.append(File.separator);
        		sb.append(file.getAbsolutePath());
        		sb.append(File.separator);
        		sb.append(basemapDataFrameName);
        		break;
        	}
        }
        
		return basemapPath = sb.toString();
	}
	
	/**
	 * Method getJSONFile.
	 * @param fileName String
	 * @return File
	 */
	public File getJSONFile(String fileName) {
		if (!externalStorageAvailable || demoDataFile == null)
			return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append(demoDataFile.getAbsolutePath());
		sb.append(File.separator);
		sb.append(offlineDataSDCardDirName);
		File offlineFile = new File(sb.toString());
		
		File[] listFiles = offlineFile.listFiles();
		
		int count = listFiles.length;
		
		if (count == 0)
			return null;
		
		for (File file : listFiles) {
			if (file.isDirectory())
				continue;
			
			if (file.getName().equals(fileName + OFFLINE_FILE_EXTENSION)) {
				try {
					return file;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	/**
	 * Method getBasemapPath.
	 * @return String
	 */
	public static String getBasemapPath() {
		return basemapPath;
	}
	
	public void closeApp() {
		externalStorageAvailable = false;
		sdCard = null;
		demoDataFile = null;
	}
}
