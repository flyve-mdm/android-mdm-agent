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

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidatorHelper {

    /**
     * Constructor
     */
    private InputValidatorHelper() {

    }

    /**
     * Checks if the email has valid characters
     * @param string the pattern to check
     * @return boolean true if valid, false otherwise
     */
    public static boolean isValidEmail(String string){
        final String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(string);
        return matcher.matches();
    }

    /**
     * Checks if it is null or empty
     * @param string the string to check
     * @return boolean true if it is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String string){
        return TextUtils.isEmpty(string);
    }

    /**
     * Checks if it contains only digits
     * @param string the string to check
     * @return boolean true if it is only digits, false otherwise
     */
    public static boolean isNumeric(String string){
        return TextUtils.isDigitsOnly(string);
    }
}
