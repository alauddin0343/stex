package cz.uhk.cityunavigate;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cz.uhk.cityunavigate.model.Category;
import cz.uhk.cityunavigate.model.FeedItem;
import cz.uhk.cityunavigate.model.Marker;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;
import cz.uhk.cityunavigate.util.Util;

public class MarkerAddActivity extends AppCompatActivity {

    private MapView mapView;

    private GoogleMap map;

    private com.google.android.gms.maps.model.Marker googleMapMarker;

    private EditText editName, editText;

    private List<Category> categoriesArray;

    private Uri thumbnail = null;

    private String selectedCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_add);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editName = (EditText) findViewById(R.id.editMarkerName);
        editText = (EditText) findViewById(R.id.editMarkerText);
        mapView = (MapView) findViewById(R.id.mapview);

        // spinner
        categoriesArray = new ArrayList<>();
        Database.getAllCategories().addItemAddListener(new ObservableList.ItemAddListener<Category>() {
            @Override
            public void onItemAdded(@NotNull ObservableList<Category> list, @NotNull Collection<Category> addedItems) {
                for (Category category : addedItems){
                    if (!categoriesArray.contains(category)) {
                        categoriesArray.add(category);
                    }
                }
            }
        });

        // mapview
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
                googleMapMarker = map.addMarker(new MarkerOptions().draggable(true)
                        .position(new LatLng(22.336292, 114.173910))
                );

                //MAP LISTENERS
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
                        //just to disable info window
                        return false;
                    }
                });
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        googleMapMarker.setPosition(latLng);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_marker_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

            case R.id.action_marker_save:
                sendMarker();
                break;

            case R.id.action_marker_thumbnail:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), Util.REQUEST_ACTIVITY_PICK_PHOTO);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void centreMapToLatLng(LatLng latLng){
        if(map != null){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 16);
            map.moveCamera(cameraUpdate);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.REQUEST_ACTIVITY_PICK_PHOTO) {

            if (resultCode == RESULT_OK) {

                try {
                    Util.uploadPicture(
                        this,
                        getContentResolver(),
                        data.getData(),
                        "markers",
                        640
                    ).success(new Promise.SuccessListener<Uri, Object>() {
                        @Override
                        public Object onSuccess(Uri result) throws Exception {
                            thumbnail = result;
                            return null;
                        }
                    });
                } catch (IOException exception) {
                    Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onChooseCategoryButtonClick(View view) {

        int selectedCategoryIndex = 0;

        final String[] categoryNames = new String[categoriesArray.size()];
        for (int i = 0; i < categoriesArray.size(); i++) {
            Category category = categoriesArray.get(i);
            categoryNames[i] = category.getName();
            if (category.getId().equals(selectedCategoryId)) {
                selectedCategoryIndex = i;
            }
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Choose category")
                .setSingleChoiceItems(categoryNames, selectedCategoryIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Category category = categoriesArray.get(which);
                        selectedCategoryId = category.getId();
                        Toast.makeText(MarkerAddActivity.this, category.getName(), Toast.LENGTH_LONG).show();
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(true)
                .create();

        alertDialog.show();
    }

    public void sendMarker() {

        LoggedInUser.get().success(new Promise.SuccessListener<LoggedInUser, Object>() {

            @Override
            public Object onSuccess(LoggedInUser result) throws Exception {

                final String userId = result.getFirebaseUser().getUid();
                final String groupId = result.getActiveGroup().getId();

                final String title = editName.getText().toString();
                final String text = editText.getText().toString();

                Marker marker = cz.uhk.cityunavigate.model.Marker.builder()
                        .withId(null)
                        .withIdGroup(groupId)
                        .withIdUserAuthor(userId)
                        .withIdCategory(selectedCategoryId)
                        .withLocation(googleMapMarker.getPosition())
                        .withTitle(title)
                        .withText(text)
                        .withCreated(System.currentTimeMillis())
                        .withImage(thumbnail)
                        .build();

                Database.addMarker(groupId, marker).success(new Promise.SuccessListener<cz.uhk.cityunavigate.model.Marker, Object>() {

                    @Override
                    public Object onSuccess(cz.uhk.cityunavigate.model.Marker result) {

                        FeedItem feedItem = FeedItem.builder()
                                .withId(null)
                                .withUserId(userId)
                                .withGroupId(groupId)
                                .withMarkerId(result.getId())
                                .withCreated(System.currentTimeMillis())
                                .withType(FeedItem.Type.MarkerAdd)
                                .withText(text)
                                .withTitle("Added marker")
                                .withThumbnail(thumbnail)
                                .build();

                        Database.addFeedItem(groupId, feedItem);
                        finish();
                        return null;
                    }
                });
                return null;
            }
        });
    }

}
