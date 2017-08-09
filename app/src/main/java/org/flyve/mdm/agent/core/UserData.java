package org.flyve.mdm.agent.core;

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
public class UserData {

    private String FirstName;
    private String LastName;
    private String Language;
    private String Phone;
    private String MobilePhone;
    private String Phone2;
    private String AdministrativeNumber;
    private String Picture;
    private List<UserData.EmailsData> Emails;

    public UserData() {

    }

    public UserData(String firstName, String lastName, String language, String phone, String mobilePhone, String phone2, String administrativeNumber, String picture, List<EmailsData> emails) {
        FirstName = firstName;
        LastName = lastName;
        Language = language;
        Phone = phone;
        MobilePhone = mobilePhone;
        Phone2 = phone2;
        AdministrativeNumber = administrativeNumber;
        Picture = picture;
        Emails = emails;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getLanguage() {
        return Language;
    }

    public void setLanguage(String language) {
        Language = language;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getMobilePhone() {
        return MobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        MobilePhone = mobilePhone;
    }

    public String getPhone2() {
        return Phone2;
    }

    public void setPhone2(String phone2) {
        Phone2 = phone2;
    }

    public String getAdministrativeNumber() {
        return AdministrativeNumber;
    }

    public void setAdministrativeNumber(String administrativeNumber) {
        AdministrativeNumber = administrativeNumber;
    }

    public String getPicture() {
        return Picture;
    }

    public void setPicture(String picture) {
        Picture = picture;
    }

    public List<EmailsData> getEmails() {
        return Emails;
    }

    public void setEmails(List<EmailsData> emails) {
        Emails = emails;
    }

    public class EmailsData {

        private String Email;
        private String Type;

        public EmailsData(String email, String type) {
            Email = email;
            Type = type;
        }

        public String getEmail() {
            return Email;
        }

        public void setEmail(String email) {
            Email = email;
        }

        public String getType() {
            return Type;
        }

        public void setType(String type) {
            Type = type;
        }
    }

}
