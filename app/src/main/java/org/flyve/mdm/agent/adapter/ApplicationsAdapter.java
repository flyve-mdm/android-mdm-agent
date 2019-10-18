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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.data.database.entity.Application;
import org.flyve.mdm.agent.data.database.entity.Policies;
import org.flyve.mdm.agent.ui.FragmentPolicies;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

import java.util.List;

public class ApplicationsAdapter extends BaseAdapter {

	private Application[] data;
	private Context context;
	private LayoutInflater inflater = null;

	public ApplicationsAdapter(Activity activity, Application[] data, Context context) {
		this.data = data;
		this.context = context;
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
		final Application app;

		try {
			app = data[position];
		} catch (Exception ex) {
			FlyveLog.e(this.getClass().getName() + ", getView",ex.getMessage());
			return vi;
		}

		TextView txtStatus = vi.findViewById(R.id.txtStatus);
		ImageButton img_trash = vi.findViewById(R.id.btnUninstall);

		String status = "";
		if(Helpers.isPackageInstalled(parent.getContext(), app.appPackage)) {

			try {
				PackageManager pm = parent.getContext().getPackageManager();
				PackageInfo packageInfo = pm.getPackageInfo(app.appPackage, 0);
				if(Integer.parseInt(app.appVersionCode) > packageInfo.versionCode) {
					status = parent.getResources().getString(R.string.app_ready_to_update);
					img_trash.setVisibility(View.GONE);
				} else {
					status = parent.getResources().getString(R.string.app_installed);
					//for this app if we have removeApp policies
					Policies policies = new PoliciesData(this.context).getByTaskId(app.taskId);
					if(policies.policyName.equalsIgnoreCase("removeApp")){
						status = parent.getResources().getString(R.string.app_need_to_be_uninstall);
						img_trash.setVisibility(View.VISIBLE);
						//on click  start activity to uninstall app
						img_trash.setOnClickListener(new ImageButton.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent();
								String mPackage = app.appPackage;

								if (Build.VERSION.SDK_INT < 14) {
									intent.setAction(Intent.ACTION_DELETE);
									//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.setDataAndType(Uri.parse("package:"+mPackage), "application/vnd.android.package-archive");
								} else if (Build.VERSION.SDK_INT < 16) {
									intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
									intent.setData(Uri.parse("package:"+mPackage));
									//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
									intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
									intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
								} else if (Build.VERSION.SDK_INT < 24) {
									intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
									intent.setData(Uri.parse("package:"+mPackage));
									//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
									intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
								} else { // Android N
									intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
									intent.setData(Uri.parse("package:"+mPackage));
									// grant READ permission for this content Uri
									//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
									intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
									intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
								}

								try {
									v.getContext().startActivity(intent);
								} catch (ActivityNotFoundException e) {
									FlyveLog.e(this.getClass().getName() + ", un	installApk", e.getMessage());
								}

							}});
					}else{
						img_trash.setVisibility(View.GONE);
					}
				}
			} catch (Exception ex) {
				FlyveLog.e(this.getClass().getName() + ", getView", ex.getMessage());
			}
		} else {
			status = parent.getResources().getString(R.string.app_not_installed);
			Policies policies = new PoliciesData(this.context).getByTaskId(app.taskId);
			if(policies.policyName.equalsIgnoreCase("deployApp")) {
				status = parent.getResources().getString(R.string.app_pending_to_install);
			}

			img_trash.setVisibility(View.GONE);
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
