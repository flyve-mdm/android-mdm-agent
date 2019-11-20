/*
 *   Copyright  Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @author    @rafaelje
 * @copyright Copyright  Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.data.database;

import android.content.Context;

import org.flyve.mdm.agent.data.database.entity.Application;
import org.flyve.mdm.agent.data.database.setup.AppDataBase;

public class ApplicationData {

    private AppDataBase dataBase;

    public ApplicationData(Context context) {
        dataBase = AppDataBase.getAppDatabase(context);
    }

    public Application[] getAllApplications() {
        return dataBase.applicationDao().loadAll();
    }

    public Application[] getApplicationsById(String id) {
        return dataBase.applicationDao().getApplicationById(id);
    }

    public void create(Application app) {
        dataBase.applicationDao().insert(app);
    }

    public void updateStatus(String id, String status) {
        dataBase.applicationDao().updateStatus(id, status);
    }

    public void updateVersionCode(String id, String version) {
        dataBase.applicationDao().updateVersionCode(id, version);
    }

    public void deleteAll() {
        dataBase.applicationDao().deleteAll();
    }

    public void deleteByPackageName(String mPackage) { dataBase.applicationDao().deleteByPackageName(mPackage);}

    public void deleteById(String id) { dataBase.applicationDao().deleteById(id);}
}
