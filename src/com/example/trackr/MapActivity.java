package com.example.trackr;

import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class MapActivity extends Activity implements
    LocationListener,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener,
    GoogleMap.OnMapLongClickListener,
    GooglePlayServicesClient.ConnectionCallbacks,
    GooglePlayServicesClient.OnConnectionFailedListener{

    private GoogleMap myMap;
    private TextView latitute;
    private TextView longitutde;
    private TextView speed;
    private TextView altitude;
    private LocationClient myLocationClient;
    private LocationRequest myLocationRequest;

    private Polyline myPolyline;
    private Polyline myPolyline2;
    private int markerPos;
    private HashMap<String,String> list;
    LinkedHashMap<String,LatLng> points;
    private static final String LOG_TAG = "MapActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapV)).getMap();
        myMap.setMyLocationEnabled(true);
        myMap.setOnMapLongClickListener(this);
        myMap.setOnMarkerClickListener(this);
        myMap.setOnMarkerDragListener(this);

        myPolyline = myMap.addPolyline( new PolylineOptions()
                                            .color(0x770000FF));

        myPolyline2 = myMap.addPolyline( new PolylineOptions()
                                             .color(0x7700FF00));
        points = new LinkedHashMap<String, LatLng>();

        latitute = (TextView) findViewById(R.id.latitute);
        longitutde = (TextView) findViewById(R.id.longitutde);
        speed = (TextView) findViewById(R.id.speed);
        altitude = (TextView) findViewById(R.id.altitude);

        // Check if Google Play Service is Available
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(status == ConnectionResult.SUCCESS){
            myLocationClient = new LocationClient(this, this, this);
            myLocationClient.connect();
        }

        // Prevent Crash from accessing Http request
        // This is a bad method
        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Move map to current location
        Location myLocation = myLocationClient.getLastLocation();
        CameraPosition myPosition = new CameraPosition.Builder()
                        .target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                        .zoom(15.5f)
                        .bearing(0)
                        .build();

        myMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition), null);


        // Set GPS update interval
        myLocationRequest = LocationRequest.create();
        myLocationRequest.setInterval(1000);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        myLocationClient.requestLocationUpdates(myLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        // TODO Auto-generated method stub
        myLocationClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null) return;
        latitute.setText(String.format("Latitute: %f",location.getLatitude()));
        longitutde.setText(String.format("Longitutde: %f", location.getLongitude()));
        speed.setText(String.format("Speed: %.2f meters/sec", location.getSpeed()));
        altitude.setText(String.format("Altitude: %.2f feet", location.getAltitude()));
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker marker = myMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));

        points.put(marker.getId(), latLng);
        myPolyline.setPoints(new ArrayList<LatLng>(points.values()));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        points.remove(marker.getId());
        marker.remove();
        myPolyline.setPoints(new ArrayList<LatLng>(points.values()));
        return true;
    }

    public void onClick_draw(View v) {
        getJson();
    }

    List<LatLng> cord = new ArrayList<LatLng>();
    public void deCodePath(String path) {
        List<Double> list = new ArrayList<Double>();
        for(String a : path.split("(?<=[\\x00-\\x5E])")) {
            int point = 0;
            for(int i = 0; i < a.length(); ++i) {
                point += ((a.charAt(i) - 63) & 0x1F) << (i*5);
            }
            list.add((double)(((point & 0x01) == 1) ? ~(point >> 1): (point >> 1)) / 100000);
        }

        //List<LatLng> cord = new ArrayList<LatLng>();
        double latBase = 0, longBase = 0;
        for(int index = 0; index < list.size(); ++index) {
            cord.add(new LatLng(latBase += list.get(index), longBase += list.get(++index)));
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        points.put(marker.getId(), marker.getPosition());
        myPolyline.setPoints(new ArrayList<LatLng>(points.values()));
    }


    public void getJson() {
        String sJson;
        list = new HashMap<String, String>();
        try {
            // Connect to website
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://maps.googleapis.com/maps/api/directions/json?origin=Manteca,CA&destination=1065%20High%20St,Santa%20Cruz,CA&sensor=false");
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();

            // Set input stream and read one line
            BufferedReader input = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = input.readLine()) != null) {
                sb.append(line);
            }
            input.close();
            sJson = sb.toString();

            // Parse Json string
            JSONObject object = new JSONObject(sJson);
            JSONArray routes = object.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);

            JSONArray legs = route.getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);
            JSONArray steps = leg.getJSONArray("steps");

            for (int index = 0; index < steps.length(); ++index) {
                JSONObject step = steps.getJSONObject(index);
                JSONObject polyline = step.getJSONObject("polyline");
                if (polyline.has("points")) {
                    deCodePath(polyline.getString("points"));
                }
            }

            myPolyline.setPoints(cord);
            cord.clear();

            JSONObject o_polyline = route.getJSONObject("overview_polyline");
            deCodePath(o_polyline.getString("points"));
            myPolyline2.setPoints(cord);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
