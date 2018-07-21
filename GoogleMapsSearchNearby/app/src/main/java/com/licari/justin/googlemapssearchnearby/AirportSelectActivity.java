package com.licari.justin.googlemapssearchnearby;

/**
 * Created by Justin on 2018-07-21.
 */

import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.firebase.ui.auth.AuthUI;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;


/**
 * Created by Justin on 2018-07-21.
 */

public class AirportSelectActivity extends FragmentActivity {

    LinkedList<String> airportNames;

    Spinner airportSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        airportNames = new LinkedList<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport_select);

        // Get a reference to the airports
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/");

        ref.child("airports/").addValueEventListener(new ValueEventListener() {
                                                     @Override
                                                     public void onDataChange(DataSnapshot dataSnapshot) {

                                                         for(DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()){

                                                                 String airport = uniqueKeySnapshot.getKey();
                                                                 setSpinnerOptions(airport);

                                                         }
                                                     }

                                                     @Override
                                                     public void onCancelled(DatabaseError databaseError) {

                                                     } });
                                                 //}

       /* Query placeNameQuery = ref.child("airports/").orderByChild("airports/");
        placeNameQuery.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getKey().toString();
                //airportNames.add(data);
                parseData(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });*/

        Button btnOpenMap = (Button) findViewById(R.id.btnOpenMap);
        btnOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMap();
            }
        });
    }

    public void  switchToMap(){
        System.out.println("selection: " + airportSelect.getSelectedItem().toString());
        GlobalData.airport = airportSelect.getSelectedItem().toString();
        Intent myIntent = new Intent(this, MapsActivity.class);
        startActivity(myIntent);
    }

    public void setSpinnerOptions(String data){

        System.out.println(data);

        airportNames.add(data);

        //Populate the spinner dropdown with airport names
        airportSelect = findViewById(R.id.airport_select);

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, airportNames);

        //set the spinners adapter to the previously created one.
        airportSelect.setAdapter(adapter);
    }
}

