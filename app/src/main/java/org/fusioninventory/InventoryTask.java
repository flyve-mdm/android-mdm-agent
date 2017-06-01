package org.fusioninventory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;

import org.fusioninventory.categories.Categories;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Xml;

import com.teclib.service.MQTTService;

public class InventoryTask extends AsyncTask<String, Void, String> {


    public ArrayList<Categories> mContent = null;
    public Date mStart = null, mEnd = null;
    public Context ctx = null;
    static final int OK = 0;
    static final int NOK = 1;
    public static final String FusionVersion = "FlyveMDM-Agent_v1.0";

    public int progress = 0;

    public InventoryTask(Context context) {
        ctx = context;
        FusionInventory.log(this, "FusionInventoryApp = ", Log.VERBOSE);
    }

    public boolean toXML() {
        Log.i("FusionInventoryApp", "toXML: ");
        if (mContent != null) {

            XmlSerializer serializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();

            try {
                serializer.setOutput(writer);
                serializer
                        .setFeature(
                                "http://xmlpull.org/v1/doc/features.html#indent-output",
                                true);
                // indentation as 3 spaces

                serializer.startDocument("utf-8", true);
                // Start REQUEST
                serializer.startTag(null, "REQUEST");
                // Start CONTENT
                serializer.startTag(null, "QUERY");
                serializer.text("INVENTORY");
                serializer.endTag(null, "QUERY");

                serializer.startTag(null, "VERSIONCLIENT");
                serializer.text(FusionVersion);
                serializer.endTag(null, "VERSIONCLIENT");

                serializer.startTag(null, "DEVICEID");
                serializer.text(Build.SERIAL);
                serializer.endTag(null, "DEVICEID");

                serializer.startTag(null, "CONTENT");
                // Start ACCESSLOG
                serializer.startTag(null, "ACCESSLOG");

                serializer.startTag(null, "LOGDATE");

                serializer.text(DateFormat.format("yyyy-mm-dd hh:MM:ss", mStart)
                        .toString());
                serializer.endTag(null, "LOGDATE");

                serializer.startTag(null, "USERID");
                serializer.text("N/A");
                serializer.endTag(null, "USERID");

                serializer.endTag(null, "ACCESSLOG");
                // End ACCESSLOG

                //Manage accountinfos :: TAG
                //   if (!mFusionApp.getTag().equals("")) {
                serializer.startTag(null, "ACCOUNTINFO");
                serializer.startTag(null, "KEYNAME");
                serializer.text("TAG");
                serializer.endTag(null, "KEYNAME");
                serializer.startTag(null, "KEYVALUE");
                //serializer.text(mFusionApp.getTag());
                serializer.text("");
                serializer.endTag(null, "KEYVALUE");
                serializer.endTag(null, "ACCOUNTINFO");
                //    }

                for (Categories cat : mContent) {

                    cat.toXML(serializer);
                }

                serializer.endTag(null, "CONTENT");
                serializer.endTag(null, "REQUEST");
                serializer.endDocument();
                WriteStringToFile(writer.toString());
                return true;
            } catch (Exception e) {
                // TODO: handle exception
                throw new RuntimeException(e);
            }

        }
        return false;
    }

    public void WriteStringToFile (String data) {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(new File(ctx.getFilesDir(),"/android_inventory.xml")));
            writer.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }


    @Override
    protected String doInBackground(String... params) {
        mStart = new Date();

        mContent = new ArrayList<Categories>();

        String [] categories = {
//                "PhoneStatus",
                "Hardware",
                "Bios",
                "Memory",
                "Inputs",
                "Sensors",
                "Drives",
                "Cpus",
                "Simcards",
                "Videos",
                "Cameras",
                "Networks",
//                "LocationProviders",
                "Envs",
                "Jvm",
                "Softwares",
                "Usb",
                "Battery",
//              "BluetoothAdapterCategory", // <- there is already a BluetoothAdapter class in android SDK
        };

        Class<Categories> cat_class;

        for(String c : categories) {
            cat_class = null;
            FusionInventory.log(this, String.format("INVENTORY of %s", c),Log.VERBOSE);
            try {
                cat_class = (Class <Categories>) Class.forName(String.format("org.fusioninventory.categories.%s",c));
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(cat_class!=null) {
                try {
                    Constructor<Categories> co = cat_class.getConstructor(Context.class);
                    mContent.add(co.newInstance(ctx));
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }
            }
        }

        FusionInventory.log(this, "end of inventory", Log.INFO);
        mEnd = new Date();
        return "true";
    }

    @Override
    protected void onPostExecute(String result) {
        if(result.equals("true")){
            toXML();
            Intent mqttstop = new Intent(ctx , MQTTService.class);
            mqttstop.setAction(MQTTService.ACTION_INVENTORY);
            ctx.startService(mqttstop);
        }

    }
}
