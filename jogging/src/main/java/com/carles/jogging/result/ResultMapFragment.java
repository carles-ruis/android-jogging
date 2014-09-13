package com.carles.jogging.result;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.carles.jogging.R;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.util.FormatUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultMapFragment extends SupportMapFragment {

    private static final String ARGS_KEY_POSITION = "args_key_position";
    private static final String ARGS_KEY_PARTIALS = "args_key_partials";
    private int position;
    private List<JoggingModel> partials = new ArrayList <JoggingModel>();

    private static final int MAP_PADDING_IN_PX = 50;
    // GoogleMap zoom value range from 0 to 19. 0 is worldwide, 19 finest zoom
    private static final float ZOOM = 15f;
    private GoogleMap map;
//    private BitmapDescriptor icon;

    public static ResultMapFragment newInstance(int position, ArrayList<JoggingModel> partials) {
        ResultMapFragment mapFragment = new ResultMapFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_KEY_POSITION, position);
        args.putParcelableArrayList(ARGS_KEY_PARTIALS, partials);
        mapFragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        // retrieve arguments
        if (getArguments() != null) {
            position = getArguments().getInt(ARGS_KEY_POSITION, 0);
            partials = getArguments().getParcelableArrayList(ARGS_KEY_PARTIALS);
        } else {
            position = 0;
            partials = new ArrayList<JoggingModel>();
        }

        // DON'T INIT THE MAP UNTIL ONCREATEVIEW DONE. So call initMap in onResume

        return view;
    }

    private void initMap() {
        map = getMap();

        if (map == null) {
            return;
        }

//        if (icon == null) {
//            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_indicator_current_position);
//        }

        // configurate map
        map.clear();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setIndoorEnabled(false);

        // No position obtained. There are not markers to show
        if (partials == null) {
            return;
        }

        // add markers
        JoggingModel partial;
        LatLng point;
        String sTime;
        String snippet;
        List<LatLng> points = new ArrayList<LatLng>();

        if (!partials.isEmpty()) {
            partial = partials.get(0);
            point = new LatLng(partial.getStart().getLatitude(), partial.getStart().getLongitude());
            map.addMarker(new MarkerOptions().position(point).title(getString(R.string.map_inici)));
            points.add(point);
        }

        for (int i=0; i<partials.size(); i++) {
            partial = partials.get(i);
            point = new LatLng(partial.getEnd().getLatitude(), partial.getEnd().getLongitude());
            sTime = FormatUtil.time(partial.getTotalTime());
            snippet = new StringBuilder().append(sTime).append("\n").append(partial.getTotalDistance()).append("m").toString();
//            map.addMarker(new MarkerOptions().position(point).title(String.valueOf(i + 1)).snippet(snippet).icon(icon));
            map.addMarker(new MarkerOptions().position(point).title(String.valueOf(i + 1)).snippet(snippet));
            points.add(point);
        }

        // draw lines between points
        map.addPolyline(new PolylineOptions().addAll(points).width(10f).color(Color.BLUE));

        // moveCamera may cause an IllegalStateException if the map has not been already sized
        // use cameraChangeListener instead
        //        map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, MAP_PADDING_IN_PX));
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                centerToPosition();
                map.setOnCameraChangeListener(null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            initMap();
        }
    }

    private void centerToPosition() {
        if (map != null && !partials.isEmpty()) {
            Location location = position == -1 ? partials.get(0).getStart() : partials.get(position).getEnd();
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, ZOOM));
        }
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            centerToPosition();
        }
    }
}
