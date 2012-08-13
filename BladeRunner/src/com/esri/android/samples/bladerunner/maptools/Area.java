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

import com.esri.android.samples.bladerunner.BladeRunnerApplication;
import com.esri.core.geometry.Polygon;

/**
 * 
 * @version $Revision: 1.0 $
 */
public class Area {
	public int index;
	public int teamIndex;
	public Polygon area;
	public String team;
	private String label;
	public int id = -1;
	public int labelID = -1;
	public boolean isSelected;

	/**
	 * Constructor for Area.
	 * @param index int
	 */
	public Area(int index) {
		this.index = index;
		this.area = new Polygon();
		this.isSelected = true;
	}
	
	/**
	 * Method isSelected.
	
	 * @return boolean */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Method setSelected.
	 * @param selected boolean
	 */
	public void setSelected(boolean selected) {
		this.isSelected = selected;
	}

	/**
	 * Method isValid.
	
	 * @return boolean */
	public boolean isValid() {
		return (!(area == null || area.isEmpty()));
	}
	
	/**
	 * Method getLabel.
	
	 * @return String */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Method getTeam.
	
	 * @return String */
	public String getTeam() {
		return team;
	}
	
	/**
	 * Method setTeam.
	 * @param team String
	 */
	public void setTeam(String team) {
		this.team = team;
		this.label = getAreaName(team, Integer.toString(index)).toString();
	}
	
	/**
	 * Method getAreaName.
	 * @param team String
	 * @param day String
	
	 * @return StringBuilder */
	private static StringBuilder getAreaName(String team, String day) {
		StringBuilder sb = new StringBuilder();
		sb.append(team);
		sb.append(BladeRunnerApplication.LINE_SEPARATOR);
		sb.append(day);
		return sb;
	}
}
