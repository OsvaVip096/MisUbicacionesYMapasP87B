package com.osvaldovillalobosperez.misubicacionesymapasp87b;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    private static final int MY_PERMISSION_REQUEST_API = 100;
    private static final int MY_PERMISSION_REQUEST_READ_LOCATION = 101;
    private static final int MY_PERMISSION_REQUEST_READ_BACKGROUND_LOCATION = 102;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "actualizar";

    private boolean requestingLocationUpdates = false;

    TextView imprimir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imprimir = findViewById(R.id.txtHelloWorld);

        updateValuesFromBundle(savedInstanceState);

        /* TODO: Verifica permisos en tiempo de ejecución. */
        boolean permissionAccessCoarseLocationApproved =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.
                        PERMISSION_GRANTED;

        if (permissionAccessCoarseLocationApproved) {
            boolean backgroundLocationPermissionApproved =
                    ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;

            /* TODO: Inicia la funcionalidad en segundo plano. */
            createLocationRequest();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                    this
            );

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(
                    this,
                    new OnSuccessListener<Location>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                String msj = "Latitud: " + location.getLatitude() +
                                        "\nLongitud: " + location.getLongitude();

                                LlamarServicio(msj);

                                imprimir.setText(msj);

                                Log.i("UBICACION", msj);
                            } else {
                                Log.i("UBICACION", "Sin ubicación");
                            }
                        }
                    }
            );

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        // Actualizar UI con localización actual.
                        String nuevaLocalizacion = "Nueva latitud: " + location.getLatitude() +
                                "\nNueva longitud: " + location.getLongitude();

                        Toast.makeText(
                                MainActivity.this,
                                nuevaLocalizacion,
                                Toast.LENGTH_LONG
                        ).show();

                        Log.i("CALLBACKLOC", nuevaLocalizacion);
                    }
                }
            };

            if (backgroundLocationPermissionApproved) {
                // App accede a localización en primer y segundo plano.
                requestingLocationUpdates = true;
            } else {
                // App solo accede a localización en primer plano.
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        MY_PERMISSION_REQUEST_READ_BACKGROUND_LOCATION
                );
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST_READ_LOCATION
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void LlamarServicio(String enviarLocation) {
        Intent serviceIntent = new Intent(this, MyNavigationService.class);
        serviceIntent.putExtra("EnviarLoc", enviarLocation);
        startForegroundService(serviceIntent);
    }

    protected void createLocationRequest() {
        /* Primera parte. */
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        /* Segunda parte. */
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

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Obtiene los permisos que se desean permitir para dar acceso a funcionalidades de la app.
     *
     * @param requestCode  Código único del permiso a permitir.
     * @param permissions  Permiso específico que solicita la app.
     * @param grantResults Resultado de la solicitud del permiso, si es concedido o no.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /* El permiso es otorgado. */
                } else {
                    /* El permiso es denegado por el usuario.
                    Se deshabilita la funcionalidad del permiso. */
                }
                return;
            }
            case MY_PERMISSION_REQUEST_READ_BACKGROUND_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY
            );
        }

        //updateUI();
    }
}
