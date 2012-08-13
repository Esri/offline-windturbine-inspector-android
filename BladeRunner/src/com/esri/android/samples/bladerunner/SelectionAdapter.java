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

import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.esri.core.map.CodedValueDomain;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;

/**
 * @version $Revision: 1.0 $
 */
public class SelectionAdapter extends ArrayAdapter<Graphic> {
	private Context context;
	private Resources resources;
	private int resourceID;
	private Graphic[] graphics;
	private CodedValueDomain codedValueDomain;
	private Field domainField;
	
	/**
	 * Constructor for SelectionAdapter.
	 * @param context Context
	 * @param resourceID int
	 * @param graphics Graphic[]
	 * @param domainField Field
	 */
	public SelectionAdapter(Context context, int resourceID, Graphic[] graphics, Field domainField) {
		super(context, resourceID);
		this.context = context;
		this.resources = context.getResources();
		this.resourceID = resourceID;
		this.graphics = graphics;
		this.domainField = domainField;
		this.codedValueDomain = (CodedValueDomain) domainField.getDomain();
	}
	
	/**
	 * Method getView.
	 * @param position int
	 * @param convertView View
	 * @param parent ViewGroup
	
	
	 * @return View * @see android.widget.Adapter#getView(int, View, ViewGroup) */
	@Override
  public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((MapViewActivity) context).getLayoutInflater();
		Graphic graphic = graphics[position];
		ChildHolder holder = null;
		
		if (convertView != null && convertView.getTag() == null)
			convertView = null;
		
		if (convertView == null){
			convertView = inflater.inflate(resourceID, null);
			holder = new ChildHolder();
			holder.id = (TextView) convertView.findViewById(R.id.idvalue);
			holder.type = (TextView) convertView.findViewById(R.id.typevalue);
			holder.study = (TextView) convertView.findViewById(R.id.studyvalue);
		}
		else
			holder = (ChildHolder) convertView.getTag();
		
		Object value = graphic.getAttributeValue(BladeRunnerApplication.windTurbineIDField);
		holder.id.setText(((value == null) ? resources.getString(R.string.n_a) : value.toString()));
		
		value = graphic.getAttributeValue(domainField.getName());
		holder.type.setText((value == null) ? resources.getString(R.string.n_a) : getCodedValue(value).toString());
		
		value = graphic.getAttributeValue("STUDY").toString();
		holder.study.setText(((value == null) ? resources.getString(R.string.n_a) : value.toString()));
		
		convertView.setTag(holder);
		
		return convertView;
	}

	/**
	 * Method getItemId.
	 * @param position int
	
	
	 * @return long * @see android.widget.Adapter#getItemId(int) */
	@Override
  public long getItemId(int position) {
		return position;
	}
	
	/**
	 * Method getItem.
	 * @param position int
	
	
	 * @return Graphic * @see android.widget.Adapter#getItem(int) */
	@Override
  public Graphic getItem(int position) {
		return graphics[position];
	}
	
	/**
	 * Method getCodedValue.
	 * @param value Object
	
	 * @return Object */
	public Object getCodedValue(Object value) {
		Map<String, String> values = codedValueDomain.getCodedValues();
		for (Entry<String, String> entry : values.entrySet()) {
			if (entry.getKey().equals(value.toString()))
				return entry.getValue();
		}
		
		return null;
	}
	
	/**
	 * Method getCount.
	
	
	 * @return int * @see android.widget.Adapter#getCount() */
	@Override
  public int getCount() {
		return graphics.length;
	}
	
	/**
	 * @author dan3488
	 * @version $Revision: 1.0 $
	 */
	class ChildHolder {
	    TextView id;
	    TextView type;
	    TextView study;
	}
}
