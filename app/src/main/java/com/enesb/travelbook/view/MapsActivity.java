package com.enesb.travelbook.view;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.enesb.travelbook.R;
import com.enesb.travelbook.model.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    SQLiteDatabase database;

    @Override
    public void onBackPressed() {
        Intent intentToMain = new Intent(this, MainActivity.class);
        startActivity(intentToMain);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info"); // Bi önce ki sayfada ki info keyi ile ayni ise

        if (info.matches("new")) { // Eger yeni bir yere gitmek istiyorsa
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // Konum degisince ne yapilacak

                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.enesb.travelbook", MODE_PRIVATE);
                    boolean trackBoolean = sharedPreferences.getBoolean("trackBoolean", false);

                    if (!trackBoolean) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15)); // nereye ve kac zoom yapacak
                        sharedPreferences.edit().putBoolean("trackBoolean", true).apply();
                    }

                }
            };

            // Izinleri alma --> izin daha önce verilmemis ise
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // izin isteme
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION} ,1);
            } else {
                // Eger izin verildiyse konumu aliyoruz
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                // Son bilinen konumu alma
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }
                // ----------------------
            }
        } else {
            // SQLite Datayi göstericez (Eger yeni bir ekleme olmayacaksa) && intent data
            mMap.clear();

            Place place = (Place) intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(place.latitude, place.longitude);
            String placeName = place.name;

            mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }

    }

    @Override // Izin verildikten sonra kontrol yapma
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (requestCode == 1) { // Yukarida tanimladigimiz requestCode ile aynisi ise
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    // Eger gercekten bizim istedigimiz izin verildiyse
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, locationListener);

                    Intent intent = getIntent();
                    String info = intent.getStringExtra("info");
                    if (info.matches("new")) {
                        // Son bilinen konumu alma
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (lastLocation != null) {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                        } else {
                            // SQLite Data
                            mMap.clear();

                            Place place = (Place) intent.getSerializableExtra("place");
                            LatLng latLng = new LatLng(place.latitude, place.longitude);
                            String placeName = place.name;

                            mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                    }

                }
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {


        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addressList != null && addressList.size() > 0){
                if (addressList.get(0).getThoroughfare() != null) {
                    address += addressList.get(0).getThoroughfare();

                    if (addressList.get(0).getSubThoroughfare() != null) {
                        address += " ";
                        address += addressList.get(0).getSubThoroughfare();
                    }

                }
            } else {
                address = "New Place";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().title(address).position(latLng));

        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;

        final Place place = new Place(address, latitude, longitude);

        // Alert Dialog ekleme
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setCancelable(false); // alert kutusunun disina basarak alert kutusunu kapatmayi engellemek
        alertDialog.setTitle("Are you sure?");
        alertDialog.setMessage(place.name);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Evet e basarsa kaydedecek
                try {
                    database = MapsActivity.this.openOrCreateDatabase("Places", MODE_PRIVATE, null);
                    database.execSQL("CREATE TABLE IF NOT EXISTS places (id INTEGER PRIMARY KEY, name VARCHAR, latitude VARCHAR, longitude VARCHAR)");

                    String toCompile = "INSERT INTO places (name, latitude, longitude) VALUES (?, ?, ?)";
                    SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
                    sqLiteStatement.bindString(1, place.name);
                    sqLiteStatement.bindString(2, String.valueOf(place.latitude)); // Double bir degeri Stringe cevirdik
                    sqLiteStatement.bindString(3, String.valueOf(place.longitude));
                    sqLiteStatement.execute();

                    Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Canceled!", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();

    }
}