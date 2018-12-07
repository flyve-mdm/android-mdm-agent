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
import android.content.Intent;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.CommonErrorType;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.localstorage.SupervisorData;
import org.flyve.mdm.agent.ui.EnrollmentActivity;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public class DeeplinkModel implements Deeplink.Model {

    private Deeplink.Presenter presenter;

    public DeeplinkModel(Deeplink.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void lint(Context context, String deeplink) {
        String deepLinkErrorMessage = context.getResources().getString(R.string.ERROR_DEEP_LINK);
        String deepLinkData;

        try {
            deepLinkData = Helpers.base64decode(deeplink);
        } catch(Exception ex) {
            presenter.showSnackError( CommonErrorType.DEEPLINK_BASE64DECODE, deepLinkErrorMessage);
            FlyveLog.e(this.getClass().getName() + ", lint", deepLinkErrorMessage + " - " + ex.getMessage());
            return;
        }

        DeeplinkSchema deeplinkSchema = new DeeplinkSchema();

        try {
            // CSV comma-separated values format
            // url; user token; invitation token; support name; support phone, support website; support email
            String[] csv = deepLinkData.split("\\\\;");

            if(csv.length > 0) {

                // url
                if(!csv[0].isEmpty()) {
                    String url = csv[0];
                    deeplinkSchema.setUrl(url);
                } else {
                    deepLinkErrorMessage = "URL " + deepLinkErrorMessage;
                    presenter.showSnackError( CommonErrorType.DEEPLINK_URL_EMPTY, deepLinkErrorMessage );
                    return;
                }

                // user token
                if(!csv[1].isEmpty()) {
                    String userToken = csv[1];
                    deeplinkSchema.setUserToken(userToken);
                } else {
                    deepLinkErrorMessage = "USER " + deepLinkErrorMessage;
                    presenter.showSnackError( CommonErrorType.DEEPLINK_USER_TOKEN, deepLinkErrorMessage );
                    return;
                }

                // invitation token
                if(!csv[2].isEmpty()) {
                    String invitationToken = csv[2];
                    deeplinkSchema.setInvitationToken(invitationToken);
                } else {
                    deepLinkErrorMessage = "TOKEN " + deepLinkErrorMessage;
                    presenter.showSnackError( CommonErrorType.DEEPLINK_INVITATION_TOKEN, deepLinkErrorMessage );
                    return;
                }

                // name
                if(csv.length > 3 && !csv[3].isEmpty()) {
                    String name = csv[3];
                    deeplinkSchema.setName(name);
                }

                // phone
                if(csv.length > 4 && !csv[4].isEmpty()) {
                    String phone = csv[4];
                    deeplinkSchema.setPhone(phone);
                }

                // website
                if(csv.length > 5 && !csv[5].isEmpty()) {
                    String website = csv[5];
                    deeplinkSchema.setWebsite(website);
                }

                // email
                if(csv.length > 6 && !csv[6].isEmpty()) {
                    String email = csv[6];
                    deeplinkSchema.setEmail(email);
                }
            } else {
                presenter.showSnackError( CommonErrorType.DEEPLINK_CSV_WRONG_FORMAT, deepLinkErrorMessage );
            }

            // Success
            presenter.lintSuccess(deeplinkSchema);

        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", lint", ex.getMessage());
            presenter.showSnackError( CommonErrorType.DEEPLINK_GENERAL_EXCEPTION, deepLinkErrorMessage );
        }
    }

    @Override
    public void saveSupervisor(Context context, String name, String phone, String webSite, String email) {
        SupervisorData supervisorData = new SupervisorData(context);

        // name
        supervisorData.setName(name);

        // phone
        supervisorData.setPhone(phone);

        // website
        supervisorData.setWebsite(webSite);

        // email
        supervisorData.setEmail(email);
    }

    @Override
    public void saveMQTTConfig(Context context, String url, String userToken, String invitationToken) {
        MqttData cache = new MqttData(context);
        cache.setUrl(url);
        cache.setUserToken(userToken);
        cache.setInvitationToken(invitationToken);
    }

    @Override
    public void openEnrollment(final Activity activity, final int request) {

        EnrollmentHelper sessionToken = new EnrollmentHelper(activity);
        sessionToken.getActiveSessionTokenEnrollment(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String data) {
                // Active EnrollmentHelper Token is stored on cache
                openEnrollmentActivity(activity, request);
                presenter.openEnrollSuccess();
            }

            @Override
            public void onError(int type, String error) {
                presenter.showSnackError( type, error );
                presenter.openEnrollFail();
            }
        });
    }

    /**
     * Open enrollment
     */
    private void openEnrollmentActivity(Activity activity, int request) {
        Intent miIntent = new Intent(activity, EnrollmentActivity.class);
        activity.startActivityForResult(miIntent, request);
    }

}
