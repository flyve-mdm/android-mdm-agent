package org.flyve.mdm.agent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.flyve.mdm.agent.data.DataStorage;


public class FragmentInformation extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_information, container, false);

        DataStorage cache = new DataStorage(FragmentInformation.this.getActivity());

        TextView txtName = (TextView) v.findViewById(R.id.txtNameUser);
        txtName.setText(cache.getUserFirstName() + " " + cache.getUserLastName());

        TextView txtEmail = (TextView) v.findViewById(R.id.txtDescriptionUser);
        txtEmail.setText(cache.getUserEmail());

        return v;
    }

}
