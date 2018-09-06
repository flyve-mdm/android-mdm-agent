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

public class DeeplinkPresenter implements Deeplink.Presenter {

    private Deeplink.View view;
    private Deeplink.Model model;

    public DeeplinkPresenter(Deeplink.View view){
        this.view = view;
        model = new DeeplinkModel(this);
    }


    @Override
    public void showSnackError(int type, String message) {
        if(view!=null) {
            view.showSnackError(type, message);
        }
    }

    @Override
    public void lintSuccess(DeeplinkSchema deeplinkSchema) {
        if(view!=null) {
            view.lintSuccess(deeplinkSchema);
        }
    }

    @Override
    public void openEnrollSuccess() {
        if(view!=null) {
            view.openEnrollSuccess();
        }
    }

    @Override
    public void openEnrollFail() {
        if(view!=null) {
            view.openEnrollFail();
        }
    }

    @Override
    public void lint(Context context, String deeplink) {
        model.lint(context, deeplink);
    }

    @Override
    public void saveSupervisor(Context context, String name, String phone, String webSite, String email) {
        model.saveSupervisor(context, name, phone, webSite, email);
    }

    @Override
    public void saveMQTTConfig(Context context, String url, String userToken, String invitationToken) {
        model.saveMQTTConfig(context, url, userToken, invitationToken);
    }

    @Override
    public void openEnrollment(Activity activity, int request) {
        model.openEnrollment(activity, request);
    }
}
