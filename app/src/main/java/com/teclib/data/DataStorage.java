/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   com.teclib.data is part of flyve-mdm-android
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
 * @author    Rafael Hernandez
 * @date      02/06/2017
 * @copyright Copyright © ${YEAR} Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package com.teclib.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataStorage {

	private final String SHARED_PREFS_FILE = "FlyveHMPrefs";
	private Context mContext;

	/**
	 * Constructor 
	 * @param context
	 */
	public DataStorage(Context context){
		mContext = context;
	}

	/**
	 * Obtiene las preferencias del setting
	 * @return tipo de datos SharedPreferences
	 */
	private SharedPreferences getSettings(){
		if (mContext != null) {
		return mContext.getSharedPreferences(SHARED_PREFS_FILE, 0);
		} else {
			return null;
		}
	}


	/**
	 * Retorna el valor almacenado en segun la llave
	 * @param llave
	 * @return
	 */
	public String getVariablePermanente(String llave){
		return getSettings().getString(llave, null);  
	}
	/**
	 * Retorna el valor almacenado en segun la llave boolean
	 * @param llave
	 * @return
	 */
	public Boolean getBooleanPermanente(String llave){
		return getSettings().getBoolean(llave, true);  
	}
	/**
	 * Retorna el valor almacenado en segun la llave entero
	 * @param llave
	 * @return
	 */
	public int getEnteroPermanente(String llave){
		return getSettings().getInt(llave, 0);  
	}

	/**
	 * Guarda las variables en formato llave valor
	 * @param llave
	 * @param valor
	 */
	public void setVariablePermanente(String llave, String valor){
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.putString(llave, valor );
			editor.commit();
		}
	}

	/**
	 * Guarda un valoor boolean especifico
	 * @param llave
	 * @param valor
	 */
	public void setBooleanPermanente(String llave, Boolean valor){
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.putBoolean(llave, valor);
			editor.commit();
		}
	}
	/**
	 * Guarda un valoor Int especifico
	 * @param llave
	 * @param valor
	 */
	public void setEnteroPermanente(String llave, int valor){
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.putInt(llave, valor);
			editor.commit();
		}
	}

	/**
	 * Borra todas las variables SharedPreferences
	 */
	public void clearSettings(){
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.clear();
			editor.commit();
		}
	}

	/**
	 * Elimina el valor de una llave especifica
	 * @param llave
	 */
	public void deleteKeyCache(String llave){
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.remove(llave);
			editor.commit();
		}
	}

	/**
	 * Guarda un Objeto Json 
	 * @param llave
	 * @param object
	 */
	public void saveJSONObject(String llave, JSONObject object) {
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.putString(llave, object.toString());
			editor.commit();
		}
	}

	/**
	 * Devuelve un Objeto Json
	 * @param llave
	 * @return
	 * @throws JSONException
	 */
	public JSONObject loadJSONObject(String llave) throws JSONException {
		return new JSONObject(getSettings().getString(llave, "{}"));
	}	

	/**
	 * Guarda un Array Json 
	 * @param llave
	 * @param array
	 */
	public void saveJSONArray(String llave, JSONArray array) {
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.putString(llave, array.toString());
			editor.commit();
		}
	}

	/**
	 * Devuelve un Array Json
	 * @param llave
	 * @return
	 * @throws JSONException
	 */
	public JSONArray loadJSONArray(String llave) throws JSONException {
		return new JSONArray(getSettings().getString(llave, "[]"));
	}	    

}
