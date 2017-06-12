package com.example.manar_000.raye7;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, LocationListener {

    private GoogleMap mMap;
    String Morigin = " ";
    String Mdestination = " ";
    Button findPath;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressBar progressBar;
    EditText input;
    EditText output;
    LocationManager gpsLocation;
    Button currentLocation;
    Location location;
    PolyUtil util;
    int chosenPolyline = 0;
    boolean flag = false;
    String distance;
    String duration;
    List<Route> selectedRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        selectedRoute = new ArrayList<Route>();
        findPath = (Button) findViewById(R.id.btnFindPath);
        currentLocation = (Button) findViewById(R.id.current_location);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        PlaceAutocompleteFragment autocompleteFragment1 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment1);
        PlaceAutocompleteFragment autocompleteFragment2 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment2);
//        gpsLocation = (LocationManager)
//                getSystemService(Context.LOCATION_SERVICE);
        //location = new Location(gpsLocation.NETWORK_PROVIDER);

        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Mdestination = place.getName().toString();
            }

            @Override
            public void onError(Status status) {

            }
        });
        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Morigin = place.getName().toString();
              //  Log.v("place : ", place.getName().toString());
            }

            @Override
            public void onError(Status status) {
                Log.v("An error occurred: ", status.toString());
            }
        });
        findPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsLocation = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                gpsLocation = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (gpsLocation.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = gpsLocation.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location == null) {
                        Toast.makeText(getApplicationContext(), "GPS is not active !", Toast.LENGTH_SHORT).show();
                    } else {

                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = new ArrayList<Address>();
                        try {
                            addresses = gcd.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);
                            Morigin = addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1);
                            output.setText(addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1));
                        //    Log.v("Current Location ", addresses.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (addresses.size() > 0) {
                            // Log.v("long press " ,addresses.get(0).getLocality().toString());
                        } else {
                            // do your staff
                        }

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "GPS is not active !", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        ViewGroup viewD = (ViewGroup) autocompleteFragment2.getView();
        input = (EditText) viewD.findViewById(R.id.place_autocomplete_search_input);
        ViewGroup viewD2 = (ViewGroup) autocompleteFragment1.getView();
        output = (EditText) viewD2.findViewById(R.id.place_autocomplete_search_input);
        //location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        gpsLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    public void sendRequest() {

        if (Morigin.equals(" ")) {
            Toast.makeText(this, " Please Enter origin address !", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Mdestination.equals(" ")) {
            Toast.makeText(this, " Please Enter destination address !", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, Morigin, Mdestination).execute();
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        LatLng cairo = new LatLng(30.103974, 31.245532);
//        LatLng shoubra = new LatLng(30.103974, 31.245532);
//        LatLng shoubra1 = new LatLng(30.103614, 31.245366);
//        LatLng shoubra2 = new LatLng(30.103614, 31.245366);
//
//        mMap.addMarker(new MarkerOptions()
//                .position(cairo)
//                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("mapicon", 100, 150))));
//        mMap.addPolyline(new PolylineOptions().add(
//                cairo,
//                shoubra1,
//                shoubra2,
//                shoubra).
//                width(10)
//                .color(Color.RED));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cairo, 18));
        // mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(true);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = new ArrayList<Address>();
                try {
                    addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    Mdestination = addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1);
                    input.setText(addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1));
                 //   Log.v("Long press ", addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses.size() > 0) {
                    // Log.v("long press " ,addresses.get(0).getLocality().toString());
                } else {
                    // do your staff
                }

            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

              //  Log.v("nta dost hna  ", latLng.toString() + polylinePaths.size());
                int i;
                for (i = 0; i < polylinePaths.size(); i++) {
                    flag = util.isLocationOnPath(latLng, polylinePaths.get(i).getPoints(), true, 50);
                    if (flag) {
                        chosenPolyline = i;
                      //  Log.v("da l polyline bta3k ", chosenPolyline + " ");
                        polylinePaths.get(chosenPolyline).setColor(Color.MAGENTA);
                        distance = selectedRoute.get(chosenPolyline).distance.getText();
                        duration = selectedRoute.get(chosenPolyline).duration.getText();
                    //    Log.v("Distance and duration", distance + duration);
                        ((TextView) findViewById(R.id.tvDuration)).setText(duration);
                        ((TextView) findViewById(R.id.tvDistance)).setText(distance);

                    }
                }

                for (int j = 0; j < polylinePaths.size(); j++) {
                    if (j != chosenPolyline) {
                        polylinePaths.get(j).setColor(Color.BLUE);

                    }
                }
            }
        });
    }


    @Override
    public void onDataStart() {

        progressBar.setVisibility(View.VISIBLE);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }

    }

    @Override
    public void onDataFetched(List<Route> routes) {


    //    Log.v("Route length ", routes.size() + "");
        selectedRoute = routes;
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            distance = route.distance.text;
            duration = route.duration.text;
            ((TextView) findViewById(R.id.tvDuration)).setText(duration);
            ((TextView) findViewById(R.id.tvDistance)).setText(distance);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("mapicon", 100, 150)))
                    .title(route.startAddres)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("mapicon2", 100, 150)))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    color(Color.BLUE).
                    geodesic(true).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
