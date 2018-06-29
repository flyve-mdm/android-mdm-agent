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

package org.flyve.mdm.agent.data.localstorage;

import android.content.Context;

import org.flyve.mdm.agent.data.localstorage.LocalStorage;

public class SupervisorData extends LocalStorage {

    private static String local = "FlyveMDMSupervisorObject_";

    private static final String NAME = local + "name";
    private static final String EMAIL = local + "email";
    private static final String PHONE = local + "phone";
    private static final String WEBSITE = local + "website";
    private static final String PICTURE = local + "picture";

    /**
     * Constructor
     *
     * @param context
     */
    public SupervisorData(Context context) {
        super(context);
    }

    /**
     * Get the name of the Supervisor Model
     * @return string the name
     */
    public String getName() {
        return getData(NAME);
    }

    /**
     * Set the name of the Supervisor Model
     * @param name
     */
    public void setName(String name) {
        setData(NAME, name);
    }

    /**
     * Get the email of the Supervisor Model
     * @return string the email
     */
    public String getEmail() {
        return getData(EMAIL);
    }

    /**
     * Set the email of the Supervisor Model
     * @param email
     */
    public void setEmail(String email) {
        setData(EMAIL, email);
    }

    /**
     * Get the phone of the Supervisor Model
     * @return string the phone
     */
    public String getPhone() {
        return getData(PHONE);
    }

    /**
     * Set the phone of the Supervisor Model
     * @param phone
     */
    public void setPhone(String phone) {
        setData(PHONE, phone);
    }

    /**
     * Get the website of the Supervisor Model
     * @return string the website
     */
    public String getWebsite() {
        return getData(WEBSITE);
    }

    /**
     * Set the website of the Supervisor Model
     * @param website
     */
    public void setWebsite(String website) {
        setData(WEBSITE, website);
    }

    /**
     * Get the picture of the Supervisor Model
     * @return string the picture
     */
    public String getPicture() {
        return getData(PICTURE);
    }

    /**
     * Set the picture of the Supervisor Model
     * @param picture
     */
    public void setPicture(String picture) {
        setData(PICTURE, picture);
    }

}
