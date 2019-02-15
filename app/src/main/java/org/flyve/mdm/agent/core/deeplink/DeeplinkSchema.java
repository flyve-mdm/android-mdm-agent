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

package org.flyve.mdm.agent.core.deeplink;

public class DeeplinkSchema {

    private String url = "";
    private String userToken = "";
    private String invitationToken = "";
    private String name = "";
    private String phone = "";
    private String website = "";
    private String email = "";

    public DeeplinkSchema() {
    }

    /**
     * Get the message of the Url
     * @return String url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the message of Url
     * @param url String Url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the message of UserToken
     * @return String UserToken
     */
    public String getUserToken() {
        return userToken;
    }

    /**
     * Set the message of UserToken
     * @param userToken
     */
    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    /**
     * Get the message of InvitationToken
     * @return String InvitationToken
     */
    public String getInvitationToken() {
        return invitationToken;
    }

    /**
     * Set the message of InvitationToken
     * @param invitationToken
     */
    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }

    /**
     * Get the message of the Name
     * @return String Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the message of the Name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the message of the Phone
     * @return String phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set the message of the Phone
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Get the message of Website
     * @return String Website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Set the message of Website
     * @param website
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Get the message of Email
     * @return String email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the message of  Email
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
