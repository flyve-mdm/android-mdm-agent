package org.flyve.mdm.agent.core.user;

import android.content.Context;

import com.google.gson.Gson;

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

    private static final String LOCAL_STORAGE = "userObject";

    /**
     * Constructor
     *
     * @param context
     */
    public UserStorage(Context context) {
        super(context);
    }

    /**
     * Get the user local storage
     * @return string the local storage
     */
    public UserModel getUser() {
        String json = getData(LOCAL_STORAGE);
        Gson gson = new Gson();
        return gson.fromJson(json, UserModel.class);
    }

    /**
     * Set the user local storage
     * @param UserModel user
     */
    public void setUser(UserModel user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        setData(LOCAL_STORAGE, json);
    }

    public void deleteUser() {
        deleteKeyCache(LOCAL_STORAGE);
    }

}
