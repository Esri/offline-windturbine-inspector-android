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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Map.Entry;

import android.content.res.Resources;
import android.location.Location;
import android.util.Log;
import android.widget.ToggleButton;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CodedValueDomain;
import com.esri.core.map.Domain;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;

/**
 * @version $Revision: 1.0 $
 */
public class Utility {
	private static String TAG = Utility.class.getSimpleName();
	
	public static NumberFormat outNumberFormat = new DecimalFormat("###0.#");
	
	static SpatialReference wgs84SR = SpatialReference.create(4326);
	
	/**
	 * Method fromWgs84ToMap.
	 * @param location Location
	 * @param spatialReference SpatialReference
	
	 * @return Point */
	public static Point fromWgs84ToMap(Location location, SpatialReference spatialReference){
		if (spatialReference == null)
			return null;
		
		if (wgs84SR == null)
			wgs84SR = SpatialReference.create(4326);

		Point point = new Point(location.getLongitude(), location.getLatitude());
		
		Point mapPoint;
		if (4236 != spatialReference.getID())
			mapPoint = (Point) GeometryEngine.project(point, wgs84SR, spatialReference);
		else
			mapPoint = point;
		
		return mapPoint;
	}
	
	/**
	 * Method fromWgs84ToMap.
	 * @param point Point
	 * @param spatialReference SpatialReference
	
	 * @return Point */
	public static Point fromWgs84ToMap(Point point, SpatialReference spatialReference){
		if (wgs84SR == null)
			wgs84SR = SpatialReference.create(4326);
		
		Point mapPoint;
		if (4236 != spatialReference.getID() )
			mapPoint = (Point) GeometryEngine.project(point, wgs84SR, spatialReference);
		else
			mapPoint = point;
		
		return mapPoint;
	}
	
	/**
	 * Method printAttibuteValues.
	 * @param graphic Graphic
	 */
	public static void printAttibuteValues(Graphic graphic){
		Map<String, Object> values = graphic.getAttributes();
		for (Entry<String, Object> entry : values.entrySet())
			Log.d(TAG, "attribute: " + entry.getKey() + ", " + entry.getValue());
	}
	
	/**
	 * Method getResultsString.
	 * @param button ToggleButton
	 * @param count int
	 */
	public static void getResultsString(ToggleButton button, int count) {
		Resources resources = button.getContext().getResources();
		StringBuilder sb = new StringBuilder();
		
		if (count == 0) {
			sb.append(resources.getString(R.string.results));
		} else {
			sb.append(count);
			sb.append(" ");
			sb.append((count == 1) ? resources.getString(R.string.result) : resources.getString(R.string.results));
		}
		
		button.setText(sb);
		button.setTextOn(sb);
		button.setTextOff(sb);
	}

    /**
     * Method addGraphicsToFeatureLayer.
     * @param featureLayer ArcGISFeatureLayer
     * @param featureSet FeatureSet
     */
    public static void addGraphicsToFeatureLayer(ArcGISFeatureLayer featureLayer, FeatureSet featureSet) {
		if (featureSet == null || featureSet.getGraphics() == null)
			return;
		
		Graphic[] graphics = featureSet.getGraphics();
		
		if (graphics == null || graphics.length == 0)
			return;
		
		featureLayer.addGraphics(graphics);
    }
    
    /**
     * Method offsetPointInKilometers.
     * @param location Location
     * @param distance double
     * @param bearing double
     * @param spatialReference SpatialReference
    
     * @return Point */
    public static Point offsetPointInKilometers(Location location, double distance, double bearing, SpatialReference spatialReference) {
		double R =  6371.009; // MEAN //6378.137; //KM ecuators radius
		double epsilon = 0.000001;
		
		double rlat = location.getLatitude() * Math.PI / 180;
		double rlon = location.getLongitude() * Math.PI / 180;
		double rbearing = (360 - bearing) * Math.PI / 180;
		double rdistance = distance / R;
		
		double lat1 = Math.asin(Math.sin(rlat) * Math.cos(rdistance) + Math.cos(rlat) * Math.sin(rdistance) 
				* Math.cos(rbearing));
		double lon1;
	    if (Math.cos(lat1) == 0 || Math.abs(Math.cos(lat1)) < epsilon)
	    	lon1 =rlon;
	    else
	    	lon1 = ( (rlon - Math.asin(Math.sin(rbearing)* Math.sin(rdistance) / Math.cos(lat1) ) + Math.PI ) 
	    			% (2* Math.PI) ) - Math.PI;
		
		Point point = new Point( ((lon1 * 180) / Math.PI), ((lat1 * 180) / Math.PI));
		
		Point mapPoint = Utility.fromWgs84ToMap(point, spatialReference);	
		
		return mapPoint;
    }
    
	/**
	 * Method getTextAngle.
	 * @param angle double
	
	 * @return double */
	public static double getTextAngle(double angle) {
		double newAngle;
		if(angle <= 90)
			newAngle = - 90 + angle;
		else if (angle > 90 && angle <= 180)
			newAngle = angle - 90;
		else if (angle > 270)
			newAngle = angle - 270;
		else
			newAngle = -(270 - angle);
		
		return newAngle;
	}
	
    /**
     * Method formatAngleWithMinutes.
     * @param angle double
    
     * @return String */
    public static String formatAngleWithMinutes(double angle){
    	long degress = (long) angle;
    	double remain = (angle - degress) * 60;
    	long minutes = (long) remain;

    	StringBuilder sb = new StringBuilder();
    	sb.append(degress);
    	sb.append("\u00B0 ");
		sb.append(minutes);
		sb.append("\u2032 ");
		
    	return sb.toString();
    }
    
	/**
	 * Method getCodedValueDomainField.
	 * @param featureLayer ArcGISFeatureLayer
	
	 * @return Field */
	public static Field getCodedValueDomainField(ArcGISFeatureLayer featureLayer) {
		Field[] fields = featureLayer.getFields();
		
		for (Field field : fields) {
			Domain domain = field.getDomain();
			if (domain instanceof CodedValueDomain)
				return field;
		}
		
		return null;
	}
}
