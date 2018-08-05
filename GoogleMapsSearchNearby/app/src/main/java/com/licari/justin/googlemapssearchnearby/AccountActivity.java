package com.licari.justin.googlemapssearchnearby;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.firebase.ui.auth.AuthUI;

import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;


/**
 * Created by Justin on 2018-07-21.
 */

public class AccountActivity extends FragmentActivity {

    TextView displayMessageWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        displayMessageWindow = (TextView)findViewById(R.id.welcomeMessage);
        displayMessageWindow.setText("Hello, " + GlobalData.username);

        Button btnAccount = (Button) findViewById(R.id.btnMap);
        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToAirportSelect();
            }
        });
    }

    public void  switchToAirportSelect(){
        Intent myIntent = new Intent(this, AirportSelectActivity.class);
        startActivity(myIntent);
    }
}
