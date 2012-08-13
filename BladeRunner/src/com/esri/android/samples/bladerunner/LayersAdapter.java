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

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.esri.android.map.Layer;

/**
 * @version $Revision: 1.0 $
 */
class LayersAdapter extends ArrayAdapter<Layer>{
	private Context context;
	private int viewResourceID;
	private ArrayList<Layer> layers;
	
	/**
	 * Constructor for LayersAdapter.
	 * @param context Context
	 * @param layers ArrayList<Layer>
	 * @param viewResourceID int
	 */
	public LayersAdapter(Context context, ArrayList<Layer> layers, int viewResourceID) {
		super(context, viewResourceID);
		this.context = context;
		this.layers = layers;
		this.viewResourceID = viewResourceID;
	}
	
	/**
	 * Method getView.
	 * @param position int
	 * @param convertView View
	 * @param parent ViewGroup
	
	
	 * @return View * @see android.widget.Adapter#getView(int, View, ViewGroup) */
	@Override
  public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		LayoutInflater inflater = ((MapViewActivity) context).getLayoutInflater();
		final Layer layer = layers.get(position);
		
		if (row == null){
			row = inflater.inflate(viewResourceID, null);
			
			final LayerHolder holder = new LayerHolder();
			
			holder.name = (TextView) row.findViewById(R.id.layername);
			holder.name.setOnClickListener(new OnClickListener() {
				@Override
        public void onClick(View view) {
					holder.visible.performClick();
				}
			});
			
			holder.visible = (CheckBox) row.findViewById(R.id.layervisibility);
			holder.visible.setOnClickListener(new OnClickListener() {
				@Override
        public void onClick(View view) {
					 int position =  (Integer) view.getTag(); 
					 boolean isChecked = ((CheckBox) view).isChecked();
	                 layers.get(position).setVisible(isChecked);
				}
			});
			
			holder.opacity = (SeekBar) row.findViewById(R.id.opacity);
			holder.opacity.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					float decimalProgress = 1 - ((float) progress / 100);
					layer.setOpacity(decimalProgress);
				}
			});
			
			row.setTag(holder);
			holder.name.setTag(position);
			holder.visible.setTag(position);
			holder.opacity.setTag(position);
		} else {
			row = convertView;
			((LayerHolder) row.getTag()).visible.setTag(position);
		}
		
		LayerHolder holder = (LayerHolder) row.getTag();
		
		holder.name.setText(layer.getName());
		
        holder.visible.setChecked(layer.isVisible());
        
        float opacity = layer.getOpacity();
        int progress = (int) (100 * (1 - opacity));
        holder.opacity.setProgress(progress);

        return row;
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
	
	
	 * @return Layer * @see android.widget.Adapter#getItem(int) */
	@Override
  public Layer getItem(int position) {
		return layers.get(position);
	}
	
	/**
	 * Method getCount.
	
	
	 * @return int * @see android.widget.Adapter#getCount() */
	@Override
  public int getCount() {
		return layers.size();
	}
	
	/**
	 * @author dan3488
	 * @version $Revision: 1.0 $
	 */
	class LayerHolder {
		CheckBox visible;
	    TextView name;
	    SeekBar opacity;
	}
}