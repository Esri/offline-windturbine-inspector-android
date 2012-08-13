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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @version $Revision: 1.0 $
 */
public class AreasSpinnerAdapter extends BaseAdapter {
	private Context context;
	private Activity activity;
	private String[] values;
	private String hintText;
	private int spinnerResourceID = R.layout.field_spinner_item;
	private int dropdownResourceID = R.layout.field_spinner_dropdown_item;
	private boolean isSelected;

	private int index = -1;
	
	/**
	 * Constructor for AreasSpinnerAdapter.
	 * @param context Context
	 * @param values String[]
	 * @param hint String
	 */
	public AreasSpinnerAdapter(Context context, String[] values, String hint) {
		this.context = context;
		this.activity = (MapViewActivity) this.context;
		this.values = values;
		this.hintText = hint;
	}
	
	/**
	 * Method setSelected.
	 * @param isSelected boolean
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		index = -1;
	}
	
	/**
	 * Method getCount.
	
	
	 * @return int * @see android.widget.Adapter#getCount() */
	@Override
  public int getCount() {
		return values.length;
	}

	/**
	 * Method getItem.
	 * @param position int
	
	
	 * @return Object * @see android.widget.Adapter#getItem(int) */
	@Override
  public Object getItem(int position) {
		return values[position];
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
	 * Method getView.
	 * @param position int
	 * @param convertView View
	 * @param parent ViewGroup
	
	
	 * @return View * @see android.widget.Adapter#getView(int, View, ViewGroup) */
	@Override
  public View getView(int position, View convertView, ViewGroup parent) {
		ChildHolder holder = null;
		
		if (convertView != null && convertView.getTag() == null)
			convertView = null;
		
		final LayoutInflater inflater = activity.getLayoutInflater();
		
		if (convertView == null) {
			convertView = inflater.inflate(spinnerResourceID, null);
			holder = new ChildHolder();
			holder.text = (TextView) convertView.findViewById(R.id.spinnertext);
		} else
			holder = (ChildHolder) convertView.getTag();

		if (!isSelected)
			holder.text.setText(hintText);
		else {
			holder.text.setText(values[position]);
			notifyDataSetChanged();
		}
		
		convertView.setTag(holder);
		
		return convertView;
	}
	
  /**
   * Method getDropDownView.
   * @param position int
   * @param convertView View
   * @param parent ViewGroup
  
  
   * @return View * @see android.widget.SpinnerAdapter#getDropDownView(int, View, ViewGroup) */
  @Override
  public View getDropDownView(final int position, View convertView, ViewGroup parent) {
		final LayoutInflater inflater = activity.getLayoutInflater();
		
		convertView = inflater.inflate(dropdownResourceID, null);
		TextView textView = (TextView) convertView.findViewById(R.id.spinnerdropdowntext);
		
		if (position == 0)
			textView.setHeight(0);
		else {
			textView.setOnTouchListener(new OnTouchListener() {
				@Override
        public boolean onTouch(View view, MotionEvent event) {
					isSelected = true;
					notifyDataSetChanged();
					index = position;
					return false;
				}
			});
		}
		
		if (position == index)
			textView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.spinner_dropdown_item_checked));
		else
			textView.setBackgroundColor(context.getResources().getColor(R.color.light_app_background));
		
		textView.setText(values[position]);
		
		return convertView;
	}
	
	/**
	 * @author dan3488
	 * @version $Revision: 1.0 $
	 */
	class ChildHolder {
	    TextView text;
	}
}
