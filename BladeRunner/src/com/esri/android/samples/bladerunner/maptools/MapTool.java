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

package com.esri.android.samples.bladerunner.maptools;

import android.content.Context;
import android.view.View.OnClickListener;

import com.esri.android.map.MapView;

// Base class for all map tools. This class has default symbols to draw
// a sketch of a geometry and to checked/unchecked the View that it is
// associated with the tool
/**
 * 
 * @version $Revision: 1.0 $
 */
public abstract class MapTool implements OnClickListener, OnMapToolListener {
	private MapView map;
	private OnMapToolListener onMapToolListener;

	// MapTool constructor. Requires the context to set the map's OnTouchListener,
	// the MapView and the UI View associated to the MapTool
	/**
	 * Constructor for MapTool.
	 * @param view MapView
	 */
	public MapTool(MapView view) {
		super();
		this.map = view;
	}
	
	/**
	 * Constructor for MapTool.
	 * @param context Context
	 */
	public MapTool(Context context) {
		super();
	}
	
	/**
	 * Method undo.
	
	 * @return boolean */
	public static boolean undo() {
		return false;
	}
	
	// Starts the maptool
	public void start(){
//		if (sketchGraphicsLayer != null)
//			sketchGraphicsLayer.setOnTouchListener(MapViewActivity.getMapViewTouchListener());
	}
	
	// Stops the maptool
	public void stop(){

	}
	
	// Resets the maptool
	public void reset(){
		
	}
	
	/**
	 * Method toString.
	
	 * @return String */
	@Override
  public String toString(){
		return this.getClass().getName();
	}
	
	/**
	 * Method setOnMapToolListener.
	 * @param listener OnMapToolListener
	 */
	public void setOnMapToolListener(OnMapToolListener listener) {
		this.onMapToolListener = listener;
	}
	
	/**
	 * Method OnStatusChanged.
	 * @param succeeded boolean
	 */
	private void OnStatusChanged(boolean succeeded) {
		if (onMapToolListener != null) 
			onMapToolListener.onStatusChanged(succeeded);
	}
	
	/**
	 * Method getContext.
	
	 * @return Context */
	public Context getContext() {
		return map.getContext();
	}

	/**
	 * Method done.
	 * @param cancelled boolean
	 */
	public void done(boolean cancelled) {
		OnStatusChanged(cancelled);
	}
}
