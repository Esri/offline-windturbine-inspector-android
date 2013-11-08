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

import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.samples.bladerunner.R;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.symbol.TextSymbol.HorizontalAlignment;
import com.esri.core.symbol.TextSymbol.VerticalAlignment;

/**
 * 
 * @version $Revision: 1.0 $
 */

public class SketchGraphicsLayer extends GraphicsLayer {
	// private static final String TAG =
	// SketchGraphicsLayer.class.getSimpleName();

	private Resources resources;

	private Type geometryType;

	private HashMap<Integer, Area> sketchedAreas = new HashMap<Integer, Area>();
	private Area sketchArea;
	private boolean isStarted;

	private TextSymbol labelTextSymbol;
	private SimpleLineSymbol lineSymbol;
	private SimpleFillSymbol fillSymbol;
	private SimpleFillSymbol selectedFillSymbol;

	private int fillTransparency = 50;
	private int selectedFillTransparency = 98;

	private OnSketchGraphicLayerListener onSketchGraphicLayerListener;

	/**
	 * Constructor for SketchGraphicsLayer.
	 * 
	 * @param context
	 *            Context
	 */
	public SketchGraphicsLayer(Context context) {
		super();
		this.resources = context.getResources();

		setTextSymbols();

		setLineSymbols();

		setFillSymbols();
	}

	public void clear() {
		this.removeAll();
		sketchedAreas = new HashMap<Integer, Area>();
		isStarted = false;
		sketchArea = null;
	}

	/**
	 * Method getSketchArea.
	 * 
	 * @return Area
	 */
	public Area getSketchArea() {
		return sketchArea;
	}

	/**
	 * Method setSketchArea.
	 * 
	 * @param sketchArea
	 *            Area
	 */
	public void setSketchArea(Area sketchArea) {
		this.sketchArea = sketchArea;
		drawSketch();
	}

	/**
	 * Method setStarted.
	 * 
	 * @param isStarted
	 *            boolean
	 */
	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	/**
	 * Method setSketchedAreas.
	 * 
	 * @param areas
	 *            HashMap<Integer,Area>
	 */
	public void setSketchedAreas(HashMap<Integer, Area> areas) {
		this.sketchedAreas = areas;
		drawSketch();
	}

	private void setLineSymbols() {
		int color = resources.getColor(R.color.area_color);
		lineSymbol = new SimpleLineSymbol(color,
				(int) resources.getDimension(R.dimen.sketch_line_stroke));
	}

	private void setFillSymbols() {
		int color = resources.getColor(R.color.area_color);
		fillSymbol = new SimpleFillSymbol(color);
		fillSymbol.setAlpha(fillTransparency);
		fillSymbol.setOutline(lineSymbol);

		selectedFillSymbol = new SimpleFillSymbol(color);
		selectedFillSymbol.setAlpha(selectedFillTransparency);
		selectedFillSymbol.setOutline(lineSymbol);
	}

	private void setTextSymbols() {
		labelTextSymbol = new TextSymbol(30, "",
				resources.getColor(R.color.white_textcolor));
		labelTextSymbol.setHorizontalAlignment(HorizontalAlignment.CENTER);
		labelTextSymbol.setVerticalAlignment(VerticalAlignment.MIDDLE);
	}

	/**
	 * Method setSketchGraphicLayerListener.
	 * 
	 * @param listener
	 *            OnSketchGraphicLayerListener
	 */
	public void setSketchGraphicLayerListener(
			OnSketchGraphicLayerListener listener) {
		this.onSketchGraphicLayerListener = listener;
	}

	/**
	 * Method setOnMeasureChanged.
	 * 
	 * @param distance
	 *            double
	 * @param angle
	 *            double
	 */
	public void setOnMeasureChanged(double distance, double angle) {
		if (onSketchGraphicLayerListener != null)
			onSketchGraphicLayerListener.onMeasureChanged(distance, angle);
	}

	/**
	 * Method setGeometryType.
	 * 
	 * @param geometryType
	 *            Type
	 */
	public void setGeometryType(Type geometryType) {
		this.geometryType = geometryType;
	}

	/**
	 * Method getFillTransparency.
	 * 
	 * @return int
	 */
	public int getFillTransparency() {
		return fillTransparency;
	}

	/**
	 * Method setFillTransparency.
	 * 
	 * @param fillTransparency
	 *            int
	 */
	public void setFillTransparency(int fillTransparency) {
		this.fillTransparency = fillTransparency;
	}

	/**
	 * Method insertVertexAtEnd.
	 * 
	 * @param point
	 *            Point
	 */
	public void insertVertexAtEnd(Point point) {
		sketch(point);
	}

	/**
	 * Method sketch.
	 * 
	 * @param point
	 *            Point
	 */
	private void sketch(Point point) {
		if (point == null || point.isEmpty())
			return;

		switch (geometryType) {
		case POLYGON:
			if (!isStarted) {
				sketchArea.area.startPath(point);
				isStarted = true;
			} else
				sketchArea.area.lineTo(point);
			break;
		default:
			break;
		}

		drawSketch();
	}

	private void drawSketch() {
		switch (geometryType) {
		case POLYGON:
			if (sketchArea.isValid()) {
				if (sketchArea.id == -1)
					sketchArea.id = addGraphic(new Graphic(sketchArea.area,
							selectedFillSymbol));
				else {
					updateGraphic(sketchArea.id, sketchArea.area);
					updateGraphic(sketchArea.id, selectedFillSymbol);
				}

				drawSketchAreaLabel(sketchArea);
			}

			for (Area area : sketchedAreas.values()) {
				if (area.id == -1) {
					area.id = addGraphic(new Graphic(area.area, fillSymbol));
				} else {
					updateGraphic(area.id, area.area);
					updateGraphic(area.id, fillSymbol);
				}

				drawSketchAreaLabel(area);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Method drawSketchAreaLabel.
	 * 
	 * @param area
	 *            Area
	 */
	private void drawSketchAreaLabel(Area area) {
		Envelope envelope = new Envelope();
		area.area.queryEnvelope(envelope);
		Point point = envelope.getCenter();
		labelTextSymbol.setText(area.getLabel());

		if (area.labelID == -1) {
			area.labelID = addGraphic(new Graphic(point, labelTextSymbol));
		} else {
			updateGraphic(area.labelID, point);
			updateGraphic(area.labelID, labelTextSymbol);
		}
	}

	/**
	 * Method getGeometry.
	 * 
	 * @return Geometry
	 */
	public Geometry getGeometry() {
		return sketchArea.area;
	}
}
