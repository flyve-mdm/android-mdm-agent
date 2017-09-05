package org.flyve.mdm.agent.core.user;

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
 * @date      9/8/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class UserModel {

    private String firstName = "";
    private String lastName = "";
    private String language = "";
    private String phone = "";
    private String mobilePhone = "";
    private String phone2 = "";
    private String administrativeNumber = "";
    private String picture = "";
    private List<UserModel.EmailsData> emails;

    /**
     * Constructor
     */
    public UserModel() {

    }

    /**
     * Set the object's properties to equal the arguments given
     * @param string firstName
     * @param string lastName
     * @param string language
     * @param string phone
     * @param string mobilePhone
     * @param string phone2
     * @param string the administrativeNumber
     * @param string the picture
     * @param string the emails
     */
    public UserModel(String firstName, String lastName, String language, String phone, String mobilePhone, String phone2, String administrativeNumber, String picture, List<EmailsData> emails) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.language = language;
        this.phone = phone;
        this.mobilePhone = mobilePhone;
        this.phone2 = phone2;
        this.administrativeNumber = administrativeNumber;
        this.picture = picture;
        this.emails = emails;
    }

    /**
     * Get the first name of the user
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the first name of the user
     * @param string the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the last name of the user
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the last name of the user
     * @param string the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get the language of the user
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Set the language of the user
     * @param string the language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Get the phone of the user
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set the phone of the user
     * @param string the phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Get the mobile phone of the user
     * @return the mobile phone
     */
    public String getMobilePhone() {
        return mobilePhone;
    }

    /**
     * Set the mobile phone of the user
     * @param string the mobile phone
     */
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    /**
     * Get a second phone of the user
     * @return the second phone 
     */
    public String getPhone2() {
        return phone2;
    }

    /**
     * Set the second phone of the user
     * @param string phone2
     */
    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    /**
     * Get the administrative number of the user
     * @return the administrative number
     */
    public String getAdministrativeNumber() {
        return administrativeNumber;
    }

    /**
     * Set the administrative number of the user
     * @param string administrativeNumber
     */
    public void setAdministrativeNumber(String administrativeNumber) {
        this.administrativeNumber = administrativeNumber;
    }

    /**
     * Get the picture of the user
     * @return the picture
     */
    public String getPicture() {
        return picture;
    }

    /**
     * Set the picture of the user
     * @param string picture
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * Get the emails of the list
     * @return the emails
     */
    public List<EmailsData> getEmails() {
        return emails;
    }

    /**
     * Set the emails of hte list
     * @param EmailsData the emails
     */
    public void setEmails(List<EmailsData> emails) {
        this.emails = emails;
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
         * @param string the email
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
         * @param string the type
         */
        public void setType(String type) {
            this.type = type;
        }
    }

}
