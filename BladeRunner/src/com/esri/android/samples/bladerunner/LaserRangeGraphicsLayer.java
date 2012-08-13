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
import android.content.res.Resources;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.symbol.TextSymbol.HorizontalAlignment;
import com.esri.core.symbol.TextSymbol.VerticalAlignment;

/**
 * @version $Revision: 1.0 $
 */
public class LaserRangeGraphicsLayer extends GraphicsLayer{
//	private static final String TAG = LaserRangeGraphicsLayer.class.getSimpleName();
	private Resources resources;
	
	// Sketches
	private Polyline polyline;
	
	private TextSymbol distanceTextSymbol;
	private TextSymbol backDistanceTextSymbol;
	private TextSymbol angleTextSymbol;
	private TextSymbol backAngleTextSymbol;
	private SimpleLineSymbol simpleLineSymbol;
	private SimpleLineSymbol backgroundSimpleLineSymbol;
	
	private PictureMarkerSymbol pinMarkerSymbol;

	private double distance = -1;
	private double angle= -1;
	private int lineID = -1;
	private int backgroundLineID = -1;
	private int angleID = -1;
	private int backgroundAngleID = -1;
	private int distanceID = -1;
	private int backgroundDistanceID = -1;
	private int pinID = -1;
	
	private boolean showDistance;
	private boolean showAngle;
	private boolean showPin;
	
	private String mapUnitsAbbrv;
	
	private Point startPoint;
	private Point endPoint;
	
	/**
	 * Constructor for LaserRangeGraphicsLayer.
	 * @param context Context
	 */
	public LaserRangeGraphicsLayer(Context context) {
		super();
		this.resources = context.getResources();
		
		setTextSymbols();

		setMarkerSymbols();
				
		setLineSymbols();
		
		this.polyline = new Polyline();
	}
	
	/**
	 * Method setMapUnits.
	 * @param unitsAbbrv String
	 */
	public void setMapUnits(String unitsAbbrv) {
		this.mapUnitsAbbrv = unitsAbbrv;
	}
	
	/**
	 * Method drawOffset.
	 * @param startPoint Point
	 * @param endPoint Point
	 * @param showPin boolean
	 */
	public void drawOffset(Point startPoint, Point endPoint, boolean showPin) {
		polyline = new Polyline();
		
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.showPin = showPin;
		
		if ((startPoint == null || startPoint.isEmpty()) && (endPoint == null || endPoint.isEmpty()))
			return;
		
		sketch();
	}

	private void setLineSymbols() {
		this.simpleLineSymbol = new SimpleLineSymbol(resources.getColor(R.color.laser_line_color), 
					((int) resources.getDimension(R.dimen.laser_line_stroke)));
		
		this.backgroundSimpleLineSymbol = new SimpleLineSymbol(resources.getColor(R.color.laser_background_line_color), 
				((int) resources.getDimension(R.dimen.laser_background_line_stroke)));
	}

	private void setMarkerSymbols() {
		this.pinMarkerSymbol = new PictureMarkerSymbol(resources.getDrawable(R.drawable.pin));
		pinMarkerSymbol.setOffsetX(-15);
		pinMarkerSymbol.setOffsetY(15);
	}

	private void setTextSymbols() {
		this.distanceTextSymbol = new TextSymbol(resources.getDimension(R.dimen.text_size_xsmall), 
				"", resources.getColor(R.color.maptext_textcolor));
		distanceTextSymbol.setHorizontalAlignment(HorizontalAlignment.CENTER);
		distanceTextSymbol.setVerticalAlignment(VerticalAlignment.BOTTOM);
		
		this.backDistanceTextSymbol = new TextSymbol(resources.getDimension(R.dimen.text_size_xsmall), 
				"", resources.getColor(R.color.laser_line_color));
		backDistanceTextSymbol.setHorizontalAlignment(HorizontalAlignment.CENTER);
		backDistanceTextSymbol.setVerticalAlignment(VerticalAlignment.BOTTOM);
		
		this.angleTextSymbol = new TextSymbol(resources.getDimension(R.dimen.text_size_xsmall), 
				"", resources.getColor(R.color.maptext_textcolor));
		angleTextSymbol.setHorizontalAlignment(HorizontalAlignment.CENTER);
		angleTextSymbol.setVerticalAlignment(VerticalAlignment.MIDDLE);
		
		backAngleTextSymbol = new TextSymbol(resources.getDimension(R.dimen.text_size_xsmall), 
				"", resources.getColor(R.color.laser_line_color));
		backAngleTextSymbol.setHorizontalAlignment(HorizontalAlignment.CENTER);
		backAngleTextSymbol.setVerticalAlignment(VerticalAlignment.MIDDLE);
	}
	
	/**
	 * Method showDistance.
	 * @param showDistance boolean
	 */
	public void showDistance(boolean showDistance) {
		this.showDistance = showDistance;
	}
	
	/**
	 * Method showAngle.
	 * @param showAngle boolean
	 */
	public void showAngle(boolean showAngle) {
		this.showAngle = showAngle;
	}
	
	/**
	 * Method showPin.
	 * @param showPin boolean
	 */
	public void showPin(boolean showPin) {
		this.showPin = showPin;
	}
	
	/**
	 * Method getDistance.
	
	 * @return double */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * Method getAngle.
	
	 * @return double */
	public double getAngle() {
		return angle;
	}
	
	public void clear() {
		removeAll();
		polyline = new Polyline();
	}

	private void sketch(){
		polyline.startPath(startPoint);
		polyline.lineTo(endPoint);

		drawSketch();
	}
	
	private void drawSketch(){
		if (lineID == -1) {
			lineID = addGraphic(new Graphic(polyline, backgroundSimpleLineSymbol));
			backgroundLineID = addGraphic(new Graphic(polyline, simpleLineSymbol));
		} else {
			updateGraphic(lineID, polyline);
			updateGraphic(backgroundLineID, polyline);
		}
		
		drawPin();
		
		drawAngleText();
		
		drawDistanceText();
	}

	private void drawPin() {
		if (!showPin)
			return;
		
		if (pinID == -1)
			pinID = addGraphic(new Graphic(endPoint, pinMarkerSymbol));
		else
			updateGraphic(pinID, endPoint);
	}

	private void drawDistanceText() {
		if (!showDistance)
			return;
		
		distance = GeometryEngine.distance(startPoint, endPoint, getSpatialReference());
		
		StringBuilder sb = new StringBuilder();
		sb.append(Utility.outNumberFormat.format(distance));
		sb.append(" ");
		sb.append(mapUnitsAbbrv);
		
		Point midPoint = getMidPoint();
		
		backDistanceTextSymbol.setText(sb.toString());
		distanceTextSymbol.setText(sb.toString());
		
		if (distanceID == -1) {
			backgroundDistanceID = addGraphic(new Graphic(midPoint, backDistanceTextSymbol));
			distanceID = addGraphic(new Graphic(midPoint, distanceTextSymbol));
		} else {
			updateGraphic(backgroundDistanceID, midPoint);
			updateGraphic(distanceID, midPoint);
		}
	}
	
	/**
	 * Method getMidPoint.
	
	 * @return Point */
	private Point getMidPoint(){
		return new Point((startPoint.getX() + endPoint.getX()) / 2, (startPoint.getY() + endPoint.getY()) / 2);
	}

	private void drawAngleText() {
		if (!showAngle)
			return;
		
		VerticalAlignment angleVerticalAlign = VerticalAlignment.TOP;
		VerticalAlignment distanceVerticalAlign = VerticalAlignment.TOP;
		
		double textAngle = Utility.getTextAngle(angle);
		
		if(angle <= 90) {
			angleVerticalAlign = VerticalAlignment.TOP;
			distanceVerticalAlign = VerticalAlignment.BOTTOM;
		} else if (angle > 90 && angle <= 180) {
			angleVerticalAlign = VerticalAlignment.BOTTOM;
			distanceVerticalAlign = VerticalAlignment.TOP;
		} else if (angle > 270) {
			angleVerticalAlign = VerticalAlignment.TOP;
			distanceVerticalAlign = VerticalAlignment.BOTTOM;
		} else {
			angleVerticalAlign = VerticalAlignment.BOTTOM;
			distanceVerticalAlign = VerticalAlignment.TOP;
		}
		
		backAngleTextSymbol.setVerticalAlignment(angleVerticalAlign);
		angleTextSymbol.setVerticalAlignment(angleVerticalAlign);
		
		String formattedAngle = Utility.formatAngleWithMinutes(angle);
		backAngleTextSymbol.setText(formattedAngle);
		angleTextSymbol.setText(formattedAngle);
		
		distanceTextSymbol.setAngle((float) textAngle);
		distanceTextSymbol.setVerticalAlignment(distanceVerticalAlign);
		backDistanceTextSymbol.setAngle((float) textAngle);
		backDistanceTextSymbol.setVerticalAlignment(distanceVerticalAlign);
		
		if (angleID == -1) {
			backgroundAngleID = addGraphic(new Graphic(startPoint, backAngleTextSymbol));
			angleID = addGraphic(new Graphic(startPoint, angleTextSymbol));
		} else {
			updateGraphic(backgroundAngleID, startPoint);
			updateGraphic(angleID, startPoint);
		}
	}
}
