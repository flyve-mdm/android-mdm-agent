package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.walkthrough.WalkthroughModel;
import org.flyve.mdm.agent.utils.Helpers;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
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
 * @date      17/8/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class FragmentSlideWalkthrough extends Fragment {

    private WalkthroughModel walkthroughModel;
    private int slides;
    private int position;

    public void config(WalkthroughModel walkthroughModel, int slides, int position) {
        this.walkthroughModel = walkthroughModel;
        this.slides = slides;
        this.position = position;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup v = (ViewGroup) inflater.inflate(
                R.layout.fragment_walkthrough_step, container, false);

        TextView txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        if(!walkthroughModel.getLink().equals("")) {
            txtMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helpers.openURL(FragmentSlideWalkthrough.this.getContext(), walkthroughModel.getLink());
                }
            });
        }
        txtMessage.setText(Html.fromHtml(walkthroughModel.getMessage()));

        ImageView imgStep = (ImageView) v.findViewById(R.id.imgStep);
        imgStep.setImageResource(walkthroughModel.getImage());

        slideDots(v);

        return v;
    }

    private void slideDots(ViewGroup v) {

        LinearLayout ln = (LinearLayout) v.findViewById(R.id.lnSliders);

        for(int i=0; i<this.slides; i++) {

            ImageView img = new ImageView(FragmentSlideWalkthrough.this.getContext());
            img.setPadding(7,0,7,0);
            if(i==this.position) {
                img.setImageResource(R.drawable.ic_dot_selected);
            } else {
                img.setImageResource(R.drawable.ic_dot);
            }
            ln.addView(img);
        }

    }


}
