package com.licari.justin.googlemapssearchnearby;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    List<String> pointNames = new ArrayList();
    List<String> pointDescriptions = new ArrayList();
    List<String> pointTypes = new ArrayList();
    List<LatLng> nearestPlaces = new ArrayList();
    List<MarkerOptions> nearestPlacesMarkers = new ArrayList();
    List<MarkerOptions> placeMarkers = new ArrayList();
    public static String pointCoordinates;
    public static LatLng[] airportPlaces;

    TextView nearestPlacesList;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    //Represents whether the info panel is displayed or not
    public boolean infoWindowVisible = true;

    //Represents if places are within 40m of a user.
    public boolean placesNearby = false;

    private GoogleMap mMap;
    double latitude;
    double longitude;
    private int PROXIMITY_RADIUS = 10000;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;

    ViewFlipper infoWindowContents;
    TextView infoWindowDescription;
    TextView toggleButtonState;
    TextView infoWindowType;

    private Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        }
        else {
            Log.d("onCreate","Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Create info window with legend
        infoWindowContents = (ViewFlipper) findViewById(R.id.infoWindow);
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        //Override default onclick method for markers
        mMap.setOnMarkerClickListener(this);

        /*Button btnRestaurant = (Button) findViewById(R.id.btnRestaurant);
        btnRestaurant.setOnClickListener(new View.OnClickListener() {
            String Restaurant = "restaurant";
            @Override
            public void onClick(View v) {
                Log.d("onClick", "Button is Clicked");
                mMap.clear();
                String url = getUrl(latitude, longitude, Restaurant);
                Object[] DataTransfer = new Object[2];
                DataTransfer[0] = mMap;
                DataTransfer[1] = url;
                Log.d("onClick", url);
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(DataTransfer);
                Toast.makeText(MapsActivity.this,"Nearby Restaurants", Toast.LENGTH_LONG).show();
            }
        });*/

        /*Button btnHospital = (Button) findViewById(R.id.btnHospital);
        btnHospital.setOnClickListener(new View.OnClickListener() {
            String Hospital = "hospital";
            @Override
            public void onClick(View v) {
                Log.d("onClick", "Button is Clicked");
                mMap.clear();
                String url = getUrl(latitude, longitude, Hospital);
                Object[] DataTransfer = new Object[2];
                DataTransfer[0] = mMap;
                DataTransfer[1] = url;
                Log.d("onClick", url);
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(DataTransfer);
                Toast.makeText(MapsActivity.this,"Nearby Hospitals", Toast.LENGTH_LONG).show();
            }
        });*/

        /*Button btnSchool = (Button) findViewById(R.id.btnSchool);
        btnSchool.setOnClickListener(new View.OnClickListener() {
            String School = "school";
            @Override
            public void onClick(View v) {
                Log.d("onClick", "Button is Clicked");
                mMap.clear();
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }*/
               /* String url = getUrl(latitude, longitude, School);
                Object[] DataTransfer = new Object[2];
                DataTransfer[0] = mMap;
                DataTransfer[1] = url;
                Log.d("onClick", url);
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(DataTransfer);*/
                /*LatLng sydney = new LatLng(-33.852, 151.211);
                mMap.addMarker(new MarkerOptions().position(sydney)
                        .title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                Toast.makeText(MapsActivity.this,"Nearby Schools", Toast.LENGTH_LONG).show();
            }
        });*/

        ImageButton btnAccount = (ImageButton) findViewById(R.id.btnAccount);
        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToAccountPage();
            }
        });

        Button btnCloseView = (Button) findViewById(R.id.btnHide);
        btnCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleInfoPanel();
            }
        });

        //Display the legend by default
        infoWindowContents.setDisplayedChild(infoWindowContents.indexOfChild(findViewById(R.id.legend)));

        // Get a reference to the airport's maps
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("airports/" + GlobalData.airport + "/maps/markers/");

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue().toString();
                setPointCoordinates(data);
                System.out.println("data: " + data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        ref = database.getReference("airports/" + GlobalData.airport + "/maps/markerNameData");

        // Attach a listener to read the data
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    pointNames.add(child.getValue().toString());
                }

                for (int i=0;i<pointNames.size();i++){

                    System.out.println("name: " + pointNames.get(i));
                }

                if (pointDescriptions.size()>0 && pointCoordinates!=null && pointNames.size()>0 && pointTypes.size()>0){
                    setAirportData();
                    System.out.print("data set ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        ref = database.getReference("airports/" + GlobalData.airport + "/maps/markerNotesData");

        // Attach a listener to read the data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    pointDescriptions.add(child.getValue().toString());
                }

                for (int i=0;i<pointDescriptions.size();i++){

                    System.out.println("desc: " + pointDescriptions.get(i));
                }

                //Once all data is loaded, plot it
                if (pointDescriptions.size()>0 && pointCoordinates!=null && pointNames.size()>0 && pointTypes.size()>0){
                    setAirportData();
                    System.out.print("data set ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        ref = database.getReference("airports/" + GlobalData.airport + "/maps/markerDescriptionData");

        // Attach a listener to read the data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    pointTypes.add(child.getValue().toString());
                }

                for (int i=0;i<pointTypes.size();i++){

                    System.out.println("type: " + pointTypes.get(i));
                }

                //Once all data is loaded, plot it
                if (pointDescriptions.size()>0 && pointCoordinates!=null && pointNames.size()>0 && pointTypes.size()>0){
                    setAirportData();
                    System.out.print("data set ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


       /* new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        //setAirportData();
                    }
                },
                3000
        );*/

    }

    public void setPointCoordinates(String data){
        pointCoordinates = data;
        System.out.print("coordinates: " + pointCoordinates);

        //Once all data is loaded, plot it
        if (pointDescriptions.size()>0 && pointCoordinates!=null && pointNames.size()>0 && pointTypes.size()>0){
            setAirportData();
            System.out.print("data set ");
        }
    }

    /*public void setPointNames(String data){
        pointNames = data;
        System.out.print("names: " + pointNames);

        if (pointDescriptions!=null && pointCoordinates!=null && pointNames!=null && pointTypes!=null){
            setAirportData();
            System.out.print("data set ");
        }
    }*/

    /*public void setPointDescriptions(String data){
        pointDescriptions = data;
        System.out.print("descriptions: " + pointDescriptions);

        if (pointDescriptions!=null && pointCoordinates!=null && pointNames!=null && pointTypes!=null){
            setAirportData();
            System.out.print("data set ");
        }
    }*/

    /*public void setPointTypes(String data){
        pointTypes = data;
        System.out.print("types: " + pointTypes);

        if (pointDescriptions!=null && pointCoordinates!=null && pointNames!=null && pointTypes!=null){
            setAirportData();
            System.out.print("data set ");
        }
    }*/

    private void setAirportData(){


        /*while(pointDescriptions==null || pointCoordinates==null || pointNames==null || pointTypes==null){
            System.out.println("waiting for airport data...");
        }*/

        //System.out.println("names: " + pointNames);
        /*System.out.println("descriptions: " + pointDescriptions);
        System.out.println("types: " + pointTypes);

        String[] names = pointNames.get(0).split("=");
        String[] descriptions = pointDescriptions.split(",");
        String[] types = pointTypes.split(",");


        for (int i=0;i<names.length;i++){
            String name = names[i].substring(names[i].indexOf("="));
            String description = descriptions[i].substring(descriptions[i].indexOf("="));
            String type = types[i].substring(types[i].indexOf("="));

            names[i] = name;
            descriptions[i] = description;
            types[i] = type;

            names[i] = names[i].replace("=", "");
            names[i] = names[i].replace("}", "");

            descriptions[i] = descriptions[i].replace("=", "");
            descriptions[i] = descriptions[i].replace("}", "");

            types[i] = types[i].replace("=", "");
            types[i] = types[i].replace("}", "");
            types[i] = types[i].replace(" ", "");

            System.out.println("name: " + names[i]);
            System.out.println("description: " + descriptions[i]);
            System.out.println("type: " + types[i]);
        }*/

        System.out.println("data: " + pointCoordinates);

        //Find "undefined" String in coordinate data, and remove it
        String s1 = pointCoordinates.substring(pointCoordinates.indexOf("u")+1);

        System.out.println(s1 + s1.charAt(1) + " " + s1.charAt(2) + " " + s1.charAt(3) + " " + s1.charAt(4) + " " + s1.charAt(5) + " ");

        while (!((s1.charAt(0)=='n') && (s1.charAt(1)=='d') && (s1.charAt(2)=='e') && (s1.charAt(3)=='f') && (s1.charAt(4)=='i'))){
            s1 = s1.substring(s1.indexOf("u")+1);
            System.out.println(s1.charAt(0) + " " + s1.charAt(1) + " " + s1.charAt(2) + " " + s1.charAt(3) + " " + s1.charAt(4) + " ");
        }

        String s2 = s1.substring(9);
        String[] points = s2.split("#");
        String[][] points2 = new String[points.length][2];


        airportPlaces = new LatLng[pointNames.size()];

        for (int i=0;i<points.length-1;i++){

            System.out.println("LOOP START");
            points2[i]=points[i].split(",");
            String x = points2[i][0].replace("(", "");
            x = x.replace(")", "");
            x = x.replace("HDL=undefined", "");
            String y = points2[i][1].replace("(", "");
            y = y.replace(")", "");
            System.out.println("x: " + x + " y: " + y + " type: " + pointTypes.get(i));

            if (pointTypes.get(i).indexOf("wash")!=-1) {
                airportPlaces[i] = new LatLng(Double.parseDouble(x), Double.parseDouble(y));

                MarkerOptions place = new MarkerOptions().position(airportPlaces[i])
                        .title(pointNames.get(i))
                        .snippet(pointTypes.get(i) + "\n" + pointDescriptions.get(i))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                mMap.addMarker(place);
                placeMarkers.add(place);

                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(airportPlaces[i]));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            } else if (pointTypes.get(i).indexOf("Food")!=-1) {
                System.out.println("FOOD");
                airportPlaces[i] = new LatLng(Double.parseDouble(x), Double.parseDouble(y));

                MarkerOptions place = new MarkerOptions().position(airportPlaces[i])
                        .title(pointNames.get(i))
                        .snippet(pointTypes.get(i) + "\n" + pointDescriptions.get(i))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                mMap.addMarker(place);
                placeMarkers.add(place);

                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(airportPlaces[i]));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            } else if (pointTypes.get(i).indexOf("Shop")!=-1) {
                airportPlaces[i] = new LatLng(Double.parseDouble(x), Double.parseDouble(y));

                MarkerOptions place = new MarkerOptions().position(airportPlaces[i])
                        .title(pointNames.get(i))
                        .snippet(pointTypes.get(i) + "\n" + pointDescriptions.get(i))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                mMap.addMarker(place);
                placeMarkers.add(place);

                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(airportPlaces[i]));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            } else if (pointTypes.get(i).indexOf("Escalator")!=-1) {
                airportPlaces[i] = new LatLng(Double.parseDouble(x), Double.parseDouble(y));

                MarkerOptions place = new MarkerOptions().position(airportPlaces[i])
                        .title(pointNames.get(i))
                        .snippet(pointTypes.get(i) + "\n" + pointDescriptions.get(i))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                mMap.addMarker(place);
                placeMarkers.add(place);

                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(airportPlaces[i]));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            }

            //move map camera
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(airportPlaces[i]));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

            //Load image from Firebase into our infoWindow
            // Reference to an image file in Firebase Storage
            /*storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            StorageReference ref = storageReference.child("/0.png");

            // ImageView in your Activity
            ImageView imageView = findViewById(R.id.photo);

            // Load the image using Glide
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(ref)
                    .into(imageView);*/
        }
    }

    public void  switchToAccountPage(){
        Intent myIntent = new Intent(this, AccountActivity.class);
        startActivity(myIntent);
    }

    public void  toggleInfoPanel(){
        toggleButtonState = (TextView) findViewById(R.id.btnHide);

        if (infoWindowVisible){
            infoWindowContents.setDisplayedChild(0);//(infoWindowContents.indexOfChild(findViewById(R.id.placeDetails)));
            toggleButtonState.setText("Legend");
            infoWindowVisible = false;
        } else {
            infoWindowContents.setDisplayedChild(3);//(infoWindowContents.indexOfChild(findViewById(R.id.placeDetails)));
            toggleButtonState.setText("Close");
            infoWindowVisible = true;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        updateNearestPlaces(latLng);

        //move map camera
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        Toast.makeText(MapsActivity.this,"Your Current Location", Toast.LENGTH_LONG).show();

        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f",latitude,longitude));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
        Log.d("onLocationChanged", "Exit");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    //Display place details when clicked
    @Override
    public boolean onMarkerClick(final Marker marker) {

        String markerTitle= marker.getTitle();
        System.out.println("title: " + markerTitle);

        if (markerTitle.indexOf("Position")==-1) {


            //Load image from Firebase into our infoWindow
            // Reference to an image file in Firebase Storage
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            StorageReference ref = storageReference.child("/images/" + GlobalData.airport + marker.getTitle() + "/" + "image.jpg");

            // ImageView in your Activity
            ImageView imageView = findViewById(R.id.photo);

            // Load the image using Glide
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(ref)
                    .into(imageView);

            infoWindowContents.setDisplayedChild(infoWindowContents.indexOfChild(findViewById(R.id.placeDetails)));

            marker.showInfoWindow();

            /*infoWindowType = (TextView)findViewById(R.id.type);
            infoWindowType.setText("Type: " + "My Awesome Text");*/

            infoWindowDescription = (TextView) findViewById(R.id.description);
            infoWindowDescription.setText("\n" + "Description: " + marker.getSnippet());

            System.out.println(marker.getTitle() + "_" + marker.getId() + "_" + marker.getZIndex());
            return true;
        } else {
            if (placesNearby) {
                System.out.println("nearest places");
                infoWindowContents.setDisplayedChild(2);
            }
        }

        return false;
    }

    public void updateNearestPlaces(LatLng position){
        for (int i=0;i<airportPlaces.length;i++){
            double distance = Math.hypot(position.latitude-airportPlaces[i].latitude, position.longitude-airportPlaces[i].longitude);

            System.out.println("distance from place " + i + ": " + distance);

            if (nearestPlaces.size()<5 && distance<40.0) {
                placesNearby = true;
                nearestPlaces.add(airportPlaces[i]);
                nearestPlacesMarkers.add(placeMarkers.get(i));
            }

            if (distance<5.0){
                System.out.println("You've arrived");
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "test")
                        .setSmallIcon(R.drawable.map)
                        .setContentTitle("You've arrived at " + placeMarkers.get(i).getTitle())
                        .setContentText("You are now at " + placeMarkers.get(i).getTitle())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                // Create an explicit intent for an Activity in your app
                Intent intent = new Intent(this, /*AlertDetails.class*/MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


                createNotificationChannel();

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(/*notificationId*/0, mBuilder.build());
            }
        }

        String nearestPlacesListString = "Places Near You: " + "\n";

        for (int i=0;i<nearestPlaces.size();i++){
            nearestPlacesListString += i+1 +": " + nearestPlacesMarkers.get(i).getTitle() + "\n";
        }

        nearestPlacesList = (TextView)findViewById(R.id.placesList);
        nearestPlacesList.setText(nearestPlacesListString);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = /*getString(R.string.channel_name*/"test"/*)*/;
            String description = /*getString(R.string.channel_description)*/"test";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(/*CHANNEL_ID*/"test", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}



