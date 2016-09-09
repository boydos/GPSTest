package com.example.gpstest;

import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
    private TextView textview;
    private LocationManager im;
    private ScrollView scrollview;
    private static final String TAG="GPS";
    private static long time =System.currentTimeMillis();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview =(TextView)findViewById(R.id.gps);
        scrollview = (ScrollView) findViewById(R.id.scroll);
        im =(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        if(!im.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }
        
        String bestProvider = im.getBestProvider(getCriteria(), true);
        Location location = im.getLastKnownLocation(bestProvider);
        updateView(location);
        im.addGpsStatusListener(gpsListener);
        time =System.currentTimeMillis();
        im.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationlistener);
    }
    private LocationListener locationlistener = new LocationListener() {
        
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
            switch (status) {
            case LocationProvider.AVAILABLE:
                appendLog("当前GPS状态为可见状态");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                appendLog("当前GPS状态为服务区外");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                appendLog("当前GPS状态为暂停服务状态");
                break;
            default:
                break;
            }
        }
        
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
            Location location = im.getLastKnownLocation(provider);
            updateView(location);
        }
        
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
           updateView(null);
        }
        
        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            updateView(location);
        }
    };
    static int oldcount =-1;
    GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            // TODO Auto-generated method stub
            switch (event) {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                appendLog(String.format("第一次定位",getCostTime()));
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                textview.append(".-");
                GpsStatus gpsStatus= im.getGpsStatus(null);
                int max = gpsStatus.getMaxSatellites();
                Iterator<GpsSatellite> iters= gpsStatus.getSatellites().iterator();
                int count=0;
                while(iters.hasNext()&&count<=max) {
                    GpsSatellite s = iters.next();
                   // appendLog(String.format("[azi:%s,elev:%s,prn:%s,snr:%s]", s.getAzimuth()+"",s.getElevation()+"",s.getPrn()+"",s.getSnr()+""));
                    count++;
                }
               if(oldcount!=count) {
                   oldcount=count;
                   appendLog(String.format("搜索到%d颗卫星...%dS", count,getCostTime()));
               }
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                appendLog(String.format("定位启动...%dS",getCostTime()));
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                appendLog(String.format("定位结束...%dS",getCostTime()));
                break;

            default:
                break;
            }
        }
    };
    private long getCostTime() {
        return (System.currentTimeMillis()-time)/1000;
    }
    private void clearLog() {
        textview.setText("");
    }
    private void appendLog(String msg) {
        textview.append(msg+"\n");
        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
    }
    private void updateView(Location location) {
        if(location !=null) {
            appendLog(String.format("位置信息:\n经度:%s\n维度:%s\n", location.getLongitude()+"",location.getLatitude()+""));
        }else {
            appendLog("未搜到位置信息");
        }
    }
    private Criteria getCriteria() {
        Criteria  criteria =new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
}
