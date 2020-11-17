package com.example.taximagangue.Actividades.Clientes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.taximagangue.R;
import com.example.taximagangue.models.ClientBooking;
import com.example.taximagangue.models.FCMBody;
import com.example.taximagangue.models.FCMResponse;
import com.example.taximagangue.provider.AuthProvider;
import com.example.taximagangue.provider.ClientBookingProvider;
import com.example.taximagangue.provider.GeoFireProvider;
import com.example.taximagangue.provider.GoogleApiProvider;
import com.example.taximagangue.provider.NotificationProvider;
import com.example.taximagangue.provider.TokenProvider;
import com.example.taximagangue.util.DecodePoints;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.InternalHelpers;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {
    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;

    private GeoFireProvider mGeoFireProvider;

    private LatLng mOriginlatLng;
    private double mRadius = 0.1;

    private boolean mDriverFound = false;
    private String mIdDriverFound = "";
    private LatLng mDriverFoundLatLng;
    private double mExtraOriginLat;
    private double mExtraOriginLog;
    private String mExtraOrigin;
    private  String mExtraDestination;
    private double mExtraDestinationLat;
    private double mExtraDestinationLog;
    private LatLng mDestinationLatLng;
    private GoogleApiProvider mGoogleApiProvider;

    private NotificationProvider mNotificationProvider;

    private TokenProvider mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;
    private AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);
        mAnimation = findViewById(R.id.animation);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelarRequest);


        mAnimation.playAnimation();
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLog = getIntent().getDoubleExtra("destination_log", 0);
        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLog = getIntent().getDoubleExtra("origin_log", 0);
        mGoogleApiProvider = new GoogleApiProvider(RequestDriverActivity.this);

        mDestinationLatLng = new LatLng(mExtraDestinationLat,mExtraDestinationLog);

        mOriginlatLng = new LatLng(mExtraOriginLat,mExtraOriginLog);
        mGeoFireProvider= new GeoFireProvider();
        mNotificationProvider  = new NotificationProvider();
        mTokenProvider = new TokenProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mAuthProvider = new AuthProvider();


        getClosestDriver();


    }

    private void getClosestDriver(){
        mGeoFireProvider.getActivarConductor(mOriginlatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!mDriverFound){
                    mDriverFound = true;
                    mIdDriverFound = key;
                    mDriverFoundLatLng = new LatLng(location.latitude, location.longitude);
                    mTextViewLookingFor.setText("CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA");
                    createClientBooking();
                    Log.d("DRIVER","ID:"+ mIdDriverFound);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                //REGRESA CUANDO TERMINA LA BUSQUEDAD DEL CONDUCTOR EN UN RADIO DE 0.1 KM
                if (!mDriverFound){
                    mRadius = mRadius + 0.1f;
                    // NO SE ENCONTRO UN CONDUCTOR
                    if (mRadius > 5){
                        mTextViewLookingFor.setText("NO SE ENCONTRO UN CONDUCTOR");
                        Toast.makeText(RequestDriverActivity.this , " NO SE ENCONTRO UN CONDUCTOR", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        getClosestDriver();
                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private  void createClientBooking(){
        mGoogleApiProvider.getDirections(mOriginlatLng , mDestinationLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
                    sendNotification(durationText,distanceText);


                }catch (Exception e){
                    Log.d("Error", "Error Encontrado" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    private void sendNotification(final String time, final String km) {
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String token = snapshot.child("device_token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title","SOLICITUD DE SERVICIO A " + time + "DE TU POSICION ");
                    map.put("body", "Un Cliente Esta Solicitando Un Servicio a una Distancia de " + km);
                    FCMBody fcmBody = new FCMBody(token , "high", map);
                    mNotificationProvider.setdNotifcation(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null){
                                if (response.body().getSuccess() == 1){
                                    ClientBooking clientBooking = new ClientBooking(
                                            mAuthProvider.getId(),
                                            mIdDriverFound,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            time,
                                            km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLog,
                                            mExtraDestinationLat,
                                            mExtraDestinationLog
                                    );
                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RequestDriverActivity.this, "La Peticion se creo correctamete", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    //Toast.makeText(RequestDriverActivity.this, "La Notificacion Se Ha Enviado Correctamente", Toast.LENGTH_SHORT).show();

                                }else {
                                    Toast.makeText(RequestDriverActivity.this, "No Se Pudo Enviar La Notificacion ", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(RequestDriverActivity.this, "No Se Pudo Enviar La Notificacion ", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error" + t.getMessage());

                        }
                    });
                }
                else{
                    Toast.makeText(RequestDriverActivity.this, "No Se Pudo Enviar La Notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}