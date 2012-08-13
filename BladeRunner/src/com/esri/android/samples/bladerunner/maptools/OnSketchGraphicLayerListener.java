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

/**
 * 
 * @version $Revision: 1.0 $
 */
public interface OnSketchGraphicLayerListener {
	/**
	 * Method onMeasureChanged.
	 * @param distance double
	 * @param angle double
	 */
	public void onMeasureChanged(double distance, double angle);
}

