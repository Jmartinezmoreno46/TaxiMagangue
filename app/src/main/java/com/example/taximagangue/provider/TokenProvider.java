package com.example.taximagangue.provider;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taximagangue.R;
import com.example.taximagangue.models.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

public class TokenProvider extends AppCompatActivity{

    private static final String TAG = null;
    DatabaseReference mDatabaseReference;

    public  TokenProvider(){

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("tokens");

    }

//    public void create(final String idUser) {
//       if (idUser == null) return;
//       FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
//           @Override
//           public void onSuccess(String s) {
//               Log.e("NEW_TOKEN", s);
//               mDatabaseReference.child(idUser);
//           }
//       });
//
//    }

    public void create (final String idUser){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        mDatabaseReference.child(idUser).child("device_token").setValue(token);
                    }
                });
  }

  public DatabaseReference getToken(String idUser){
        return  mDatabaseReference.child(idUser);
  }




    }
