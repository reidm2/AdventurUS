/* CSCI 4176: Mobile Computing Group Project
Group #9

This Java class contains a singleton that we use to manage app permissions.
Namely, we are managing location and camera permissions in our project, this singleton helps us track them.
 */
package group9.adventurus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

class PermissionManager
{
    private boolean mLocationPermissionGranted;
    private boolean mStoragePermissionGranted;
    private boolean mCameraPermissionGranted;
    private LocationManager locationManager;
    private static PermissionManager mInstance = null;
    private PermissionManager() { }

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 2;
    public static final int PERMISSIONS_REQUEST_CAMERA = 3;

    //This is a singleton class accessible by all activities
    public static PermissionManager getInstance ()
    {
        if (mInstance == null)
        {
            mInstance = new PermissionManager();
        }
        return mInstance;
    }

    //Checks and requests location permission
    boolean getLocationPermission(Context context, Activity activity)
    {
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            mLocationPermissionGranted = true;
        }
        else
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            mLocationPermissionGranted = false;
        }

        return mLocationPermissionGranted;
    }

    //Checks and requests storage permission
    boolean getStoragePermission(Context context, Activity activity)
    {
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context.getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
        {
            mStoragePermissionGranted = true;
        }
        else
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
            mStoragePermissionGranted = false;
        }

        return mStoragePermissionGranted;
    }

    public boolean checkStoragePermissionGranted() { return mStoragePermissionGranted; }

    public void setStoragePermissionGranted(boolean value)
    {
        this.mStoragePermissionGranted = value;
    }

    public boolean checkLocationPermissionGranted()
    {
        return mLocationPermissionGranted;
    }

    public void setLocationPermissionGranted(boolean mLocationPermissionGranted)
    {
        this.mLocationPermissionGranted = mLocationPermissionGranted;
    }

    //Checks whether GPS is enabled on user's phone
    public boolean checkLocationEnabled(Context context)
    {
        locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        try
        {
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            return gpsEnabled;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    //Checks and requests camera permission
    boolean getCameraPermission(Context context, Activity activity)
    {
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
        {
            mCameraPermissionGranted = true;
        }
        else
        {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
            mCameraPermissionGranted = false;
        }

        return mCameraPermissionGranted;
    }

    public boolean checkCameraPermissionGranted() { return mCameraPermissionGranted; }

    public void setCameraPermissionGranted(boolean value)
    {
        this.mCameraPermissionGranted = value;
    }
}
