package cz.uhk.cityunavigate;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment {

    private MapView mapView;
    private GoogleMap map;

    private List<Marker> markers;
    private List<Circle> circles;
    private List<Polygon> polygons;

    private int mapStyle = 0;

    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() { //String param1, String param2
        MapFragment fragment = new MapFragment();
        /*
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        */
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //FUNCTIONS
    private void centerMapToLatLng(LatLng latLng){
        if(map != null){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 16);
            map.animateCamera(cameraUpdate);
        }
    }

    @Deprecated
    private void putPlaceOnMap(Object place){ //TODO STILL IN DEPLOY
        //z model objektu vytvořit marker na mapě
        LatLng l = new LatLng(22.336292, 114.173910); //TODO get latLng and other stuff from place

        markers.add(map.addMarker(new MarkerOptions().position(l).title("TODO TITLE").snippet("TODO SNIPPET")));
    }

    private void clearMap(){
        map.clear(); //FULL CLEAN
    }
    private void removeMarker(Marker m){
        m.remove();
        markers.remove(m);
    }

    private void changeMap(){
        if(mapStyle == 0){
            map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }else if(mapStyle == 1){
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }else{
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mapStyle = -1;
        }
        mapStyle++;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        setHasOptionsMenu(true);

        markers = new ArrayList<>();
        circles = new ArrayList<>();
        polygons = new ArrayList<>();

        mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.setMyLocationEnabled(true);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 10);

                map.setBuildingsEnabled(true);
                map.getUiSettings().setAllGesturesEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(true);

                map.animateCamera(cameraUpdate);
                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Toast.makeText(getActivity(), "TODO přidat akci", Toast.LENGTH_SHORT).show();
                    }
                });
                map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        markers.add(map.addMarker(new MarkerOptions().position(latLng).title("Si podržel").snippet("dobrý ne?")));
                    }
                });
                map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(Marker marker) {
                        marker.hideInfoWindow();
                        removeMarker(marker);
                    }
                });
            }
        });

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//noinspection SimplifiableIfStatement

        int id = item.getItemId();

        if (id == R.id.action_map_change) {
            if(mapStyle == 0){
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }else if (mapStyle == 1){
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }else if (mapStyle == 2){
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }else if (mapStyle == 3){
                mapStyle = -1;
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
        mapStyle++;

        return super.onOptionsItemSelected(item);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    //FOLLOWING METHODS ARE FOR MAPVIEW CONTROLLING
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
