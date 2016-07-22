package cz.uhk.cityunavigate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import cz.uhk.cityunavigate.model.Category;
import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;

public class AddMarkerActivity extends AppCompatActivity {

    private MapView mapView;
    private GoogleMap map;
    private Marker marker;

    private EditText editName, editText;
    private Spinner dropdownCategories;
    private CategorySpinnerAdapter categoryAdapter;
    private ArrayList<Category> categoriesArray;
    private Button btnImageUpload, btnSubmitMarker;

    private void centreMapToLatLng(LatLng latLng){
        if(map != null){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 16);
            map.moveCamera(cameraUpdate);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);

        editName = (EditText)findViewById(R.id.editMarkerName);
        editText = (EditText)findViewById(R.id.editMarkerText);

        dropdownCategories = (Spinner)findViewById(R.id.spinnerCategories);
        categoriesArray = new ArrayList<>();
        categoryAdapter = new CategorySpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoriesArray);
        dropdownCategories.setAdapter(categoryAdapter);
        ObservableList<Category> categories = Database.getAllCategories();
        categories.addItemAddListener(new ObservableList.ItemAddListener<Category>() {
            @Override
            public void onItemAdded(@NotNull ObservableList<Category> list, @NotNull Collection<Category> addedItems) {
                for(Category c : addedItems){
                    if(!categoriesArray.contains(c)){
                        categoryAdapter.add(c);
                        categoryAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        btnSubmitMarker = (Button)findViewById(R.id.btnSubmitMarker);
        btnSubmitMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(!editName.getText().toString().trim().isEmpty())
                    if(!editText.getText().toString().trim().isEmpty())
                        sendMarker();

            }
        });

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setMyLocationButtonEnabled(false);

                //map.setMyLocationEnabled(true);
                map.setBuildingsEnabled(true);
                map.getUiSettings().setAllGesturesEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(true);

                //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 10);
                //TODO - ADD CENTER TO MY POSITION
                centreMapToLatLng(new LatLng(22.336292, 114.173910));

                //PUT MARKER IN THE MIDDLE OF SCREEN AND IT IS FULLY DRAGGABLE OR YOU CAN CLICK SOMEWHERE TO CHANGE HIS LOC
                marker = map.addMarker(new MarkerOptions().draggable(true)
                        .position(new LatLng(22.336292, 114.173910))
                );

                //MAP LISTENERS
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        //just to disable info window
                        return false;
                    }
                });
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        marker.setPosition(latLng);
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            this.finish();
            return true;
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

    public void sendMarker() {

        // TODO load current group
        final String groupId = "-KMviPeXMiaorp1p49au";
        final Category c = (Category)dropdownCategories.getSelectedItem();
        //final Group g = ()

        final String title = ((EditText) findViewById(R.id.editMarkerName)).getText().toString();
        final String text = ((EditText) findViewById(R.id.editMarkerText)).getText().toString();

        Database.addMarker(groupId, cz.uhk.cityunavigate.model.Marker.builder()
                .withId(null)
                .withIdGroup(groupId)
                .withIdUserAuthor(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .withIdCategory(c.getId())
                .withLocation(marker.getPosition())
                .withTitle(title)
                .withText(text)
                .withCreated(System.currentTimeMillis())
                .withImage(null)
                .build()
        ).success(new Promise.SuccessListener<cz.uhk.cityunavigate.model.Marker, Void>() {

            @Override
            public Void onSuccess(cz.uhk.cityunavigate.model.Marker result) {

                Database.addFeedItem(groupId, FeedItem.builder()
                        .withId(null)
                        .withUserId(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .withGroupId(groupId)
                        .withMarkerId(result.getId())
                        .withCreated(System.currentTimeMillis())
                        .withType(FeedItem.Type.MarkerAdd)
                        .withText(text)
                        .withTitle("Added marker")
                        .withThumbnail(null)
                        .build()
                );
                finish();
                return null;
            }
        });
    }

}
