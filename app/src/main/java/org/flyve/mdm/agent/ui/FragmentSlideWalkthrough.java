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

package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.walkthrough.WalkthroughSchema;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public class FragmentSlideWalkthrough extends Fragment {

    private WalkthroughSchema walkthroughData;
    private int slides;
    private int position;

    private String mLink = "";
    private int mMessage = 0;
    private int mImage = 0;

    /**
     * Set the properties to equal the given arguments
     * @param walkthroughData the walkthroug Model class
     * @param slides the number of slides the Walkthrough has
     * @param position the user is currently on
     */
    public void config(WalkthroughSchema walkthroughData, int slides, int position) {
        this.walkthroughData = walkthroughData;
        this.slides = slides;
        this.position = position;
    }

    /**
     * Called to have the fragments instantiate its user interface View
     * It displays the View for the Walkthrough
     * @param inflater the object that can be used to inflate any views
     * @param container the parent View
     * @param savedInstanceState if non-null, this fragment is being reconstructed from a previous saved state
     * @return View the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup v = (ViewGroup) inflater.inflate(
                R.layout.fragment_walkthrough_step, container, false);

        try {
            mLink = walkthroughData.getLink();
            mMessage = walkthroughData.getMessage();
            mImage = walkthroughData.getImage();
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", onCreateView", ex.getMessage());
        }


        ImageView imgStep = v.findViewById(R.id.imgStep);
        if (mImage>0) {
            imgStep.setImageResource(mImage);
        }

        ImageView imgMessage = v.findViewById(R.id.imgDescription);
        if (mMessage>0) {
            imgMessage.setImageResource(mMessage);
        }

        ImageView imgMore = v.findViewById(R.id.imgMore);
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.openURL( FragmentSlideWalkthrough.this.getActivity(), mLink );
            }
        });

        slideDots(v);

        return v;
    }

    /**
     * Shows the slide dots of the screen
     * @param v the View
     */
    private void slideDots(ViewGroup v) {

        LinearLayout ln = v.findViewById(R.id.lnSliders);

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
