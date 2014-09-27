package com.carles.jogging.result;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.carles.jogging.R;
import com.carles.jogging.model.JoggingModel;
import com.carles.jogging.util.FormatUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carles1 on 20/04/14.
 */
public class ResultMapFragment extends SupportMapFragment {

    private static final String TAG = ResultMapFragment.class.getSimpleName();
    private static final String ARGS_KEY_POSITION = "args_key_position";
    private static final String ARGS_KEY_JOGGING = "args_key_jogging";

    private int position = 0;
    private Marker markerWithWindowShown = null;
    private JoggingModel jogging = null;
    private List<JoggingModel> partials = new ArrayList<JoggingModel>();
    private List<JoggingModel> partialsForKm = new ArrayList <JoggingModel>();
    private List<Marker> markers = new ArrayList<Marker>();

    // GoogleMap zoom value range from 0 to 19. 0 is worldwide, 19 finest zoom
    private static final float ZOOM = 15f;
    private GoogleMap map;

    public static ResultMapFragment newInstance(int position, JoggingModel jogging) {
        ResultMapFragment mapFragment = new ResultMapFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_KEY_POSITION, position);
        args.putParcelable(ARGS_KEY_JOGGING, jogging);
        mapFragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        // retrieve arguments
        if (getArguments() != null) {
            position = getArguments().getInt(ARGS_KEY_POSITION, 0);
            jogging = getArguments().getParcelable(ARGS_KEY_JOGGING);
            partials = jogging.getPartials();
            partialsForKm = jogging.getPartialsForKilometer();
        }

        // DON'T INIT THE MAP UNTIL ONCREATEVIEW DONE. So we call initMap in onResume

        return view;
    }

    private void initMap() {
        map = getMap();

        if (map == null) {
            return;
        }

        // configurate map
        map.clear();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setIndoorEnabled(false);

        // No position obtained. There are not markers to show
        if (jogging == null || partials == null || partialsForKm == null) {
            // this should never happen. Map option shouldn't be enabled if no locations found
            return;
        }

        // set custom info window
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                markerWithWindowShown = null;
                marker.hideInfoWindow();
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                markerWithWindowShown = marker;
                // don't consume the event, marker should be shown and map centered
                return false;
            }
        });

        // Add markers. One for each kilometer, get points from jogging.getPartialsForKilometer()
        JoggingModel partialForKm;
        String sTime;
        String snippet;
        markers = new ArrayList<Marker>();

        LatLng point = new LatLng(jogging.getStart().getLatitude(), jogging.getStart().getLongitude());
        Marker marker = map.addMarker(new MarkerOptions().position(point).title(getString(R.string.map_inici)));
        markers.add(marker);

        for (int i = 0; i < partialsForKm.size(); i++) {
            partialForKm = partialsForKm.get(i);
            point = new LatLng(partialForKm.getEnd().getLatitude(), partialForKm.getEnd().getLongitude());
            sTime = FormatUtil.time(partialForKm.getGoalTime());
            snippet = new StringBuilder().append(sTime).append("  -  ").append((int) partialForKm.getGoalDistance()).append("m").toString();
            marker = map.addMarker(new MarkerOptions().position(point).title(String.valueOf(i + 1)).snippet(snippet));
            markers.add(marker);
        }

        // draw lines between points. Use all points, get them from jogging.getPartials()
        if (partials.size() > 0) {
            List<LatLng> points = new ArrayList<LatLng>();
            points.add(new LatLng(jogging.getStart().getLatitude(), jogging.getStart().getLongitude()));

            JoggingModel partial = null;
            for (int i = 0; i < partials.size(); i++) {
                partial = partials.get(i);
                points.add(new LatLng(partial.getEnd().getLatitude(), partial.getEnd().getLongitude()));
            }
            map.addPolyline(new PolylineOptions().addAll(points).width(10f).color(Color.BLUE));
        }

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
        if (map != null && partialsForKm!= null && !partialsForKm.isEmpty()) {
            Location location = position == -1 ? partialsForKm.get(0).getStart() : partialsForKm.get(position).getEnd();
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, ZOOM));
            markers.get(position+1).showInfoWindow();
            markerWithWindowShown = markers.get(position+1);
        }
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (markerWithWindowShown != null) {
                markerWithWindowShown.hideInfoWindow();
                markerWithWindowShown = null;
            }
        } else {
            centerToPosition();
        }
    }

    /*- ********************************************************************************* */
    /*- ********************************************************************************* */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            // initial marker uses default infoContents to show only the title
            if (marker.getTitle().equals(getString(R.string.map_inici))) {
                return null;
            }

            try {
                View contentsView = getActivity().getLayoutInflater().inflate(R.layout.infowindow_custom, null);
                JoggingModel partialForKm = partialsForKm.get(Integer.parseInt(marker.getTitle())-1);

                final TextView txtTitle = (TextView) contentsView.findViewById(R.id.txt_title);
                final TextView txtTime = (TextView) contentsView.findViewById(R.id.txt_time);
                final TextView txtDistance = (TextView) contentsView.findViewById(R.id.txt_distance);
                final TextView txtAccuracy = (TextView) contentsView.findViewById(R.id.txt_accuracy);

                txtTitle.setText(marker.getTitle());
                txtTime.setText(FormatUtil.time(partialForKm.getGoalTime()));
                txtDistance.setText(new StringBuilder().append((int) partialForKm.getGoalDistance()).append("m"));
                txtAccuracy.setText(new StringBuilder().append((int) partialForKm.getAccuracy()).append("m"));

                return contentsView;

            } catch (NumberFormatException e) {
                Log.e(TAG, "Error getInfoContents. Marker title cannot be parsed to int");
                return null;
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "Error getInfoContents. Marker title parses to an index out of bounds");
                return null;
            }
        }
    }
}
