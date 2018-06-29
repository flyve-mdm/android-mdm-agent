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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class UserData extends LocalStorage {

    private static String local = "FlyveMDMUserObject_";

    private static final String FIRST_NAME = local + "firstName";
    private static final String LAST_NAME = local + "lastName";
    private static final String LANGUAGE = local + "language";
    private static final String PHONE = local + "phone";
    private static final String MOBILE_PHONE = local + "mobilePhone";
    private static final String PHONE2 = local + "phone2";
    private static final String ADMINISTRATIVE_NUMBER = local + "administrativeNumber";
    private static final String PICTURE = local + "picture";
    private static final String EMAIL = local + "email";

    /**
     * Constructor
     *
     * @param context
     */
    public UserData(Context context) {
        super(context);
    }

    /**
     * Get the first name of the user
     * @return the first name
     */
    public String getFirstName() {
        return getData(FIRST_NAME);
    }

    /**
     * Set the first name of the user
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        setData(FIRST_NAME, firstName);
    }

    /**
     * Get the last name of the user
     * @return the last name
     */
    public String getLastName() {
        return getData(LAST_NAME);
    }

    /**
     * Set the last name of the user
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        setData(LAST_NAME, lastName);
    }

    /**
     * Get the language of the user
     * @return the language
     */
    public String getLanguage() {
        return getData(LANGUAGE);
    }

    /**
     * Set the language of the user
     * @param language
     */
    public void setLanguage(String language) {
        setData(LANGUAGE, language);
    }

    /**
     * Get the phone of the user
     * @return the phone
     */
    public String getPhone() {
        return getData(PHONE);
    }

    /**
     * Set the phone of the user
     * @param phone
     */
    public void setPhone(String phone) {
        setData(PHONE, phone);
    }

    /**
     * Get the mobile phone of the user
     * @return the mobile phone
     */
    public String getMobilePhone() {
        return getData(MOBILE_PHONE);
    }

    /**
     * Set the mobile phone of the user
     * @param mobilePhone
     */
    public void setMobilePhone(String mobilePhone) {
        setData(MOBILE_PHONE, mobilePhone);
    }

    /**
     * Get a second phone of the user
     * @return the second phone
     */
    public String getPhone2() {
        return getData(PHONE2);
    }

    /**
     * Set the second phone of the user
     * @param phone2
     */
    public void setPhone2(String phone2) {
        setData(PHONE2, phone2);
    }

    /**
     * Get the administrative number of the user
     * @return the administrative number
     */
    public String getAdministrativeNumber() {
        return getData(ADMINISTRATIVE_NUMBER);
    }

    /**
     * Set the administrative number of the user
     * @param administrativeNumber
     */
    public void setAdministrativeNumber(String administrativeNumber) {
        setData(ADMINISTRATIVE_NUMBER, administrativeNumber);
    }

    /**
     * Get the picture of the user
     * @return the picture
     */
    public String getPicture() {
        return getData(PICTURE);
    }

    /**
     * Set the picture of the user
     * @param picture
     */
    public void setPicture(String picture) {
        setData(PICTURE, picture);
    }

    /**
     * Get the emails of the list
     * @return the emails
     */
    public List<EmailsData> getEmails() {
        String json = getData(EMAIL);
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<List<EmailsData>>(){}.getType());
    }

    /**
     * Set the emails of the list
     * @param emails
     */
    public void setEmails(List<EmailsData> emails) {
        Gson gson = new Gson();
        String json = gson.toJson(emails);
        setData(EMAIL, json);
    }

    public class EmailsData {

        private String email;
        private String type;

        /**
         * Get the email of the user
         * @return the email
         */
        public String getEmail() {
            return email;
        }

        /**
         * Set the email of the user
         * @param email
         */
        public void setEmail(String email) {
            this.email = email;
        }

        /**
         * Get the type of the email
         * @return string the type
         */
        public String getType() {
            return type;
        }

        /**
         * Set the type of the email
         * @param type
         */
        public void setType(String type) {
            this.type = type;
        }
    }

}
