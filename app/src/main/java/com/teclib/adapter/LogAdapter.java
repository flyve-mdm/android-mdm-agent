package com.teclib.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.teclib.flyvemdm.R;
import java.util.ArrayList;
import java.util.HashMap;

public class LogAdapter extends BaseAdapter {

	private Activity _activity;
	private ArrayList<HashMap<String, String>> _data;
	private static LayoutInflater inflater = null;

	public LogAdapter(Activity activity, ArrayList<HashMap<String, String>> data) {
		this._data = data;
		this._activity = activity;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return _data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;

		HashMap<String, String> hashdata = _data.get(position);

		vi = inflater.inflate(R.layout.listitem_main, null);

		TextView tvLog = (TextView) vi.findViewById(R.id.tvLog);
		tvLog.setText(hashdata.get("message"));

		return vi;
	}

}
