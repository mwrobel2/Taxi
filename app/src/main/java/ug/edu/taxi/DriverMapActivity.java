package ug.edu.taxi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Arrays;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private DatabaseReference databaseReference;

    GoogleApiClient mGoogleApiClient;
    //Location mLastLocation;
    LocationManager mLocationManager;
    LocationCallback mLocationCallback;  //
    LocationListener mLocationListener;
    //LocationRequest mLocationRequest;
    //FusedLocationProviderClient mFusedLocationClient;


    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 4;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private LatLng latLng;
    private Button mLogout;
    //private Button mRequest; //2
    //private LatLng pickupLocation; //2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


         ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_GRANTED);
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_GRANTED);

        editTextLatitude = findViewById(R.id.editText);
        editTextLongitude = findViewById(R.id.editText2);

        databaseReference = FirebaseDatabase.getInstance().getReference("Location");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    String databaseLatitudeString = dataSnapshot.child("latitude").getValue().toString().substring(1, dataSnapshot.child("latitude").getValue().toString().length() -1);
                    String databaseLongitudeString = dataSnapshot.child("longitude").getValue().toString().substring(1, dataSnapshot.child("latitude").getValue().toString().length() -1);

                    String[] stringLat = databaseLatitudeString.split(", ");
                    Arrays.sort(stringLat);
                    String latitude = stringLat[stringLat.length-1].split("=")[1];

                    String[] stringLong = databaseLongitudeString.split(", ");
                    Arrays.sort(stringLong);
                    String longitude = stringLong[stringLong.length-1].split("=")[1];

                    LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                    mMap.addMarker(new MarkerOptions().position(latLng).title(latitude + ", " + longitude));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 1));

                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mLogout = (Button) findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;

            }
        });
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

       // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Add a marker
        //LatLng postoj_gdansk = new LatLng(54.3554025, 18.6422703);
       // mMap.addMarker(new MarkerOptions().position(postoj_gdansk).title("Postój taxi- Gdańsk").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postoj_gdansk, 7));

    mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    editTextLatitude.setText(Double.toString((location.getLatitude())));
                    editTextLongitude.setText(Double.toString(location.getLongitude()));
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    mMap.addMarker(new MarkerOptions().position(latLng).title("Pozycja kierowcy"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,7));
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME,MIN_DIST, mLocationListener );
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME,MIN_DIST, mLocationListener );

    }
    catch (SecurityException e ){
          e.printStackTrace();}

    }



public void updateButtonOnclick(View view) {
       databaseReference.child("latitude").push().setValue(editTextLatitude.getText().toString());
       databaseReference.child("longitude").push().setValue(editTextLongitude.getText().toString());
}
}





