/* CSCI 4176: Mobile Computing Group Project
Group #9

This is a unique class; Utilities is defined as a singleton, and is used to give us app-wide access to useful functions such as calculating distance from the device's current location
 */

package group9.adventurus;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class Utilities {

    private static final int DEFAULT_ZOOM = 15;
    private static Utilities utilSingleton = null;
    private LatLng searchLocation = null;
    private String locationName = null;

    //Singleton - only instantiate once
    private Utilities(){

    }

    public static Utilities getInstance()
    {
        if (utilSingleton == null)
            utilSingleton = new Utilities();

        return utilSingleton;
    }

    public LatLng getCoordinates(Context context, String addrStr) {
        List<Address> addrList;
        Geocoder gc = new Geocoder(context);
        LatLng coordinates = null;

        try{
            addrList = gc.getFromLocationName(addrStr, 4);
            Address address = addrList.get(0);
            coordinates = new LatLng(address.getLatitude(), address.getLongitude());
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return coordinates;
    }

    //Allow retrieval of a single default zoom value
    public int getDefaultZoom(){
        return DEFAULT_ZOOM;
    }

    public void setSearchLocation(LatLng currentLocation) {
        this.searchLocation = currentLocation;
    }

    public double calculateDistanceFromMe(LatLng destination){

        double distance;
        Location dest = new Location("");
        if(destination != null) {
            dest.setLatitude(destination.latitude);
            dest.setLongitude(destination.longitude);
        }
        else{
            dest.setLatitude(0);
            dest.setLongitude(0);
        }

        Location src = new Location("");
        src.setLatitude(searchLocation.latitude);
        src.setLongitude(searchLocation.longitude);

        distance = src.distanceTo(dest);
        return distance;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public String getLocationName()
    {
        return locationName;
    }

    /*Check if the device is currently connected to a network.
    We were not sure how to do this, so we referred to the Android developer documentation:
    https://developer.android.com/training/monitoring-device-state/connectivity-monitoring
     */
    public boolean checkConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }
}