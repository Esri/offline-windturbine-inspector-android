package com.esri.android.samples.bladerunner;

import java.util.HashMap;
import java.util.List;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.samples.bladerunner.maptools.Area;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Polygon;
import com.esri.core.renderer.UniqueValue;
import com.esri.core.renderer.UniqueValueRenderer;

/**
 */
public class Helper {
	ArcGISFeatureLayer teamareasFeatureLayer;

	/**
	 * Method createBlocksFeatureLayer.
	 * @return ArcGISFeatureLayer
	 */
	public ArcGISFeatureLayer createBlocksFeatureLayer() {
		ArcGISFeatureLayer.Options layerOptions = new ArcGISFeatureLayer.Options();
		layerOptions.mode = ArcGISFeatureLayer.MODE.SNAPSHOT;

		ArcGISFeatureLayer blocksFeatureLayer = new ArcGISFeatureLayer(
				BladeRunnerApplication.blocksLayerDefinition, null,
				layerOptions);
		blocksFeatureLayer.setName(BladeRunnerApplication.blocksName);
		return blocksFeatureLayer;
	}

	/**
	 * Method createWindFarmZonesFeatureLayer.
	 * @return ArcGISFeatureLayer
	 */
	public ArcGISFeatureLayer createWindFarmZonesFeatureLayer() {
		ArcGISFeatureLayer.Options layerOptions = new ArcGISFeatureLayer.Options();
		layerOptions.mode = ArcGISFeatureLayer.MODE.SNAPSHOT;

		ArcGISFeatureLayer windFarmZonesFeatureLayer = new ArcGISFeatureLayer(
				BladeRunnerApplication.windFarmZonesLayerDefinition, null,
				layerOptions);
		windFarmZonesFeatureLayer
				.setName(BladeRunnerApplication.windFarmZonesName);
		return windFarmZonesFeatureLayer;
	}

	/**
	 * Method createTeamAreasFeatureLayer.
	 * @return ArcGISFeatureLayer
	 */
	public ArcGISFeatureLayer createTeamAreasFeatureLayer() {
		ArcGISFeatureLayer.Options layerOptions = new ArcGISFeatureLayer.Options();
		layerOptions.mode = ArcGISFeatureLayer.MODE.SNAPSHOT;

		ArcGISFeatureLayer teamareasFeatureLayer = new ArcGISFeatureLayer(
				BladeRunnerApplication.teamAreasLayerDefinition, null,
				layerOptions);
		teamareasFeatureLayer.setName(BladeRunnerApplication.teamAreasName);
		teamareasFeatureLayer.setOpacity(0.5f);
		this.teamareasFeatureLayer = teamareasFeatureLayer;
		return teamareasFeatureLayer;
	}

	/**
	 * Method createWindTurbinesFeatureLayer.
	 * @return ArcGISFeatureLayer
	 */
	public ArcGISFeatureLayer createWindTurbinesFeatureLayer() {
		ArcGISFeatureLayer.Options layerOptions = new ArcGISFeatureLayer.Options();
		layerOptions.mode = ArcGISFeatureLayer.MODE.SNAPSHOT;

		ArcGISFeatureLayer windTurbinesFeatureLayer = new ArcGISFeatureLayer(
				BladeRunnerApplication.windTurbineLayerDefinition, null,
				layerOptions);
		windTurbinesFeatureLayer
				.setName(BladeRunnerApplication.windTurbineName);
		return windTurbinesFeatureLayer;
	}

	/**
	 * Method getEnvelopeCenter.
	 * @param polygon Polygon
	 * @return Geometry
	 */
	public static Geometry getEnvelopeCenter(Polygon polygon) {
		Envelope envelope = new Envelope();
		polygon.queryEnvelope(envelope);
		return envelope.getCenter();
	}

	/**
	 * Method getUniqueValue.
	 * @param team String
	 * @return UniqueValue
	 */
	public UniqueValue getUniqueValue(String team) {
		UniqueValueRenderer uniqueValueRenderer = (UniqueValueRenderer) teamareasFeatureLayer
				.getRenderer();
		List<UniqueValue> values = uniqueValueRenderer.getUniqueValueInfos();

		for (UniqueValue uniqueValue : values) {
			if (team.toLowerCase().contains(
					uniqueValue.getLabel().toLowerCase())) {
				return uniqueValue;
			}
		}
		return null;
	}
	
	
	/**
	 * Method setAttributes.
	 * @param index Integer
	 * @param area Area
	 * @return HashMap<String,Object>
	 */
	public static HashMap<String, Object> setAttributes(Integer index, Area area) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(BladeRunnerApplication.teamAreasIDField, area.teamIndex);
		attributes.put(BladeRunnerApplication.teamAreasNameField, area.teamIndex);
		attributes.put(BladeRunnerApplication.teamAreasLabelField, area.getLabel());
		return attributes;
	}
	
	
	
	
}
