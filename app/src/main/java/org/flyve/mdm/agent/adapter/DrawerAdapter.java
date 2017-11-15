package org.flyve.mdm.agent.adapter;

/*
 *   Copyright (C) 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @date      15/8/17
 * @copyright Copyright (C) 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.flyve.mdm.agent.R;
import java.util.HashMap;
import java.util.List;

public class DrawerAdapter extends BaseAdapter {

	private List<HashMap<String, String>> data;
	private LayoutInflater inflater = null;

	public DrawerAdapter(Activity activity, List<HashMap<String, String>> data) {
		this.data = data;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Get count of the data
	 * @return int the data size 
	 */
	@Override
	public int getCount() {
		return this.data.size();
	}

	/**
	 * Get the data item associated with the specified position
     * @param position of the item whose data we want
	 * @return Object the data at the specified position
 	 */
	@Override
	public Object getItem(int position) {
		return position;
	}

	/**
	 * Get the row ID associated with the specified position
	 * @param position of the item whose row ID we want
	 * @return long the ID of the item at the specified position
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Get a view that displays the data at the specified position
	 * @param position of the item within the adapter's data set of the item whose view we want
	 * @param convertView the old view to reuse, if possible
	 * @param parent the parent that this view will eventually be attached to
	 * @return View a view corresponding to the data at the specified position
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HashMap<String, String> hashdata;
		hashdata = this.data.get(position);
		
		View vi = inflater.inflate(R.layout.list_item_drawer, null);

		View viewSeparator = vi.findViewById(R.id.viewSeparator);
		if(hashdata.containsKey("separator") && "true".equalsIgnoreCase(hashdata.get("separator"))) {
			viewSeparator.setVisibility(View.VISIBLE);
		} else {
			viewSeparator.setVisibility(View.GONE);
		}

		TextView txtTitle = (TextView)vi.findViewById(R.id.txtTitle);
        ImageView img = (ImageView) vi.findViewById(R.id.img);

		Context context = img.getContext();
		int iddw = context.getResources().getIdentifier(hashdata.get("img"), "drawable", context.getPackageName());
		img.setImageResource(iddw);

		String title = hashdata.get("name");
		txtTitle.setText(title);

		return vi;
	}
}
