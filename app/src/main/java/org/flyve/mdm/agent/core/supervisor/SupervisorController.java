package org.flyve.mdm.agent.core.supervisor;

import android.content.Context;

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
public class SupervisorController {

    private Context context;

    public SupervisorController(Context context) {
        this.context = context;
    }

    public SupervisorModel getCache() {
        SupervisorStorage cache = new SupervisorStorage(context);
        return cache.getSupervisor();
    }

    public boolean save(SupervisorModel supervisor) {
        try {
            SupervisorStorage cache = new SupervisorStorage(context);
            cache.setSupervisor(supervisor);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
