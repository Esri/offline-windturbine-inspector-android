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
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.esri.android.map.Callout;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.samples.bladerunner.BladeRunnerApplication;
import com.esri.android.samples.bladerunner.MapViewActivity;
import com.esri.android.samples.bladerunner.R;
import com.esri.android.samples.bladerunner.Utility;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.CodedValueDomain;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;

/**
 * 
 * @version $Revision: 1.0 $
 */
public class LocationMapTool extends MapTool {
//	private static final String TAG = LocationMapTool.class.getSimpleName();
	
	Activity activity;
	private Context context;
	private Resources resources;
	private MapView map;
	private ArcGISFeatureLayer windTurbinesFeatureLayer;
	
	// Callout
	static ViewGroup calloutContent;
	public Callout calloutView;

	private Graphic selectedGraphic;
	
	private int yOffset;
	protected ArrayAdapter<String> turbineTypeAdapter;
	private View turbineTypeView;
	private ListView turbineTypeListView;
	private Field domainField;
	private Map<String, String> codedValueDomain;
	
	/**
	 * Constructor for LocationMapTool.
	 * @param view MapView
	 */
	public LocationMapTool(MapView view) {
		super(view);
		
		this.activity = (Activity) view.getContext();
		this.map = view;
		this.context = map.getContext();
		this.resources = map.getResources();
		
		this.windTurbinesFeatureLayer = ((MapViewActivity) activity).windTurbinesFeatureLayer;
		
		domainField = Utility.getCodedValueDomainField(windTurbinesFeatureLayer);
		codedValueDomain = ((CodedValueDomain) domainField.getDomain()).getCodedValues();
		
		setCallout();
		
		setTurbineTypeList();
	}
	
	/**
	 * Method setWindTurbineSelection.
	 * @param id int
	 * @param graphic Graphic
	 */
	public void setWindTurbineSelection(int id, Graphic graphic) {
		this.selectedGraphic = graphic;
		
		updateCalloutContent(selectedGraphic);
		
		showCallout((Point) graphic.getGeometry());
	}
	
	private void setTurbineTypeList() {
		turbineTypeView = activity.getLayoutInflater().inflate(R.layout.turbinetypelist, null);
		
		turbineTypeAdapter = new ArrayAdapter<String>(context, R.layout.listitem, codedValueDomain.values().toArray(new String[0]));
		turbineTypeListView = (ListView) turbineTypeView.findViewById(R.id.turbinetypelistview);
		turbineTypeListView.setAdapter(turbineTypeAdapter);
		
		turbineTypeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String value =  parent.getItemAtPosition(position).toString();
				String key = getCodedValue(value);
				
				updateGraphic(key);
				activity.dismissDialog(MapViewActivity.DIALOG_TURBINETYPES);
				((TextView) calloutContent.findViewById(R.id.turbinetypevalue)).setText(value);
				((ToggleButton) calloutContent.findViewById(R.id.editattribbutton)).setChecked(false);
			}
		});
	}
	
	/**
	 * Method getCodedValue.
	 * @param value String
	
	 * @return String */
	public String getCodedValue(String value) {
		for (Map.Entry<String, String> entry : codedValueDomain.entrySet()) {
		    if (value.equals(entry.getValue()))
		    	return entry.getKey();
		}
		
		return null;
	}
	
  /**
   * Method updateGraphic.
   * @param value String
   */
  void updateGraphic(String value) {
		HashMap<String, Object> attributes = (HashMap<String, Object>) selectedGraphic.getAttributes();
		
		attributes.put(BladeRunnerApplication.windTurbineTypeField, value);
		
		int uid = selectedGraphic.getUid();
		
		final Graphic graphic = new Graphic(selectedGraphic.getGeometry(), selectedGraphic.getSymbol(), attributes, selectedGraphic.getInfoTemplate());
		
		windTurbinesFeatureLayer.updateGraphic(uid, graphic);
		Utility.printAttibuteValues(selectedGraphic);
		this.done(false);
	}
	
	private void setCallout() {
		calloutView = map.getCallout();
		calloutView.setStyle(R.xml.calloutstyle);
		float widthPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, resources.getDimension(R.dimen.width300), resources.getDisplayMetrics());
		float heightPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, resources.getDimension(R.dimen.height200), resources.getDisplayMetrics());
		calloutView.setMaxWidth((int) widthPixels);
		calloutView.setMaxHeight((int) heightPixels);
		
		yOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, resources.getDimension(R.dimen.y_offset10), resources.getDisplayMetrics());
		
		calloutContent = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.callout_layout, null);
		calloutView.setContent(calloutContent);
		
		ToggleButton editAttribButton = (ToggleButton) calloutContent.findViewById(R.id.editattribbutton);
		editAttribButton.setOnClickListener(new OnClickListener() {
			@Override
      public void onClick(View view) {
				activity.showDialog(MapViewActivity.DIALOG_TURBINETYPES);
			}
		});
	}
	
//	public void start() {
//	}
//
//	public void stop() {
//	}
//	
//	public void reset() {
//		super.reset();
//	}
	
	/**
	 * Method identify.
	 * @param point MotionEvent
	 */
	public void identify(MotionEvent point) {
		hideCallout();

		if (windTurbinesFeatureLayer == null)
			return;
			
		findWindTurbines(point);
	}
	
	/**
	 * Method findWindTurbines.
	 * @param point MotionEvent
	 */
	private void findWindTurbines(MotionEvent point) {
		int[] windTurbines = windTurbinesFeatureLayer.getGraphicIDs(point.getX(), point.getY(), BladeRunnerApplication.NEAREST_TOLERANCE);
		ToggleButton resultsButton = (ToggleButton) ((MapViewActivity) activity).findViewById(R.id.resultsbutton);
		int count = windTurbines.length;
		
		Utility.getResultsString(resultsButton, count);
		
		if (count == 0)
			return;
		
		selectedGraphic = windTurbinesFeatureLayer.getGraphic(windTurbines[0]);
		
		updateCalloutContent(selectedGraphic);
		
		showCallout((Point) selectedGraphic.getGeometry());
	}
	
	/**
	 * Method updateCalloutContent.
	 * @param graphic Graphic
	 */
	private void updateCalloutContent(Graphic graphic) {
		Object value = graphic.getAttributeValue(BladeRunnerApplication.windTurbineIDField);
		((TextView) calloutContent.findViewById(R.id.turbineidvalue)).setText((value == null) ? resources.getString(R.string.n_a) : value.toString());
		
		Object code = graphic.getAttributeValue(domainField.getName());
		String value2 = codedValueDomain.get(code);
		((TextView) calloutContent.findViewById(R.id.turbinetypevalue)).setText((value == null) ? resources.getString(R.string.n_a) : value2);
	}
	
	/**
	 * Method showCallout.
	 * @param graphicPoint Point
	 */
	private void showCallout(Point graphicPoint) {
		if (graphicPoint == null || graphicPoint.isEmpty())
			return;
		
		calloutView.setCoordinates(graphicPoint);
		calloutView.setOffset(0, yOffset);
		calloutView.show();
	}
	
	/**
	 * Method applyEditsCallBackListener.
	
	 * @return CallbackListener<FeatureEditResult[][]> */
	CallbackListener<FeatureEditResult[][]> applyEditsCallBackListener() {
		return new CallbackListener<FeatureEditResult[][]>() {	
			@Override
      public void onCallback(FeatureEditResult[][] graphics) {
				int count = graphics.length;
				for (int i = 0; i < count; i ++){
					if (i != 2)
						continue;
					
					onStatusChanged(true);
				}
			}
			
			@Override
      public void onError(Throwable error) {
				error.printStackTrace();
				onStatusChanged(false);
			}
		};
	}

	/**
	 * Method prepareTurbineTypesDialog.
	 * @param dialog Dialog
	 */
	public static void prepareTurbineTypesDialog(Dialog dialog) {
		Window window = dialog.getWindow();
		window.setLayout(400, 500);
	}
	
	/**
	 * Method createTurbineTypesDialog.
	
	 * @return Dialog */
	public Dialog createTurbineTypesDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setTitle(R.string.edit_dialog_title);
		builder.setIcon(R.drawable.propeller);
		builder.setView(turbineTypeView);
		final Dialog turbineTypesDialog = builder.create();
		
		turbineTypesDialog.setOnKeyListener(new OnKeyListener() {
			@Override
      public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (event.getAction()!= KeyEvent.ACTION_DOWN)
					return true;
				
				if (keyCode == KeyEvent.KEYCODE_BACK)
					turbineTypesDialog.dismiss();
				
				return false;
			}
		});
		
        return turbineTypesDialog;
	}
	
	/**
	 * Method onClick.
	 * @param view View
	
	 * @see android.view.View$OnClickListener#onClick(View) */
	@Override
  public void onClick(View view) {
		hideCallout();
		
		this.done(false);
	}
	
	private void hideCallout(){
		if (!map.getCallout().isShowing())
			return;
		
		map.getCallout().hide();
	}
	
	/**
	 * Method onStatusChanged.
	 * @param succeeded boolean
	
	 * @see com.esri.android.samples.bladerunner.maptools.OnMapToolListener#onStatusChanged(boolean) */
	@Override
  public void onStatusChanged(boolean succeeded) {
	}

	public void onBackPressed() {
		activity.removeDialog(MapViewActivity.DIALOG_TURBINETYPES);
	}
}
