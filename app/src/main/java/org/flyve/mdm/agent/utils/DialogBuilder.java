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

package org.flyve.mdm.agent.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.flyve.mdm.agent.R;

public class DialogBuilder extends AlertDialog.Builder
{
	private final View customTitle;
	private final ImageView iconView;
	private final TextView titleView;

	public static DialogBuilder warn(final Context context, final int titleResId)
	{
		final DialogBuilder builder = new DialogBuilder(context);
		builder.setIcon(R.drawable.ic_warning_grey600_24dp);
		builder.setTitle(titleResId);
		return builder;
	}

	public DialogBuilder(final Context context)
	{
		super(context, Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? AlertDialog.THEME_HOLO_LIGHT : AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

		this.customTitle = LayoutInflater.from(context).inflate(R.layout.dialog_title, null);
		this.iconView = (ImageView) customTitle.findViewById(android.R.id.icon);
		this.titleView = (TextView) customTitle.findViewById(android.R.id.title);
	}

	@Override
	public DialogBuilder setIcon(final Drawable icon)
	{
		if (icon != null)
		{
			setCustomTitle(customTitle);
			iconView.setImageDrawable(icon);
			iconView.setVisibility(View.VISIBLE);
		}

		return this;
	}

	@Override
	public DialogBuilder setIcon(final int iconResId)
	{
		if (iconResId != 0)
		{
			setCustomTitle(customTitle);
			iconView.setImageResource(iconResId);
			iconView.setVisibility(View.VISIBLE);
		}

		return this;
	}

	@Override
	public DialogBuilder setTitle(final CharSequence title)
	{
		if (title != null)
		{
			setCustomTitle(customTitle);
			titleView.setText(title);
		}

		return this;
	}

	@Override
	public DialogBuilder setTitle(final int titleResId)
	{
		if (titleResId != 0)
		{
			setCustomTitle(customTitle);
			titleView.setText(titleResId);
		}

		return this;
	}

	@Override
	public DialogBuilder setMessage(final CharSequence message)
	{
		super.setMessage(message);

		return this;
	}

	@Override
	public DialogBuilder setMessage(final int messageResId)
	{
		super.setMessage(messageResId);

		return this;
	}

	public DialogBuilder singleDismissButton(@Nullable final DialogInterface.OnClickListener dismissListener)
	{
		setNeutralButton(R.string.button_dismiss, dismissListener);

		return this;
	}
}
