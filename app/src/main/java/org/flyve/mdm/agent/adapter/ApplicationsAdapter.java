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
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.room.entity.Application;
import org.flyve.mdm.agent.utils.FlyveLog;

public class ApplicationsAdapter extends BaseAdapter {

	private Application data[];
	private LayoutInflater inflater = null;

	public ApplicationsAdapter(Activity activity, Application data[]) {
		FlyveLog.d(activity.getLocalClassName());

		this.data = data;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Get how many items are in the data
	 * @return int the data size
	 */
	@Override
	public int getCount() {
		return data.length;
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
	 * Get a View that displays the data at the specified position
	 * @param position of the item within the adapter's data set of the item whose View we want
	 * @param convertView the old View to reuse, if possible
	 * @param parent the parent that this View will eventually be attached to
	 * @return View a View corresponding to the data at the specified position
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Application app = data[position];

		View vi = inflater.inflate(R.layout.list_item_log, null);

		TextView txtId = (TextView) vi.findViewById(R.id.txtId);
		txtId.setText(app.appId);

		TextView txtAppName = (TextView) vi.findViewById(R.id.txtAppName);
		txtAppName.setText(app.appName);

		TextView txtPackageName = (TextView) vi.findViewById(R.id.txtPackageName);
		txtPackageName.setText(app.appPackage);

		return vi;

	}

}
