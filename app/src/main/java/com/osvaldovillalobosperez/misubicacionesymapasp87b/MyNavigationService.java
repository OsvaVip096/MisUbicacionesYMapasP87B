package com.osvaldovillalobosperez.misubicacionesymapasp87b;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MyNavigationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public MyNavigationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private final IBinder binder = new MiBinder();

    public class MiBinder extends Binder {
        public MyNavigationService getService() {
            return MyNavigationService.this;
        }
    }

    public IBinder getBinder() {
        return binder;
    }

    MainActivity mainActivity = new MainActivity();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String recibirLocation = intent.getStringExtra("EnviarLoc");
        IniciarServicio(recibirLocation);
        return START_STICKY_COMPATIBILITY;
    }

    private void IniciarServicio(String msj) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(MyNavigationService.this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                MyNavigationService.this,
                0,
                notificationIntent,
                0
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Mi Ubicación")
                .setContentText("La ubicación actual es: " + msj)
                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
