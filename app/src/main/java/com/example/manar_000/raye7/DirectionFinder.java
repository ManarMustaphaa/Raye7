package com.example.manar_000.raye7;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by manar_000 on 6/11/2017.
 */

public class DirectionFinder {

    DirectionFinderListener listener;
    String origin;
    String destination;
    String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    String GOOGLE_API_KEY = "AIzaSyAXVrFlsOvfNVaetaKgcm5cdtJzCA54Jds";


    public DirectionFinder(DirectionFinderListener listener, String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }

    public void execute() throws UnsupportedEncodingException {

        listener.onDataStart();
        new DownloadData().execute(CreateUrl(origin, destination));
    }

    public String CreateUrl(String origin, String destination) throws UnsupportedEncodingException {
        String UrlOrigin = URLEncoder.encode(origin, "utf-8");
        String UlrDestination = URLEncoder.encode(destination, "utf-8");
        Log.v("The Url ", DIRECTION_URL_API + "origin=" + UrlOrigin + "&destination=" + UlrDestination + "&key=" + GOOGLE_API_KEY);
        return DIRECTION_URL_API + "origin=" + UrlOrigin + "&destination=" + UlrDestination + "&alternatives=true&key=" + GOOGLE_API_KEY;
    }


    class DownloadData extends AsyncTask<String, String, String> {

        String routeString;

        public void setListener(onAsyncDataFetched listener) {
            this.listener = listener;
        }

        onAsyncDataFetched listener;


        @Override
        protected String doInBackground(String... params) {

            String link = params[0];

            Log.v("String ", link);
            URL url = null;
            try {
                url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                routeString = buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return routeString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Log.v("onPostExecute ", s);
                parseJSon(s);
            } catch (Exception e) {
               // Toast.makeText(new MapsActivity(), "No internet connection.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        ArrayList<Route> routes;
        JSONObject routeJson = new JSONObject(data);
        JSONArray routeArray = routeJson.getJSONArray("routes");
        routes = new ArrayList<Route>();

        Log.v("TheRoutesAfterJson ", routes.toString());
        int i ;
        for ( i = 0; i < routeArray.length(); i++) {

            JSONObject jsonRoute = routeArray.getJSONObject(i);

            Log.v("TheRoutesAfterJson ", routes.toString() + routeArray.length());
            Route route = new Route();
            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");
            Log.v("TheRoutesAfterJson ", routes.toString());

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddres = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));

            routes.add(route);
            Log.v("TheRoutesAfterJson ", routes.toString());
        }
        listener.onDataFetched(routes);
    }


    private ArrayList<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        ArrayList<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}

