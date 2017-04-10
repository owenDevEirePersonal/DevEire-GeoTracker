package com.deveire.dev.deveiregeofindergolf;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, DownloadCallback<String>
{

    private GoogleApiClient mGoogleApiClient;
    private Location whereTheyAt;
    private AddressResultReceiver geoCoderServiceResultReciever;
    private int locationScanInterval;

    private TextView latTextView;
    private TextView longTextView;
    private TextView addressTextView;
    private TextView intervalTextView;

    private Button sendCurrentLocalButton;
    private Button sendDummyLocalButton;
    private Button toMapButton;


    LocationRequest request;
    private final int SETTINGS_REQUEST_ID = 8888;
    private final String SAVED_LOCATION_KEY = "79";

    private boolean pingingServer;
    private String serverURL;
    private NetworkFragment aNetworkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        pingingServer = false;

        //aNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), "https://192.168.1.188:8080/smrttrackerserver-1.0.0-SNAPSHOT/hello?isDoomed=yes");
        serverURL = "http://geo.dev.deveire.com/store/location?id=" + Settings.Secure.ANDROID_ID.toString() + "&lat=" + 0000 + "&lon=" + 0000;
        //0000,0000 is a location in the middle of the atlantic occean south of western africa and unlikely to contain a golf course.

        latTextView = (TextView) findViewById(R.id.latTextView);
        longTextView = (TextView) findViewById(R.id.longTextView);
        addressTextView = (TextView) findViewById(R.id.addressTextView);
        intervalTextView = (TextView) findViewById(R.id.intervalTextView);

        sendCurrentLocalButton = (Button) findViewById(R.id.sendCurrentLocalButton);
        sendDummyLocalButton = (Button) findViewById(R.id.sendDummyLocalButton);
        toMapButton = (Button) findViewById(R.id.toMapButton);

        toMapButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                if(whereTheyAt != null)
                {
                    mapIntent.putExtra("userLat", (float) whereTheyAt.getLatitude());
                    Log.i("Map Update", "sending intent to mapActivity with user Latitude of :" + mapIntent.getFloatExtra("userLat", 0) + " from a location of lat: " + whereTheyAt.getLatitude());
                    mapIntent.putExtra("userLong",(float) whereTheyAt.getLongitude());
                    Log.i("Map Update", "sending intent to mapActivity with user Longitude of :" + mapIntent.getFloatExtra("userLong", 0) + " from a location of long: " + whereTheyAt.getLatitude());
                }
                startActivity(mapIntent);
            }
        });

        sendCurrentLocalButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(whereTheyAt != null)
                {
                    serverURL = "http://geo.dev.deveire.com/store/location?id=" + Settings.Secure.ANDROID_ID.toString() + "&lat=" + whereTheyAt.getLatitude() + "&lon=" + whereTheyAt.getLongitude();
                        //lat and long are doubles, will cause issue?
                    Log.i("Network Update", "Attempting to start download from locale button on " + serverURL);
                    aNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), serverURL);
                    //startDownload();
                }
                else
                {
                    serverURL = "http://geo.dev.deveire.com/store/location?id=" + Settings.Secure.ANDROID_ID.toString() + "&lat=" + 52.67 + "&lon=" + -8.54;
                    //lat and long are doubles, will cause issue?
                    Log.i("Network Update", "Attempting to start download from locale button on " + serverURL);
                    aNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), serverURL);
                    //startDownload();
                    Log.e("ERROR", "Unable to send location to sevrver, current location = null");
                }
            }
        });

        sendDummyLocalButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //try
                {
                    String lon = "" + 0000;
                    Log.i("Network Update", "sending lon: " + lon);
                    String lat = "" + 0000;
                    Log.i("Network Update", "sending lat: " + lat);
                    serverURL = "http://geo.dev.deveire.com/store/location?id=jim&lat=0&lon=0" /*URLEncoder.encode(("http://geo.dev.deveire.com/store/location?id=" + Settings.Secure.ANDROID_ID.toString() + "&lat=" + lat + "&lon=" + lon), "UTF-8")*/;
                    Log.i("Network Update", "Attempting to start download from dummy button on " + serverURL);
                    aNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), serverURL);
                    //startDownload();
                }
                /*catch(UnsupportedEncodingException e)
                {
                    Log.e("ERROR", "encoding of url failed in dummy with " + e.toString());
                }*/
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        mGoogleApiClient.connect();

        locationScanInterval = 30;//in seconds

        request = new LocationRequest();
        request.setInterval(locationScanInterval * 1000);//in mileseconds
        request.setFastestInterval(5000);//caps how fast the locations are recieved, as other apps could be triggering updates faster than our app.
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); //accurate to 100 meters.

        LocationSettingsRequest.Builder requestBuilder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        requestBuilder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>()
        {
            @Override
            public void onResult(@NonNull LocationSettingsResult aResult)
            {
                final Status status = aResult.getStatus();
                final LocationSettingsStates states = aResult.getLocationSettingsStates();
                switch (status.getStatusCode())
                {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try
                        {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, SETTINGS_REQUEST_ID);
                        } catch (IntentSender.SendIntentException e)
                        {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

        geoCoderServiceResultReciever = new AddressResultReceiver(new Handler());

        restoreSavedValues(savedInstanceState);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            whereTheyAt = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
            if(whereTheyAt != null)
            {
                updateText("" + whereTheyAt.getLatitude(), "" + whereTheyAt.getLongitude());
                //YES, lat and long are multi digit.
                if(Geocoder.isPresent())
                {
                    startIntentService();
                }
                else
                {
                    Log.e("ERROR:", "Geocoder is not avaiable");
                }
            }
            else
            {
                updateText("LOCATION NOT FOUND", "RELEASE THE HOUNDS");
                updateText("ERROR, CANNOT GET ADDRESS WITHOUT LONG AND LAT");
            }


        }



    }

    @Override
    public void onConnectionSuspended(int i)
    {
        //put other stuff here
    }

    @Override
    public void onLocationChanged(Location location)
    {
        updateText("" + location.getLatitude(), "" + location.getLongitude());
        whereTheyAt = location;
        if(whereTheyAt != null)
        {
            serverURL = "http://geo.dev.deveire.com/store/location?id=" + Settings.Secure.ANDROID_ID.toString() + "&lat=" + whereTheyAt.getLatitude() + "&lon=" + whereTheyAt.getLongitude();
            //lat and long are doubles, will cause issue?
            Log.i("Network Update", "Attempting to start download from onLocationChanged." + serverURL);
            aNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), serverURL);
            //startDownload();
        }
        else
        {
            serverURL = "http://geo.dev.deveire.com/store/location?id=" + Settings.Secure.ANDROID_ID.toString() + "&lat=" + 52.67 + "&lon=" + -8.54;
            //lat and long are doubles, will cause issue?
            Log.i("Network Update", "Attempting to start download from onLocationChanged." + serverURL);
            aNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), serverURL);
            //startDownload();
            Log.e("ERROR", "Unable to send location to sevrver, current location = null");
        }

    }

    private void updateText(String aLat, String aLong)
    {
        latTextView.setText("Your latitude: " + aLat);
        longTextView.setText("You longitude: " + aLong);
    }

    private void updateText(String addressIn)
    {
        addressTextView.setText("Your Address: " + addressIn);
    }

    private void updateText(int intervalIn)
    {
        intervalTextView.setText("Interval: " + intervalIn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //receive request changed.
    }

    @Override
    public void onSaveInstanceState(Bundle savedState)
    {
        savedState.putParcelable(SAVED_LOCATION_KEY, whereTheyAt);
        super.onSaveInstanceState(savedState);
    }

    private void restoreSavedValues(Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
        {

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(SAVED_LOCATION_KEY))
            {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                whereTheyAt = savedInstanceState.getParcelable(SAVED_LOCATION_KEY);
            }

        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, geoCoderIntent.class);
        intent.putExtra(Constants.RECEIVER, geoCoderServiceResultReciever);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, whereTheyAt);
        startService(intent);
    }

    private void startDownload()
    {
        if (!pingingServer && aNetworkFragment != null) {
            // Execute the async download.
            Log.i("Network Update", "starting Downloading");
            aNetworkFragment.startDownload();
            pingingServer = true;
        }
    }

    @Override
    public void updateFromDownload(String result) {
        intervalTextView.setText("Interval: " + result);
        try
        {
            JSONObject jsonResultFromServer = new JSONObject(result);
            if(locationScanInterval != jsonResultFromServer.getInt("intervalRequest"))
            {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    locationScanInterval = jsonResultFromServer.getInt("intervalRequest");
                    request.setInterval(locationScanInterval * 1000);

                    LocationSettingsRequest.Builder requestBuilder = new LocationSettingsRequest.Builder().addLocationRequest(request);
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
                    Log.i("Location Update", "Interval Changed, locationRequest changed.");
                }
                updateText(locationScanInterval);
            }

        }
        catch(JSONException e)
        {}



        Log.e("download Output", result);
        // Update your UI here based on result of download.
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch(progressCode) {
            // You can add UI behavior for progress updates here.
            case Progress.ERROR:
                Log.e("Progress Error", "there was an error during a progress report at: " + percentComplete + "%");
                break;
            case Progress.CONNECT_SUCCESS:
                Log.i("Progress ", "connection successful during a progress report at: " + percentComplete + "%");
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                Log.i("Progress ", "input stream acquired during a progress report at: " + percentComplete + "%");
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                Log.i("Progress ", "input stream in progress during a progress report at: " + percentComplete + "%");
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                Log.i("Progress ", "input stream processing successful during a progress report at: " + percentComplete + "%");
                break;
        }
    }

    @Override
    public void finishDownloading() {
        pingingServer = false;
        Log.i("Network Update", "finished Downloading");
        if (aNetworkFragment != null) {
            Log.e("Network Update", "network fragment not found, canceling download");
            aNetworkFragment.cancelDownload();
        }
    }

    class AddressResultReceiver extends ResultReceiver
    {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            resultData.getString(Constants.RESULT_DATA_KEY);


            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT)
            {
                updateText(resultData.getString(Constants.RESULT_DATA_KEY));
                Log.i("Success", "Address found");
            }
            else
            {
                updateText("Error:" + resultData.getString(Constants.RESULT_DATA_KEY));
            }

        }
    }



}
