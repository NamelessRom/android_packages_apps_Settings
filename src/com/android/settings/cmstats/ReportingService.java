/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cmstats;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.android.settings.R;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import java.util.Map;

public class ReportingService extends Service {
    /* package */ static final String TAG = "NamelessStats";

    private StatsUploadTask mTask;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "User has opted in -- reporting.");

        if (mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED) {
            mTask = new StatsUploadTask();
            mTask.execute();
        }

        return Service.START_REDELIVER_INTENT;
    }

    private class StatsUploadTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            String deviceId = Utilities.getUniqueID(getApplicationContext());
            String deviceName = Utilities.getDevice();
            String deviceVersion = Utilities.getModVersion();
            String deviceCountry = Utilities.getCountryCode(getApplicationContext());
            String deviceCarrier = Utilities.getCarrier(getApplicationContext());
            String deviceCarrierId = Utilities.getCarrierId(getApplicationContext());

            Log.d(TAG, "SERVICE: Device ID=" + deviceId);
            Log.d(TAG, "SERVICE: Device Name=" + deviceName);
            Log.d(TAG, "SERVICE: Device Version=" + deviceVersion);
            Log.d(TAG, "SERVICE: Country=" + deviceCountry);
            Log.d(TAG, "SERVICE: Carrier=" + deviceCarrier);
            Log.d(TAG, "SERVICE: Carrier ID=" + deviceCarrierId);

            // report to google analytics
            Tracker tracker = GoogleAnalytics.getInstance(ReportingService.this)
                    .getTracker(getString(R.string.ga_trackingId));
            tracker.send(createMap(deviceName, deviceVersion, deviceCountry));

            String deviceVersionNoDevice = Utilities.getModVersionNoDevice();
            if (deviceVersionNoDevice != null) {
                tracker.send(createMap("checkin", deviceName, deviceVersionNoDevice));
            }

            // report to the cmstats service
            /*HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("https://stats.cyanogenmod.org/submit");
            boolean success = false;

            try {
                List<NameValuePair> kv = new ArrayList<NameValuePair>(5);
                kv.add(new BasicNameValuePair("device_hash", deviceId));
                kv.add(new BasicNameValuePair("device_name", deviceName));
                kv.add(new BasicNameValuePair("device_version", deviceVersion));
                kv.add(new BasicNameValuePair("device_country", deviceCountry));
                kv.add(new BasicNameValuePair("device_carrier", deviceCarrier));
                kv.add(new BasicNameValuePair("device_carrier_id", deviceCarrierId));

                httpPost.setEntity(new UrlEncodedFormEntity(kv));
                httpClient.execute(httpPost);

                success = true;
            } catch (IOException e) {
                Log.w(TAG, "Could not upload stats checkin", e);
            }*/

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            final Context context = ReportingService.this;
            long interval;

            if (result) {
                final SharedPreferences prefs = AnonymousStats.getPreferences(context);
                prefs.edit().putLong(AnonymousStats.ANONYMOUS_LAST_CHECKED,
                        System.currentTimeMillis()).apply();
                // use set interval
                interval = 0;
            } else {
                // error, try again in 3 hours
                interval = 3L * 60L * 60L * 1000L;
            }

            ReportingServiceManager.setAlarm(context, interval);
            stopSelf();
        }
    }

    private Map<String, String> createMap(String category, String action, String label) {
        return MapBuilder.createEvent(category,     // Event category (required)
                action,                     // Event action (required)
                label,                      // Event label
                null)                       // Event value
                .build();
    }
}
