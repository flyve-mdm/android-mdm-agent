package org.flyve.mdm.agent.core;

import android.content.Context;

import org.flyve.mdm.agent.data.LocalStorage;

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
public class UserStorage extends LocalStorage {

    /**
     * Constructor
     *
     * @param context
     */
    public UserStorage(Context context) {
        super(context);
    }

    public String getFirstName() {
        return getData("userFirstName");
    }
    public void setFirstName(String firstName) {
        setData("userFirstName", firstName);
    }

    public String getLastName() {
        return getData("userLastName");
    }
    public void setLastName(String lastName) {
        setData("userLastName", lastName);
    }

    public String getEmail() { return getData("userEmail");}
    public void setEmail(String email) { setData("userEmail", email); }

    public String getPhone() {
        return getData("userPhone");
    }
    public void setPhone(String userPhone) {
        setData("userPhone", userPhone);
    }
}
