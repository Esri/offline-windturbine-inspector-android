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

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;

/**
 * @version $Revision: 1.0 $
 */
public class SaveAreasIntentService extends IntentService {
//	private static final String TAG = SaveAreasIntentService.class.getSimpleName();
	private static final String TAG = "SaveAreasIntentService";
	
	public static final String RESULTRECEIVER = "ResultReceiver";
	public static final String FILENAME = "file_name";
	public static final String CURRENT_FEATURESET = "current_featureset";
	public static final String NEW_FEATURESET = "new_featureset";
	public static final String FEATURESET_RESULTS = "featureset_results";
	public static final String EXCEPTION = "exception";
	
	public static final int START_CODE = 0x1;
	public static final int FINISH_CODE = 0x2;
	public static final int RESULTS_CODE = 0x3;
	public static final int ERROR_CODE = 0x4;

	public SaveAreasIntentService() {
		super(TAG);
	}

	/**
	 * Method onHandleIntent.
	 * @param intent Intent
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		ResultReceiver receiver = intent.getParcelableExtra(RESULTRECEIVER);
		
		String fileName = intent.getStringExtra(FILENAME);
		FeatureSet featureSet = (FeatureSet) intent.getSerializableExtra(CURRENT_FEATURESET);
		FeatureSet newFeatureSet = (FeatureSet) intent.getSerializableExtra(NEW_FEATURESET);
		
		if (featureSet == null || featureSet.getGraphics() == null || featureSet.getGraphics().length == 0)
			featureSet = newFeatureSet;
		else
			joinGraphicsArrays(featureSet, newFeatureSet);
		
		File jsonFile = new File(fileName);
		JsonFactory jsonFactory = new JsonFactory();
	    
	    try {
	    	JsonGenerator jsonGen = jsonFactory.createJsonGenerator(jsonFile, JsonEncoding.UTF8);
		    String sw =  FeatureSet.toJson(featureSet);
		    jsonGen.writeRawValue(sw);
		    jsonGen.close();
		    Bundle bundle = new Bundle();
		    bundle.putSerializable(FEATURESET_RESULTS, featureSet);
		    receiver.send(RESULTS_CODE, bundle);
		} catch (Exception e) {
			e.printStackTrace();
			Bundle bundle = new Bundle();
			bundle.putString(EXCEPTION, e.toString());
			receiver.send(ERROR_CODE, bundle);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Method joinGraphicsArrays.
	 * @param featureSet FeatureSet
	 * @param newFeatureSet FeatureSet
	 */
	private static void joinGraphicsArrays(FeatureSet featureSet, FeatureSet newFeatureSet) {
		Graphic[] currentGraphics = featureSet.getGraphics();
		Graphic[] newGraphics = newFeatureSet.getGraphics();
		
		Graphic[] both = new Graphic[currentGraphics.length + newGraphics.length];
		System.arraycopy(currentGraphics, 0, both, 0, currentGraphics.length);
		System.arraycopy(newGraphics, 0, both, currentGraphics.length, newGraphics.length);
		featureSet.setGraphics(both);
	}
}
