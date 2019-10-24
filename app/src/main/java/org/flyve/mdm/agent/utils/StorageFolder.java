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
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import org.flyve.mdm.agent.R;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StorageFolder {

    private static final String EXTERNAL_STORAGE = "EXTERNAL_STORAGE";

    private Context context;
    private Boolean isSDMemory = true;

    public StorageFolder(Context context){
        this.context = context;
    }

    /**
     * Get the directory of the apk
     * @return string the apk directory
     */
    public String getApkDir() throws Exception {
        String path = getFolderPath(Environment.DIRECTORY_DOWNLOADS);
        if(!checkPath(path)) { throw new RuntimeException(context.getResources().getString(R.string.check_store_fail)); }
        return path;
    }

    /**
     * Get the directory of the UPK
     * @return string the UPK directory
     */
    public String getUpkDir() {
        String path = getFolderPath(Environment.DIRECTORY_DOWNLOADS) + "/.fdroid/";
        if(!checkPath(path)) { throw new RuntimeException(context.getResources().getString(R.string.check_store_fail)); }

        FlyveLog.d(path);
        return path;
    }

    /**
     * Get the directory of the Secure Digital Card
     * @return string the SD card directory
     */
    public String getSDcardDir() {
        String path = System.getenv(EXTERNAL_STORAGE);
        if(!checkPath(path)) { throw new RuntimeException(context.getResources().getString(R.string.check_store_fail)); }

        FlyveLog.d(path);
        return path;
    }

    /**
     * Get the directory of the pictures
     * @return string the pictures directory
     */
    public String getPicturesDir() {
        String path = getFolderPath(Environment.DIRECTORY_DCIM);
        if(!checkPath(path)) { throw new RuntimeException(context.getResources().getString(R.string.check_store_fail)); }

        FlyveLog.d(path);
        return path;
    }

    /**
     * Get the directory of the documents
     * @return string the documents directory
     */
    public String getDocumentsDir() {
        String path;
        if(Build.VERSION.SDK_INT >= 19) {
            path = getFolderPath(Environment.DIRECTORY_DOCUMENTS);
        } else {
            path = getSDcardDir() + "/Documents";
        }
        if(!checkPath(path)) { throw new RuntimeException(context.getResources().getString(R.string.check_store_fail)); }

        FlyveLog.d(path);
        return path;
    }

    /**
     * Get the directory of the download files
     * @return string the download directory
     */
    public String getDownloadDir() {
        String path = getSDcardDir() + "/Download";
        if(!checkPath(path)) { throw new RuntimeException(context.getResources().getString(R.string.check_store_fail)); }
        FlyveLog.d(path);
        return path;
    }

    /**
     * Get the directory of the music
     * @return string the music directory
     */
    public String getMusicsDir() {
        String path = getFolderPath(Environment.DIRECTORY_MUSIC);
        if(!checkPath(path)) { throw new RuntimeException(context.getResources().getString(R.string.check_store_fail)); }

        FlyveLog.d(path);
        return path;
    }

    /**
     * Convert the path according to the given arguments
     * @param receivePath
     * @return string the converted path
     */
    public String convertPath(String receivePath) {

        String sreturn = receivePath;

        Pattern sdcard = Pattern.compile("%SDCARD%");
        Pattern document = Pattern.compile("%DOCUMENTS%");
        Pattern music = Pattern.compile("%MUSIC%");
        Pattern photo = Pattern.compile("%PHOTOS%");

        Matcher msdcard = sdcard.matcher(receivePath);
        Matcher mdocument = document.matcher(receivePath);
        Matcher mmusic = music.matcher(receivePath);
        Matcher mphoto = photo.matcher(receivePath);

        //Find the sequence that matches the pattern
        if (msdcard.find()) {
            sreturn = receivePath;
            sreturn = sreturn.replace("%SDCARD%", getSDcardDir());
        }

        if (mdocument.find()) {
            sreturn = receivePath;
            sreturn = sreturn.replace("%DOCUMENTS%", getDocumentsDir());
        }

        if (mmusic.find()) {
            sreturn = receivePath;
            sreturn = sreturn.replace("%MUSIC%", getMusicsDir());
        }

        if (mphoto.find()) {
            sreturn = receivePath;
            sreturn = sreturn.replace("%PHOTOS%", getPicturesDir());
        }

        return sreturn;
    }

    private String getFolderPath(String folderName) {
        return Environment.getExternalStoragePublicDirectory(folderName).getAbsolutePath() + "/";
    }

    private Boolean checkPath(String path) {
        // Check if storage is writable
        if(!isExternalStorageWritable()) {
            return false;
        }

        // check if the folder exists if not try to create
        File folder = new File(path);

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }

        return success;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static long getFreeExternalMemorySize() {
        String secStore = System.getenv("SECONDARY_STORAGE");
        File path = new File(secStore);
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        return availableBlocks * blockSize;
    }

}
