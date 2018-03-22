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

package org.flyve.mdm.agent.core.permission;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.utils.Helpers;

public class PermissionModel implements Permission.Model {

    private Permission.Presenter presenter;

    public PermissionModel(Permission.Presenter presenter) {
        this.presenter = presenter;
    }


    @Override
    public void showDialogShare(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context );
        builder.setTitle(R.string.dialog_share_title);

        final int[] type = new int[1];

        //list of items
        String[] items = context.getResources().getStringArray(R.array.export_list);
        builder.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        type[0] = which;
                    }
                });

        String positiveText = context.getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                        Helpers.share(context, "Inventory File", type[0] );
                    }
                });

        String negativeText = context.getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button logic
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();

    }

    @Override
    public void generateInventory(final Context context) {
        final ProgressDialog progress = ProgressDialog.show(context, "MDM Agent",
                "Creating inventory...", true);

        InventoryTask inventoryTask = new InventoryTask(context, "", true);
        inventoryTask.getXML(new InventoryTask.OnTaskCompleted() {
            @Override
            public void onTaskSuccess(String s) {
                progress.setMessage("Creating session...");

                EnrollmentHelper sessionToken = new EnrollmentHelper(context);
                sessionToken.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
                    @Override
                    public void onSuccess(String data) {
                        // Active EnrollmentHelper Token is stored on cache
                        progress.dismiss();

                        presenter.inventorySuccess();
                    }

                    @Override
                    public void onError(String error) {
                        presenter.showError(error);
                    }
                });
            }

            @Override
            public void onTaskError(Throwable throwable) {
                presenter.showError("The inventory fail");
            }
        });

    }
}
