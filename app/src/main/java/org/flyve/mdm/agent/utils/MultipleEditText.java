package org.flyve.mdm.agent.utils;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.flyve.mdm.agent.R;

import java.util.ArrayList;
import java.util.List;

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
 * @date      8/8/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class MultipleEditText {

    private List<EditText> editList = new ArrayList<>();
    private int index = 0;
    private Context context;
    private ViewGroup container;
    private String hint;
    private int inputType;
    private int limit = 0;

    public MultipleEditText(Context context, ViewGroup container, String hint) {
        this.context = context;
        this.container = container;
        this.hint = hint;
        this.inputType = InputType.TYPE_CLASS_TEXT;
    }

    public void setLimit(int size) {
        this.limit = size;
    }

    public void setInputType(int type) {
        inputType = type;
    }

    public List<EditText> getEditList() {
        return editList;
    }

    public LinearLayout createEditText() {
        int id = ++index;

        // if limit is mayor of id return null
        if(limit < id) {
            return new LinearLayout(context);
        }

        // LinearLayout
        final LinearLayout ll = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,0);
        ll.setLayoutParams(params);

        // Email Layout
        LinearLayout.LayoutParams paramsEdit = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

        // Email EditText
        final EditText editText = new EditText(context);
        editText.setId(id);
        editText.setLayoutParams(paramsEdit);
        editText.setHint(hint);
        editText.setTag("");
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

        // Clear Button
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
                    container.removeView(ll);
                    editList.remove(editText);
                } else {
                    editText.setText("");
                    editText.setTag("");
                    index = 0;
                }
            }
        });
        ll.addView(imgDelete);

        // Add email to list
        editList.add(editText);

        return ll;

    }

}
