package com.osvaldovillalobosperez.misubicacionesymapasp87b;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int MY_PERMISSION_REQUEST_READ_LOCATION = 101;
    private static final int MY_PERMISSION_REQUEST_API = 102;
    private boolean banderaPermiso = false;

    TextView imprimir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imprimir = findViewById(R.id.txtHelloWorld);

        boolean permissionAccessCoarseLocationApproved =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.
                        PERMISSION_GRANTED;

        if (permissionAccessCoarseLocationApproved) {
            banderaPermiso = true;
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST_READ_LOCATION
            );
        }

        if (banderaPermiso == true) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                    this
            );

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(
                    this,
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                String msj = "Latitud: " + location.getLatitude() +
                                        "\nLongitud: " + location.getLongitude();

                                /*Toast.makeText(
                                        MainActivity.this,
                                        msj,
                                        Toast.LENGTH_LONG
                                ).show();*/

                                imprimir.setText(msj);

                                Log.i("UBICACION", msj);
                            } else {
                                Log.i("UBICACION", "Sin ubicación");
                            }
                        }
                    }
            );
        } else {
            finish();
        }

        createLocationRequest();
    }

    protected void createLocationRequest() {
        /* TODO: Primera parte. */
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        /* TODO: Segunda parte. */
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Esta bien.
                Toast.makeText(
                        MainActivity.this,
                        "El SuccessListener de LocationSettingsResponse funciono" +
                                " correctamente",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Las instancias no están satisfechas.
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(
                                MainActivity.this,
                                MY_PERMISSION_REQUEST_API
                        );
                        Toast.makeText(
                                MainActivity.this,
                                "Fallo el Listener, se necesita la activación de la API",
                                Toast.LENGTH_LONG
                        ).show();
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignorar el error.
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    banderaPermiso = true;
                } else {
                    /* El permiso es denegado por el usuario.
                    Se deshabilita la funcionalidad del permiso. */
                    banderaPermiso = false;
                }
                return;
            }
        }
    }
}
