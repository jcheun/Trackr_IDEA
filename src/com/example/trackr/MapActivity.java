package com.example.trackr;

import android.view.View;
import android.widget.Toast;
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

    private int markerPos;

    LinkedHashMap<String,LatLng> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapV)).getMap();
        myMap.setMyLocationEnabled(true);
        myMap.setOnMapLongClickListener(this);
        myMap.setOnMarkerClickListener(this);
        myMap.setOnMarkerDragListener(this);

        myPolyline = myMap.addPolyline( new PolylineOptions() );
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
        String path = "whueFj_zbVdBhAzAEvCBxSa@lX]jNYfB\\`A~CnFtOb@`Gj@vvEMto@eLnw@kUzsAcGjl@lA`X`F`ZhEjLrO|UzX~b@reAl_BhOvb@`L~cCV~oI`Bt\\rGb~@l@bcBtDtWrXx~@`r@p{Bfq@vzBlDrYSni@uMlgFsA|i@hAzi@fDjnAfEreAhNt`BhEpjAfPhhBjGtb@fOdSfMtIhG|IfGdUtAvYzLzc@~P`f@tKnb@nCv\\oFhXmLbYgBnc@xNjoAxMjbApc@luAro@`sBtC|R`@xUgA|mBnLdsA`C|i@t@ng@}Etb@yA`WmChFOpDxDrEpHdJjG~FdDzAbg@lApLj@do@aBncAMbxAvAva@s@pR}GnVgVtJaF|LgBbK^zGrG~HrHlLp@lMIpHlCxStZhMjJbWtIxSbX~X`g@`Ohd@lFrJlN~q@rNxt@bIjQtNnS~DxQ~Evk@~H~f@nD|G~HlDxR`@nYtKf^~OzTpXpHdZrKjVto@dvAvQxU|Kv_@fF~LrObN~j@lYn\\zE~_@nb@di@doArXfg@tMjGnNeAxMoKrc@kY|m@gZxL{Gp`@mb@tOeP`FmCbLkBdVxVvHzKhPvNdF`L~EpEpLCj_@wSxiBgt@j^wGnh@eGn|A{O`VoCvJoDjRuGz|@gTxpAeVvzDik@t^iCdK~BdKpGj]n]vWvWrr@tdAdQbRvw@dhAlPpNxHfArb@Uje@sAviAbAla@GtNqCvPgHfXoJ|KClHrB`KnInSrYnLlKz_@lIbr@xLri@r[fShNpq@t[|NtJtXf[fMhJx_Ab_@dJnFh\\vf@rRf`@|KpIjJeAzIs@nH|GxGhCpImArEn@nLnIlm@|Hp`@`EhEaAlUqMlHq@zNtExl@fWhGsB`LmQ|M{NzGoC~UcFnE~FdB~G`CnAlDUdCkCAkJaAqJhIoKzJmMxCmAtHnB~GlOhDh@jH}BhHZ~IdIzJ|FbDwBdCsJbGcDbQpBxEo@rKyCpMNdIcGdNsO~[k\\fIaLtPmCpF|EvGpK~JeDjJzBbDKlEsDlCq@dE`BtJpFvS~DnEqBlEiHrGU|HpLdJt@zN_KjGsAhPvLxJrF`ES|IkDjJvCvDjB`FGzPwKtFUbEzArUr\\pDbWvGpLhd@xe@rZho@nGzH~SpJbs@n\\nj@|]lj@bSpKhRdKfFn^`FnRtQ|Qn@vPrDxLAjQ_NvVoOxSiHt\\aBvPpDbKiAvO{JhH_@nFpBfXhTnOjLhGvZ~HhLtKvBdXgBzH_BjBq@`Cg@bAz@nFdBK{E{Ef@qAJ";
        List<Double> list = new ArrayList<Double>();
        for(String a : path.split("(?<=[\\x00-\\x5E])")) {
            int point = 0;
            for(int i = 0; i < a.length(); ++i) {
                point += ((a.charAt(i) - 63) & 0x1F) << (i*5);
            }
            list.add((double)(((point & 0x01) == 1) ? ~(point >> 1): (point >> 1)) / 100000);
        }

        List<LatLng> cord = new ArrayList<LatLng>();
        double latBase = 0, longBase = 0;
        for(int index = 0; index < list.size(); ++index) {
            cord.add(new LatLng(latBase += list.get(index), longBase += list.get(++index)));

        }
        myPolyline.setPoints(cord);


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
}
