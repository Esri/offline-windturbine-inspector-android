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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.event.OnPinchListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.samples.bladerunner.laserrangefinder.BluetoothSerialService;
import com.esri.android.samples.bladerunner.laserrangefinder.ByteQueue;
import com.esri.android.samples.bladerunner.maptools.Area;
import com.esri.android.samples.bladerunner.maptools.AreaSketchTool;
import com.esri.android.samples.bladerunner.maptools.LocationMapTool;
import com.esri.android.samples.bladerunner.maptools.OnMapToolListener;
import com.esri.core.geometry.AngularUnit;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.renderer.UniqueValue;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.symbol.TextSymbol.HorizontalAlignment;
import com.esri.core.symbol.TextSymbol.VerticalAlignment;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;

/**
 */
public class MapViewActivity extends Activity {
	static String TAG = MapViewActivity.class.getSimpleName();

	private static final int DIALOG_LAYERS_VISIBILITY = 1;
	public static final int DIALOG_TURBINETYPES = 2;
	public static final int DIALOG_BLUETOOTH_DEVICES = 3;
	public static final int DIALOG_ENABLED_BLUETOOTH = 4;

	public static String AREA_RECEIVER = "area_receiver";

	public static final String BLOCKS_FEATURESET = "blocks results";
	public static final String WIND_FARM_ZONES_FEATURESET = "wind farm zones results";
	public static final String TEAMAREAS_FEATURESET = "team areas results";
	public static final String WIND_TURBINE_FEATURESET = "wind turbines results";
	protected static final int DESERIALIZED_RESULT_SUCCESSFULL = 0;
	protected static final int DESERIALIZED_RESULT_UNSUCCESSFULL = 1;

	private static final String SENTENCE_DELIMITER = "\r\n";
	private static final String DATA_FIELD_DELIMITER = ",";
	private static final String ADDRESS_FIELD = "$PLTIT";
	private static final String SENTENCE_TYPE = "HV";
	private static final int ADDRESS_FIELD_INDEX = 0;
	private static final int SENTENCE_TYPE_INDEX = 1;
	private static final int HD_INDEX = 2;
	private static final int HDU_INDEX = 3;
	private static final int HA_INDEX = 4; // bearing
	private static final int VA_INDEX = 6;
	private static final int SD_INDEX = 8;

	public static final String TOAST = "toast";

	BladeRunnerApplication bladeRunnerApp;
	MapView map;
	private ArcGISLocalTiledLayer baseMapLayer;

	public ArcGISFeatureLayer blocksFeatureLayer;
	public ArcGISFeatureLayer windFarmZonesFeatureLayer;
	public ArcGISFeatureLayer teamareasFeatureLayer;
	public ArcGISFeatureLayer windTurbinesFeatureLayer;

	GraphicsLayer teamAreasLabelsGraphicsLayer;
	public GraphicsLayer foundGraphicsLayer;
	public LaserRangeGraphicsLayer laserRangerGraphicsLayer;
	Graphic[] windTurbineGraphics;

	FeatureSet teamAreasfeatureSet = new FeatureSet();
	private SavedAreasResultReceiver resultReceiver;

	public static MapViewTouchListener mapViewTouchListener;
	static volatile BluetoothSerialService bluetoothSerialService;

	LocationMapTool locationMapTool;
	AreaSketchTool areaSketchTool;

	Envelope initialExtent;
	ArrayList<Layer> visibleLayers;

	TextSymbol labelTextSymbol;

	Spinner teamSpinner;
	Spinner daySpinner;

	BluetoothAdapter bluetoothAdapter;

	ArrayAdapter<String> newDevicesAdapter;
	StringBuffer laserRangeData;
	ByteQueue byteQueue;
	String connectedDeviceName;

	ToggleButton gpsToggleButton;
	ToggleButton laserToggleButton;
	ToggleButton layersButton;
	ToggleButton resultsButton;
	TextView laserStatusTextView;
	ListView windTurbineListView;
	ViewGroup laserRangeLayout;
	Animation fadeinAnimation;
	Animation fadeoutAnimation;

	private Button zoomButton;
	private ToggleButton compassButton;

	ProgressBar progressBar;

	private LocationManager locationManager;
	private LocationListener locationListener;
	private LocationProvider locationProvider;
	boolean firstLocation = true;
	Point firstMapLocation;
	Location currentLocation;
	Point offsetLaserPoint;
	private PictureMarkerSymbol gpsMarkerSymbol;
	private int gpsID = -1;
	Helper helper;

	SensorManager sensorManager;
	Sensor orientationSensor;
	float[] sensorValues;

	private ArrayList<Graphic> teamAreasGraphics;
	int loadedCount;
	boolean loaded;

	PictureMarkerSymbol windTurbineSymbol;
	int selectedID = -1;

	Dialog dialog;

	/**
	 * Method onAttachedToWindow.
	 * 
	 * @see android.view.Window$Callback#onAttachedToWindow()
	 */
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
		window.addFlags(WindowManager.LayoutParams.FLAG_DITHER);
	}

	/**
	 * Method onCreate.
	 * 
	 * @param savedInstanceState
	 *            Bundle
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.mapview);
		helper = new Helper();
		bladeRunnerApp = (BladeRunnerApplication) getApplication();

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		map = (MapView) findViewById(R.id.map);
		map.setMapBackground(Color.WHITE, Color.LTGRAY, 50, 1);

		createLayers();

		// Comment out the following two lines of code
		// if you want to use the external Mini SD Card to plug in
		// the data. The following code looks for the data in your
		// internal SD Card
		addLayersToMap();
		loadDataFromSDCard(false);

		setDaysAreaUI();

		setTeamsAdapter();

		setDaysAdapter();

	}

	private void registerSDCardReceiver() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addDataScheme("file");
		registerReceiver(sdcardReceiver, filter);
	}

	private void registerBluetoothReceiver() {
		// Register for broadcasts when a device is discovered
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		initBluetoothSerialService();

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		filter.addAction(BluetoothDevice.EXTRA_BOND_STATE);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(bluetoothReceiver, filter);
	}

	private void registerSensor() {
		if (!((ToggleButton) findViewById(R.id.compassbutton)).isChecked())
			return;

		if (sensorManager == null || sensorListener == null)
			return;

		sensorManager.registerListener(sensorListener, orientationSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();

		registerBluetoothReceiver();

		registerSDCardReceiver();
	}

	@Override
	protected void onStop() {
		super.onStop();

		unregisterBluetooth();
		unregisterSDCardReceiver();
		unregisteredSensor();
	}

	@Override
	protected synchronized void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();

		if (map == null)
			return;

		String mapState = bladeRunnerApp.readMapState();

		if (mapState == null)
			return;

		map.restoreState(mapState);

		registerSensor();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();

		if (map == null || !map.isLoaded())
			return;

		if (bladeRunnerApp
				.readBooleanApplicationPrefecence(BladeRunnerApplication.MAP_STATE_SAVED))
			return;

		bladeRunnerApp.writeApplicationPreference(
				BladeRunnerApplication.MAP_STATE, map.retainState());
		bladeRunnerApp.writeApplicationPreference(
				BladeRunnerApplication.MAP_STATE_SAVED, true);
	}

	private void unregisteredSensor() {
		if (sensorManager == null || sensorListener == null)
			return;

		sensorManager.unregisterListener(sensorListener);
	}

	private void unregisterSDCardReceiver() {
		if (sdcardReceiver == null)
			return;

		unregisterReceiver(sdcardReceiver);
	}

	private void unregisterBluetooth() {
		if (bluetoothSerialService != null) {
			bluetoothSerialService.stop();
			bluetoothSerialService = null;
		}

		if (bluetoothAdapter != null) {
			bluetoothAdapter.cancelDiscovery();
			bluetoothAdapter = null;
		}

		if (bluetoothReceiver == null)
			return;

		unregisterReceiver(bluetoothReceiver);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed");
		super.onBackPressed();

		if (laserToggleButton != null && laserToggleButton.isChecked())
			laserToggleButton.performClick();

		if (bladeRunnerApp.isExternalSDCardPresent()) {
			map.removeAll();
			bladeRunnerApp.closeApp();
		}
	}

	private void createLayers() {
		createBaseMap();

		this.blocksFeatureLayer = helper.createBlocksFeatureLayer();

		this.windFarmZonesFeatureLayer = helper
				.createWindFarmZonesFeatureLayer();

		this.teamareasFeatureLayer = helper.createTeamAreasFeatureLayer();

		this.windTurbinesFeatureLayer = helper.createWindTurbinesFeatureLayer();

		createGraphicsLayers();

		map.setOnStatusChangedListener(new OnStatusChangedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {
				Log.d(TAG, "onStatusChanged --> " + source.getClass().getName()
						+ ", " + status);

				if (source == map && status == STATUS.INITIALIZED) {
					loadedCount++;
					setWidgets();
					setMapViewListener();
					setSensor();
					setDefaultMapExtent();
				} else if (source == windTurbinesFeatureLayer
						&& status == STATUS.LAYER_LOADED) {
					loadedCount++;
				} else if (source == foundGraphicsLayer
						&& status == STATUS.LAYER_LOADED)
					// gpsToggleButton.performClick();

					if (loadedCount == 2 && !loaded) {
						locationMapTool = new LocationMapTool(map);
						mapViewTouchListener
								.setLocationMapTool(locationMapTool);
						layersVisibility();
						loaded = true;
					}
			}
		});
	}

	private final BroadcastReceiver sdcardReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				Log.v(TAG, "ACTION_MEDIA_MOUNTED");
				addLayersToMap();
				loadDataFromSDCard(true);
			} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
					|| action.equals(Intent.ACTION_MEDIA_REMOVED)) {
				Log.v(TAG, "ACTION_MEDIA_UNMOUNTED || ACTION_MEDIA_REMOVED");
				map.removeAll();
			}
		}
	};

	void addLayersToMap() {

		map.addLayer(baseMapLayer);
		map.addLayer(blocksFeatureLayer);
		map.addLayer(windFarmZonesFeatureLayer);
		map.addLayer(teamareasFeatureLayer);
		map.addLayer(windTurbinesFeatureLayer);
		map.addLayer(teamAreasLabelsGraphicsLayer);
		map.addLayer(laserRangerGraphicsLayer);
		map.addLayer(foundGraphicsLayer);

	}

	/**
	 * Method loadDataFromSDCard. The geometries from the json files are read
	 * and added to the respective layers In order to create the json, do a
	 * query on the feature layer, (where 1=1, outputparams ="*", select output
	 * format as json)
	 * 
	 * Rendering information is stored in the layer definition in config.xml,
	 * this is used to render the geometries
	 * 
	 * Each json fetch is run in its own thread.
	 * 
	 * @param removableSDCard
	 *            boolean
	 */
	void loadDataFromSDCard(boolean removableSDCard) {
		bladeRunnerApp.getDataStorage(removableSDCard);
		reinitializeBaseMap();
		getBlocksFromJSON();
		getWindFarmZonesFromJSON();
		getWindTurbinesFromJSON();
		getTeamAreasFromJSON();
	}

	protected void reinitializeBaseMap() {
		String basemapFile = "file:///mnt/sdcard/demo/basemap/ImageryTPK.tpk";

		if (basemapFile == null)
			return;

		baseMapLayer.reinitializeLayer(basemapFile);
	}

	/**
	 * Method startWithMockupPoint.
	 * 
	 * @param x
	 *            double
	 * @param y
	 *            double
	 */
	void startWithMockupPoint(double x, double y) {
		String provider = BladeRunnerApplication.MOCKUP_PROVIDER;

		setLocationProvider(provider);

		currentLocation = new Location(provider);
		currentLocation.setLatitude(x);
		currentLocation.setLongitude(y);

		locationManager.setTestProviderLocation(provider, currentLocation);

		Point mapPoint = Utility.fromWgs84ToMap(currentLocation,
				map.getSpatialReference());

		if (gpsID == -1)
			gpsID = foundGraphicsLayer.addGraphic(new Graphic(mapPoint,
					gpsMarkerSymbol));
		else
			foundGraphicsLayer.updateGraphic(gpsID, mapPoint);

		zoomToCurrentLocation(mapPoint, 5000000); // meters
	}

	/**
	 * Method setLocationProvider.
	 * 
	 * @param provider
	 *            String
	 */
	private void setLocationProvider(String provider) {
		locationProvider = locationManager.getProvider(provider);

		if (locationProvider == null)
			locationManager.addTestProvider(provider, false, false, false,
					false, true, true, true, 0, 5);
		else {
			locationManager.clearTestProviderEnabled(provider);
			locationManager.clearTestProviderLocation(provider);
			locationManager.clearTestProviderStatus(provider);
		}

		locationManager.setTestProviderEnabled(provider, true);
		locationManager.setTestProviderStatus(provider,
				LocationProvider.AVAILABLE, null, System.currentTimeMillis());
	}

	private void createBaseMap() {
		baseMapLayer = new ArcGISLocalTiledLayer(
				"file:///mnt/sdcard/demo/basemap/ImageryTPK.tpk");
		baseMapLayer.setName(BladeRunnerApplication.basemapName);
	}

	@SuppressWarnings("deprecation")
	void setSensor() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		orientationSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}

	private void createGraphicsLayers() {
		teamAreasLabelsGraphicsLayer = new GraphicsLayer();
		teamAreasLabelsGraphicsLayer
				.setName(BladeRunnerApplication.teamAreasLabelName);
		setTeamAreasLabelsSymbol();

		laserRangerGraphicsLayer = new LaserRangeGraphicsLayer(this);
		laserRangerGraphicsLayer
				.setName(BladeRunnerApplication.laserRangeFinderName);
		laserRangerGraphicsLayer.showAngle(false);
		laserRangerGraphicsLayer.showDistance(false);
		laserRangerGraphicsLayer.showPin(false);

		foundGraphicsLayer = new GraphicsLayer();
		foundGraphicsLayer.setName(BladeRunnerApplication.foundName);
	}

	private void setTeamAreasLabelsSymbol() {
		labelTextSymbol = new TextSymbol(32, "", getResources().getColor(
				R.color.white_textcolor));
		labelTextSymbol.setHorizontalAlignment(HorizontalAlignment.CENTER);
		labelTextSymbol.setVerticalAlignment(VerticalAlignment.MIDDLE);

		SimpleRenderer simpleRenderer = new SimpleRenderer(labelTextSymbol);
		teamAreasLabelsGraphicsLayer.setRenderer(simpleRenderer);
	}

	/**
	 * Get the featureset from the json files and add the feature set to block
	 * layer.
	 */
	private void getBlocksFromJSON() {
		new Thread() {
			@Override
			public void run() {
				try {
					File jsonFile = bladeRunnerApp
							.getJSONFile(BladeRunnerApplication.blocksName);
					JsonFactory jsonFactory = new JsonFactory();
					JsonParser jsonParser = jsonFactory
							.createJsonParser(jsonFile);
					FeatureSet featureSet = FeatureSet.fromJson(jsonParser,
							true);
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_SUCCESSFULL);
					Bundle bundle = new Bundle();
					bundle.putSerializable(BLOCKS_FEATURESET, featureSet);
					msg.obj = bundle;
					msg.sendToTarget();
				} catch (InterruptedException e) {
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_UNSUCCESSFULL);
					msg.obj = null;
					msg.sendToTarget();
					e.printStackTrace();
				} catch (Exception e) {
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_UNSUCCESSFULL);
					msg.obj = null;
					msg.sendToTarget();
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * Get the featureset from the json files and add the feature set to
	 * windfarmzone layer.
	 */
	private void getWindFarmZonesFromJSON() {
		new Thread() {
			@Override
			public void run() {
				try {
					File jsonFile = bladeRunnerApp
							.getJSONFile(BladeRunnerApplication.windFarmZonesName);
					JsonFactory jsonFactory = new JsonFactory();
					JsonParser jsonParser = jsonFactory
							.createJsonParser(jsonFile);
					FeatureSet featureSet = FeatureSet.fromJson(jsonParser,
							true);
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_SUCCESSFULL);
					Bundle bundle = new Bundle();
					bundle.putSerializable(WIND_FARM_ZONES_FEATURESET,
							featureSet);
					msg.obj = bundle;
					msg.sendToTarget();
				} catch (InterruptedException e) {
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_UNSUCCESSFULL);
					msg.obj = null;
					msg.sendToTarget();
					e.printStackTrace();
				} catch (Exception e) {
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_UNSUCCESSFULL);
					msg.obj = null;
					msg.sendToTarget();
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * Get the featureset from the json files and add the feature set to
	 * teamareas layer.
	 */
	protected void getTeamAreasFromJSON() {
		new Thread() {
			@Override
			public void run() {
				try {
					File jsonFile = bladeRunnerApp
							.getJSONFile(BladeRunnerApplication.teamAreasName);
					JsonFactory jsonFactory = new JsonFactory();
					JsonParser jsonParser = jsonFactory
							.createJsonParser(jsonFile);
					teamAreasfeatureSet = FeatureSet.fromJson(jsonParser, true);
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_SUCCESSFULL);
					Bundle bundle = new Bundle();
					bundle.putSerializable(TEAMAREAS_FEATURESET,
							teamAreasfeatureSet);
					msg.obj = bundle;
					msg.sendToTarget();
				} catch (InterruptedException e) {
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_UNSUCCESSFULL);
					msg.obj = null;
					msg.sendToTarget();
					e.printStackTrace();
				} catch (Exception e) {
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_UNSUCCESSFULL);
					msg.obj = null;
					msg.sendToTarget();
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * Get the featureset from the json files and add the feature set to
	 * windturbines layer.
	 */
	private void getWindTurbinesFromJSON() {
		new Thread() {
			@Override
			public void run() {
				try {
					File jsonFile = bladeRunnerApp
							.getJSONFile(BladeRunnerApplication.windTurbineName);
					JsonFactory jsonFactory = new JsonFactory();
					JsonParser jsonParser = jsonFactory
							.createJsonParser(jsonFile);
					FeatureSet featureSet = FeatureSet.fromJson(jsonParser,
							true);
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_SUCCESSFULL);
					Bundle bundle = new Bundle();
					bundle.putSerializable(WIND_TURBINE_FEATURESET, featureSet);
					msg.obj = bundle;
					msg.sendToTarget();
				} catch (InterruptedException e) {
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_UNSUCCESSFULL);
					msg.obj = null;
					msg.sendToTarget();
					e.printStackTrace();
				} catch (Exception e) {
					Message msg = handler
							.obtainMessage(DESERIALIZED_RESULT_UNSUCCESSFULL);
					msg.obj = null;
					msg.sendToTarget();
					e.printStackTrace();
				}
			}
		}.start();
	}

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "handler --> message " + msg.what);

			switch (msg.what) {
			case DESERIALIZED_RESULT_SUCCESSFULL:
				Bundle bundle = (Bundle) msg.obj;
				if (bundle.containsKey(BLOCKS_FEATURESET))
					Utility.addGraphicsToFeatureLayer(blocksFeatureLayer,
							(FeatureSet) bundle
									.getSerializable(BLOCKS_FEATURESET));
				else if (bundle.containsKey(WIND_TURBINE_FEATURESET))
					Utility.addGraphicsToFeatureLayer(windTurbinesFeatureLayer,
							(FeatureSet) bundle
									.getSerializable(WIND_TURBINE_FEATURESET));
				else if (bundle.containsKey(WIND_FARM_ZONES_FEATURESET))
					Utility.addGraphicsToFeatureLayer(
							windFarmZonesFeatureLayer,
							(FeatureSet) bundle
									.getSerializable(WIND_FARM_ZONES_FEATURESET));
				else if (bundle.containsKey(TEAMAREAS_FEATURESET)) {
					FeatureSet featureSet = (FeatureSet) bundle
							.getSerializable(TEAMAREAS_FEATURESET);
					Utility.addGraphicsToFeatureLayer(teamareasFeatureLayer,
							featureSet);
					addGraphicsAsLabels(featureSet);
				}
				break;
			case DESERIALIZED_RESULT_UNSUCCESSFULL:
				break;
			}
		}
	};

	/**
	 * Method addGraphicsAsLabels.
	 * 
	 * @param featureSet
	 *            FeatureSet
	 */
	protected synchronized void addGraphicsAsLabels(FeatureSet featureSet) {
		if (featureSet == null || featureSet.getGraphics() == null)
			return;

		Graphic[] graphics = featureSet.getGraphics();

		if (graphics == null || graphics.length == 0)
			return;

		for (Graphic graphic : graphics) {
			Object value = graphic
					.getAttributeValue(BladeRunnerApplication.teamAreasLabelField);
			if (value == null)
				continue;

			labelTextSymbol.setText(value.toString());
			teamAreasLabelsGraphicsLayer.addGraphic(new Graphic(Helper
					.getEnvelopeCenter((Polygon) graphic.getGeometry()),
					labelTextSymbol));
		}
	}

	void setDefaultMapExtent() {
		Point p1 = map.toMapPoint((float) 649.1942, (float) 682.25275);
		Point p2 = map.toMapPoint((float) 702.76074, (float) 169.16583);

		initialExtent = new Envelope(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		map.setExtent(initialExtent);

	}

	void setWidgets() {
		fadeinAnimation = AnimationUtils.loadAnimation(this, R.anim.appear);
		fadeoutAnimation = AnimationUtils.loadAnimation(this, R.anim.disappear);

		compassButton = (ToggleButton) findViewById(R.id.compassbutton);
		compassButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean isChecked = ((ToggleButton) view).isChecked();

				if (isChecked)
					sensorManager.registerListener(sensorListener,
							orientationSensor,
							SensorManager.SENSOR_DELAY_NORMAL);
				else {
					map.setRotationAngle(0);
					sensorManager.unregisterListener(sensorListener);
				}
			}
		});

		layersButton = (ToggleButton) findViewById(R.id.layersbutton);
		layersButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean isChecked = ((ToggleButton) view).isChecked();

				if (isChecked)
					showDialog(DIALOG_LAYERS_VISIBILITY);
				else
					removeDialog(DIALOG_LAYERS_VISIBILITY);
			}
		});

		zoomButton = (Button) findViewById(R.id.zoombutton);
		zoomButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				map.setExtent(initialExtent);
			}
		});

		laserRangeButton();

		windTurbineListView = (ListView) findViewById(R.id.resultslistview);

		resultsButton = (ToggleButton) findViewById(R.id.resultsbutton);
		resultsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ToggleButton button = (ToggleButton) view;

				if (button.isChecked())
					showResults();
				else
					mapWidgetsVisibility(true);
			}
		});

		laserRangeLayout = (ViewGroup) findViewById(R.id.laserrangedatalayout);

		gpsButton();

		makeWidgetsVisible();
	}

	private void gpsButton() {
		locationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
				if (firstLocation) {
					firstMapLocation = Utility.fromWgs84ToMap(location,
							map.getSpatialReference());
					zoomToCurrentLocation(firstMapLocation,
							(location.hasAccuracy() ? location.getAccuracy()
									: 0f));
					firstLocation = false;
				}

				currentLocation = new Location(location);
				map.getLocationService().setAutoPan(false);
			}
		};

		gpsMarkerSymbol = new PictureMarkerSymbol(getResources().getDrawable(
				R.drawable.location));
		map.getLocationService().setSymbol(gpsMarkerSymbol);

		gpsToggleButton = (ToggleButton) findViewById(R.id.gpsbutton);
		gpsToggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean isChecked = ((ToggleButton) view).isChecked();

				// Comment the startWithMockupPoint() and uncomment the other
				// code to use
				// the device's gps
				if (isChecked) {
					startWithMockupPoint(33.907839, -116.593239);
					// map.getLocationService().start();
					// map.getLocationService().setAllowNetworkLocation(true);
					// map.getLocationService().setLocationListener(locationListener);
					// map.getLocationService().setAutoPan(true);
				} else {
					// map.getLocationService().stop();
					// map.getLocationService().setLocationListener(null);
					// locationManager.removeUpdates(locationListener);
				}
			}
		});
	}

	private void makeWidgetsVisible() {
		layersButton.setAnimation(fadeinAnimation);
		layersButton.setVisibility(View.VISIBLE);
		resultsButton.setAnimation(fadeinAnimation);
		resultsButton.setVisibility(View.VISIBLE);
		laserToggleButton.setAnimation(fadeinAnimation);
		laserToggleButton.setVisibility(View.VISIBLE);

		zoomButton.setAnimation(fadeinAnimation);
		zoomButton.setVisibility(View.VISIBLE);
		compassButton.setAnimation(fadeinAnimation);
		compassButton.setVisibility(View.VISIBLE);
		gpsToggleButton.setAnimation(fadeinAnimation);
		gpsToggleButton.setVisibility(View.VISIBLE);

		findViewById(R.id.teamspinner).setAnimation(fadeinAnimation);
		findViewById(R.id.teamspinner).setVisibility(View.VISIBLE);
	}

	protected void showResults() {
		if (windTurbineGraphics == null || windTurbineGraphics.length == 0)
			return;

		mapWidgetsVisibility(false);

		if (windTurbineSymbol == null) {
			windTurbineSymbol = new PictureMarkerSymbol(getResources()
					.getDrawable(R.drawable.pin));
			windTurbineSymbol.setOffsetX(-25);
		}

		SelectionAdapter selectionAdapter = new SelectionAdapter(this,
				R.layout.selection_item, windTurbineGraphics,
				Utility.getCodedValueDomainField(windTurbinesFeatureLayer));
		windTurbineListView.setAdapter(selectionAdapter);
		windTurbineListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Graphic graphic = (Graphic) parent.getItemAtPosition(position);

				if (selectedID == -1) {
					selectedID = foundGraphicsLayer.addGraphic(graphic);
					foundGraphicsLayer.updateGraphic(selectedID,
							windTurbineSymbol);
				} else {
					foundGraphicsLayer.updateGraphic(selectedID, graphic);
					foundGraphicsLayer.updateGraphic(selectedID,
							windTurbineSymbol);
				}
			}
		});
	}

	/**
	 * Method mapWidgetsVisibility.
	 * 
	 * @param visible
	 *            boolean
	 */
	void mapWidgetsVisibility(boolean visible) {
		Animation animation = (visible) ? fadeinAnimation : fadeoutAnimation;
		int visibility = (visible) ? View.VISIBLE : View.GONE;

		findViewById(R.id.resultsdatalayout).setVisibility(
				(!visible) ? View.VISIBLE : View.GONE);

		zoomButton.setAnimation(animation);
		zoomButton.setVisibility(visibility);
		compassButton.setAnimation(animation);
		compassButton.setVisibility(visibility);
		gpsToggleButton.setAnimation(animation);
		gpsToggleButton.setVisibility(visibility);
	}

	private void laserRangeButton() {
		laserStatusTextView = (TextView) findViewById(R.id.statustext);
		laserToggleButton = (ToggleButton) findViewById(R.id.laserbutton);

		laserToggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean isChecked = ((ToggleButton) view).isChecked();

				if (!isChecked)
					laserStatusTextView.setText("");

				if (isChecked) {
					if (bluetoothAdapter == null) {
						bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					} else {
						if (!bluetoothAdapter.isEnabled()) {
							Intent enableIntent = new Intent(
									BluetoothAdapter.ACTION_REQUEST_ENABLE);
							startActivityForResult(
									enableIntent,
									BladeRunnerApplication.REQUEST_ENABLE_BLUETOOTH);
						} else {
							if (!bluetoothAdapter.isDiscovering()) {
								initBluetoothSerialService();

								bluetoothSerialService.start();
								showDialog(DIALOG_BLUETOOTH_DEVICES);
							} else
								bluetoothAdapter.startDiscovery();
						}
					}
				} else {
					laserRangeLayout.setVisibility(View.GONE);
					String value = getResources().getString(R.string.n_a);
					((TextView) laserRangeLayout.findViewById(R.id.hdvalue))
							.setText(value);
					((TextView) laserRangeLayout.findViewById(R.id.sdvalue))
							.setText(value);
					((TextView) laserRangeLayout
							.findViewById(R.id.bearingvalue)).setText(value);
					((TextView) laserRangeLayout
							.findViewById(R.id.inclinationvalue))
							.setText(value);

					if (bluetoothAdapter != null)
						bluetoothAdapter.cancelDiscovery();
					if (bluetoothSerialService != null)
						bluetoothSerialService.stop();
				}
			}
		});

		fadeoutAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				laserToggleButton.startAnimation(fadeinAnimation);
			}
		});

		fadeinAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				laserToggleButton.startAnimation(fadeoutAnimation);
			}
		});
	}

	/**
	 * Method onActivityResult.
	 * 
	 * @param requestCode
	 *            int
	 * @param resultCode
	 *            int
	 * @param data
	 *            Intent
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult --> " + requestCode + ", " + resultCode
				+ ", " + (data == null));

		switch (requestCode) {
		case BladeRunnerApplication.REQUEST_CONNECT_BLUETOOTH_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						BladeRunnerApplication.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = bluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				bluetoothSerialService.connect(device);
			}
			break;
		case BladeRunnerApplication.REQUEST_ENABLE_BLUETOOTH:
			// When the request to enable Bluetooth returns
			Log.v(TAG, "REQUEST_ENABLE_BLUETOOTH ---> "
					+ (resultCode == Activity.RESULT_OK));
			Log.v(TAG, "REQUEST_ENABLE_BLUETOOTH ---> result code: "
					+ resultCode);

			if (resultCode == Activity.RESULT_OK) {
				if (bluetoothAdapter == null)
					bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

				if (!bluetoothAdapter.isDiscovering()) {
					initBluetoothSerialService();

					bluetoothSerialService.start();
					showDialog(DIALOG_BLUETOOTH_DEVICES);
				} else
					bluetoothAdapter.startDiscovery();
			}
			break;
		}
	}

	void initBluetoothSerialService() {
		Log.v(TAG, "initBluetoothSerialService()");
		if (bluetoothSerialService != null)
			return;
		laserRangeData = new StringBuffer();
		byteQueue = new ByteQueue(1024);
		bluetoothSerialService = new BluetoothSerialService(this,
				bluetoothHandler, byteQueue);
	}

	protected void layersVisibility() {
		boolean layersDialog = bladeRunnerApp
				.readBooleanApplicationPrefecence(BladeRunnerApplication.LAYERS_VISIBILITY);
		if (layersDialog) {
			boolean isVisible;
			for (Layer layer : map.getLayers()) {
				if (layer instanceof ArcGISLocalTiledLayer
						|| layer instanceof ArcGISFeatureLayer) {
					isVisible = bladeRunnerApp
							.readBooleanApplicationPrefecence(layer.getName());
					layer.setVisible(isVisible);

					if (layer.getName().equals(
							BladeRunnerApplication.teamAreasName))
						teamAreasLabelsGraphicsLayer.setVisible(isVisible);
				}
			}
		}

		getVisibleLayers();
	}

	private void getVisibleLayers() {
		visibleLayers = new ArrayList<Layer>();
		for (Layer layer : map.getLayers()) {
			if (layer instanceof ArcGISLocalTiledLayer
					|| layer instanceof ArcGISFeatureLayer)
				visibleLayers.add(layer);
		}
	}

	void setMapViewListener() {
		mapViewTouchListener = new MapViewTouchListener(
				getApplicationContext(), map);
		map.setOnTouchListener(mapViewTouchListener);
	}

	private void setTeamsAdapter() {
		teamSpinner = (Spinner) findViewById(R.id.teamspinner);
		final String[] teams = getResources().getStringArray(R.array.teamname);
		final AreasSpinnerAdapter teamAdapter = new AreasSpinnerAdapter(this,
				teams, getResources().getString(R.string.team_hint));
		teamSpinner.setAdapter(teamAdapter);

		teamSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0)
					return;

				dayWidgetsVisibility(View.VISIBLE);

				setAreaSketchTool();

				if (areaSketchTool.getArea() == null)
					return;

				areaSketchTool.setTeamName(parent.getAdapter()
						.getItem(position).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	/**
	 * Method dayWidgetsVisibility.
	 * 
	 * @param visible
	 *            int
	 */
	void dayWidgetsVisibility(int visible) {
		findViewById(R.id.divider).setVisibility(visible);
		findViewById(R.id.daylayout).setVisibility(visible);
	}

	void setAreaSketchTool() {
		if (areaSketchTool != null)
			return;

		areaSketchTool = new AreaSketchTool(map);
		areaSketchTool.setOnMapToolListener(new OnMapToolListener() {
			@Override
			public void onStatusChanged(boolean cancelled) {
				mapViewTouchListener.stopCollecting();
				mapViewTouchListener.setTool(null);

				if (!cancelled)
					return;

				teamSpinner.setSelection(0);
				daySpinner.setSelection(0);
			}
		});
	}

	private void setDaysAdapter() {
		daySpinner = (Spinner) findViewById(R.id.daysspinner);
		final String[] days = getResources().getStringArray(R.array.teamday);
		final AreasSpinnerAdapter dayAdapter = new AreasSpinnerAdapter(this,
				days, getResources().getString(R.string.day_hint));
		daySpinner.setAdapter(dayAdapter);

		daySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0)
					return;

				Area area = areaSketchTool.getArea();
				if (area != null) {
					if (area.index != position)
						areaSketchTool.done(false);
				}

				startCollecting(position);
				areaSketchTool.editAreaAtIndex(position, teamSpinner
						.getSelectedItemPosition() - 1, teamSpinner
						.getSelectedItem().toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void setDaysAreaUI() {
		Button acceptbutton = (Button) findViewById(R.id.acceptbutton);
		Button deleteButton = (Button) findViewById(R.id.deletebutton);

		acceptbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				saveAreas();
			}
		});

		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Spinner localDaySpinner = (Spinner) findViewById(R.id.daysspinner);
				int index = localDaySpinner.getSelectedItemPosition();

				areaSketchTool.removeAreaAtIndex(index);

				startCollecting(index);
			}
		});
	}

	/**
	 * Method startCollecting.
	 * 
	 * @param index
	 *            int
	 */
	void startCollecting(int index) {
		mapViewTouchListener.setTool(areaSketchTool);
		mapViewTouchListener.startCollecting();
	}

	void saveAreas() {
		if (areaSketchTool.isValid())
			areaSketchTool.done(false);

		convertAreasToGraphics();

		findWindTurbines();

		resetAdapters();

		areaSketchTool.done(true);
	}

	private void convertAreasToGraphics() {
		HashMap<Integer, Area> teamAreas = areaSketchTool.getAreas();

		if (teamAreas == null || teamAreas.isEmpty())
			return;

		int count = teamAreas.size();

		Graphic[] areaGraphics = new Graphic[count];
		Graphic[] labelGraphics = new Graphic[count];

		int i = 0;
		for (Integer index : teamAreas.keySet()) {
			Area area = teamAreas.get(index);
			Polygon polygon = area.area;

			HashMap<String, Object> attributes = setAttributes(index, area);

			UniqueValue uniqueValue = helper.getUniqueValue(area.getLabel());

			Graphic areaGraphic = new Graphic(polygon, uniqueValue.getSymbol(),
					attributes);

			areaGraphics[i] = areaGraphic;

			// Labels
			labelTextSymbol.setText(area.getLabel());
			Graphic labelGraphic = new Graphic(
					Helper.getEnvelopeCenter(polygon), labelTextSymbol,
					attributes);
			labelGraphics[i] = labelGraphic;

			i++;
		}

		if (teamAreasGraphics == null || teamAreasGraphics.isEmpty())
			teamAreasGraphics = new ArrayList<Graphic>();

		Collections.addAll(teamAreasGraphics, areaGraphics);
		teamareasFeatureLayer.addGraphics(areaGraphics);
		teamAreasLabelsGraphicsLayer.addGraphics(labelGraphics);

		saveEdits(areaGraphics);
	}

	/**
	 * Method setAttributes.
	 * 
	 * @param index
	 *            Integer
	 * @param area
	 *            Area
	 * @return HashMap<String,Object>
	 */
	private static HashMap<String, Object> setAttributes(Integer index,
			Area area) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(BladeRunnerApplication.teamAreasIDField, area.teamIndex);
		attributes.put(BladeRunnerApplication.teamAreasNameField,
				area.teamIndex);
		attributes.put(BladeRunnerApplication.teamAreasLabelField,
				area.getLabel());
		return attributes;
	}

	/**
	 * Method saveEdits.
	 * 
	 * @param graphics
	 *            Graphic[]
	 */
	private void saveEdits(Graphic[] graphics) {
		Intent intent = new Intent(this, SaveAreasIntentService.class);
		intent.putExtra(SaveAreasIntentService.RESULTRECEIVER,
				savedAreasResultReceiver());
		intent.putExtra(SaveAreasIntentService.FILENAME, bladeRunnerApp
				.getJSONFile(BladeRunnerApplication.teamAreasName)
				.getAbsolutePath());
		intent.putExtra(SaveAreasIntentService.CURRENT_FEATURESET,
				teamAreasfeatureSet);
		FeatureSet featureSet = new FeatureSet();
		featureSet.setGraphics(graphics);
		intent.putExtra(SaveAreasIntentService.NEW_FEATURESET, featureSet);
		startService(intent);

	}

	/**
	 * Method savedAreasResultReceiver.
	 * 
	 * @return SavedAreasResultReceiver
	 */
	private SavedAreasResultReceiver savedAreasResultReceiver() {
		if (resultReceiver == null)
			resultReceiver = new SavedAreasResultReceiver(new Handler());
		return resultReceiver;
	}

	/**
     */
	class SavedAreasResultReceiver extends ResultReceiver {
		/**
		 * Constructor for SavedAreasResultReceiver.
		 * 
		 * @param handler
		 *            Handler
		 */
		public SavedAreasResultReceiver(Handler handler) {
			super(handler);
		}

		/**
		 * Method onReceiveResult.
		 * 
		 * @param resultCode
		 *            int
		 * @param resultData
		 *            Bundle
		 */
		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			if (resultCode == SaveAreasIntentService.RESULTS_CODE)
				teamAreasfeatureSet = (FeatureSet) resultData
						.getSerializable(SaveAreasIntentService.FEATURESET_RESULTS);
			else if (resultCode == SaveAreasIntentService.ERROR_CODE)
				Log.e(TAG,
						"onReceiveResult: ERROR --> "
								+ resultData
										.getString(SaveAreasIntentService.EXCEPTION));
		}
	}

	private void resetAdapters() {
		AreasSpinnerAdapter adapter = (AreasSpinnerAdapter) teamSpinner
				.getAdapter();
		adapter.setSelected(false);
		adapter.notifyDataSetChanged();
		adapter = (AreasSpinnerAdapter) daySpinner.getAdapter();
		adapter.setSelected(false);
		adapter.notifyDataSetChanged();

		findViewById(R.id.divider).setVisibility(View.GONE);
		findViewById(R.id.daylayout).setVisibility(View.GONE);
	}

	private void findWindTurbines() {
		HashMap<Integer, Area> teamAreas = areaSketchTool.getAreas();

		if (teamAreas == null || teamAreas.isEmpty())
			return;

		Polygon polygon = new Polygon();
		for (Integer index : teamAreas.keySet()) {
			Area area = teamAreas.get(index);
			polygon.addPath(area.area, 0, true);
		}

		if (polygon.isEmpty())
			return;

		Query query = new Query();
		query.setGeometry(polygon);
		query.setSpatialRelationship(SpatialRelationship.INTERSECTS);
		query.setReturnGeometry(true);
		query.setOutFields(new String[] { "*" });

		windTurbinesFeatureLayer.queryFeatures(query,
				new CallbackListener<FeatureSet>() {
					@Override
					public void onCallback(FeatureSet featureSet) {
						if (featureSet == null
								|| featureSet.getGraphics() == null)
							return;

						windTurbineGraphics = featureSet.getGraphics();

						MapViewActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								int count = windTurbineGraphics.length;

								resultsButton
										.setVisibility((count == 0) ? View.GONE
												: View.VISIBLE);

								if (count == 0)
									return;

								Utility.getResultsString(resultsButton, count);
							}
						});
					}

					@Override
					public void onError(Throwable e) {
						e.printStackTrace();
					}
				});
	}

	// **********************************************
	// Main Menu
	// **********************************************
	/**
	 * Method onCreateOptionsMenu.
	 * 
	 * @param menu
	 *            Menu
	 * @return boolean
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mapview_menu, menu);

		return true;
	}

	/**
	 * Method onOptionsItemSelected.
	 * 
	 * @param item
	 *            MenuItem
	 * @return boolean
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!map.isLoaded())
			return false;

		switch (item.getItemId()) {
		case R.id.maplayers_menuitem:
			showDialog(DIALOG_LAYERS_VISIBILITY);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Method onCreateDialog.
	 * 
	 * @param id
	 *            int
	 * @return Dialog
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		dialog = null;

		switch (id) {
		case DIALOG_LAYERS_VISIBILITY:
			dialog = createLayersVisibilityDialog();
			break;
		case DIALOG_TURBINETYPES:
			dialog = locationMapTool.createTurbineTypesDialog();
			break;
		case DIALOG_BLUETOOTH_DEVICES:
			dialog = createBluetoothDevicesDialog();
			break;
		default:
			dialog = null;
			break;
		}

		return dialog;
	}

	/**
	 * Method onPrepareDialog.
	 * 
	 * @param id
	 *            int
	 * @param localDialog
	 *            Dialog
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog localDialog) {
		switch (id) {
		case DIALOG_TURBINETYPES:
			LocationMapTool.prepareTurbineTypesDialog(localDialog);
			break;
		default:
			break;
		}
	}

	/**
	 * Method createLayersVisibilityDialog.
	 * 
	 * @return Dialog
	 */
	private Dialog createLayersVisibilityDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View view = getLayoutInflater().inflate(R.layout.maplayersview,
				null);

		builder.setTitle(R.string.maplayers);
		builder.setIcon(R.drawable.maplayers);
		builder.setView(view);
		Dialog localDialog = builder.create();

		LayersAdapter layersAdapter = new LayersAdapter(this, visibleLayers,
				R.layout.maplayer_item);
		ListView list = (ListView) view.findViewById(R.id.maplayerslistview);
		list.setAdapter(layersAdapter);

		localDialog
				.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialogInterface) {
						int i = 0;
						for (Layer layer : map.getLayers()) {
							if (layer instanceof ArcGISLocalTiledLayer
									|| layer instanceof ArcGISFeatureLayer) {
								bladeRunnerApp.writeApplicationPreference(layer
										.getName(), visibleLayers.get(i)
										.isVisible());

								if (layer.getName().equals(
										BladeRunnerApplication.teamAreasName))
									teamAreasLabelsGraphicsLayer
											.setVisible(visibleLayers.get(i)
													.isVisible());

								i++;
							}
						}
						bladeRunnerApp.writeApplicationPreference(
								BladeRunnerApplication.LAYERS_VISIBILITY, true);

						((ToggleButton) findViewById(R.id.layersbutton))
								.setChecked(false);
					}
				});

		return localDialog;
	}

	/**
	 * Method createBluetoothDevicesDialog.
	 * 
	 * @return Dialog
	 */
	private Dialog createBluetoothDevicesDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View view = getLayoutInflater().inflate(
				R.layout.bluetoothdeviceslist, null);

		builder.setTitle(R.string.bluetooth_devices);
		builder.setIcon(R.drawable.bluetooth);
		builder.setView(view);
		Dialog localDialog = builder.create();

		TextView pairedDevicesText = (TextView) view
				.findViewById(R.id.paired_devices_text);

		if (bluetoothAdapter == null) {
			pairedDevicesText.setText(R.string.alert_dialog_no_bt);
			return localDialog;
		}

		progressBar = (ProgressBar) view.findViewById(R.id.progressbar);

		ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<String>(
				this, R.layout.listitem);
		ListView pairedListView = (ListView) view
				.findViewById(R.id.paired_devices_list);
		pairedListView.setAdapter(pairedDevicesAdapter);
		pairedListView.setOnItemClickListener(deviceOnClickListener);

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter
				.getBondedDevices();

		if (pairedDevices.size() > 0) {
			pairedDevicesText.setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
		} else {
			String noDevices = getResources().getText(R.string.none_paired)
					.toString();
			pairedDevicesAdapter.add(noDevices);
		}

		newDevicesAdapter = new ArrayAdapter<String>(this, R.layout.listitem);
		ListView newListView = (ListView) view.findViewById(R.id.new_devices);
		newListView.setAdapter(newDevicesAdapter);
		newListView.setOnItemClickListener(deviceOnClickListener);

		Button scanDevicesButton = (Button) view.findViewById(R.id.button_scan);
		scanDevicesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doDiscovery(view);
			}
		});

		localDialog
				.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						MapViewActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (bluetoothSerialService == null)
									return;

								if (bluetoothSerialService.isConnecting())
									return;

								boolean notConnected = bluetoothSerialService
										.getState() == BluetoothSerialService.STATE_NONE
										|| bluetoothSerialService.getState() == BluetoothSerialService.STATE_STOP;
								if (notConnected
										&& laserToggleButton.isChecked())
									laserToggleButton.performClick();
							}
						});
					}
				});

		return localDialog;
	}

	/**
	 * Method doDiscovery.
	 * 
	 * @param view
	 *            View
	 */
	void doDiscovery(View view) {
		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);

		// Turn on sub-title for new devices
		view.findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (bluetoothAdapter.isDiscovering())
			bluetoothAdapter.cancelDiscovery();

		// Request discover from BluetoothAdapter
		bluetoothAdapter.startDiscovery();
	}

	private OnItemClickListener deviceOnClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			bluetoothAdapter.cancelDiscovery();
			// Get the device MAC address, which is the last 17 chars in the
			// View
			String info = ((TextView) view).getText().toString();
			String address = info.substring(info.length() - 17);

			Log.v(TAG, "address --> " + address);
			BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

			// Attempt to connect to the device
			bluetoothSerialService.connect(device);
		}
	};

	private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// Finds a device
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					String deviceName = device.getName()
							+ BladeRunnerApplication.LINE_SEPARATOR
							+ device.getAddress();
					boolean exists = false;
					int count = newDevicesAdapter.getCount();
					for (int i = 0; i < count; i++) {
						if (newDevicesAdapter.getItem(i).equals(deviceName)) {
							exists = true;
							break;
						}
					}

					if (!exists)
						newDevicesAdapter.add(deviceName);
				}
			} else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				int bondState = intent
						.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
								BluetoothDevice.ERROR);

				switch (bondState) {
				case BluetoothDevice.BOND_NONE:
					break;
				case BluetoothDevice.BOND_BONDING:
					break;
				case BluetoothDevice.BOND_BONDED:
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					bluetoothSerialService.connect(device);
					break;
				}
			} else if (action
					.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				setProgressBarIndeterminateVisibility(false);

				if (newDevicesAdapter.getCount() == 0) {
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					newDevicesAdapter.add(noDevices);
				}
			}
		}
	};

	// The Handler that gets information back from the BluetoothService
	private final Handler bluetoothHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BladeRunnerApplication.BLUETOOTH_MESSAGE_STATE_CHANGE:

				switch (msg.arg1) {
				case BluetoothSerialService.STATE_STARTED:
					if (laserToggleButton != null) {
						if (laserToggleButton.isChecked())
							laserToggleButton.startAnimation(fadeoutAnimation);
					}

					laserStatusTextView.setText(R.string.connection_started);
					break;
				case BluetoothSerialService.STATE_CONNECTED:
					if (progressBar != null)
						progressBar.setVisibility(View.GONE);

					if (dialog != null && dialog.isShowing())
						dismissDialog(DIALOG_BLUETOOTH_DEVICES);

					if (laserRangeLayout != null) {
						if (laserRangeLayout.getVisibility() == View.GONE)
							laserRangeLayout.setVisibility(View.VISIBLE);
					}

					laserStatusTextView.setText("Connected to: "
							+ connectedDeviceName);
					break;
				case BluetoothSerialService.STATE_CONNECTING:
					if (progressBar != null)
						progressBar.setVisibility(View.VISIBLE);

					laserStatusTextView.setText(R.string.connecting_to_device);
					break;
				case BluetoothSerialService.STATE_STOP:
					if (laserToggleButton != null
							&& laserToggleButton.isChecked())
						laserToggleButton.performClick();

					if (laserRangeLayout != null)
						laserRangeLayout.setVisibility(View.GONE);

					if (dialog != null)
						removeDialog(DIALOG_BLUETOOTH_DEVICES);
					break;
				case BluetoothSerialService.STATE_LISTEN:
				case BluetoothSerialService.STATE_NONE:
					Log.i(TAG, "bluetoothHandler --> STATE_NONE");
					if (progressBar != null)
						progressBar.setVisibility(View.GONE);

					if (laserRangeLayout != null)
						laserRangeLayout.setVisibility(View.GONE);

					if (dialog != null)
						removeDialog(DIALOG_BLUETOOTH_DEVICES);
					break;
				}
				break;
			case BladeRunnerApplication.BLUETOOTH_MESSAGE_WRITE:
				break;
			case BladeRunnerApplication.BLUETOOTH_MESSAGE_READ:
				// $PLTIT,HV,2.00,F,171.10,D,3.20,D,2.00,F*60\r\n

				int bytesAvailable = byteQueue.getBytesAvailable();

				if (bytesAvailable == 0)
					return;

				byte[] buffer = new byte[bytesAvailable];
				try {
					byteQueue.read(buffer, 0, bytesAvailable);
					laserRangeData.append(new String(buffer));
					Log.e(TAG, laserRangeData.toString());

					int delim = laserRangeData.indexOf(SENTENCE_DELIMITER);
					if (delim >= 0) {
						String s = laserRangeData.substring(0, delim);
						String[] d = s.split(DATA_FIELD_DELIMITER);

						parseSentence(d);

						laserRangeData = new StringBuffer(
								laserRangeData.substring(delim
										+ SENTENCE_DELIMITER.length()));
					}
				} catch (InterruptedException e) {
					// @TODO log the exception and prompt to user.
				}
				break;
			case BladeRunnerApplication.BLUETOOTH_MESSAGE_DEVICE_NAME:
				connectedDeviceName = msg.getData().getString(
						BladeRunnerApplication.BLUETOOTH_DEVICE_NAME);
				laserStatusTextView.setText("Connected to: "
						+ connectedDeviceName);
				break;
			case BladeRunnerApplication.BLUETOOTH_MESSAGE_TOAST:
				laserStatusTextView.setText(msg.getData().getString(TOAST));
				break;
			}
		}

		private void parseSentence(String[] d) {
			if (d.length != 10)
				return;

			if ((d[ADDRESS_FIELD_INDEX].equals(ADDRESS_FIELD))
					&& (d[SENTENCE_TYPE_INDEX].equals(SENTENCE_TYPE))) {
				double distance = Double.valueOf(d[HD_INDEX]);
				double bearing = Double.valueOf(d[HA_INDEX]);

				Unit unit = getDeviceUnits(d);

				updateUI(d, distance, bearing, unit);

				if (currentLocation == null)
					return;

				drawLaserPoint(distance, bearing, unit);

				createWindTurbineFeature(offsetLaserPoint);
			}
		}

		private void drawLaserPoint(double distance, double bearing, Unit unit) {
			SpatialReference spatialReference = map.getSpatialReference();
			double mapDistance = Unit.convertUnits(distance, unit,
					Unit.create(LinearUnit.Code.KILOMETER));
			offsetLaserPoint = Utility.offsetPointInKilometers(currentLocation,
					mapDistance, bearing, spatialReference);
			Point startPoint = Utility.fromWgs84ToMap(currentLocation,
					spatialReference);
			laserRangerGraphicsLayer.setMapUnits(spatialReference.getUnit()
					.getAbbreviation());
			laserRangerGraphicsLayer.drawOffset(startPoint, offsetLaserPoint,
					false);
		}

		private void updateUI(String[] d, double distance, double bearing,
				Unit unit) {
			StringBuilder sb = new StringBuilder();
			sb.append(" ");
			sb.append(unit.getAbbreviation());

			((TextView) laserRangeLayout.findViewById(R.id.hdvalue))
					.setText(Double.toString(distance) + sb.toString());
			((TextView) laserRangeLayout.findViewById(R.id.sdvalue))
					.setText(d[SD_INDEX] + sb.toString());
			((TextView) laserRangeLayout.findViewById(R.id.bearingvalue))
					.setText(Double.toString(bearing));
			((TextView) laserRangeLayout.findViewById(R.id.inclinationvalue))
					.setText(d[VA_INDEX]);
		}

		private Unit getDeviceUnits(String[] d) {
			if (d[HDU_INDEX].equals("F"))
				return Unit.create(LinearUnit.Code.FOOT);
			else if (d[HDU_INDEX].equals("M"))
				return Unit.create(LinearUnit.Code.METER);
			else if (d[HDU_INDEX].equals("Y"))
				return Unit.create(LinearUnit.Code.YARD);
			else if (d[HDU_INDEX].equals("D"))
				return Unit.create(AngularUnit.Code.DEGREE);

			return null;
		}
	};

	/**
	 * Method createWindTurbineFeature.
	 * 
	 * @param point
	 *            Point
	 */
	protected void createWindTurbineFeature(Point point) {
		FeatureTemplate[] featureTemplates = windTurbinesFeatureLayer
				.getTemplates();

		if (featureTemplates == null || featureTemplates.length == 0)
			return;

		Graphic graphic = windTurbinesFeatureLayer.createFeatureWithTemplate(
				featureTemplates[0], point);

		int id = windTurbinesFeatureLayer.addGraphic(graphic);

		Utility.printAttibuteValues(graphic);

		if (locationMapTool == null)
			return;

		locationMapTool.setWindTurbineSelection(id, graphic);
	}

	/**
	 * Method zoomToCurrentLocation.
	 * 
	 * @param mapPoint
	 *            Point
	 * @param factor
	 *            float
	 */
	public void zoomToCurrentLocation(Point mapPoint, float factor) {
		map.zoomTo(mapPoint, factor);

		if (map.getLocationService().isAutoPan())
			map.getLocationService().setAutoPan(false);
	}

	SensorEventListener sensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			sensorValues = event.values;
			// Azimuth = 0; picthc = 1; roll = 2
			map.setRotationAngle(sensorValues[0]);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
}