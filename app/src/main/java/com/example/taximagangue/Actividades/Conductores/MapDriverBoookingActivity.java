package com.example.taximagangue.Actividades.Conductores;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taximagangue.Actividades.Clientes.RequestDriverActivity;
import com.example.taximagangue.R;
import com.example.taximagangue.models.ClientBooking;
import com.example.taximagangue.models.FCMBody;
import com.example.taximagangue.models.FCMResponse;
import com.example.taximagangue.provider.AuthProvider;
import com.example.taximagangue.provider.ClientBookingProvider;
import com.example.taximagangue.provider.ClientProvider;
import com.example.taximagangue.provider.GeoFireProvider;
import com.example.taximagangue.provider.GoogleApiProvider;
import com.example.taximagangue.provider.NotificationProvider;
import com.example.taximagangue.provider.TokenProvider;
import com.example.taximagangue.util.DecodePoints;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDriverBoookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private NotificationProvider mNotificationProvider;

    private GoogleMap tMap;
    private SupportMapFragment tMapFragment;
    private AuthProvider tAuthProvider;

    private LocationRequest tLocationRequest;
    private FusedLocationProviderClient tFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private LatLng tCurrentLatLng;
    private Marker tMarker;
    private GeoFireProvider tGeoFireProvider;

    private TokenProvider mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;
    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;

    private String mExtraClientId;
    private ClientProvider mClientProvider;
    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private Button mButtonStartBooking;
    private Button mButtonFinishBooking;
    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private boolean tFirsTime = true;
    private boolean mIsCloseToClient = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_boooking);

        mNotificationProvider  = new NotificationProvider();

        mButtonStartBooking = findViewById(R.id.btnStartBooking);
        mButtonFinishBooking = findViewById(R.id.btnFinishBooking);

        tAuthProvider = new AuthProvider();
        tGeoFireProvider = new GeoFireProvider("Conductores_Trabajando");
        tFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mClientProvider = new ClientProvider();
        tMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        tMapFragment.getMapAsync(this);

        mTextViewClientBooking = findViewById(R.id.textViewClientBooking);
        mTextViewEmailClientBooking = findViewById(R.id.textViewEmailClientBooking);
        mClientBookingProvider = new ClientBookingProvider();
        mExtraClientId = getIntent().getStringExtra("idClient");

        mTokenProvider = new TokenProvider();
        mTextViewOriginClientBooking = findViewById(R.id.textViewOriginClientBooking);
        mTextViewDestinationClientBooking = findViewById(R.id.textViewDestinationClientBooking);
        mGoogleApiProvider = new GoogleApiProvider(MapDriverBoookingActivity.this);
        getClient();

        //mButtonStartBooking.setEnabled(false);

        mButtonStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCloseToClient){
                    startBooking();
                }else{
                    Toast.makeText(MapDriverBoookingActivity.this, "Debes Estar Mas Cerca  A La Posicion  de Recogida", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mButtonFinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishBooking();
            }
        });

    }

    LocationCallback tLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    tCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (tMarker != null) {
                        tMarker.remove();
                    }
                    tMarker = tMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            ).title("Tu Posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi))
                    );

                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    tMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));

                    updateLocation();
                    if (tFirsTime){
                        tFirsTime=false;
                        getClientBooking();
                    }
                }
            }
        }
    };
    private void updateLocation() {
        if (tAuthProvider.existSesion() && tCurrentLatLng != null) {
            tGeoFireProvider.saveLocation(tAuthProvider.getId(), tCurrentLatLng);
            if (!mIsCloseToClient){
                if (mOriginLatLng != null && tCurrentLatLng != null){
                double distance = getDistanceDetween(mOriginLatLng, tCurrentLatLng);
                if (distance <= 300) {
                    //mButtonStartBooking.setEnabled(true);
                    mIsCloseToClient = true;
                    Toast.makeText(MapDriverBoookingActivity.this, "Estas Cerca A La Posicion Del Cliente", Toast.LENGTH_SHORT).show();


                }

                }
            }

        }

    }



    private void finishBooking() {
        mClientBookingProvider.updateestatus(mExtraClientId, "finish");
        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId);
        sendNotification("Viaje Finalizado");
        if (tFusedLocation != null){
            tFusedLocation.removeLocationUpdates(tLocationCallback);
        }
        tGeoFireProvider.removeLocation(tAuthProvider.getId());
        Intent intent = new Intent(MapDriverBoookingActivity.this ,  CalificacionClientActivity.class);
        intent.putExtra("idClient", mExtraClientId);
        startActivity(intent);
        finish();
    }

    private void startBooking() {
        mClientBookingProvider.updateestatus(mExtraClientId, "start");
        mButtonStartBooking.setVisibility(View.GONE);
        mButtonFinishBooking.setVisibility(View.VISIBLE);
        tMap.clear();
        tMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

        drawRoute(mDestinationLatLng);
        sendNotification("Viaje Iniciado");
    }

    private  double getDistanceDetween(LatLng clientLatLng, LatLng driverLatLng){
        double distance = 0;
        Location ClientLocation = new Location("");
        Location DriverLocation = new Location("");
        ClientLocation.setLatitude(clientLatLng.latitude);
        ClientLocation.setLongitude(clientLatLng.longitude);
        DriverLocation.setLatitude(driverLatLng.latitude);
        DriverLocation.setLongitude(driverLatLng.longitude);
        distance = ClientLocation.distanceTo(DriverLocation);
        return distance;


    }

    private void getClientBooking() {
    mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()){
                String destino = snapshot.child("destination").getValue().toString();
                String origin = snapshot.child("origin").getValue().toString();
                double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                double destinationLog = Double.parseDouble(snapshot.child("destinationLog").getValue().toString());

                double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                double originLog = Double.parseDouble(snapshot.child("originLog").getValue().toString());
                mOriginLatLng = new LatLng(originLat, originLog);
                mDestinationLatLng = new LatLng(destinationLat, destinationLog);

                mTextViewOriginClientBooking.setText("Recoger En: " + origin);
                mTextViewDestinationClientBooking.setText("destino: " + destino);

                tMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger Aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));

                drawRoute(mOriginLatLng);

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });


    }

    private void drawRoute(LatLng latLng){
        mGoogleApiProvider.getDirections(tCurrentLatLng , latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");

                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    tMap.addPolyline(mPolylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");

                }catch (Exception e){
                    Log.d("Error", "Error Encontrado" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    private void getClient() {
        mClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    mTextViewClientBooking.setText(name);
                    mTextViewEmailClientBooking.setText(email);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        tMap = googleMap;
        tMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        tMap.getUiSettings().setZoomControlsEnabled(true);

        tLocationRequest = new LocationRequest();
        tLocationRequest.setInterval(1000);
        tLocationRequest.setFastestInterval(1000);
        tLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        tLocationRequest.setSmallestDisplacement(5);

        startLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        tFusedLocation.requestLocationUpdates(tLocationRequest, tLocationCallback, Looper.myLooper());
                        tMap.setMyLocationEnabled(true);
                    } else {
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                tFusedLocation.requestLocationUpdates(tLocationRequest, tLocationCallback, Looper.myLooper());
                tMap.setMyLocationEnabled(true);
            }
        }
        else {
            showAlertDialogNOGPS();
        }
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void disconnet(){

        if (tFusedLocation != null){
            tFusedLocation.removeLocationUpdates(tLocationCallback);
            if (tAuthProvider.existSesion()){
                tGeoFireProvider.removeLocation(tAuthProvider.getId());
            }
        }
        else {
            Toast.makeText(MapDriverBoookingActivity.this , "No Te Puedes Desconectar", Toast.LENGTH_SHORT).show();
        }

    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    tFusedLocation.requestLocationUpdates(tLocationRequest, tLocationCallback, Looper.myLooper());
                    tMap.setMyLocationEnabled(true);
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        }else {
            if (gpsActived()) {
                tFusedLocation.requestLocationUpdates(tLocationRequest, tLocationCallback, Looper.myLooper());
                tMap.setMyLocationEnabled(true);
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapDriverBoookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        }).create().show();
            }
            else {
                ActivityCompat.requestPermissions(MapDriverBoookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private void sendNotification(final String status) {
        mTokenProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String token = snapshot.child("device_token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title","ESTADO DE TU VIAJE");
                    map.put("body",
                            "Tu Estado del Viaje Es: " + status
                    );

                    FCMBody fcmBody = new FCMBody(token , "high","4500s",  map);
                    mNotificationProvider.setdNotifcation(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null){
                                if (response.body().getSuccess() != 1){
                                    Toast.makeText(MapDriverBoookingActivity.this, "No Se Pudo Enviar La Notificacion ", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(MapDriverBoookingActivity.this, "No Se Pudo Enviar La Notificacion ", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error" + t.getMessage());

                        }
                    });
                }
                else{
                    Toast.makeText(MapDriverBoookingActivity.this, "No Se Pudo Enviar La Notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}