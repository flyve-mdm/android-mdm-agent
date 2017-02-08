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

package com.teclib.api;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class AndroidShell {

    /**
     * Exec shell command
     *
     * @param command an absolute URL giving the base location of the image
     * @return Output command
     */
    public static String execSh(String command) {

        String line;
        String output = "";

        if (command.endsWith("\n")) {
            command = command.substring(0, command.length() - 1);
        }
        command = command + "; echo __end\n";

        try {
            /* Exec command */
            Process p = Runtime.getRuntime().exec("/system/bin/sh");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command);

            FlyveLog.d("cmd = + %s", command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = reader.readLine();

            while (!line.startsWith("__end") && line != null) {
                FlyveLog.d("line = + %s", line);
                output = output + line + "\n";
                line = reader.readLine();
            }

            reader.close();
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            p.waitFor();
            p.destroy();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            FlyveLog.e(e.getMessage());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            FlyveLog.e(e.getMessage());
        }

        return output;
    }
}