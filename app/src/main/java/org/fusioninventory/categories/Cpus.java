package org.fusioninventory.categories;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.fusioninventory.FusionInventory;


import android.content.Context;
import android.util.Log;


public class Cpus extends Categories {


    /**
     * 
     */
    private static final long serialVersionUID = 4846706700566208666L;

    public Cpus(Context xCtx) {
        super(xCtx);
        // TODO Auto-generated constructor stub

        Category c = new Category(mCtx, "CPUS");
        c.put("NAME", getCpuName());
        c.put("SPEED", getCpuFrequency());

        this.add(c);
        
    }
    
    public String getCpuName() {
        FusionInventory.log(this, "Parse /proc/cpuinfo", Log.VERBOSE);
        String cpuname = "";
        File f = new File("/proc/cpuinfo");
        try {
            BufferedReader br = new BufferedReader(new FileReader(f), 8 * 1024);
            String infos = br.readLine();
            cpuname = infos.replaceAll("(.*):\\ (.*)", "$2");
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	return cpuname;
    }
    
    public String getCpuFrequency() {
        String cpuFrequency = "";
    	FusionInventory.log(this, "Parse /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", Log.VERBOSE);
        File f = new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
        try {
            BufferedReader br = new BufferedReader(new FileReader(f),8 * 1024);
            String line = br.readLine();
            Integer speed = new Integer(line);
            speed = speed / 1000;
            cpuFrequency = speed.toString();
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	return cpuFrequency;
    }
}
