package com.example.stealth;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserManager extends DatabaseManager{
    //    Constant  int GoodColor =15;
//    String LastReadData;


    String UserId;



    public UserManager(String Email)
    {
        RetrieveUserId(Email);
    }
    private void RetrieveUserId(String Email)
    {

        String ModifiedEmail=Email.replace('.','-');
        EmailUserid.child(ModifiedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserId=snapshot.getValue(String.class);
//                Out(UserId);
                if(UserId==null)//case when Email is new there will no user id
                {
                    UserId = EmailUserid.push().getKey();
                    EmailUserid.child(UserId).setValue(Email);  //making double referance
                    EmailUserid.child(ModifiedEmail).setValue(UserId); //key can't content .
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

}
