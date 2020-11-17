package com.example.taximagangue.provider;

import com.example.taximagangue.models.ClientBooking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ClientBookingProvider {

    private DatabaseReference mDatabaseReference;

    public ClientBookingProvider() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("ClientBooking");
    }

    public Task<Void> create (ClientBooking clientBooking){
        return  mDatabaseReference.child(clientBooking.getIdClient()).setValue(clientBooking);
    }
}
