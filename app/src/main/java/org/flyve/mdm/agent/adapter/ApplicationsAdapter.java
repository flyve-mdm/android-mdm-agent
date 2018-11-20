/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
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
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.database.entity.Application;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public class ApplicationsAdapter extends BaseAdapter {

	private Application[] data;
	private LayoutInflater inflater = null;

	public ApplicationsAdapter(Activity activity, Application[] data) {
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

		View vi = inflater.inflate(R.layout.list_item_application, null);
		Application app;

		try {
			app = data[position];
		} catch (Exception ex) {
			FlyveLog.e(this.getClass().getName() + ", getView",ex.getMessage());
			return vi;
		}

		TextView txtStatus = vi.findViewById(R.id.txtStatus);

		String status = "";
		if(Helpers.isPackageInstalled(parent.getContext(), app.appPackage)) {

			try {
				PackageManager pm = parent.getContext().getPackageManager();
				PackageInfo packageInfo = pm.getPackageInfo(app.appPackage, 0);
				if(Integer.parseInt(app.appVersionCode) > packageInfo.versionCode) {
					status = parent.getResources().getString(R.string.app_ready_to_update);
				} else {
					status = parent.getResources().getString(R.string.app_installed);
				}
			} catch (Exception ex) {
				FlyveLog.e(this.getClass().getName() + ", getView", ex.getMessage());
			}
		} else {
			status = parent.getResources().getString(R.string.app_not_installed);
		}

		txtStatus.setText(status);

		TextView txtAppName = vi.findViewById(R.id.txtAppName);
		txtAppName.setText(app.appName);

		TextView txtPackageName = vi.findViewById(R.id.txtPackageName);
		txtPackageName.setText(app.appPackage);

		TextView txtVersionCode =  vi.findViewById(R.id.txtVersionCode);
		txtVersionCode.setText(parent.getContext().getString(R.string.version_code, app.appVersionCode));

		TextView txtVersionName =  vi.findViewById(R.id.txtVersionName);
		txtVersionName.setText(parent.getContext().getString(R.string.version_name, app.appVersionName));

		ImageView imgApp = vi.findViewById(R.id.imgApp);
		imgApp.setImageDrawable(Helpers.getApplicationImage(parent.getContext(), app.appPackage));

		return vi;

	}

}
