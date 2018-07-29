package com.Alkowskey.alek.rovver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity {

    MapView mapView = null;
    public String mode = "vehicle=bike";
    Toolbar toolbar;
    DrawerLayout mDrawer;
    NavigationView nv;
    MenuItem iitem = null;
    Polyline roadOverlay;

    LocationManager locationManager;
    LocationListener listener;

    InterstitialAd mInterstitialAd = null;

    int counter = 0;

    SwitchCompat switchCompat = null;

    RoadManager roadManager = new GraphHopperRoadManager("969f167b-c332-43db-afc7-065a9b6fb0da", true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("RoVVer");
        toolbar.setTitleTextColor(Color.WHITE);

        MobileAds.initialize(this, "ca-app-pub-1654370331152660~3728135482");

        setAd();//function setting ad

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mapView = (MapView) findViewById(R.id.map);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        nv = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(nv);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Przyznano uprawnienia", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            mapView.invalidate();
        }

        mapView.setVisibility(View.VISIBLE);

        setMap(mapView);
    }

    void setAd()
    {

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1654370331152660/1986133811");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                //Toast.makeText(MapsActivity.this, "Ad loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //Toast.makeText(MapsActivity.this, "Ad failed to load", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                //Toast.makeText(MapsActivity.this, "Ad closed", Toast.LENGTH_SHORT).show();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }


    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(iitem!=null)iitem.setChecked(false);
                item.setChecked(true);
                iitem=item;

                mDrawer.closeDrawers();

                int id = item.getItemId();

                if(id==R.id.site){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.Aleksanderkulinski.pl"));
                    startActivity(browserIntent);
                }
                else if(id==R.id.contact){
                    Intent mail = new Intent(Intent.ACTION_VIEW);
                    Uri data = Uri.parse("mailto:Alkowskey@gmail.com?subject=&body=");
                    mail.setData(data);
                    startActivity(mail);
                }

                else if(id==R.id.main)setContentView(R.layout.activity_maps);

                return true;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user:
                if(mDrawer.isDrawerOpen(GravityCompat.START))mDrawer.closeDrawer(GravityCompat.START);
                else mDrawer.openDrawer(GravityCompat.START);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Zresetuj aplikacje, aby działa poprawnie", Toast.LENGTH_LONG).show();

            }else
            {
                Toast.makeText(this, "Nie przyznano uprawnień", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setMap(MapView mMap) {
        GeoPoint point = new GeoPoint(52.2297, 21.0122);
        IMapController controller = mMap.getController();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Loc","not granted!");
            return;
        }
        controller.setCenter(point);
        controller.setZoom(15);
        mMap.setBuiltInZoomControls(false);
        mMap.setMultiTouchControls(true);
        mMap.setUseDataConnection(true);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
    }

    public void search(View v)
    {
        EditText eText = (EditText) findViewById(R.id.EditText);
        EditText eText2 = (EditText) findViewById(R.id.EditText2);
        Geocoder code = new Geocoder(this);
        if(eText.getText().toString().equals(""))
        {
            Toast.makeText(this, "Wpisz miejscowosc",  Toast.LENGTH_SHORT).show();
            return;
        }
        if(eText2.getText().toString().equals(""))eText2.setText("1");
        double dst = Double.parseDouble(eText2.getText().toString())/2;
        double dst1[]={1, 1};
        while(dst1[0]<dst*0.70&&dst1[1]<dst*0.70)
        {
            for(int i=0; i<2; i++)
            {
                dst1[i] = (Math.random()*dst)-(dst/4);
            }
        }

        if(Math.random()-0.5>0.16)dst1[0]*=-1;
        else if(Math.random()-0.5>-0.16) dst1[1]*=-1;
        else if(Math.random()-0.5<-0.33){
            dst1[1]*=-1;
            dst1[0]*=-1;
        }


        try {
            List<Address> result = (List<Address>) code.getFromLocationName(eText.getText().toString(), 20);
            if(result.isEmpty())
            {
                Toast.makeText(this, "Nie znaleziono miejscowości", Toast.LENGTH_SHORT).show();
            }
            else {
                GeoPoint point = new GeoPoint(result.get(0).getLatitude(), result.get(0).getLongitude());
                mapView.getController().setCenter(new GeoPoint(result.get(0).getLatitude(), result.get(0).getLongitude()));
                GeoPoint point2 = new GeoPoint(result.get(0).getLatitude() + (dst1[0]) / 100, result.get(0).getLongitude() + (dst1[1]) / 100);
                GeoPoint point3 = new GeoPoint(point2.getLatitude() - (Math.random() - 0.5) / 10000, point2.getLongitude() - (Math.random() - 0.5) / 10000);
                drawPath(point, point2, point3);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(counter==4)counter = 0;
        if(mInterstitialAd.isLoaded()&&counter==0){
            mInterstitialAd.show();
            counter++;
        }
        else counter++;

    }

    private void drawPath(GeoPoint point, GeoPoint point2, GeoPoint point3)
    {
        if(roadOverlay!=null)roadOverlay.setVisible(false);
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(point);
        waypoints.add(point2);
        waypoints.add(point3);
        waypoints.add(point);
        roadManager.addRequestOption("vehicle=bike");
        Road road = roadManager.getRoad(waypoints);
        roadOverlay = RoadManager.buildRoadOverlay(road);
        mapView.getOverlays().add(roadOverlay);
        mapView.invalidate();

        TextView desc = (TextView) findViewById(R.id.desc);
        double length = road.mLength;

        desc.setText("Odległość: "+Double.toString(round(length))+"KM");


    }

    private double round(double var)
    {
        var*=100;
        var=Math.round(var);
        var/=100;
        return var;
    }

    public void check(View view) {
        RadioButton radio = (RadioButton) findViewById(R.id.radioButton);
        RadioButton radio2 = (RadioButton) findViewById(R.id.radioButton2);
        RadioButton radio3 = (RadioButton) findViewById(R.id.radioButton3);
        if(view.getId()==R.id.radioButton3)
        {
            radio.setChecked(false);
            radio2.setChecked(false);
            mode="vehicle=foot";
        }
        else if(view.getId()==R.id.radioButton2)
        {
            radio.setChecked(false);
            radio3.setChecked(false);
            mode="vehicle=bike";
        }
        else
        {
            radio2.setChecked(false);
            radio3.setChecked(false);
            mode = "vehicle=car";
        }
    }


    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(final Location loc) {
            Toast.makeText( getApplicationContext(), "Your Lat is:"+loc.getLatitude(), Toast.LENGTH_LONG).show();
            Toast.makeText( getApplicationContext(), "Your Lng is:"+loc.getLongitude(), Toast.LENGTH_LONG).show();
            Marker marker = new Marker (mapView);
            marker.setPosition(new GeoPoint(loc.getLongitude(), loc.getLatitude()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
        }
        public void onProviderDisabled(String provider) {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_LONG).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_LONG).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

}
