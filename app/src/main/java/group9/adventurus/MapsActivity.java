/* CSCI 4176: Mobile Computing Group Project
Group #9

This Java class contains the logic for the "main" page, which contains Google Maps.

Much like the Login page, this class is heavily modified from the template Google Maps Activity
that is provided by the Android Studio New Activity wizard.
 */

package group9.adventurus;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback
{

    //Get the PermissionManager Singleton
    private PermissionManager permissionManager = PermissionManager.getInstance();

    //Set the default location to (0,0)
    private final LatLng mDefaultLocation = new LatLng(0, 0);

    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private PlaceAutocompleteFragment placeAutoComplete;
    private TextView tvLocationWarning;
    private TextView tvLoading;
    private Button btnSearch;
    private Button btnSuggest;

    //Get the Utilities Singleton
    private Utilities utils = Utilities.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Initialize all the variables with their corresponding views
        tvLocationWarning = findViewById(R.id.warnLocation);
        tvLoading = findViewById(R.id.loadScreen);
        btnSearch = findViewById(R.id.btnSearch);
        btnSuggest = findViewById(R.id.btnSuggest);

        //Start the loading screen, hide all the buttons, and wait for map to load
        toggleLoading(true);
        hideActionButtons(true);

        //Check for network connection
        if (utils.checkConnection(getApplicationContext()) == false)
        {
            Toast.makeText(this, "No network connection detected, logging out of the system", Toast.LENGTH_SHORT).show();
            logout();
        }

        //Get activities from the database
        getAllResults();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_search);

        //Move the view to the selected "place", and set the "current location" to whatever is selected
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place)
            {

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(place.getLatLng().latitude,
                                place.getLatLng().longitude), utils.getDefaultZoom())
                );
                utils.setSearchLocation(new LatLng(place.getLatLng().latitude,
                        place.getLatLng().longitude));
                utils.setLocationName(place.getName().toString());

                //Disable loading screen and display the buttons
                toggleLoading(false);
                hideActionButtons(false);
            }

            @Override
            public void onError(Status status)
            {
                //Do nothing for now, may come back to this later
            }
        });

        // Set up Location Manager to get device location
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location)
            {
                mLastKnownLocation = location;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mLastKnownLocation.getLatitude(),
                                mLastKnownLocation.getLongitude()), utils.getDefaultZoom())
                );
                utils.setSearchLocation(new LatLng(mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()));
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try
                {
                    List<Address> listAddresses = geocoder.getFromLocation(
                            mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude(), 1);
                    if (null != listAddresses && listAddresses.size() > 0)
                    {
                        utils.setLocationName(listAddresses.get(0).getAddressLine(0));
                    }
                }
                catch (IOException e)
                {
                    utils.setLocationName(" ");

                }
                toggleLoading(false);
                hideActionButtons(false);
            }
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                //Do Nothing. We don't want to constantly request location and drain battery
            }

            public void onProviderEnabled(String provider)
            {
                requestLocationUpdate();
            }

            public void onProviderDisabled(String provider)
            {
                updateLocationDetectionButtonUI();
            }
        };
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    //The above Javadoc was incorporated via the Google Maps template from Android Studio
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
        requestLocationUpdate();

    }

    //Updates users current location by sending a request to GPS
    @SuppressLint("MissingPermission")
    public void requestLocationUpdate()
    {
        if (permissionManager.getLocationPermission(this.getApplicationContext(), this) &&
                permissionManager.checkLocationEnabled(this.getApplicationContext()))
        {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                    locationListener, null);
        }
        else
        {
            toggleLoading(false);
            Toast.makeText(this, "Error Updating User Location", Toast.LENGTH_SHORT).show();
        }
        updateLocationDetectionButtonUI();
    }

    //Sets an "update location" button in the top right corner of the Map
    private void updateLocationDetectionButtonUI()
    {
        if (mMap == null)
        {
            return;
        }
        try
        {
            //Sets the location and enables the UI button if permission has been granted
            if (permissionManager.checkLocationPermissionGranted() &&
                    permissionManager.checkLocationEnabled(this.getApplicationContext()))
            {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                tvLocationWarning.setVisibility(View.INVISIBLE);

            }
            else
            {
                if (!permissionManager.checkLocationEnabled(this.getApplicationContext()))
                {
                    tvLocationWarning.setVisibility(View.VISIBLE);
                }
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        }
        catch (SecurityException e)
        {
            Toast.makeText(this, "Error Updating User Location", Toast.LENGTH_SHORT).show();
        }
    }

    /*Handles user's acceptance or rejection of permission. If the user grants permissions,
     * then perform the tasks that requested thos permissions*/
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case PermissionManager.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    permissionManager.setLocationPermissionGranted(true);
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                            locationListener, null);
                }
                else
                {
                    permissionManager.setLocationPermissionGranted(false);
                }

                return;
        }
    }

    //Direct the user to the Search page
    public void findActivities(View v)
    {
        Intent intent = new Intent(getApplicationContext(), SearchPage.class);
        startActivity(intent);
    }

    //Direct the user to the Activity Submission page
    //Called from button click
    public void SuggestActivities(View v)
    {
        Intent intent = new Intent(getApplicationContext(), SuggestionsPage.class);
        startActivity(intent);
    }

    /* This method is in place to prevent accidentally returning to the login screen.
    When the system UI's back button is pressed, a toast appears indicating that pressing
    "back" a second time will log the user out.
    If they do wish to be logged out, then call the logout method and have Firebase sign the user out.
     */
    private boolean backPressedOnce = false;
    @Override
    public void onBackPressed(){
        if(backPressedOnce){
            logout();
            super.onBackPressed();
        }

        backPressedOnce = true;
        Toast.makeText(this, "Press back again to logout.", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                backPressedOnce = false;
            }
        }, 2000);
    }

    //Sign the user out of Firebase and redirect back to the Login page
    private void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //Get list of activity suggestions from the database
    public void getAllResults() {
        DatabaseReference coursesDataReference = Firebase.getRootDataReference().child("Activities/");

        coursesDataReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<Activity> searchResults = new ArrayList<>();
                        Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                        for (DataSnapshot dsActivity : dataSnapshots) {
                            searchResults.add(dsActivity.getValue(Activity.class));
                        }
                        dataChangeHandler(searchResults);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("Error", databaseError.toString());
                    }
                }
        );
    }
    //Update the activity list
    public void dataChangeHandler(ArrayList<Activity> newData) {
        Activity.activities.clear();
        Activity.activities.addAll(newData);
        addMarkers(Activity.activities);
    }

    //Display the location of the activities on the map
    public void addMarkers(ArrayList<Activity> activities){
        for(Activity a : activities){
            LatLng activityLocation = utils.getCoordinates(this, a.getLocation());
            mMap.addMarker(new MarkerOptions().position(activityLocation).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title(a.getName()));
        }
    }

    //Displays or hides the loading screen
    private void toggleLoading(boolean start)
    {
        if(start)
        {
            tvLoading.setVisibility(View.VISIBLE);
        }
        else
        {
            tvLoading.setVisibility(View.INVISIBLE);
        }
    }

    //Displays or hides the buttons on Maps Screen
    private void hideActionButtons(boolean hide)
    {
        if(hide)
        {
            btnSuggest.setVisibility(View.INVISIBLE);
            btnSearch.setVisibility(View.INVISIBLE);
        }
        else
        {
            btnSuggest.setVisibility(View.VISIBLE);
            btnSearch.setVisibility(View.VISIBLE);
        }
    }

}

