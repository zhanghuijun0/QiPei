/*
 *    This file is part of GPSLogger for Android.
 *
 *    GPSLogger for Android is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    GPSLogger for Android is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gammainfo.qipei;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.gammainfo.qipei.utils.Constant;

class GeneralLocationListener implements LocationListener, GpsStatus.Listener {
	public static final String TAG = GeneralLocationListener.class
			.getSimpleName();
	private static GpsService mainActivity;

	GeneralLocationListener(GpsService activity) {
		mainActivity = activity;
	}

	/**
	 * Event raised when a new fix is received.
	 */
	public void onLocationChanged(Location loc) {

		try {
			Log.d(TAG, "----GeneralLocationListener---onLocationChanged----");
			if (loc != null) {
				mainActivity.onLocationChanged(loc);
			}

		} catch (Exception ex) {
			if (Constant.DEBUG) {
				ex.printStackTrace();
			}
		}

	}

	public void onProviderDisabled(String provider) {
		mainActivity.restartGpsManagers();
	}

	public void onProviderEnabled(String provider) {
		mainActivity.restartGpsManagers();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.OUT_OF_SERVICE) {
			Log.d(TAG, provider + " is out of service");
			// mainActivity.StopManagerAndResetAlarm();
		}

		if (status == LocationProvider.AVAILABLE) {
			Log.d(TAG, provider + " is available");
		}

		if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			Log.d(TAG, provider + " is temporarily unavailable");
			// mainActivity.StopManagerAndResetAlarm();
		}
	}

	public void onGpsStatusChanged(int event) {

		switch (event) {
		case GpsStatus.GPS_EVENT_FIRST_FIX:
			Log.d(TAG, "GPS Event First Fix");
			// mainActivity.SetStatus("fix_obtained");
			break;

		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

			Log.d(TAG, "GPS Satellite status obtained");
			// GpsStatus status = mainActivity.gpsLocationManager
			// .getGpsStatus(null);
			//
			// int maxSatellites = status.getMaxSatellites();
			//
			// Iterator<GpsSatellite> it = status.getSatellites().iterator();
			// int count = 0;
			//
			// while (it.hasNext() && count <= maxSatellites) {
			// it.next();
			// count++;
			// }

			break;

		case GpsStatus.GPS_EVENT_STARTED:
			Log.d(TAG, "GPS started, waiting for fix");
			// mainActivity.SetStatus("started_waiting");
			break;

		case GpsStatus.GPS_EVENT_STOPPED:
			Log.d(TAG, "GPS Stopped");
			// mainActivity.SetStatus("gps_stopped");
			break;

		}
	}

}
