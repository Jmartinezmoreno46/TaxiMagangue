package com.example.taximagangue.provider;

import com.example.taximagangue.models.ClientBooking;
import com.example.taximagangue.models.HistoryBooking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class HistoryBookingProvider {

    private DatabaseReference mDatabaseReference;

    public HistoryBookingProvider() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("HistoryBooking");
    }

    public Task<Void> create (HistoryBooking historyBooking){
        return  mDatabaseReference.child(historyBooking.getIdHistoryBooking()).setValue(historyBooking);
    }

    public Task<Void> updateCalificationClient (String idHistoryBooking, float calificationClient){
        Map<String , Object> map = new HashMap<>();
        map.put("calificationClient", calificationClient);
        return mDatabaseReference.child(idHistoryBooking).updateChildren(map);
    }

    public Task<Void> updateCalificationDriver (String idHistoryBooking, float calificationDriver){
        Map<String , Object> map = new HashMap<>();
        map.put("calificationDriver", calificationDriver);
        return mDatabaseReference.child(idHistoryBooking).updateChildren(map);
    }

    public DatabaseReference getHistoryBooking(String idHistoryBooking){
        return mDatabaseReference.child(idHistoryBooking);
    }

}
