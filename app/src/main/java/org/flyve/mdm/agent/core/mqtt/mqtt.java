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

import android.app.Activity;
import android.content.Context;

public interface Deeplink {

    interface View {
        void showSnackError(int type, String message);
        void lintSuccess(DeeplinkSchema deeplinkSchema);
        void openEnrollSuccess();
        void openEnrollFail();

    }

    interface Presenter {
        // Views
        void showSnackError(int type, String message);
        void lintSuccess(DeeplinkSchema deeplinkSchema);
        void openEnrollSuccess();
        void openEnrollFail();

        // Models
        void lint(Context context, String deeplink);
        void saveSupervisor(Context context, String name, String phone, String webSite, String email);
        void saveMQTTConfig(Context context, String url, String userToken, String invitationToken);
        void openEnrollment(final Activity activity, final int request);
    }

    interface Model {
        void lint(Context context, String deeplink);
        void saveSupervisor(Context context, String name, String phone, String webSite, String email);
        void saveMQTTConfig(Context context, String url, String userToken, String invitationToken);
        void openEnrollment(final Activity activity, final int request);
    }
}
