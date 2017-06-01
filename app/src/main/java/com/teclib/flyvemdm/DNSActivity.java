/*
 * Copyright (C) 2016 Teclib'
 *
 * This file is part of Flyve MDM Android.
 *
 * Flyve MDM Android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.flyvemdm;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teclib.api.Network;

import java.lang.reflect.InvocationTargetException;

public class DNSActivity extends Activity {

    private RelativeLayout btn_nxt;
    private RelativeLayout connection_status;
    private RelativeLayout serial_status;
    private RelativeLayout client_status;
    private String URLIp;
    private String DNSIp;
    private String link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dns);
        btn_nxt = (RelativeLayout)findViewById(R.id.imageButton2);
        btn_nxt.setVisibility(View.INVISIBLE);

        connection_status = (RelativeLayout)findViewById(R.id.connection_status);
        serial_status = (RelativeLayout)findViewById(R.id.serial_status);
        client_status = (RelativeLayout)findViewById(R.id.client_status);

        connection_status.setVisibility(View.INVISIBLE);
        serial_status.setVisibility(View.INVISIBLE);
        client_status.setVisibility(View.INVISIBLE);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        checkConnection();
    }


    public void checkConnection (){

        TextView connection_status_text = (TextView)findViewById(R.id.connection_status_text);
        ImageView connection_status_image = (ImageView)findViewById(R.id.connection_status_image);

        TextView serial_status_text = (TextView)findViewById(R.id.serial_status_text);
        ImageView serial_status_image = (ImageView)findViewById(R.id.serial_status_image);

        TextView client_status_text = (TextView)findViewById(R.id.client_status_text);
        ImageView client_status_image = (ImageView)findViewById(R.id.client_status_image);

        Button retry_btn = (Button)findViewById(R.id.retry_btn);
        retry_btn.setVisibility(View.INVISIBLE);

        Network ac = new Network();

        //int count = 0;
        int flag = 0;

        if (ac.getInstance(this).isOnline()){
            retry_btn.setVisibility(View.INVISIBLE);
            connection_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_ok));
            connection_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_white_48dp));
            connection_status_text.setText(R.string.connected);
            connection_status.setVisibility(View.VISIBLE);
        } else {
            flag += 1;
            connection_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_fail));
            connection_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_48dp));
            connection_status_text.setText(R.string.no_connection);
            connection_status.setVisibility(View.VISIBLE);
            retry_btn.setVisibility(View.VISIBLE);
        }
        URLIp = ac.getAddress("www.teclib.com");

        if (!URLIp.isEmpty()) {
            serial_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_ok));
            serial_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_white_48dp));
            serial_status_text.setText("URL IP: " + URLIp);
        } else {
            flag += 1;
            serial_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_fail));
            serial_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_48dp));
            serial_status_text.setText("NO URL IP");
        }
        serial_status.setVisibility(View.VISIBLE);

        try {
            DNSIp = ac.getDNSAddress();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (!DNSIp.isEmpty()) {
            client_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_ok));
            client_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_white_48dp));
            client_status_text.setText("DNS IP: " + DNSIp);
        } else {
            flag += 1;
            client_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_fail));
            client_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_48dp));
            client_status_text.setText("NO DNS IP");
        }
        client_status.setVisibility(View.VISIBLE);
        if (flag == 0){
            btn_nxt.setVisibility(View.VISIBLE);
        }
        else {
            retry_btn.setVisibility(View.VISIBLE);
        }
    }

    public void retry(View v) {
        this.checkConnection();
    }

    public void prev_Page(View v){
        Intent next = new Intent(DNSActivity.this, MainActivity.class);
        DNSActivity.this.startActivity(next);
        this.finish();
    }

    public void next_Page(View v){
        link="&serial="+Build.SERIAL;
        Intent next = new Intent(DNSActivity.this, HTTPActivity.class);
        next.putExtra("serial", URLIp);
        next.putExtra("dnsip", DNSIp);
        next.putExtra("previous","DNSActivity");
        DNSActivity.this.startActivity(next);
        this.finish();
    }


}
