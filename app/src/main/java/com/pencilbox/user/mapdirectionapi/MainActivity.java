package com.pencilbox.user.mapdirectionapi;

import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnPolylineClickListener{
    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private GoogleMapOptions options;
    private GoogleMap map;
    private String origin = "23.7505516,90.3937944";
    private String destination = "23.8201737,90.3687156";
    private String waypoints1 = "23.758159, 90.374580";
    private String waypoints2 = "23.796051, 90.349607";
    private DirectionService service;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(7);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private int routeCount = 0;
    private int step = 0;
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nextBtn = findViewById(R.id.nextBtn);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(DirectionService.class);
        options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_TERRAIN).compassEnabled(true)
                .rotateGesturesEnabled(true)
                .zoomControlsEnabled(true)
                .tiltGesturesEnabled(true);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.mapContainer, mapFragment);
        ft.commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnPolylineClickListener(this);
        getDirections();
        /*LatLng start = new LatLng(23.7505,90.3934);
        LatLng end = new LatLng(23.7532,90.3878);
        Polyline polyline1 = map.addPolyline(new PolylineOptions()
                .add(start)
                .add(end)
                .clickable(true)
                .color(Color.BLUE));
        polyline1.setStartCap(new RoundCap());
        polyline1.setJointType(JointType.ROUND);
        polyline1.setTag("A");
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(start,15));*/
    }

    private void getDirections() {
            String urlString = String.format("directions/json?origin=%s&destination=%s&avoid=indoor&alternatives=true&waypoints=via:%s|via:%s&key=%s",origin,destination,waypoints1,waypoints2,getResources().getString(R.string.google_direction_api));
        Call<DirectionResponse>directionResponseCall = service.getDirectionResponse(urlString);
        directionResponseCall.enqueue(new Callback<DirectionResponse>() {
            @Override
            public void onResponse(Call<DirectionResponse> call, Response<DirectionResponse> response) {
                if(response.code() == 200){
                    DirectionResponse directionResponse = response.body();
                    final TextView tv = new TextView(MainActivity.this);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(directionResponse.getRoutes().get(0).getLegs().get(0).getStartLocation().getLat(),
                            directionResponse.getRoutes().get(0).getLegs().get(0).getStartLocation().getLng()),12));
                    //tv.setText(Html.fromHtml(directionResponse.getRoutes().get(0).getLegs().get(0).getSteps().get(0).getHtmlInstructions()));
                    routeCount = directionResponse.getRoutes().size();
                    if(routeCount > 1){
                        nextBtn.setEnabled(true);
                    }
                    List<DirectionResponse.Step>steps = directionResponse.getRoutes().get(step).getLegs().get(0).getSteps();
                    map.clear();
                    for(int i = 0; i < steps.size(); i++){
                        Polyline polyline = map.addPolyline(new PolylineOptions()
                        .add(new LatLng(steps.get(i).getStartLocation().getLat(),steps.get(i).getStartLocation().getLng()))
                                .add(new LatLng(steps.get(i).getEndLocation().getLat(),steps.get(i).getEndLocation().getLng()))
                        .clickable(true));
                        String instruction = String.valueOf(Html.fromHtml(directionResponse.getRoutes().get(step).getLegs().get(0).getSteps().get(i).getHtmlInstructions()));
                        polyline.setTag(instruction);
                        polyline.setStartCap(new RoundCap());
                        polyline.setEndCap(new RoundCap());
                        polyline.setJointType(JointType.ROUND);
                    }
                    /*AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setView(tv)
                            .show();*/

                    /*Log.e("direction", "onResponse: "+directionResponse.getRoutes().get(0).getLegs().get(0).getSteps().get(0).getHtmlInstructions());*/
                }
            }

            @Override
            public void onFailure(Call<DirectionResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Toast.makeText(this, polyline.getTag().toString(), Toast.LENGTH_SHORT).show();
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        }else {
            polyline.setPattern(null);
        }
    }

    public void nextRoute(View view) {
        if(step < routeCount){
            getDirections();
            step++;
            if(step == (routeCount)){
                step = 0;
            }
        }
    }
}
