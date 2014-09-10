package com.carles.jogging.result;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.carles.jogging.R;
import com.carles.jogging.BaseFragment;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.util.FormatUtil;
import com.carles.jogging.util.LocationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

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
    private GoogleMap map;
    private BitmapDescriptor icon;

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

        Log.e("carles", "oncreateview");

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

        Log.e("carles","about to initMap...");
        map = getMap();

        if (map == null) {
            return;
        }

        Log.e("carles","map not null. proceed");

        if (icon == null) {
            // TODO maybe not necessary
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_indicator_current_position);
        }

        // configurate map
        map.clear();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setIndoorEnabled(false);

        // add markers
        JoggingModel partial;
        LatLng point;
        String sTime;
        String snippet;

        final LatLngBounds.Builder builder = LatLngBounds.builder();
        if (!partials.isEmpty()) {
            partial = partials.get(0);
            point = new LatLng(partial.getStart().getLatitude(), partial.getStart().getLongitude());
            map.addMarker(new MarkerOptions().position(point).title(getString(R.string.map_inici)));
            builder.include(point);
        }

        for (int i=0; i<partials.size(); i++) {
            partial = partials.get(i);
            point = new LatLng(partial.getEnd().getLatitude(), partial.getEnd().getLongitude());
            sTime = FormatUtil.runningTime(partial.getTotalTime());
            snippet = new StringBuilder().append(sTime).append("\n").append(partial.getTotalDistance()).append("m").toString();
            map.addMarker(new MarkerOptions().position(point).title(String.valueOf(i+1)).snippet(snippet).icon(icon));
            builder.include(point);
        }

        // zooms camera according to the map dimensions
        final LatLngBounds latLngBounds = builder.build();
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, MAP_PADDING_IN_PX));

        Log.e("carles","init map ending");

    }

    @Override
    public void onResume() {
        Log.e("carles","about to call super.onresume");
        super.onResume();
        Log.e("carles", "after calling super.onresume");

        if (map == null) {
            initMap();
        }
        centerToPosition();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("carles","onstart");
    }

    private void centerToPosition() {
        Log.e("carles", "center to position");
        if (map != null && !partials.isEmpty()) {
            Location location = position == 0 ? partials.get(0).getStart() : partials.get(position - 1).getEnd();
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLng(point));
        }
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
