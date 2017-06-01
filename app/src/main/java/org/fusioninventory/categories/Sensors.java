package org.fusioninventory.categories;

import java.util.List;

import org.fusioninventory.FusionInventory;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

public class Sensors extends Categories {

	/**
     * 
     */
	private static final long serialVersionUID = 4846706700566208666L;

	public Sensors(Context xCtx) {
		super(xCtx);
		// TODO Auto-generated constructor stub
		SensorManager sensorManager = (SensorManager) mCtx
				.getSystemService(Context.SENSOR_SERVICE);

		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
		FusionInventory.log(this, "Get sensors ", Log.VERBOSE);

		for (Sensor s : sensors) {
			Category c = new Category(mCtx, "SENSORS");
			c.put("NAME", s.getName());
			c.put("MANUFACTURER", s.getVendor());
			int type = s.getType();
			String strtype = "";
			switch (type) {
				case Sensor.TYPE_ACCELEROMETER:
					strtype = "ACCELEROMETER";
					break;
				case Sensor.TYPE_GRAVITY:
					strtype = "GRAVITY";
					break;
				case Sensor.TYPE_GYROSCOPE:
					strtype = "GYROSCOPE";
					break;
				case Sensor.TYPE_LINEAR_ACCELERATION:
					strtype = "LINEAR ACCELERATION";
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					strtype = "MAGNETIC FIELD";
					break;
				case Sensor.TYPE_ORIENTATION:
					strtype = "ORIENTATION";
					break;
				case Sensor.TYPE_PRESSURE:
					strtype = "PRESSURE";
					break;
				case Sensor.TYPE_PROXIMITY:
					strtype = "PROXIMITY";
					break;
				case Sensor.TYPE_ROTATION_VECTOR:
					strtype = "ROTATION VECTOR";
					break;
				case Sensor.TYPE_TEMPERATURE:
					strtype = "TEMPERATURE";
					break;
				default:
					strtype = "";
					break;
			}
			c.put("TYPE", strtype);
			Float f = s.getPower();
			c.put("POWER", f.toString());
			Integer version = s.getVersion();
			c.put("VERSION", version.toString());
			this.add(c);
		}
	}
}
