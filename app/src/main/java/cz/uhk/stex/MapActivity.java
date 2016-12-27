package cz.uhk.stex;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import cz.uhk.stex.model.Category;
import cz.uhk.stex.model.Marker;
import cz.uhk.stex.util.ObservableList;
import cz.uhk.stex.util.Promise;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;

    private GoogleMap map;

    private HashMap<com.google.android.gms.maps.model.Marker, Marker> markerIds;

    private List<com.google.android.gms.maps.model.Marker> markers;

    private int mapStyle = 0;

    private String markerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        markerId = getIntent().getStringExtra("detail");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        markers = new ArrayList<>();
        markerIds = new HashMap<>();

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        LoggedInUser.get().success(new Promise.SuccessListener<LoggedInUser, Object>() {

            @Override
            public Object onSuccess(final LoggedInUser result) throws Exception {

                final String groupId = result.getActiveGroup().getId();

                mapView.getMapAsync(new OnMapReadyCallback() {

                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        map = googleMap;
                        map.getUiSettings().setMyLocationButtonEnabled(false);

                        map.setBuildingsEnabled(true);
                        map.getUiSettings().setAllGesturesEnabled(true);
                        map.getUiSettings().setCompassEnabled(true);
                        map.getUiSettings().setMyLocationButtonEnabled(true);
                        map.getUiSettings().setMapToolbarEnabled(true);
                        map.getUiSettings().setZoomControlsEnabled(true);

                        //MAP LISTENERS
                        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(com.google.android.gms.maps.model.Marker marker) {
                                marker.hideInfoWindow();
                                Intent detailIntent = new Intent(MapActivity.this, DetailActivity.class);
                                detailIntent.putExtra("id", markerIds.get(marker).getId());
                                detailIntent.putExtra("groupid", markerIds.get(marker).getIdGroup());
                                startActivity(detailIntent);
                            }
                        });

                        if (markerId != null) {
                            putOneMarkerOnMap(groupId, markerId);
                        } else {
                            putAllMarkersOnMap(groupId);
                            centreMapToLatLng(new LatLng(22.336292, 114.173910));
                        }
                    }
                });
                return null;
            }
        });


    }

    private void centreMapToLatLng(LatLng latLng) {
        if (map != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 16);
            map.moveCamera(cameraUpdate);
        }
    }
    private void putAllMarkersOnMap(String groupId) {

        Database.getGroupMarkers(groupId).addItemAddListener(new ObservableList.ItemAddListener<Marker>() {
            @Override
            public void onItemAdded(@NotNull ObservableList<Marker> list, @NotNull Collection<Marker> addedItems) {
                for (Marker marker : addedItems) {
                    putMarker(marker);
                }
            }
        });
    }

    private void putOneMarkerOnMap(String groupId, String markerId) {

        Database.getMarkerById(groupId, markerId).success(new Promise.SuccessListener<Marker, Object>() {
            @Override
            public Object onSuccess(Marker result) throws Exception {
                putMarker(result);
                centreMapToLatLng(result.getLocation());
                return null;
            }
        });
    }

    private void putMarker(final Marker marker) {

        Database.getCategoryById(marker.getIdCategory()).success(new Promise.SuccessListener<Category, Object>() {

            @Override
            public Object onSuccess(Category result) {

                com.google.android.gms.maps.model.Marker mapMarker = map
                        .addMarker(new MarkerOptions()
                        .position(marker.getLocation())
                        .title(marker.getTitle())
                        .icon(BitmapDescriptorFactory.defaultMarker(result.getHue()))
                        .snippet(marker.getText()));

                markerIds.put(mapMarker, marker);
                markers.add(mapMarker);
                return null;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

            case R.id.action_map_change:
                if (mapStyle == 0) {
                    map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                } else if (mapStyle == 1) {
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else if (mapStyle == 2) {
                    mapStyle = -1;
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                mapStyle ++;
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    //FOLLOWING METHODS ARE FOR MAPVIEW CONTROLLING (map fragment must have)
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
