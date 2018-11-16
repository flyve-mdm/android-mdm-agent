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

import org.flyve.mdm.agent.data.database.entity.MDMLog;
import org.flyve.mdm.agent.data.database.setup.AppDataBase;

public class MDMLogData {

    private AppDataBase dataBase;

    public MDMLogData(Context context) {
        dataBase = AppDataBase.getAppDatabase(context);
    }

    public MDMLog[] getAllFiles() {
        return dataBase.MDMLogDao().loadAll();
    }

    public void deleteAll() {
        dataBase.MDMLogDao().deleteAll();
    }

    public void addLog(String message) {
        MDMLog log = new MDMLog();
        log.description = message;
        dataBase.MDMLogDao().insert(log);
    }
}
