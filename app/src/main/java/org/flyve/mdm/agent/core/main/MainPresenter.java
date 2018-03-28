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

package org.flyve.mdm.agent.core.main;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPresenter implements Main.Presenter {

    private Main.View view;
    private Main.Model model;

    public MainPresenter(Main.View view){
        this.view = view;
        model = new MainModel(this);
    }

    @Override
    public void showError(String message) {
        if(view!=null) {
            view.showError(message);
        }
    }

    @Override
    public Map<String, String> setupDrawer(Activity activity, ListView lst) {
        return model.setupDrawer(activity, lst);
    }

    @Override
    public void onClickItem(FragmentManager fragmentManager, Toolbar toolbar, Map<String, String> item) {
        model.onClickItem(fragmentManager, toolbar, item);
    }

    @Override
    public List<HashMap<String, String>> getMenuItem() {
        return model.getMenuItem();
    }

    @Override
    public void startMQTTService(Context context) {
        model.startMQTTService(context);
    }

    @Override
    public void closeMQTTService(Context context) {
        model.closeMQTTService(context);
    }

    @Override
    public void checkNotifications(Context context) {
        model.checkNotifications(context);
    }
}
