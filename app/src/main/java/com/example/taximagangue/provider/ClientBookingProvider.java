package com.example.taximagangue.provider;

import com.example.taximagangue.models.ClientBooking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ClientBookingProvider {

    private DatabaseReference mDatabaseReference;

    public ClientBookingProvider() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("ClientBooking");
    }

    public Task<Void> create (ClientBooking clientBooking){
        return  mDatabaseReference.child(clientBooking.getIdClient()).setValue(clientBooking);
    }
    public  Task<Void> updateestatus(String idClientBooking, String status){
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        return  mDatabaseReference.child(idClientBooking).updateChildren(map);

    }

    public  Task<Void> updateIdHistoryBooking(String idClientBooking){
        String idPush = mDatabaseReference.push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put("idHistoryBooking", idPush);
        return  mDatabaseReference.child(idClientBooking).updateChildren(map);

    }

    public DatabaseReference getStatus(String idClienBooking){
        return  mDatabaseReference.child(idClienBooking).child("status");
    }

    public DatabaseReference getClientBooking(String idClienBooking){
        return  mDatabaseReference.child(idClienBooking);
    }


}
