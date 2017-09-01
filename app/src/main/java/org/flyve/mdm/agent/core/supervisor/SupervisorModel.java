package org.flyve.mdm.agent.core.supervisor;

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
public class SupervisorModel {

    private String name;
    private String email;
    private String phone;
    private String website;
    private String picture;

    public SupervisorModel() {
    }

    public SupervisorModel(String name, String email, String phone, String website, String picture) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.website = website;
        this.picture = picture;
    }

    /**
     * Get the name of the Supervisor Model
     * @return string the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the Supervisor Model
     * @param string the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the email of the Supervisor Model
     * @return string the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the email of the Supervisor Model
     * @param string the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the phone of the Supervisor Model
     * @return string the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set the phone of the Supervisor Model
     * @param string the phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
