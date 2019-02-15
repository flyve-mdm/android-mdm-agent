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

package org.flyve.mdm.agent.core.user;

import org.flyve.mdm.agent.data.localstorage.UserData;

import java.util.List;

public class UserSchema {

    private List<UserData.EmailsData> emails;
    private String firstName = "";
    private String lastName = "";
    private String phone = "";
    private String phone2 = "";
    private String mobilePhone = "";
    private String picture = "";
    private String language = "";
    private String administrativeNumber = "";

    public UserSchema() {
    }

    /**
     * Get the list of Emails
     * @return List emails
     */
    public List<UserData.EmailsData> getEmails() {
        return emails;
    }

    /**
     * Set the list of Emails
     * @param emails
     */
    public void setEmails(List<UserData.EmailsData> emails) {
        this.emails = emails;
    }

    /**
     * Get the message of FirstName
     * @return String FirstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the message of FirstName
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the message of LastName
     * @return String lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the message of LastName
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get the message of Phone
     * @return String phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set the message of Phone
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Get the message of Phone2
     * @return String phone2
     */
    public String getPhone2() {
        return phone2;
    }

    /**
     * Set the message of Phone2
     * @param phone2
     */
    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    /**
     * Get the message of MobilePhone
     * @return String MobilePhone
     */
    public String getMobilePhone() {
        return mobilePhone;
    }

    /**
     * Set the message of MobilePhone
     * @param mobilePhone
     */
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    /**
     * Get the message of Picture
     * @return String picture
     */
    public String getPicture() {
        return picture;
    }

    /**
     * Set the message of Picture
     * @param picture
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * Get the message of Language
     * @return String language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Set the message of Language
     * @param language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Get the message of AdministrativeNumber
     * @return String administrativeNumber
     */
    public String getAdministrativeNumber() {
        return administrativeNumber;
    }

    /**
     * Set the message of AdministrativeNumber
     * @param administrativeNumber
     */
    public void setAdministrativeNumber(String administrativeNumber) {
        this.administrativeNumber = administrativeNumber;
    }
}
