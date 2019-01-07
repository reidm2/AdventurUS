/* CSCI 4176: Mobile Computing Group Project
Group #9

This defines the logic for the Detailed Activity View that appears when an item is selected from the Search Results.
 */
package group9.adventurus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailedViewActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ImageView imgView;
    private GoogleMap map;
    private TextView name, price, location, desc, weatherWarning;
    private LatLng activityLocation;
    private Utilities util;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);
        util = Utilities.getInstance();

        imgView = findViewById(R.id.imageDetail);
        name = findViewById(R.id.nameDetail);
        price = findViewById(R.id.priceDetail);
        location = findViewById(R.id.locationDetail);
        desc = findViewById(R.id.descDetail);
        weatherWarning = findViewById(R.id.weatherWarning);
        getIncomingIntent();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detailMap);
        mapFragment.getMapAsync(this);
    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra("Activity")){
            //Get activity
            Activity activity = (Activity) getIntent().getExtras().get("Activity");
            //Load image from URL using the Picasso Library: http://square.github.io/picasso/
            String url = activity.getImage();
            Picasso.get().load(url).into(imgView);

            //Set text fields
            name.setText(activity.getName());
            price.setText(activity.getPrice());
            location.setText(activity.getLocation());
            desc.setText(activity.getDescription());

            //Set activity location on the map
            activityLocation = util.getCoordinates(this, activity.getLocation());

            //If applicable, apply a weather warning
            if(activity.getActivityType().contains("outdoor")){
                getWeather(activityLocation);
            }
            else{
                weatherWarning.setText("");
            }
        }
    }

    //Show activity location on a map
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;

        map.addMarker(new MarkerOptions().position(activityLocation).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(activityLocation, util.getDefaultZoom()));

    }

    /*Do an API call to get the weather and display a warning if necessary.

    Our weather checking functionality is inspired by the work we did in Assignment 3.
    Therefore, much of the work will be similar to what we did for that assignment.
     */
    public void getWeather(LatLng location){
        final String url =
                "http://api.openweathermap.org/data/2.5/weather?APPID=0e22d44e92f13df73794b7385bb7c10f&units=metric&lat="
                        +location.latitude+"&lon="+location.longitude;

        //HTTP request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            //Get necessary data from the API as JSON
                            JSONArray weatherJSON = response.getJSONArray("weather");
                            JSONObject weatherData = weatherJSON.getJSONObject(0);
                            JSONObject mainData = response.getJSONObject("main");

                            //Get temperature and weather conditions
                            int weatherID = weatherData.getInt("id");
                            double temp = mainData.getDouble("temp");

                            //Check what kind of weather it is
                            checkForDanger(weatherID, temp);
                        }
                        //If the API can't be reached, show a warning
                        catch (JSONException e){
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Warning: Could not " +
                                    "retrieve weather information. Proceed with caution.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Warning: Could not " +
                                "retrieve weather information. Proceed with caution.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
        //Add weather request to the queue
        RequestQueue.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    //This function uses the three-digit id code provided by the API to determine weather type.
    public void checkForDanger(int id, double temp){
        String warning;
        if(temp<-5.0){
            warning = "Dress warmly! It's "+temp+"º out there.";
        }
        else if(id<233){
            warning = "WARNING: Current weather conditions suggest thunderstorms.";
        }
        else if(id>501 && id<532){
            warning = "WARNING: Current weather conditions suggest heavy rain.";
        }
        else if(id>=600 && id<=622){
            warning = "Dress warmly! Snow is in the forecast.";
        }
        else if(id == 711 || id == 721){
            warning = "WARNING: Air quality low. Avoid outdoor activities or wear appropriate protection.";
        }
        else if(id == 741){
            warning = "Note that current foggy conditions may limit your views.";
        }
        else if(id == 762){
            warning = "WARNING: High volume of volcanic ash in the air. Stay indoors if possible.";
        }
        else if(id == 781){
            //Hopefully this one goes without saying
            warning = "WARNING: Chance of tornadoes. Please stay indoors.";
        }
        else {
            warning = "";
        }

        weatherWarning.setText(warning);
    }
}
