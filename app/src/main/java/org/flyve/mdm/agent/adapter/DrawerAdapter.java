package org.flyve.mdm.agent.adapter;

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
	private static LayoutInflater inflater = null;

	public DrawerAdapter(Activity activity, List<HashMap<String, String>> data) {
		this.data = data;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return this.data.size();
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
