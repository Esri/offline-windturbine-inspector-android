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

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.samples.bladerunner.maptools.AreaSketchTool;
import com.esri.android.samples.bladerunner.maptools.LocationMapTool;
import com.esri.android.samples.bladerunner.maptools.MapTool;
import com.esri.core.geometry.Point;

/**
 * @version $Revision: 1.0 $
 */
public class MapViewTouchListener extends MapOnTouchListener implements OnClickListener{
//	private static String TAG = MapOnTouchListener.class.getSimpleName();
	
	private MapView map;
	
	private LocationMapTool locationMapTool;
	private MapTool tool;
	
	private boolean isCollecting;
	private Point mapPoint;
	private boolean isDirty;
	private boolean stopPanning;

	/**
	 * Constructor for MapViewTouchListener.
	 * @param context Context
	 * @param view MapView
	 */
	public MapViewTouchListener(Context context, MapView view) {
		super(context, view);
		map = view;
	}
	
	/**
	 * Method setLocationMapTool.
	 * @param locationMapTool LocationMapTool
	 */
	public void setLocationMapTool(LocationMapTool locationMapTool) {
		this.locationMapTool = locationMapTool;
	}

	/**
	 * Method getLocationMapTool.
	
	 * @return LocationMapTool */
	public LocationMapTool getLocationMapTool() {
		return locationMapTool;
	}
	
	/**
	 * Method setTool.
	 * @param tool MapTool
	 */
	public void setTool(MapTool tool) {
		this.tool = tool;
		
		if (tool instanceof AreaSketchTool)
			stopPanning = true;
		else
			stopPanning = false;
	}
	
	/**
	 * Method getMapTool.
	
	 * @return MapTool */
	public MapTool getMapTool() {
		return tool;
	}
	
	
	/**
	 * Method onTouch.
	 * @param v View
	 * @param event MotionEvent
	
	
	 * @return boolean * @see android.view.View$OnTouchListener#onTouch(View, MotionEvent) */
	@Override
  public boolean onTouch(View v, MotionEvent event) {
		isDirty = true;
		
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE:
			useTool(event, false);
			break;
		case MotionEvent.ACTION_UP:
			if (isCollecting) {
				tool.done(false);
				stopPanning = false;
			}
			break;
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
		case MotionEvent.ACTION_POINTER_UP:
		default:
			break;
		}
		
		return super.onTouch(v, event);
	}
	
	/**
	 * Method onLongPress.
	 * @param event MotionEvent
	
	 * @see com.esri.android.map.MapGestureDetector$OnGestureListener#onLongPress(MotionEvent) */
	@Override
  public void onLongPress(MotionEvent event) {
	}
		
	/**
	 * Method onDragPointerMove.
	 * @param from MotionEvent
	 * @param to MotionEvent
	
	
	 * @return boolean * @see com.esri.android.map.MapGestureDetector$OnGestureListener#onDragPointerMove(MotionEvent, MotionEvent) */
	@Override
  public boolean onDragPointerMove(MotionEvent from, final MotionEvent to){
		if (stopPanning)
			return false;
		
		return super.onDragPointerMove(from, to);
  }
	
		
	/**
	 * Method onDragPointerUp.
	 * @param from MotionEvent
	 * @param to MotionEvent
	
	
	 * @return boolean * @see com.esri.android.map.MapGestureDetector$OnGestureListener#onDragPointerUp(MotionEvent, MotionEvent) */
	@Override
  public boolean onDragPointerUp(MotionEvent from, final MotionEvent to){
		return super.onDragPointerUp(from, to);
	}
	
	
	/**
	 * Method onSingleTap.
	 * @param event MotionEvent
	
	
	 * @return boolean * @see com.esri.android.map.MapGestureDetector$OnGestureListener#onSingleTap(MotionEvent) */
	@Override
  public boolean onSingleTap(MotionEvent event){
		useTool(event, true);
		return true;
	}
	
	/**
	 * Method useTool.
	 * @param event MotionEvent
	 * @param singleTap boolean
	 */
	private void useTool(MotionEvent event, boolean singleTap) {
		mapPoint = map.toMapPoint(event.getX(), event.getY());
		
		if (tool == null && singleTap) {
			if (locationMapTool != null)
				locationMapTool.identify(event);
			return;
		}

		if (isCollecting) {
			if (tool instanceof AreaSketchTool)
				((AreaSketchTool) tool).getSketchGraphicsLayer().insertVertexAtEnd(mapPoint);
		}
	}

	public void startCollecting(){
		this.isCollecting = true;
	}
	
	public void stopCollecting(){
		this.isCollecting = false;
	}
	
	/**
	 * Method setDirty.
	 * @param isDirty boolean
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	/**
	 * Method isDirty.
	
	 * @return boolean */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Method getMapPoint.
	
	 * @return Point */
	public Point getMapPoint() {
		return mapPoint;
	}

	/**
	 * Method onClick.
	 * @param v View
	
	 * @see android.view.View$OnClickListener#onClick(View) */
	@Override
  public void onClick(View v) {
	}
}
