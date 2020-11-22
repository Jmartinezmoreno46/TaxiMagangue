package com.example.taximagangue.Actividades.Clientes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;


import com.example.taximagangue.R;
import com.example.taximagangue.provider.AuthProvider;
import com.example.taximagangue.provider.ClientBookingProvider;
import com.example.taximagangue.provider.ConductorProvider;
import com.example.taximagangue.provider.GeoFireProvider;
import com.example.taximagangue.provider.GoogleApiProvider;
import com.example.taximagangue.provider.TokenProvider;
import com.example.taximagangue.util.DecodePoints;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final String TAG = null;
    private GoogleMap tMap;
    private SupportMapFragment tMapFragment;
    private AuthProvider tAuthProvider;
    DatabaseReference mDatabaseReference;

    private GeoFireProvider tGeoFireProvider;
    private boolean tIsFirsTime = true;
    private ClientBookingProvider mClientBookingProvider;
    private Marker tMarkerDriver;
    private String mOrigin;
    private LatLng mOriginLatLng;
    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private String mDestination;
    private LatLng mDestinationLatLng;
    private LatLng mDriverLatLng;
    private ConductorProvider mConductorProvider;
    private TextView mTextViewConductorBooking;
    private TextView mTextViewEmailConductorBooking;
    private TextView mTextViewOriginConductorBooking;
    private TextView mTextViewDestinationConductorBooking;

    private TextView mTextViewEstado;

    private TokenProvider mTokenProvider;

    private ValueEventListener mListener;

    private String mIdDriver;
    private ValueEventListener mListenerstatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);
        tAuthProvider = new AuthProvider();
        tGeoFireProvider = new GeoFireProvider("Conductores_Trabajando");
        mConductorProvider = new ConductorProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapClientBookingActivity.this);
        mTokenProvider = new TokenProvider();
        mTextViewConductorBooking = findViewById(R.id.textViewConductorBooking);
        mTextViewEmailConductorBooking = findViewById(R.id.textViewEmailConductorBooking);
        mTextViewOriginConductorBooking = findViewById(R.id.textViewOriginConductorBooking);
        mTextViewDestinationConductorBooking = findViewById(R.id.textViewDestinationConductorBooking);

        mTextViewEstado = findViewById(R.id.textViewStatus);
        mClientBookingProvider = new ClientBookingProvider();
        tMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        tMapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        getStatus();
        getClientBooking();

    }

    private void getStatus() {
        mListenerstatus = mClientBookingProvider.getStatus(tAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String status = snapshot.getValue().toString();

                    if (status.equals("accept")){
                        mTextViewEstado.setText("Estado: Aceptado");
                    }
                    if (status.equals("start")){
                        mTextViewEstado.setText("Estado: Viaje Iniciado");
                        starBooking();

                    }else if (status.equals("finish")){
                        mTextViewEstado.setText("Estado: Viaje Finalizado");
                        finishBooking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishBooking() {
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationDriverActivity.class);
        startActivity(intent);
        finish();
    }

    private void starBooking() {
        tMap.clear();
        tMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
        drawRoute(mDestinationLatLng);
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(tAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String destino = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    String idDriver = snapshot.child("idDriver").getValue().toString();
                    mIdDriver = idDriver;
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLog = Double.parseDouble(snapshot.child("destinationLog").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLog = Double.parseDouble(snapshot.child("originLog").getValue().toString());
                    mOriginLatLng = new LatLng(originLat, originLog);
                    mDestinationLatLng = new LatLng(destinationLat, destinationLog);
                    mTextViewOriginConductorBooking.setText("Recoger En: " + origin);
                    mTextViewDestinationConductorBooking.setText("destino: " + destino);
                    tMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger Aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                    getDriver(idDriver);
                    getDriverLocation(idDriver);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null){
            tGeoFireProvider.getDriverLocation(mIdDriver).removeEventListener(mListener);
        }
        if (mListenerstatus != null){
            mClientBookingProvider.getStatus(tAuthProvider.getId()).removeEventListener(mListenerstatus);
        }
    }

    private void getDriver(String idDriver) {
      mConductorProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("nombre").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    mTextViewConductorBooking.setText(name);
                    mTextViewEmailConductorBooking.setText(email);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getDriverLocation(String idDriver) {
        mListener = tGeoFireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                    double log = Double.parseDouble(snapshot.child("1").getValue().toString());
                    mDriverLatLng = new LatLng(lat,log);
                    if(tMarkerDriver != null){
                        tMarkerDriver.remove();
                    }
                    tMarkerDriver = tMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat,log))
                            .title("Tu Conductor")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));

                    if (tIsFirsTime){
                        tIsFirsTime = false;
                        tMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(mDriverLatLng)
                                        .zoom(14f)
                                        .build()
                        ));
                        drawRoute(mOriginLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void drawRoute(LatLng latLng){
        mGoogleApiProvider.getDirections(mDriverLatLng, latLng).enqueue(new Callback<String>() {
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
                    mPolylineOptions.width(14f);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        tMap = googleMap;
        tMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        tMap.getUiSettings().setZoomControlsEnabled(true);

    }

}