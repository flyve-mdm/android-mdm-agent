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

package org.flyve.mdm.agent.utils;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.flyve.mdm.agent.R;

import java.util.ArrayList;
import java.util.List;

public class MultipleEditText {

    private List<EditText> editList = new ArrayList<>();
    private List<Spinner> spinnList = new ArrayList<>();
    private int index = 0;
    private Context context;
    private ViewGroup container;
    private String hint;
    private int inputType;
    private int limit = 0;
    private ArrayAdapter<CharSequence> adapter = null;

    public MultipleEditText(Context context, ViewGroup container, String hint) {
        this.context = context;
        this.container = container;
        this.hint = hint;
        this.inputType = InputType.TYPE_CLASS_TEXT;
    }

    /**
     * Set the limit size
     * @param size
     */
    public void setLimit(int size) {
        this.limit = size;
    }

    /**
     * Set the input type
     * @param type
     */
    public void setInputType(int type) {
        inputType = type;
    }

    /**
     * Set the values of the Spinner and Edit Text lists
     * @param editValue the edit value
     * @param spinnerValue the spinner value
     */
    public void setValue(List<String> editValue, List<String> spinnerValue) {
        for(int i=0; i<editValue.size(); i++) {
            container.addView(createEditText(editValue.get(i), spinnerValue.get(i)));
        }
    }

    /**
     * Set the spinner array
     * @param array
     */
    public void setSpinnerArray(int array) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(context,
                array, android.R.layout.simple_spinner_item);
    }

    /**
     * Get the edit list
     * @return List the edit list
     */
    public List<EditText> getEditList() {
        return editList;
    }

    /**
     * Get the spinner list
     * @return List the spinner list
     */
    public List<Spinner> getSpinnList() {
        return spinnList;
    }

    /**
     * Returns the created layout to edit the text
     */
    public LinearLayout createEditText() {
        return createEditText("","");
    }

    /**
     * Set the layout to edit text
     * @param editTextValue the text value to edit
     * @param spinnerValue the spinner value to edit
     * @return the Linear Layout View
     */
    public LinearLayout createEditText(String editTextValue, String spinnerValue) {
        int id = ++index;

        // if limit is mayor of id return null
        if(limit < id && limit > 0) {
            return new LinearLayout(context);
        }

        final LinearLayout llv = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,0);
        llv.setLayoutParams(params);
        llv.setOrientation(LinearLayout.VERTICAL);

        // -------------------
        // LinearLayout HORIZONTAL
        // -------------------
        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(params);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        // -------------------
        // Email Layout
        // -------------------
        LinearLayout.LayoutParams paramsEdit = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

        // -------------------
        // Email EditText
        // -------------------
        final EditText editText = new EditText(context);
        editText.setId(id);
        editText.setLayoutParams(paramsEdit);
        editText.setHint(hint);
        editText.setTag("");
        editText.setText(editTextValue);
        editText.setInputType(inputType);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(!"used".equalsIgnoreCase(v.getTag().toString())) {
                    v.setTag("used");
                    container.addView(createEditText());
                }
                return false;
            }
        });
        ll.addView(editText);

        // -------------------
        // Clear Button
        // -------------------
        LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsImg.gravity= Gravity.CENTER;

        ImageView imgDelete = new ImageView(context);
        imgDelete.setId(id);
        imgDelete.setLayoutParams(paramsImg);
        imgDelete.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_clear));
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editList.size()>=2) {
                    container.removeView(llv);
                    editList.remove(editText);
                } else {
                    editText.setText("");
                    editText.setTag("");
                    index = 0;
                }
            }
        });
        ll.addView(imgDelete);

        // -------------------
        // Add email to list
        // -------------------
        editList.add(editText);

        llv.addView(ll);

        // -----------------
        // Add spinner
        // -----------------
        if(adapter != null) {
            Spinner spinner = new Spinner(context);

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);

            if(spinnerValue!=null && !spinnerValue.equals("")) {
                int spinnerPosition = adapter.getPosition(spinnerValue);
                spinner.setSelection(spinnerPosition);
            }
            spinnList.add(spinner);

            llv.addView(spinner);
        }

        return llv;
    }

}
