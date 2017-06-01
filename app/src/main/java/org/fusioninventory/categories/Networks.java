package org.fusioninventory.categories;

import org.fusioninventory.FusionInventory;

import android.app.Service;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Networks extends Categories {

	/**
     * 
     */
	private static final long serialVersionUID = 6829495385432791427L;

	public Networks(Context xCtx) {
		super(xCtx);
		// TODO Auto-generated constructor stub

		WifiManager pWM = (WifiManager) xCtx
				.getSystemService(Service.WIFI_SERVICE);

		boolean wasWifiEnabled = pWM.isWifiEnabled();

		// Enable Wifi State if not
		if (!wasWifiEnabled) {
			pWM.setWifiEnabled(true);
		}
		Category c = new Category(xCtx, "NETWORKS");
		c.put("TYPE", "WIFI");

		DhcpInfo dhcp = pWM.getDhcpInfo();
		WifiInfo wifi = pWM.getConnectionInfo();

		FusionInventory.log(this, "<===WIFI INFO===>", Log.VERBOSE);
		if (wifi.getMacAddress() != null) {
			FusionInventory.log(this, "Mac Adress=" + wifi.getMacAddress(),
					Log.VERBOSE);
			c.put("MACADDR", wifi.getMacAddress());
		}
		FusionInventory.log(this, "Link Speed=" + wifi.getLinkSpeed()
				+ WifiInfo.LINK_SPEED_UNITS, Log.VERBOSE);
		c.put("SPEED", String.valueOf(wifi.getLinkSpeed()));
		if (wifi.getBSSID() != null) {
			c.put("BSSID", String.valueOf(wifi.getBSSID()));
		}
		if (wifi.getSSID() != null) {
			c.put("SSID", String.valueOf(wifi.getBSSID()));
		}

		FusionInventory.log(this, "<===WIFI DHCP===>", Log.VERBOSE);
		FusionInventory.log(this, "dns1=" + StringUtils.int_to_ip(dhcp.dns1),
				Log.VERBOSE);

		FusionInventory.log(this, "dns2=" + StringUtils.int_to_ip(dhcp.dns2),
				Log.VERBOSE);
		FusionInventory.log(this, "gateway="
				+ StringUtils.int_to_ip(dhcp.gateway), Log.VERBOSE);
		c.put("IPGATEWAY", StringUtils.int_to_ip(dhcp.gateway));
		FusionInventory.log(this, "ipAddress="
				+ StringUtils.int_to_ip(dhcp.ipAddress), Log.VERBOSE);
		c.put("IPADDRESS", StringUtils.int_to_ip(dhcp.ipAddress));
		FusionInventory.log(this, "leaseDuration=" + dhcp.leaseDuration,
				Log.VERBOSE);
		FusionInventory.log(this, "netmask="
				+ StringUtils.int_to_ip(dhcp.netmask), Log.VERBOSE);
		c.put("IPMASK", StringUtils.int_to_ip(dhcp.netmask));
		FusionInventory.log(this, "serverAdress="
				+ StringUtils.int_to_ip(dhcp.serverAddress), Log.VERBOSE);
		c.put("IPDHCP", StringUtils.int_to_ip(dhcp.serverAddress));

		this.add(c);
		// Restore Wifi State
		if (!wasWifiEnabled) {
			pWM.setWifiEnabled(false);
		}

		/*
		 * ConnectivityManager CM = (ConnectivityManager)
		 * mCtx.getSystemService(Service.CONNECTIVITY_SERVICE);
		 * 
		 * NetworkInfo[] list = CM.getAllNetworkInfo();
		 * 
		 * 
		 * for( NetworkInfo e : list ) { c = new Category(xCtx, "NETWORKS");
		 * c.put("TYPE", e.getTypeName()); c.put("SUBTYPE", e.getSubtypeName());
		 * c.put("STATE",e.getState().toString());
		 * 
		 * this.add(c); }
		 */

		// TODO Use java.net.NetworkInterface with android 2.3+
		// (More informations will be unlocked)
		/*
		 * Enumeration<NetworkInterface> pNetIfs = null; NetworkInterface pNetIf
		 * = null; try { pNetIfs = NetworkInterface.getNetworkInterfaces(); }
		 * catch (SocketException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 * 
		 * 
		 * if(pNetIfs != null) { while (pNetIfs.hasMoreElements()) { //Category
		 * c = new Category(xCtx, "NETWORKS"); pNetIf = pNetIfs.nextElement();
		 * if(pNetIf != null) { //c.put("DESCRIPTION", pNetIf.getDisplayName());
		 * 
		 * 
		 * FusionInventory.log(this, "DESCRIPTION = "+pNetIf.getDisplayName() ,
		 * Log.VERBOSE);
		 * 
		 * InetAddress inet; Enumeration<InetAddress> inets =
		 * pNetIf.getInetAddresses(); while ( inets.hasMoreElements()) { inet =
		 * inets.nextElement(); FusionInventory.log(this,
		 * "|-> INET = "+inet.getHostAddress() , Log.VERBOSE);
		 * FusionInventory.log(this, "|-> HOST = "+inet.getHostName() ,
		 * Log.VERBOSE);
		 * 
		 * }
		 * 
		 * } }
		 * 
		 * }
		 */
	}

}
