package org.fusioninventory.categories;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class Battery extends Categories {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4096347994131285426L;
	private String level, voltage, temperature, status, health,
			technology;

	/**
     * 
     */

	public Battery(Context xCtx) {
		super(xCtx);
		// TODO Auto-generated constructor stub
		
		xCtx.registerReceiver(this.myBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		this.myBatteryReceiver.onReceive(xCtx, new Intent(Intent.ACTION_BATTERY_CHANGED));
		Category c  = new Category(xCtx, "BATTERIES");
		c.put("CHEMISTRY", technology);
		c.put("TEMPERATURE", temperature);
		c.put("VOLTAGE", voltage);
		c.put("LEVEL", level);
		c.put("HEALTH", health);
		c.put("STATUS", status);
		this.add(c);
	}

	private BroadcastReceiver myBatteryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub

			if (arg1.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				level = String.valueOf(arg1.getIntExtra("level", 0)) + "%";
				voltage = String
						.valueOf((float) arg1.getIntExtra("voltage", 0) / 1000)
						+ "V";
				temperature = String.valueOf((float) arg1.getIntExtra(
						"temperature", 0) / 10)
						+ "c";
				technology = arg1.getStringExtra("technology");

				int intstatus = arg1.getIntExtra("status",
						BatteryManager.BATTERY_STATUS_UNKNOWN);
				if (intstatus == BatteryManager.BATTERY_STATUS_CHARGING) {
					status = "Charging";
				} else if (intstatus == BatteryManager.BATTERY_STATUS_DISCHARGING) {
					status = "Dis-charging";
				} else if (intstatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
					status = "Not charging";
				} else if (intstatus == BatteryManager.BATTERY_STATUS_FULL) {
					status = "Full";
				} else {
					status = "Unknown";
				}

				int inthealth = arg1.getIntExtra("health",
						BatteryManager.BATTERY_HEALTH_UNKNOWN);
				if (inthealth == BatteryManager.BATTERY_HEALTH_GOOD) {
					health = "Good";
				} else if (inthealth == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
					health = "Over Heat";
				} else if (inthealth == BatteryManager.BATTERY_HEALTH_DEAD) {
					health = "Dead";
				} else if (inthealth == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE) {
					health = "Over Voltage";
				} else if (inthealth == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE) {
					health = "Unspecified Failure";
				} else {
					health = "Unknown";
				}

			}
		}

	};
}
