package com.example.taximagangue.Actividades.Clientes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taximagangue.Actividades.Conductores.CalificacionClientActivity;
import com.example.taximagangue.Actividades.Conductores.MapConductoresActivity;
import com.example.taximagangue.R;
import com.example.taximagangue.models.ClientBooking;
import com.example.taximagangue.models.HistoryBooking;
import com.example.taximagangue.provider.AuthProvider;
import com.example.taximagangue.provider.ClientBookingProvider;
import com.example.taximagangue.provider.HistoryBookingProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class CalificationDriverActivity extends AppCompatActivity {

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private RatingBar mRatingBar;
    private Button mButtonCalification;

    private ClientBookingProvider mClientBookingProvider;
    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;

    private AuthProvider mAuthProvider;
    private float mCalification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_driver);

        mTextViewOrigin = findViewById(R.id.textViewOriginCalification);
        mTextViewDestination =  findViewById(R.id.textViewDestinationCalification);
        mHistoryBookingProvider = new HistoryBookingProvider();
        mRatingBar = findViewById(R.id.ratingbarCalification);
        mButtonCalification = findViewById(R.id.btnCalifaction);
        mAuthProvider = new AuthProvider();
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calification, boolean fromUser) {
                mCalification  = calification;
            }
        });

        mClientBookingProvider = new ClientBookingProvider();

        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calificate();
            }
        });

        getClientBooking();
    }

    private void getClientBooking(){
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ClientBooking clientBooking = snapshot.getValue(ClientBooking.class);
                    mTextViewOrigin.setText(clientBooking.getOrigin());
                    mTextViewDestination.setText(clientBooking.getDestination());
                    mHistoryBooking = new HistoryBooking(
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLog(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLog(),
                            clientBooking.getIdHistoryBooking()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void calificate() {

        if (mCalification > 0){
            mHistoryBooking.setCalificationDriver(mCalification);
            mHistoryBooking.setTimeStang(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        mHistoryBookingProvider.updateCalificationDriver(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationDriverActivity.this, "La Calificacion Se Guardo Correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationDriverActivity.this, MapClienteActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        });
                    }
                    else{
                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationDriverActivity.this, "La Calificacion Se Guardo Correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationDriverActivity.this, MapClienteActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else{
            Toast.makeText(this, "Debes Ingresar La Calificacion ", Toast.LENGTH_SHORT).show();
        }
    }
}