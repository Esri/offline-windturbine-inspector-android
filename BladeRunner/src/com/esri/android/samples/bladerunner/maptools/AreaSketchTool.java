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
import android.view.View;

import com.esri.android.map.MapView;
import com.esri.android.samples.bladerunner.R;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;

/**
 * 
 * @version $Revision: 1.0 $
 */

public class AreaSketchTool extends MapTool{
	
	private Context context;
	private MapView map;
	private SketchGraphicsLayer sketchGraphicsLayer;
	private String teamName;
	private SimpleFillSymbol areasFillSymbol;
	private int areasFillTransparency = 75;
	private Area sketchArea;
	private HashMap<Integer, Area> areas;

	/**
	 * Constructor for AreaSketchTool.
	 * @param view MapView
	 */
	public AreaSketchTool(MapView view) {
		super(view);
		this.context = view.getContext();
		this.map = view;
		
		sketchGraphicsLayer = new SketchGraphicsLayer(context);
		sketchGraphicsLayer.setGeometryType(Type.POLYGON);
		map.addLayer(sketchGraphicsLayer);
		
		int color = context.getResources().getColor(R.color.map_areas_color);
		areasFillSymbol = new SimpleFillSymbol(color);
		areasFillSymbol.setAlpha(areasFillTransparency);
		areasFillSymbol.setOutline(new SimpleLineSymbol(color, (int) context.getResources().getDimension(R.dimen.sketch_line_stroke)));
		
		this.areas = new HashMap<Integer, Area>();
	}
	
	
	@Override
  public void reset() {
		sketchGraphicsLayer.removeAll();
	}
	

	/**
	 * Method isValid.
	
	 * @return boolean */
	public boolean isValid() {
		return !(sketchArea == null || !sketchArea.isSelected);
	}

	/**
	 * Method onClick.
	 * @param view View
	
	 * @see android.view.View$OnClickListener#onClick(View) */
	@Override
  public void onClick(View view) {
	}

	/**
	 * Method onStatusChanged.
	 * @param succeeded boolean
	
	 * @see com.esri.android.samples.bladerunner.maptools.OnMapToolListener#onStatusChanged(boolean) */
	@Override
  public void onStatusChanged(boolean succeeded) {
		
	}
	
	/**
	 * Method getTeamName.
	
	 * @return String */
	public String getTeamName() {
		return teamName;
	}
	
	/**
	 * Method getArea.
	
	 * @return Area */
	public Area getArea() {
		return sketchArea;
	}
	
	/**
	 * Method setTeamName.
	 * @param name String
	 */
	public void setTeamName(String name) {
		if (this.teamName.equals(name))
			return;
		
		this.teamName = name;
		sketchArea.setTeam(this.teamName);
		
		if (!(areas == null || areas.isEmpty())) {
			for (Area area : areas.values()) {
				area.setTeam(this.teamName);
			}
			
			sketchGraphicsLayer.setSketchedAreas(areas);
		}
	}
	
	/**
	 * Method getSketchGraphicsLayer.
	
	 * @return SketchGraphicsLayer */
	public SketchGraphicsLayer getSketchGraphicsLayer() {
		return sketchGraphicsLayer;
	}
	
	/**
	 * Method done.
	 * @param cancelled boolean
	 */
	@Override
  public void done(boolean cancelled) {
		if (!cancelled) {
			sketchArea.setSelected(false);
			areas.put(sketchArea.index, sketchArea);
			sketchGraphicsLayer.setSketchedAreas(areas);
		} else {
			sketchGraphicsLayer.clear();
			sketchArea = null;
			areas = new HashMap<Integer, Area>();
			teamName = "";
		}
		
		super.done(cancelled);
	}
	
	/**
	 * Method editAreaAtIndex.
	 * @param index int
	 * @param teamIndex int
	 * @param team String
	 */
	public void editAreaAtIndex(int index, int teamIndex, String team) {
		this.teamName = team;
		
		if (areas != null && !areas.isEmpty()) {
			sketchArea = areas.get(index);
			sketchGraphicsLayer.setStarted(true);
			areas.remove(index);
			sketchGraphicsLayer.setSketchedAreas(areas);
		}
		
		if (sketchArea == null || !sketchArea.isValid()) {
			sketchArea = new Area(index);
			sketchArea.teamIndex = teamIndex;
			sketchGraphicsLayer.setStarted(false);
		}
		
		sketchArea.setTeam(teamName);
		sketchGraphicsLayer.setSketchArea(sketchArea);
	}
	
	/**
	 * Method removeAreaAtIndex.
	 * @param index int
	 */
	public void removeAreaAtIndex(int index) {
		sketchGraphicsLayer.removeGraphic(sketchArea.id);
		sketchGraphicsLayer.removeGraphic(sketchArea.labelID);
		int teamIndex = sketchArea.teamIndex;
		sketchArea = null;
		editAreaAtIndex(index, teamIndex, teamName);
	}
	
	/**
	 * Method getAreas.
	
	 * @return HashMap<Integer,Area> */
	public HashMap<Integer, Area> getAreas() {
		return areas;
	}
}
